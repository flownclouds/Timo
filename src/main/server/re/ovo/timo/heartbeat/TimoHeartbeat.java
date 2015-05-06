/*
 * Copyright 1999-2012 Alibaba Group.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package re.ovo.timo.heartbeat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import re.ovo.timo.TimoNode;
import re.ovo.timo.config.Alarms;
import re.ovo.timo.config.model.TimoNodeConfig;
import re.ovo.timo.net.mysql.OkPacket;
import re.ovo.timo.statistic.HeartbeatRecorder;
import re.ovo.timo.util.TimeUtil;

/**
 * @author xianmao.hexm
 */
public class TimoHeartbeat {
    public static final int OK_STATUS = 1;
    public static final int OFF_STATUS = 2;
    public static final int SEND = 3;
    public static final int ERROR_STATUS = -1;
    private static final int TIMEOUT_STATUS = -2;
    private static final int INIT_STATUS = 0;
    private static final int MAX_RETRY_COUNT = 5;
    private static final Logger ALARM = Logger.getLogger("alarm");
    private static final Logger LOGGER = Logger.getLogger(TimoHeartbeat.class);
    private static final Logger HEARTBEAT = Logger.getLogger("heartbeat");

    private final TimoNode node;
    private final AtomicBoolean isStop;
    private final AtomicBoolean isChecking;
    private final TimoDetectorFactory factory;
    private final HeartbeatRecorder recorder;
    private final ReentrantLock lock;
    private final int maxRetryCount;
    private int errorCount;
    private volatile int status;
    private TimoDetector detector;
    public final AtomicLong detectCount;

    public TimoHeartbeat(TimoNode node) {
        this.node = node;
        this.isStop = new AtomicBoolean(false);
        this.isChecking = new AtomicBoolean(false);
        this.factory = new TimoDetectorFactory();
        this.recorder = new HeartbeatRecorder();
        this.lock = new ReentrantLock(false);
        this.maxRetryCount = MAX_RETRY_COUNT;
        this.status = OK_STATUS;
        this.detectCount = new AtomicLong(0);
    }

    public TimoNode getNode() {
        return node;
    }

    public TimoDetector getDetector() {
        return detector;
    }

