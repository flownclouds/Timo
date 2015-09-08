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
 * @author xianmao.hexm
 */
public final class ServerParse {

    public static final int SELECT            = 1;
    public static final int INSERT            = 2;
    public static final int UPDATE            = 3;
    public static final int DELETE            = 4;
    public static final int USE               = 5;
    public static final int SET               = 6;
    public static final int COMMIT            = 7;
    public static final int ROLLBACK          = 8;
    public static final int BEGIN             = 9;
    public static final int SAVEPOINT         = 10;
    public static final int SHOW              = 11;
    public static final int REPLACE           = 12;
    public static final int START             = 13;
    public static final int KILL              = 14;
    public static final int EXPLAIN           = 15;
    public static final int KILL_QUERY        = 16;
    public static final int HELP              = 17;
    public static final int MYSQL_CMD_COMMENT = 18;
    public static final int MYSQL_COMMENT     = 19;
    public static final int CALL              = 20;
    public static final int DESC              = 21;
    public static final int SEQUENCE_DDL      = 22;
    public static final int LOCK              = 23;
    public static final int UNLOCK            = 24;
    public static final int PREPARE           = 25;
    public static final int EXECUTE           = 26;
    public static final int DROP_PREPARE      = 28;
    public static final int PROCEDURE         = 29;
    public static final int VIEW              = 30;
    public static final int OTHER             = -1;

    public static int parse(String stmt) {
        int lenth = stmt.length();
        for (int i = 0; i < lenth; ++i) {
            switch (stmt.charAt(i)) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                case '/':
                    // such as /*!40101 SET character_set_client = @saved_cs_client
                    // */;
                    if (i == 0 && stmt.charAt(1) == '*' && stmt.charAt(2) == '!'
                            && stmt.charAt(lenth - 2) == '*' && stmt.charAt(lenth - 1) == '/') {
                        return MYSQL_CMD_COMMENT;
                    }
                case '#':
                    i = ParseUtil.comment(stmt, i);
                    if (i + 1 == lenth) {
                        return MYSQL_COMMENT;
                    }
                    continue;
                case 'A':
                case 'a':
                    return alterCheck(stmt, i);
                case 'B':
                case 'b':
                    return beginCheck(stmt, i);
                case 'C':
                case 'c':
                    return commitOrCallCheck(stmt, i);
                case 'D':
                case 'd':
                    return deCheck(stmt, i);
                case 'E':
                case 'e':
                    return eCheck(stmt, i);
                case 'I':
                case 'i':
                    return insertCheck(stmt, i);
                case 'L':
                case 'l':
                    return lockCheck(stmt, i);
                case 'P':
                case 'p':
                    return prepareCheck(stmt, i);
                case 'R':
                case 'r':
                    return rCheck(stmt, i);
                case 'S':
                case 's':
                    return sCheck(stmt, i);
                case 'U':
                case 'u':
                    return uCheck(stmt, i);
                case 'K':
                case 'k':
                    return killCheck(stmt, i);
                case 'H':
                case 'h':
                    return helpCheck(stmt, i);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    private static int prepareCheck(String stmt, int offset) {
        if (stmt.length() > offset + "REPARE ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            if ((c1 == 'R' || c1 == 'r') && (c2 == 'E' || c2 == 'e') && (c3 == 'P' || c3 == 'p')
                    && (c4 == 'A' || c4 == 'a') && (c5 == 'R' || c5 == 'r')
                    && (c6 == 'E' || c6 == 'e')
                    && (c7 == ' ' || c7 == '\t' || c7 == '\r' || c7 == '\n')) {
                return PREPARE;
            }
        }
        return OTHER;
    }

    private static int lockCheck(String stmt, int offset) {
        if (stmt.length() > offset + "OCK ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'O' || c1 == 'o') && (c2 == 'C' || c2 == 'c') && (c3 == 'K' || c3 == 'k')
                    && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n')) {
                return LOCK;
            }
        }
        return OTHER;
    }

