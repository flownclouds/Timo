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
package fm.liu.timo.merger;

/**
 * @author Liu Huanting 2015年6月4日
 */
public class ColumnType {
    public static final int DECIMAL = 0;
    public static final int INT = 1;
    public static final int SHORT = 2;
    public static final int LONG = 3;
    public static final int FLOAT = 4;
    public static final int DOUBLE = 5;
    public static final int NULL = 6;
    public static final int TIMSTAMP = 7;
    public static final int LONGLONG = 8;
    public static final int INT24 = 9;
    public static final int DATE = 0x0a;
    public static final int DATETIME = 0X0C;
    public static final int TIME = 0x0b;
    public static final int YEAR = 0x0d;
    public static final int NEWDATE = 0x0e;
    public static final int VACHAR = 0x0f;
    public static final int BIT = 0x10;
    public static final int NEWDECIMAL = 0xf6;
    public static final int ENUM = 0xf7;
    public static final int SET = 0xf8;
    public static final int TINY_BLOB = 0xf9;
    public static final int TINY_TYPE_MEDIUM_BLOB = 0xfa;
    public static final int TINY_TYPE_LONG_BLOB = 0xfb;
    public static final int BLOB = 0xfc;
    public static final int VAR_STRING = 0xfd;
    public static final int STRING = 0xfe;
    public static final int GEOMETRY = 0xff;
}
