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

import java.util.Map;
import java.util.Set;
import fm.liu.timo.config.model.User;
import fm.liu.timo.net.handler.FrontendPrivileges;

/**
 * @author xianmao.hexm
 */
public class TimoPrivileges implements FrontendPrivileges {

    @Override
    public boolean schemaExists(String db) {
        TimoConfig conf = TimoServer.getInstance().getConfig();
        return conf.getDatabases().containsKey(db.toUpperCase());
    }

    @Override
    public boolean userExists(String user, String host) {
        Map<String, User> users = TimoServer.getInstance().getConfig().getUsers();
        if (users.containsKey(user)) {
            Set<String> hosts = users.get(user).getHosts();
            if (!hosts.isEmpty() && !hosts.contains(host)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String getPassword(String user) {
        TimoConfig conf = TimoServer.getInstance().getConfig();
        if (user != null && user.equals(conf.getSystem().getClusterHeartbeatUser())) {
            return conf.getSystem().getClusterHeartbeatPass();
        } else {
            User uc = conf.getUsers().get(user);
            if (uc != null) {
                return uc.getPassword();
            } else {
                return null;
            }
        }
    }

    @Override
    public Set<String> getUserSchemas(String user) {
        TimoConfig conf = TimoServer.getInstance().getConfig();
        User uc = conf.getUsers().get(user);
        if (uc != null) {
            return uc.getDatabases();
        } else {
            return null;
        }
    }

}
