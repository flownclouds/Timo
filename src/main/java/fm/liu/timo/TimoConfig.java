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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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

/**
 * @author Liu Huanting 2015年5月10日
 */
public class TimoConfig {
    private volatile SystemConfig system;
    private volatile Map<String, User> users;
    private volatile Map<String, Database> databases;
    private volatile Map<Integer, Node> nodes;
    private ReentrantLock lock = new ReentrantLock();
    private final Map<Integer, Datasource> datasources;

    public TimoConfig() {
        this.system = new SystemConfigLoader().getSystemConfig();
        ServerConfigLoader conf =
                new ServerConfigLoader(system.getUrl(), system.getUsername(), system.getPassword());
        this.users = conf.getUsers();
        this.databases = conf.getDatabases();
        this.datasources = conf.getDatasources();
        this.nodes = initDatanodes(conf.getDatanodes(), conf.getDatasources());
    }

    private Map<Integer, Node> initDatanodes(Map<Integer, Datanode> datanodes,
            Map<Integer, Datasource> datasources) {
        Map<Integer, Node> nodes = new HashMap<Integer, Node>();
        Variables variables = new Variables();
        variables.setCharset(system.getCharset());
        variables.setIsolationLevel(system.getTxIsolation());
        for (Datanode datanode : datanodes.values()) {
            Map<Integer, Source> sources = new HashMap<Integer, Source>();
            for (Integer i : datanode.getDatasources()) {
                Datasource datasource = datasources.get(i);
                Source source = new Source(datasource, i, variables);
                sources.put(i, source);
            }
            Node node = new Node(datanode.getID(), sources);
            nodes.put(datanode.getID(), node);
        }
        return nodes;
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
