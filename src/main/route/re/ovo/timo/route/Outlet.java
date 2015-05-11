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
package re.ovo.timo.route;

/**
 * @author Liu Huanting
 * 2015年5月10日
 */
public class Outlet {
    private final int id;
    private final String sql;

    public Outlet(int id, String sql) {
        this.id = id;
        this.sql = sql;
    }

    public int getID() {
        return id;
    }

    public String getSql() {
        return sql;
    }
    
    @Override
    public int hashCode(){
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Outlet out = (Outlet) o;
        if (id != out.id) {
            return false;
        }
        return true;
    }
}
