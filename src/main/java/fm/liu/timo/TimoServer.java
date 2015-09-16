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
package fm.liu.timo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.pmw.tinylog.Logger;
import fm.liu.messenger.Mail;
import fm.liu.messenger.User;
import fm.liu.timo.backend.Node;
import fm.liu.timo.config.Versions;
import fm.liu.timo.config.model.SystemConfig;
import fm.liu.timo.manager.ManagerConnectionFactory;
import fm.liu.timo.mysql.connection.MySQLConnection;
import fm.liu.timo.mysql.handler.xa.XARecoverHandler;
import fm.liu.timo.mysql.packet.CommandPacket;
import fm.liu.timo.net.NIOAcceptor;
import fm.liu.timo.net.NIOConnector;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.connection.Variables;
import fm.liu.timo.parser.recognizer.mysql.lexer.MySQLLexer;
import fm.liu.timo.server.ServerConnectionFactory;
import fm.liu.timo.server.session.handler.ResultHandler;
import fm.liu.timo.statistic.SQLRecorder;
import fm.liu.timo.util.ExecutorUtil;
import fm.liu.timo.util.NameableExecutor;
import fm.liu.timo.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-4-19 下午02:58:59
 */
public class TimoServer {
    public static final String       NAME               = "Timo";
    private static final long        TIME_UPDATE_PERIOD = 20L;
    private static final SQLRecorder RECORDER           = new SQLRecorder();
    private static final User        SENDER             = new User() {
        @Override
        public void receive(Mail<?> mail) {}
    };

    private static final TimoServer INSTANCE = new TimoServer();

    public static final TimoServer getInstance() {
        return INSTANCE;
    }

    private final AtomicLong       xid = new AtomicLong();
    private final TimoConfig       config;
    private final Timer            timer;
    private final NameableExecutor timerExecutor;
    private final AtomicBoolean    isOnline;
    private final long             startupTime;
    private NIOProcessor[]         processors;
    private NIOConnector           connector;
    private NIOAcceptor            manager;
    private NIOAcceptor            server;
    private User                   starter;
    private Set<File>              xaLogs;
    private volatile boolean       xaCommiting;
    private volatile boolean       xaStarting;

    private TimoServer() {
        this.config = new TimoConfig();
        SystemConfig system = config.getSystem();
        MySQLLexer.setCStyleCommentVersion(system.getParserCommentVersion());
        this.timer = new Timer(NAME + "Timer", true);
        this.timerExecutor = ExecutorUtil.create("TimerExecutor", system.getTimerExecutor());
        this.isOnline = new AtomicBoolean(true);
        this.startupTime = TimeUtil.currentTimeMillis();
        RECORDER.register();
    }

    public static User getSender() {
        return SENDER;
    }

    public static SQLRecorder getRecorder() {
        return RECORDER;
    }

    public User getStarter() {
        return starter;
    }

    public TimoConfig getConfig() {
        return config;
    }

    public String nextXID() {
        long id = this.xid.incrementAndGet();
        if (id < 0) {
            synchronized (xid) {
                if (xid.get() < 0) {
                    xid.set(0);
                }
                id = xid.incrementAndGet();
            }
        }
        return "'TimoXA" + id + "'";
    }

