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
package re.ovo.timo.manager.response;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import re.ovo.timo.TimoCluster;
import re.ovo.timo.TimoConfig;
import re.ovo.timo.TimoServer;
import re.ovo.timo.config.ErrorCode;
import re.ovo.timo.config.model.DataSourceConfig;
import re.ovo.timo.config.model.QuarantineConfig;
import re.ovo.timo.config.model.SchemaConfig;
import re.ovo.timo.config.model.UserConfig;
import re.ovo.timo.manager.ManagerConnection;
import re.ovo.timo.mysql.MySQLDataNode;
import re.ovo.timo.mysql.MySQLDataSource;
import re.ovo.timo.net.mysql.OkPacket;

/**
 * @author xianmao.hexm
 */
public final class RollbackConfig {
    private static final Logger LOGGER = Logger.getLogger(RollbackConfig.class);

    public static void execute(ManagerConnection c) {
        final ReentrantLock lock = TimoServer.getInstance().getConfig().getLock();
        lock.lock();
        try {
            if (rollback()) {
                StringBuilder s = new StringBuilder();
                s.append(c).append("Rollback config success by manager");
                LOGGER.warn(s.toString());
                OkPacket ok = new OkPacket();
                ok.packetId = 1;
                ok.affectedRows = 1;
                ok.serverStatus = 2;
                ok.message = "Rollback config success".getBytes();
                ok.write(c);
            } else {
                c.writeErrMessage(ErrorCode.ER_YES, "Rollback config failure");
            }
        } finally {
            lock.unlock();
        }
    }

    private static boolean rollback() {
        TimoConfig conf = TimoServer.getInstance().getConfig();
        Map<String, UserConfig> users = conf.getBackupUsers();
        Map<String, SchemaConfig> schemas = conf.getBackupSchemas();
        Map<String, MySQLDataNode> dataNodes = conf.getBackupDataNodes();
        Map<String, DataSourceConfig> dataSources = conf.getBackupDataSources();
        TimoCluster cluster = conf.getBackupCluster();
        QuarantineConfig quarantine = conf.getBackupQuarantine();

        // 检查可回滚状态
        if (!conf.canRollback()) {
            return false;
        }

        // 如果回滚已经存在的pool，初始化连接数参考空闲连接数，否则为1。
        boolean rollbackStatus = true;
        Map<String, MySQLDataNode> cNodes = conf.getDataNodes();
        for (MySQLDataNode dn : dataNodes.values()) {
            MySQLDataNode cdn = cNodes.get(dn.getName());
            if (cdn != null && cdn.getSource() != null) {
                int size = Math.min(cdn.getSource().getIdleCount(), dn.getConfig().getPoolSize());
                dn.init(size > 0 ? size : 1, dn.getActivedIndex());
            } else {
                dn.init(1, dn.getActivedIndex());
            }
            if (!dn.isInitSuccess()) {
                rollbackStatus = false;
                break;
            }
        }
        // 如果回滚不成功，则清理已初始化的资源。
        if (!rollbackStatus) {
            for (MySQLDataNode dn : dataNodes.values()) {
                MySQLDataSource ds = dn.getSource();
                if (ds != null) {
                    ds.clear();
                }
            }
            return false;
        }

        // 应用回滚
        conf.rollback(users, schemas, dataNodes, dataSources, cluster, quarantine);

        // 处理旧的资源
        for (MySQLDataNode dn : cNodes.values()) {
            MySQLDataSource ds = dn.getSource();
            if (ds != null) {
                ds.clear();
            }
        }

        return true;
    }

}
