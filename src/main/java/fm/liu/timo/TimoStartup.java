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

import java.text.SimpleDateFormat;
import java.util.Date;
import org.pmw.tinylog.Logger;

/**
 * @author xianmao.hexm 2011-4-22 下午09:43:05
 */
public final class TimoStartup {
    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    public static void main(String[] args) {
        try {
            TimoServer server = TimoServer.getInstance();
            server.startup();
        } catch (Throwable e) {
            e.printStackTrace();
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            Logger.error(sdf.format(new Date()) + " startup error", e);
            System.exit(-1);
        }
    }

}