    public void startup() throws IOException {
        Logger.info("===============================================");
        Logger.info("{} v{} is ready to startup ...", NAME, Versions.version);
        // 初始化配置
        SystemConfig system = config.getSystem();
        // 启动线程池
        Logger.info("Startup processors ...");
        int handler = system.getProcessorHandler();
        int executor = system.getProcessorExecutor();
        processors = new NIOProcessor[system.getProcessors()];
        for (int i = 0; i < processors.length; i++) {
            processors[i] =
                    new NIOProcessor("Processor" + i, handler, executor, system.getQueryTimeout());
            processors[i].startup();
        }

        Logger.info("Startup connector ...");
        connector = new NIOConnector(NAME + "Connector");
        connector.start();

        // 初始化数据节点
        Map<Integer, Node> nodes = config.getNodes();
        Logger.info("Initialize dataNodes ...");
        for (Node node : nodes.values()) {
            if (!node.init()) {
                Logger.error("Node:{} init failed, check your config", node);
                System.exit(-1);
            }
        }

        starter = new User() {
            @Override
            public void receive(Mail<?> mail) {
                try {
                    xaLogs.forEach(log -> log.delete());
                    TimoServer.getInstance().lisen(system);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        starter.register();

        // XA恢复
        Logger.info("Checking XA transaction recover ...");
        xaRecover(nodes);
    }

    private void xaRecover(Map<Integer, Node> nodes) {
        HashMap<String, ConcurrentHashMap<Integer, Boolean>> recoveryLog = new HashMap<>();
        File dir = new File(".");
        if (dir.isDirectory()) {
            this.xaLogs = new HashSet<>();
            File[] files = dir.listFiles();
            for (File f : files) {
                String name = f.getName();
                if (name.startsWith("TimoXA")) {
                    try {
                        FileInputStream in = new FileInputStream(f);
                        ObjectInputStream stream = new ObjectInputStream(in);
                        @SuppressWarnings("unchecked")
                        ConcurrentHashMap<Integer, Boolean> result =
                                (ConcurrentHashMap<Integer, Boolean>) stream.readObject();
                        stream.close();
                        in.close();
                        recoveryLog.put(name, result);
                        this.xaLogs.add(f);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        ResultHandler handler = new XARecoverHandler(recoveryLog, nodes);
        nodes.values().parallelStream().forEach(n -> {
            MySQLConnection con = (MySQLConnection) n.getSource().notNullGet();
            con.setResultHandler(handler);
            CommandPacket packet = new CommandPacket(CommandPacket.COM_QUERY);
            packet.arg = "XA RECOVER".getBytes();
            packet.write(con);
        });
    }

    private void lisen(SystemConfig system) throws IOException {
        // 初始化定时任务
        timer.schedule(updateTime(), 0L, TIME_UPDATE_PERIOD);
        timer.schedule(processorCheck(), 0L, system.getProcessorCheckPeriod());
        timer.schedule(dataNodeIdleCheck(), 0L, system.getDataNodeIdleCheckPeriod());
        timer.schedule(dataNodeHeartbeat(), 0L, system.getHeartbeatTimeout());

        // 初始化管理端和服务端
        Variables variables = new Variables();
        variables.setCharset(system.getCharset());
        ManagerConnectionFactory mf = new ManagerConnectionFactory(variables);
        manager = new NIOAcceptor(NAME + "Manager", system.getManagerPort(), mf);
        manager.start();
        Logger.info("{} is started and listening on {}", manager.getName(), manager.getPort());
        ServerConnectionFactory sf = new ServerConnectionFactory(variables);
        server = new NIOAcceptor(NAME + "Server", system.getServerPort(), sf);
        server.start();

        // 启动完成
        Logger.info("{} is started and listening on {}", server.getName(), server.getPort());
        Logger.info("===============================================");
    }

    public NIOProcessor[] getProcessors() {
        return processors;
    }

    public NIOConnector getConnector() {
        return connector;
    }

    public NameableExecutor getTimerExecutor() {
        return timerExecutor;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public boolean isOnline() {
        return isOnline.get();
    }

    public void offline() {
        isOnline.set(false);
    }

    public void online() {
        isOnline.set(true);
    }

    // 系统时间定时更新任务
    private TimerTask updateTime() {
        return new TimerTask() {
            @Override
            public void run() {
                TimeUtil.update();
            }
        };
    }

    // 处理器定时检查任务
    private TimerTask processorCheck() {
        return new TimerTask() {
            @Override
            public void run() {
                timerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (NIOProcessor p : processors) {
                            p.check();
                        }
                    }
                });
            }
        };
    }

    // 数据节点定时连接空闲超时检查任务
    private TimerTask dataNodeIdleCheck() {
        return new TimerTask() {
            @Override
            public void run() {
                timerExecutor.execute(
                        () -> config.getNodes().values().forEach(node -> node.idleCheck()));
            }
        };
    }

    // 数据节点定时心跳任务
    private TimerTask dataNodeHeartbeat() {
        return new TimerTask() {
            @Override
            public void run() {
                timerExecutor.execute(
                        () -> config.getNodes().values().forEach(node -> node.heartbeat()));
            }
        };
    }

    private int processorIndex = 0;

    public NIOProcessor nextProcessor() {
        if (processors.length == 1) {
            return processors[0];
        } else {
            return processors[(++processorIndex) % processors.length];
        }
    }

    public boolean isXACommiting() {
        return xaCommiting;
    }

    public void setXACommiting(boolean xaCommiting) {
        this.xaCommiting = xaCommiting;
    }

    public boolean isXAStarting() {
        return xaStarting;
    }

    public void setXAStarting(boolean xaStarting) {
        this.xaStarting = xaStarting;
    }

}
