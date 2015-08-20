/*
 * Copyright 2015 Liu Huanting.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import org.pmw.tinylog.Logger;
import fm.liu.timo.config.loader.ServerConfigLoader;
import fm.liu.timo.config.loader.SystemConfigLoader;
import fm.liu.timo.config.model.Database;
import fm.liu.timo.config.model.Datanode;
import fm.liu.timo.config.model.Datasource;
import fm.liu.timo.config.model.SystemConfig;
import fm.liu.timo.config.model.User;
import fm.liu.timo.net.backend.Node;
import fm.liu.timo.net.backend.Source;
import fm.liu.timo.net.connection.Variables;
import fm.liu.timo.util.TimeUtil;

/**
 * @author Liu Huanting 2015年5月10日
 */
public class TimoConfig {
    private volatile SystemConfig             system;
    private volatile Map<String, User>        users;
    private volatile Map<String, Database>    databases;
    private volatile Map<Integer, Node>       nodes;
    private volatile Map<Integer, Datasource> datasources;
    private ReentrantLock                     lock = new ReentrantLock();
    private long                              lastReloadTime;

    public TimoConfig() {
        this.system = new SystemConfigLoader().getSystemConfig();
        ServerConfigLoader conf =
                new ServerConfigLoader(system.getUrl(), system.getUsername(), system.getPassword());
        this.users = conf.getUsers();
        this.databases = conf.getDatabases();
        this.datasources = conf.getDatasources();
        this.nodes = initDatanodes(conf.getDatanodes(), conf.getDatasources(), conf.getHandovers());
    }

    private Map<Integer, Node> initDatanodes(Map<Integer, Datanode> datanodes,
            Map<Integer, Datasource> datasources, Map<Integer, ArrayList<Integer>> handovers) {
        Variables variables = new Variables();
        variables.setCharset(system.getCharset());
        variables.setIsolationLevel(system.getTxIsolation());
        Map<Integer, Source> sources = new HashMap<>();
        for (Entry<Integer, Datasource> datasource : datasources.entrySet()) {
            sources.put(datasource.getKey(),
                    new Source(datasource.getValue(), variables, system.getHeartbeatPeriod()));
        }

        for (Integer id : handovers.keySet()) {
            ArrayList<Source> backups = new ArrayList<>();
            for (Integer handover : handovers.get(id)) {
                backups.add(sources.get(handover));
            }
            sources.get(id).setBackups(backups);
        }

        Map<Integer, Node> nodes = new HashMap<Integer, Node>();
        for (Datanode datanode : datanodes.values()) {
            ArrayList<Source> sourceList = new ArrayList<Source>();
            for (Integer i : datanode.getDatasources()) {
                sourceList.add(sources.get(i));
            }
            Node node = new Node(datanode.getID(), datanode.getStrategy(), sourceList);
            nodes.put(datanode.getID(), node);
        }
        return nodes;
    }

    private volatile SystemConfig             _system;
    private volatile Map<String, User>        _users;
    private volatile Map<String, Database>    _databases;
    private volatile Map<Integer, Node>       _nodes;
    private volatile Map<Integer, Datasource> _datasources;

    /**
     * reload @@config
     */
    public boolean reload() {
        boolean success = false;
        _system = this.system;
        _users = this.users;
        _databases = this.databases;
        _datasources = this.datasources;
        _nodes = this.nodes;
        lock.lock();
        try {
            this.system = new SystemConfigLoader().getSystemConfig();
            ServerConfigLoader conf = new ServerConfigLoader(system.getUrl(), system.getUsername(),
                    system.getPassword());
            this.users = conf.getUsers();
            this.databases = conf.getDatabases();
            this.datasources = conf.getDatasources();
            this.nodes =
                    initDatanodes(conf.getDatanodes(), conf.getDatasources(), conf.getHandovers());
            for (Node node : nodes.values()) {
                if (!node.init()) {
                    throw new Exception(
                            "node " + node.getID() + " init failed in config reloading.");
                }
            }
            success = true;
        } catch (Exception e) {
            this.system = _system;
            this.users = _users;
            this.databases = _databases;
            this.datasources = _datasources;
            this.nodes = _nodes;
            Logger.warn("reload config failed due to {}", e.getMessage());
        } finally {
            lock.unlock();
        }
        if (success) {
            for (Node node : _nodes.values()) {
                node.clear();
            }
            lastReloadTime = TimeUtil.currentTimeMillis();
        }
        return success;
    }

    /**
     * rollback @@config
     */
    public boolean rollback() {
        if (lastReloadTime == 0) {
            return false;
        }
        Collection<Node> backNodes = this.nodes.values();
        boolean success = false;
        lock.lock();
        try {
            for (Node node : _nodes.values()) {
                if (!node.init()) {
                    throw new Exception(
                            "node " + node.getID() + " init failed in config reloading.");
                }
            }
            this.system = _system;
            this.users = _users;
            this.databases = _databases;
            this.datasources = _datasources;
            this.nodes = _nodes;
            success = true;
        } catch (Exception e) {
            for (Node node : _nodes.values()) {
                node.clear();
            }
        } finally {
            lock.unlock();
        }
        if (success) {
            for (Node node : backNodes) {
                node.clear();
            }
        }
        return success;
    }

    public SystemConfig getSystem() {
        return system;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Database> getDatabases() {
        return databases;
    }

    public Map<Integer, Node> getNodes() {
        return nodes;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Map<Integer, Datasource> getDatasources() {
        return datasources;
    }

}