    private static int alterCheck(String stmt, int offset) {
        if (stmt.length() > offset + "LTER ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'L' || c1 == 'l') && (c2 == 'T' || c2 == 't') && (c3 == 'E' || c3 == 'e')
                    && (c4 == 'R' || c4 == 'r')
                    && (c5 == ' ' || c5 == '\t' || c5 == '\r' || c5 == '\n')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            continue;
                        case 'T':
                        case 't':
                            return SEQUENCE_DDL;
                        case 'P':
                        case 'p':
                            // ALTER PROCEDURE
                            return PROCEDURE;
                        case 'V':
                        case 'v':
                            // ALTER VIEW
                            return VIEW;
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    // HELP' '
    static int helpCheck(String stmt, int offset) {
        if (stmt.length() > offset + "ELP ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            if ((c1 == 'E' || c1 == 'e') && (c2 == 'L' || c2 == 'l') && (c3 == 'P' || c3 == 'p')) {
                return (offset << 8) | HELP;
            }
        }
        return OTHER;
    }

    // EXPLAIN' ' | EXECUTE
    static int eCheck(String stmt, int offset) {
        if (stmt.length() > offset + "XPLAIN ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            if ((c1 == 'X' || c1 == 'x')) {
                if ((c2 == 'P' || c2 == 'p') && (c3 == 'L' || c3 == 'l') && (c4 == 'A' || c4 == 'a')
                        && (c5 == 'I' || c5 == 'i') && (c6 == 'N' || c6 == 'n')
                        && (c7 == ' ' || c7 == '\t' || c7 == '\r' || c7 == '\n')) {
                    return (offset << 8) | EXPLAIN;
                } else if ((c2 == 'E' || c2 == 'e') && (c3 == 'C' || c3 == 'c')
                        && (c4 == 'U' || c4 == 'u') && (c5 == 'T' || c5 == 't')
                        && (c6 == 'E' || c6 == 'e')
                        && (c7 == ' ' || c7 == '\t' || c7 == '\r' || c7 == '\n')) {
                    return EXECUTE;
                }
            }
        }
        return OTHER;
    }

