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
package fm.liu.timo.config.model;

import java.util.Set;

/**
 * @author Liu Huanting 2015年5月9日
 */
public class User {
    private final String username;
    private final String password;
    private final Set<String> databases;
    private final Set<String> hosts;

    public User(String username, String password, Set<String> databases, Set<String> hosts) {
        this.username = username;
        this.password = password;
        this.databases = databases;
        this.hosts = hosts;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<String> getDatabases() {
        return databases;
    }

    public Set<String> getHosts() {
        return hosts;
    }
}
