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
package fm.liu.timo.server.parser;

import fm.liu.timo.parser.util.ParseUtil;

/**
 * @author xianmao.hexm 2011-5-7 下午01:23:06
 */
public final class ServerParseShow {

    public static final int OTHER        = -1;
    public static final int DATABASES    = 1;
    public static final int DATASOURCES  = 2;
    public static final int TIMO_STATUS  = 3;
    public static final int TIMO_CLUSTER = 4;
    public static final int TABLES       = 5;
    public static final int FULL_TABLES  = 6;

    public static int parse(String stmt, int offset) {
        int i = offset;
        for (; i < stmt.length(); i++) {
            switch (stmt.charAt(i)) {
                case ' ':
                    continue;
                case '/':
                case '#':
                    i = ParseUtil.comment(stmt, i);
                    continue;
                case 'F':
                case 'f':
                    return fullCheck(stmt, i);
                case 'T':
                case 't':
                    return tCheck(stmt, i);
                case 'D':
                case 'd':
                    return dataCheck(stmt, i);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SHOW FULL TABLES
    private static int fullCheck(String stmt, int offset) {
        if (stmt.length() > offset + "ull tables".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            char c9 = stmt.charAt(++offset);
            char c10 = stmt.charAt(++offset);
            if ((c1 == 'U' || c1 == 'u') && (c2 == 'L' || c2 == 'l') && (c3 == 'L' || c3 == 'l')
                    && (c4 == ' ') && (c5 == 'T' || c5 == 't') && (c6 == 'A' || c6 == 'a')
                    && (c7 == 'B' || c7 == 'b') && (c8 == 'L' || c8 == 'l')
                    && (c9 == 'E' || c9 == 'e') && (c10 == 'S' || c10 == 's')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return FULL_TABLES;
            }
        }
        return OTHER;
    }

    private static int tCheck(String stmt, int offset) {
        if (stmt.length() > offset + 1) {
            switch (stmt.charAt(++offset)) {
                case 'A':
                case 'a':
                    return blesCheck(stmt, offset);
                case 'I':
                case 'i':
                    return moCheck(stmt, offset);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SHOW TABLES
    private static int blesCheck(String stmt, int offset) {
        if (stmt.length() > offset + "bles".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'B' || c1 == 'b') && (c2 == 'L' || c2 == 'l') && (c3 == 'E' || c3 == 'e')
                    && (c4 == 'S' || c4 == 's')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return TABLES;
            }
        }
        return OTHER;
    }

    // SHOW TIMO_
    static int moCheck(String stmt, int offset) {
        if (stmt.length() > offset + "mo_?".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            if ((c1 == 'M' || c1 == 'm') && (c2 == 'O' || c2 == 'o') && (c3 == '_')) {
                switch (stmt.charAt(++offset)) {
                    case 'S':
                    case 's':
                        return showTimoStatus(stmt, offset);
                    case 'C':
                    case 'c':
                        return showTimoCluster(stmt, offset);
                    default:
                        return OTHER;
                }
            }
        }
        return OTHER;
    }

    // SHOW TIMO_STATUS
    static int showTimoStatus(String stmt, int offset) {
        if (stmt.length() > offset + "tatus".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 't' || c1 == 'T') && (c2 == 'a' || c2 == 'A') && (c3 == 't' || c3 == 'T')
                    && (c4 == 'u' || c4 == 'U') && (c5 == 's' || c5 == 'S')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return TIMO_STATUS;
            }
        }
        return OTHER;
    }

    // SHOW Timo_CLUSTER
    static int showTimoCluster(String stmt, int offset) {
        if (stmt.length() > offset + "luster".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            if ((c1 == 'L' || c1 == 'l') && (c2 == 'U' || c2 == 'u') && (c3 == 'S' || c3 == 's')
                    && (c4 == 'T' || c4 == 't') && (c5 == 'E' || c5 == 'e')
                    && (c6 == 'R' || c6 == 'r')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return TIMO_CLUSTER;
            }
        }
        return OTHER;
    }

    // SHOW DATA
    static int dataCheck(String stmt, int offset) {
        if (stmt.length() > offset + "ata?".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            if ((c1 == 'A' || c1 == 'a') && (c2 == 'T' || c2 == 't') && (c3 == 'A' || c3 == 'a')) {
                switch (stmt.charAt(++offset)) {
                    case 'B':
                    case 'b':
                        return showDatabases(stmt, offset);
                    case 'S':
                    case 's':
                        return showDataSources(stmt, offset);
                    default:
                        return OTHER;
                }
            }
        }
        return OTHER;
    }

    // SHOW DATABASES
    static int showDatabases(String stmt, int offset) {
        if (stmt.length() > offset + "ases".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'A' || c1 == 'a') && (c2 == 'S' || c2 == 's') && (c3 == 'E' || c3 == 'e')
                    && (c4 == 'S' || c4 == 's')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return DATABASES;
            }
        }
        return OTHER;
    }

    // SHOW DATASOURCES
    static int showDataSources(String stmt, int offset) {
        if (stmt.length() > offset + "ources".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            if ((c1 == 'O' || c1 == 'o') && (c2 == 'U' || c2 == 'u') && (c3 == 'R' || c3 == 'r')
                    && (c4 == 'C' || c4 == 'c') && (c5 == 'E' || c5 == 'e')
                    && (c6 == 'S' || c6 == 's')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return DATASOURCES;
            }
        }
        return OTHER;
    }

}
