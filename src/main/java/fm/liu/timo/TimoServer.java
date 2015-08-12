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

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.pmw.tinylog.Logger;
import fm.liu.timo.config.model.SystemConfig;
import fm.liu.timo.manager.ManagerConnectionFactory;
import fm.liu.timo.net.NIOAcceptor;
import fm.liu.timo.net.NIOConnector;
import fm.liu.timo.net.NIOProcessor;
import fm.liu.timo.net.backend.Node;
import fm.liu.timo.net.connection.Variables;
import fm.liu.timo.parser.recognizer.mysql.lexer.MySQLLexer;
import fm.liu.timo.server.ServerConnectionFactory;
import fm.liu.timo.util.ExecutorUtil;
import fm.liu.timo.util.NameableExecutor;
import fm.liu.timo.util.TimeUtil;

/**
 * @author xianmao.hexm 2011-4-19 下午02:58:59
 */
public class TimoServer {
    public static final String      NAME               = "Timo";
    private static final long       TIME_UPDATE_PERIOD = 20L;
    private static final TimoServer INSTANCE           = new TimoServer();

    public static final TimoServer getInstance() {
        return INSTANCE;
    }

    private final TimoConfig       config;
    private final Timer            timer;
    private final NameableExecutor timerExecutor;
    private final AtomicBoolean    isOnline;
    private final long             startupTime;
    private NIOProcessor[]         processors;
    private NIOConnector           connector;
    private NIOAcceptor            manager;
    private NIOAcceptor            server;

    private TimoServer() {
        this.config = new TimoConfig();
        SystemConfig system = config.getSystem();
        MySQLLexer.setCStyleCommentVersion(system.getParserCommentVersion());
        this.timer = new Timer(NAME + "Timer", true);
        this.timerExecutor = ExecutorUtil.create("TimerExecutor", system.getTimerExecutor());
        this.isOnline = new AtomicBoolean(true);
        this.startupTime = TimeUtil.currentTimeMillis();
    }

    public TimoConfig getConfig() {
        return config;
    }

    public void startup() throws IOException {
        // server startup
        Logger.info("===============================================");
        Logger.info("{} is ready to startup ...", NAME);
        SystemConfig system = config.getSystem();
        timer.schedule(updateTime(), 0L, TIME_UPDATE_PERIOD);
        Variables variables = new Variables();
        variables.setCharset(system.getCharset());
        // startup processors
        Logger.info("Startup processors ...");
        int handler = system.getProcessorHandler();
        int executor = system.getProcessorExecutor();
        processors = new NIOProcessor[system.getProcessors()];
        for (int i = 0; i < processors.length; i++) {
            processors[i] = new NIOProcessor("Processor" + i, handler, executor);
            processors[i].startup();
        }

        // startup connector
        Logger.info("Startup connector ...");
        connector = new NIOConnector(NAME + "Connector");
        connector.start();

        // init dataNodes
        Map<Integer, Node> nodes = config.getNodes();
        Logger.info("Initialize dataNodes ...");
        for (Node node : nodes.values()) {
            if (!node.init()) {
                Logger.error("Node init failed, check your config");
                System.exit(-1);
            }
        }

        timer.schedule(processorCheck(), 0L, system.getProcessorCheckPeriod());
        timer.schedule(dataNodeIdleCheck(), 0L, system.getDataNodeIdleCheckPeriod());
        timer.schedule(dataNodeHeartbeat(), 0L, system.getDataNodeHeartbeatPeriod());

        // startup manager
        ManagerConnectionFactory mf = new ManagerConnectionFactory(variables);
        mf.setIdleTimeout(system.getIdleTimeout());
        manager = new NIOAcceptor(NAME + "Manager", system.getManagerPort(), mf);
        manager.start();
        Logger.info("{} is started and listening on {}", manager.getName(), manager.getPort());

        // startup server
        ServerConnectionFactory sf = new ServerConnectionFactory(variables);
        sf.setIdleTimeout(system.getIdleTimeout());
        server = new NIOAcceptor(NAME + "Server", system.getServerPort(), sf);
        server.start();

        // server started
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
                timerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<Integer, Node> nodes = config.getNodes();
                        for (Node node : nodes.values()) {
                            node.idleCheck();
                        }
                    }
                });
            }
        };
    }

    // 数据节点定时心跳任务
    private TimerTask dataNodeHeartbeat() {
        return new TimerTask() {
            @Override
            public void run() {
                timerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Map<Integer, Node> nodes = config.getNodes();
                        for (Node node : nodes.values()) {
                            node.heartbeat();
                        }
                    }
                });
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

}
