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
package fm.liu.timo.config.loader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fm.liu.timo.config.model.SystemConfig;
import fm.liu.timo.config.util.ConfigException;
import fm.liu.timo.config.util.ConfigUtil;
import fm.liu.timo.config.util.ParameterMapping;

/**
 * @author Liu Huanting 2015年5月9日
 */
public class SystemConfigLoader {
    private final SystemConfig system;

    public SystemConfigLoader() {
        this.system = new SystemConfig();
        this.load();
    }

    public SystemConfig getSystemConfig() {
        return system;
    }

    private void load() {
        InputStream xml = null;
        try {
            xml = SystemConfigLoader.class.getResourceAsStream("/server.xml");
            Element root = ConfigUtil.getDocument(null, xml).getDocumentElement();
            loadSystem(root);
        } catch (ConfigException e) {
            throw e;
        } catch (Throwable e) {
            throw new ConfigException(e);
        } finally {
            if (xml != null) {
                try {
                    xml.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void loadSystem(Element root) throws IllegalAccessException, InvocationTargetException {
        NodeList list = root.getElementsByTagName("system");
        for (int i = 0, n = list.getLength(); i < n; i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                Map<String, Object> props = ConfigUtil.loadElements((Element) node);
                ParameterMapping.mapping(system, props);
            }
        }
    }
}