    public int getStatus() {
        return status;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public long getTimeout() {
        TimoDetector detector = this.detector;
        if (detector == null) {
            return -1L;
        }
        return detector.getHeartbeatTimeout();
    }

    public HeartbeatRecorder getRecorder() {
        return recorder;
    }

    public String lastActiveTime() {
        TimoDetector detector = this.detector;
        if (detector == null) {
            return null;
        }
        long t = Math.max(detector.lastReadTime(), detector.lastWriteTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(t));
    }

    public boolean isStop() {
        return isStop.get();
    }

    public boolean isChecking() {
        return isChecking.get();
    }

    public void start() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            isStop.compareAndSet(true, false);
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (isStop.compareAndSet(false, true)) {
                if (isChecking.get()) {
                    // nothing
                } else {
                    TimoDetector detector = this.detector;
                    if (detector != null) {
                        detector.quit();
                        isChecking.set(false);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 执行心跳
     */
    public void heartbeat() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (isChecking.compareAndSet(false, true)) {
                TimoDetector detector = this.detector;
                if (detector == null || detector.isQuit() || detector.isClosed()) {
                    try {
                        detector = factory.make(this);
                    } catch (Throwable e) {
                        LOGGER.warn(node.toString(), e);
                        setError(null);
                        return;
                    }
                    this.detector = detector;
                } else {
                    detector.heartbeat();
                }
            } else {
                TimoDetector detector = this.detector;
                if (detector != null) {
                    if (detector.isQuit() || detector.isClosed()) {
                        isChecking.compareAndSet(true, false);
                    } else if (detector.isHeartbeatTimeout()) {
                        setTimeout(detector);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 设定结果
     */
    public void setResult(int result, TimoDetector detector, boolean isTransferError, byte[] message) {
        switch (result) {
            case OK_STATUS:
                setOk(detector);
                if (HEARTBEAT.isInfoEnabled()) {
                    HEARTBEAT.info(requestMessage(OK_STATUS, message));
                }
                break;
            case OFF_STATUS:
                setOff(detector);
                if (HEARTBEAT.isInfoEnabled()) {
                    HEARTBEAT.info(requestMessage(OFF_STATUS, message));
                }
                break;
            case ERROR_STATUS:
                if (detector.isQuit()) {
                    isChecking.set(false);
                } else {
                    if (isTransferError) {
                        detector.close();
                    }
                    setError(detector);
                }
                if (HEARTBEAT.isInfoEnabled()) {
                    HEARTBEAT.info(requestMessage(ERROR_STATUS, message));
                }
                break;
        }
    }

    private void setOk(TimoDetector detector) {
        recorder.set(detector.lastReadTime() - detector.lastWriteTime());
        switch (status) {
            case TIMEOUT_STATUS:
                this.status = INIT_STATUS;
                this.errorCount = 0;
                this.isChecking.set(false);
                if (isStop.get()) {
                    detector.quit();
                } else {
                    heartbeat();// 超时状态，再次执行心跳。
                }
                break;
            default:
                this.status = OK_STATUS;
                this.errorCount = 0;
                this.isChecking.set(false);
                if (isStop.get()) {
                    detector.quit();
                }
        }
    }

    private void setOff(TimoDetector detector) {
        this.status = OFF_STATUS;
        this.errorCount = 0;
        this.isChecking.set(false);
        if (isStop.get()) {
            detector.quit();
        }
    }

    private void setError(TimoDetector detector) {
        if (++errorCount < maxRetryCount) {
            this.isChecking.set(false);
            if (detector != null && isStop.get()) {
                detector.quit();
            } else {
                heartbeat();// 未到达错误次数，再次执行心跳。
            }
        } else {
            this.status = ERROR_STATUS;
            this.errorCount = 0;
            this.isChecking.set(false);
            try {
                ALARM.error(alarmMessage("ERROR"));
            } finally {
                if (detector != null && isStop.get()) {
                    detector.quit();
                }
            }
        }
    }

    private void setTimeout(TimoDetector detector) {
        status = TIMEOUT_STATUS;
        try {
            ALARM.error(alarmMessage("TIMEOUT"));
            if (HEARTBEAT.isInfoEnabled()) {
                HEARTBEAT.info(requestMessage(TIMEOUT_STATUS, null));
            }
        } finally {
            detector.quit();
            isChecking.set(false);
        }
    }

    /**
     * 报警信息
     */
    private String alarmMessage(String reason) {
        TimoNodeConfig cnc = node.getConfig();
        return new StringBuilder().append(Alarms.DEFAULT).append("[name=").append(cnc.getName())
                .append(",host=").append(cnc.getHost()).append(",port=").append(cnc.getPort())
                .append(",reason=").append(reason).append(']').toString();
    }

    /**
     * 心跳日志信息
     */
    public String requestMessage(int type, byte[] message) {
        String action = null;
        String id = null;
        switch (type) {
            case OK_STATUS:
                action = "OK";
                OkPacket ok = new OkPacket();
                ok.read(message);
                id = String.valueOf(ok.affectedRows);
                break;
            case OFF_STATUS:
                action = "OFFLINE";
                if (message != null) {
                    id = new String(message);
                }
                break;
            case ERROR_STATUS:
                action = "ERROR";
                if (message != null) {
                    id = new String(message);
                }
                break;
            case TIMEOUT_STATUS:
                action = "TIMEOUT";
                if (message != null) {
                    id = new String(message);
                }
                break;
            case SEND:
                action = "SEND";
                if (message != null) {
                    id = new String(message);
                }
                break;
            default:
                action = "UNKNOWN";
                if (message != null) {
                    id = new String(message);
                }
        }

        // 如果取不到从服务端返回的id，则从本地取得。
        if (id == null) {
            id = "$" + detectCount.get();
        }

        return new StringBuilder().append("REQUEST:").append(action).append(", id=").append(id)
                .append(", host=").append(node.getConfig().getHost()).append(", port=")
                .append(node.getConfig().getPort()).append(", time=")
                .append(TimeUtil.currentTimeMillis()).toString();
    }

}