    // KILL' '
    static int killCheck(String stmt, int offset) {
        if (stmt.length() > offset + "ILL ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'I' || c1 == 'i') && (c2 == 'L' || c2 == 'l') && (c3 == 'L' || c3 == 'l')
                    && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            continue;
                        case 'Q':
                        case 'q':
                            return killQueryCheck(stmt, offset);
                        default:
                            return (offset << 8) | KILL;
                    }
                }
                return OTHER;
            }
        }
        return OTHER;
    }

    // KILL QUERY' '
    static int killQueryCheck(String stmt, int offset) {
        if (stmt.length() > offset + "UERY ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'U' || c1 == 'u') && (c2 == 'E' || c2 == 'e') && (c3 == 'R' || c3 == 'r')
                    && (c4 == 'Y' || c4 == 'y')
                    && (c5 == ' ' || c5 == '\t' || c5 == '\r' || c5 == '\n')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            continue;
                        default:
                            return (offset << 8) | KILL_QUERY;
                    }
                }
                return OTHER;
            }
        }
        return OTHER;
    }

    // BEGIN
    static int beginCheck(String stmt, int offset) {
        if (stmt.length() > offset + 4) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'E' || c1 == 'e') && (c2 == 'G' || c2 == 'g') && (c3 == 'I' || c3 == 'i')
                    && (c4 == 'N' || c4 == 'n')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return BEGIN;
            }
        }
        return OTHER;
    }

    // COMMIT
    static int commitCheck(String stmt, int offset) {
        if (stmt.length() > offset + 5) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'O' || c1 == 'o') && (c2 == 'M' || c2 == 'm') && (c3 == 'M' || c3 == 'm')
                    && (c4 == 'I' || c4 == 'i') && (c5 == 'T' || c5 == 't')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return COMMIT;
            }
        }

        return OTHER;
    }

    // CALL
    static int callCheck(String stmt, int offset) {
        if (stmt.length() > offset + 3) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            if ((c1 == 'A' || c1 == 'a') && (c2 == 'L' || c2 == 'l') && (c3 == 'L' || c3 == 'l')) {
                return CALL;
            }
        }

        return OTHER;
    }

    static int commitOrCallCheck(String stmt, int offset) {
        int sqlType = OTHER;
        switch (stmt.charAt((offset + 1))) {
            case 'O':
            case 'o':
                sqlType = commitCheck(stmt, offset);
                break;
            case 'A':
            case 'a':
                sqlType = callCheck(stmt, offset);
                break;
            case 'R':
            case 'r':
                sqlType = createCheck(stmt, offset);
                break;
            default:
                sqlType = OTHER;
        }
        return sqlType;
    }

    private static int createCheck(String stmt, int offset) {
        if (stmt.length() > offset + 5) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            if ((c1 == 'R' || c1 == 'r') && (c2 == 'E' || c2 == 'e') && (c3 == 'A' || c3 == 'a')
                    && (c4 == 'T' || c4 == 't') && (c5 == 'E' || c5 == 'e')) {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            continue;
                        case 'T':
                        case 't':
                            return SEQUENCE_DDL;
                        case 'P':
                        case 'p':
                            // CREATE PROCEDURE
                            return PROCEDURE;
                        case 'V':
                        case 'v':
                            // CREATE VIEW
                            return VIEW;
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    static int deCheck(String stmt, int offset) {
        if (stmt.length() > offset + 2) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            // 非严格判断
            if (c1 == 'E' || c1 == 'e') {
                if (c2 == 'L' || c2 == 'l') {
                    return DELETE;
                } else if (c2 == 'S' || c2 == 's') {
                    return DESC;
                } else if (c2 == 'A' || c2 == 'a') {
                    // DEALLOCATE
                    return DROP_PREPARE;
                }
            } else if ((c1 == 'R' || c1 == 'r') && (c2 == 'O' || c2 == 'o')) {
                return dropCheck(stmt, offset);
            }
        }
        return OTHER;
    }

    static int dropCheck(String stmt, int offset) {
        if (stmt.length() > offset + 2) {
            char c1 = stmt.charAt(++offset);
            if (c1 == 'P' || c1 == 'p') {
                while (stmt.length() > ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            continue;
                        case 'P':
                        case 'p':
                            return dropPrepareCheck(stmt, offset);
                        case 'V':
                        case 'v':
                            return VIEW;
                        default:
                            return OTHER;
                    }
                }
            }
        }
        return OTHER;
    }

    private static int dropPrepareCheck(String stmt, int offset) {
        if (stmt.length() > offset + "REPARE ".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            if ((c1 == 'R' || c1 == 'r') && (c2 == 'E' || c2 == 'e') && (c3 == 'P' || c3 == 'p')
                    && (c4 == 'A' || c4 == 'a') && (c5 == 'R' || c5 == 'r')
                    && (c6 == 'E' || c6 == 'e')
                    && (c7 == ' ' || c7 == '\t' || c7 == '\r' || c7 == '\n')) {
                return DROP_PREPARE;
            } else if ((c1 == 'R' || c1 == 'r') && (c2 == 'O' || c2 == 'o')) {
                // DROP PROCEDURE
                return PROCEDURE;
            }
        }
        return OTHER;
    }

    // DELETE' '
    static int deleteCheck(String stmt, int offset) {
        if (stmt.length() > offset + 6) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            if ((c1 == 'E' || c1 == 'e') && (c2 == 'L' || c2 == 'l') && (c3 == 'E' || c3 == 'e')
                    && (c4 == 'T' || c4 == 't') && (c5 == 'E' || c5 == 'e')
                    && (c6 == ' ' || c6 == '\t' || c6 == '\r' || c6 == '\n')) {
                return DELETE;
            }
        }
        return OTHER;
    }

    // INSERT' '
    static int insertCheck(String stmt, int offset) {
        if (stmt.length() > offset + 6) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            if ((c1 == 'N' || c1 == 'n') && (c2 == 'S' || c2 == 's') && (c3 == 'E' || c3 == 'e')
                    && (c4 == 'R' || c4 == 'r') && (c5 == 'T' || c5 == 't')
                    && (c6 == ' ' || c6 == '\t' || c6 == '\r' || c6 == '\n')) {
                return INSERT;
            }
        }
        return OTHER;
    }

    static int rCheck(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case 'E':
                case 'e':
                    return replaceCheck(stmt, offset);
                case 'O':
                case 'o':
                    return rollabckCheck(stmt, offset);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // REPLACE' '
    static int replaceCheck(String stmt, int offset) {
        if (stmt.length() > offset + 6) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            if ((c1 == 'P' || c1 == 'p') && (c2 == 'L' || c2 == 'l') && (c3 == 'A' || c3 == 'a')
                    && (c4 == 'C' || c4 == 'c') && (c5 == 'E' || c5 == 'e')
                    && (c6 == ' ' || c6 == '\t' || c6 == '\r' || c6 == '\n')) {
                return REPLACE;
            }
        }
        return OTHER;
    }

    // ROLLBACK
    static int rollabckCheck(String stmt, int offset) {
        if (stmt.length() > offset + 6) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            if ((c1 == 'L' || c1 == 'l') && (c2 == 'L' || c2 == 'l') && (c3 == 'B' || c3 == 'b')
                    && (c4 == 'A' || c4 == 'a') && (c5 == 'C' || c5 == 'c')
                    && (c6 == 'K' || c6 == 'k')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return ROLLBACK;
            }
        }
        return OTHER;
    }

    static int sCheck(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case 'A':
                case 'a':
                    return savepointCheck(stmt, offset);
                case 'E':
                case 'e':
                    return seCheck(stmt, offset);
                case 'H':
                case 'h':
                    return showCheck(stmt, offset);
                case 'T':
                case 't':
                    return startCheck(stmt, offset);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SAVEPOINT
    static int savepointCheck(String stmt, int offset) {
        if (stmt.length() > offset + 8) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            if ((c1 == 'V' || c1 == 'v') && (c2 == 'E' || c2 == 'e') && (c3 == 'P' || c3 == 'p')
                    && (c4 == 'O' || c4 == 'o') && (c5 == 'I' || c5 == 'i')
                    && (c6 == 'N' || c6 == 'n') && (c7 == 'T' || c7 == 't')
                    && (c8 == ' ' || c8 == '\t' || c8 == '\r' || c8 == '\n')) {
                return SAVEPOINT;
            }
        }
        return OTHER;
    }

    static int seCheck(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case 'L':
                case 'l':
                    return selectCheck(stmt, offset);
                case 'T':
                case 't':
                    if (stmt.length() > ++offset) {
                        char c = stmt.charAt(offset);
                        if (c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == '/'
                                || c == '#') {
                            return (offset << 8) | SET;
                        }
                    }
                    return OTHER;
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // SELECT' '
    static int selectCheck(String stmt, int offset) {
        if (stmt.length() > offset + 4) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'E' || c1 == 'e') && (c2 == 'C' || c2 == 'c') && (c3 == 'T' || c3 == 't')
                    && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n' || c4 == '/'
                            || c4 == '#' || c4 == '*')) {
                return (offset << 8) | SELECT;
            }
        }
        return OTHER;
    }

    // SHOW' '
    static int showCheck(String stmt, int offset) {
        if (stmt.length() > offset + 3) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            if ((c1 == 'O' || c1 == 'o') && (c2 == 'W' || c2 == 'w')
                    && (c3 == ' ' || c3 == '\t' || c3 == '\r' || c3 == '\n')) {
                return (offset << 8) | SHOW;
            }
        }
        return OTHER;
    }

    // START' '
    static int startCheck(String stmt, int offset) {
        if (stmt.length() > offset + 4) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            if ((c1 == 'A' || c1 == 'a') && (c2 == 'R' || c2 == 'r') && (c3 == 'T' || c3 == 't')
                    && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n')) {
                return (offset << 8) | START;
            }
        }
        return OTHER;
    }

    // UNLOCK TABLES | UPDATE' ' | USE' '
    static int uCheck(String stmt, int offset) {
        if (stmt.length() > ++offset) {
            switch (stmt.charAt(offset)) {
                case 'N':
                case 'n':
                    if (stmt.length() > offset + 5) {
                        char c1 = stmt.charAt(++offset);
                        char c2 = stmt.charAt(++offset);
                        char c3 = stmt.charAt(++offset);
                        char c4 = stmt.charAt(++offset);
                        char c5 = stmt.charAt(++offset);
                        if ((c1 == 'L' || c1 == 'l') && (c2 == 'O' || c2 == 'o')
                                && (c3 == 'C' || c3 == 'c') && (c4 == 'K' || c4 == 'k')
                                && (c5 == ' ' || c5 == '\t' || c5 == '\r' || c5 == '\n')) {
                            return UNLOCK;
                        }
                    }
                    break;
                case 'P':
                case 'p':
                    if (stmt.length() > offset + 5) {
                        char c1 = stmt.charAt(++offset);
                        char c2 = stmt.charAt(++offset);
                        char c3 = stmt.charAt(++offset);
                        char c4 = stmt.charAt(++offset);
                        char c5 = stmt.charAt(++offset);
                        if ((c1 == 'D' || c1 == 'd') && (c2 == 'A' || c2 == 'a')
                                && (c3 == 'T' || c3 == 't') && (c4 == 'E' || c4 == 'e')
                                && (c5 == ' ' || c5 == '\t' || c5 == '\r' || c5 == '\n')) {
                            return UPDATE;
                        }
                    }
                    break;
                case 'S':
                case 's':
                    if (stmt.length() > offset + 2) {
                        char c1 = stmt.charAt(++offset);
                        char c2 = stmt.charAt(++offset);
                        if ((c1 == 'E' || c1 == 'e')
                                && (c2 == ' ' || c2 == '\t' || c2 == '\r' || c2 == '\n')) {
                            return (offset << 8) | USE;
                        }
                    }
                    break;
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

}
