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
/**
 * (created at 2012-6-14)
 */
package re.ovo.timo.config.loader.xml;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import re.ovo.timo.config.loader.ConfigLoader;
import re.ovo.timo.config.loader.SchemaLoader;
import re.ovo.timo.config.model.ClusterConfig;
import re.ovo.timo.config.model.DataNodeConfig;
import re.ovo.timo.config.model.DataSourceConfig;
import re.ovo.timo.config.model.QuarantineConfig;
import re.ovo.timo.config.model.SchemaConfig;
import re.ovo.timo.config.model.SystemConfig;
import re.ovo.timo.config.model.UserConfig;
import re.ovo.timo.config.model.rule.RuleAlgorithm;
import re.ovo.timo.config.model.rule.RuleConfig;

/**
 * @author <a href="mailto:shuo.qius@alibaba-inc.com">QIU Shuo</a>
 */
public class XMLConfigLoader implements ConfigLoader {
    /** unmodifiable */
    private final Set<RuleConfig> rules;
    /** unmodifiable */
    private final Map<String, RuleAlgorithm> functions;
    /** unmodifiable */
    private final Map<String, DataSourceConfig> dataSources;
    /** unmodifiable */
    private final Map<String, DataNodeConfig> dataNodes;
    /** unmodifiable */
    private final Map<String, SchemaConfig> schemas;
    private final SystemConfig system;
    /** unmodifiable */
    private final Map<String, UserConfig> users;
    private final QuarantineConfig quarantine;
    private final ClusterConfig cluster;

    public XMLConfigLoader(SchemaLoader schemaLoader) {
        this.functions = Collections.unmodifiableMap(schemaLoader.getFunctions());
        this.dataSources = schemaLoader.getDataSources();
        this.dataNodes = schemaLoader.getDataNodes();
        this.schemas = schemaLoader.getSchemas();
        this.rules = schemaLoader.listRuleConfig();
        schemaLoader = null;
        XMLServerLoader serverLoader = new XMLServerLoader();
        this.system = serverLoader.getSystem();
        this.users = serverLoader.getUsers();
        this.quarantine = serverLoader.getQuarantine();
        this.cluster = serverLoader.getCluster();
    }

    @Override
    public ClusterConfig getClusterConfig() {
        return cluster;
    }

    @Override
    public QuarantineConfig getQuarantineConfig() {
        return quarantine;
    }

    @Override
    public UserConfig getUserConfig(String user) {
        return users.get(user);
    }

    @Override
    public Map<String, UserConfig> getUserConfigs() {
        return users;
    }

    @Override
    public SystemConfig getSystemConfig() {
        return system;
    }

    @Override
    public Map<String, RuleAlgorithm> getRuleFunction() {
        return functions;
    }

    @Override
    public Set<RuleConfig> listRuleConfig() {
        return rules;
    }

    @Override
    public Map<String, SchemaConfig> getSchemaConfigs() {
        return schemas;
    }

    @Override
    public Map<String, DataNodeConfig> getDataNodes() {
        return dataNodes;
    }

    @Override
    public Map<String, DataSourceConfig> getDataSources() {
        return dataSources;
    }

    @Override
    public SchemaConfig getSchemaConfig(String schema) {
        return schemas.get(schema);
    }

}
