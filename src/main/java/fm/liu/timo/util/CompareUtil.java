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
package fm.liu.timo.util;

/**
 * @author Liu Huanting 2015年6月4日
 */
public class CompareUtil {
    public static int compareBytes(byte[] l, byte[] r) {
        if (l == null || l.length == 0)
            return -1;
        else if (r == null || r.length == 0)
            return 1;
        boolean isNegetive = l[0] == 45 || r[0] == 45;
        if (isNegetive == false && l.length != r.length) {
            return l.length - r.length;
        }
        int len = l.length > r.length ? r.length : l.length;
        int result = 0;
        int index = -1;
        for (int i = 0; i < len; i++) {
            int b1val = l[i];
            int b2val = r[i];
            if (b1val > b2val) {
                result = 1;
                index = i;
                break;
            } else if (b1val < b2val) {
                index = i;
                result = -1;
                break;
            }
        }
        if (index == 0) {
            // first byte compare
            return result;
        } else {
            if (l.length != r.length) {
                int lenDelta = l.length - r.length;
                return isNegetive ? 0 - lenDelta : lenDelta;
            } else {
                return isNegetive ? 0 - result : result;
            }
        }
    }

    public static int compareInt(int l, int r) {

        if (l > r) {
            return 1;
        } else if (l < r) {
            return -1;
        } else {
            return 0;
        }

    }

    public static int compareDouble(double l, double r) {

        if (l > r) {
            return 1;
        } else if (l < r) {
            return -1;
        } else {
            return 0;
        }

    }

    public static int compareFloat(float l, float r) {

        if (l > r) {
            return 1;
        } else if (l < r) {
            return -1;
        } else {
            return 0;
        }

    }

    public static int compareLong(long l, long r) {
        if (l > r) {
            return 1;
        } else if (l < r) {
            return -1;
        } else {
            return 0;
        }

    }

    public static int compareString(String l, String r) {
        if (l == null)
            return -1;
        else if (r == null)
            return 1;
        return l.compareToIgnoreCase(r);
    }

    public static int compareChar(char l, char r) {

        if (l > r) {
            return 1;
        } else if (l < r) {
            return -1;
        } else {
            return 0;
        }

    }

    public static int compareUtilDate(Object left, Object right) {

        java.util.Date l = (java.util.Date) left;
        java.util.Date r = (java.util.Date) right;

        return l.compareTo(r);

    }

    public static int compareSqlDate(Object left, Object right) {

        java.sql.Date l = (java.sql.Date) left;
        java.sql.Date r = (java.sql.Date) right;

        return l.compareTo(r);

    }

    public static int compareStringForChinese(String s1, String s2) {
        String m_s1 = null, m_s2 = null;
        try {
            // 先将两字符串编码成GBK
            m_s1 = new String(s1.getBytes("GB2312"), "GBK");
            m_s2 = new String(s2.getBytes("GB2312"), "GBK");
        } catch (Exception ex) {
            ex.printStackTrace();
            return s1.compareTo(s2);
        }
        int res = chineseCompareTo(m_s1, m_s2);

        // System.out.println("比较：" + s1 + " | " + s2 + "==== Result: " + res);
        return res;
    }

    // 获取一个汉字/字母的Char值
    private static int getCharCode(String s) {
        if (s == null || s.length() == 0)
            return -1;// 保护代码
        byte[] b = s.getBytes();
        int value = 0;
        // 保证取第一个字符（汉字或者英文）
        for (int i = 0; i < b.length && i <= 2; i++) {
            value = value * 100 + b[i];
        }
        if (value < 0) {
            value += 100000;
        }

        return value;
    }

    // 比较两个字符串
    private static int chineseCompareTo(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int n = Math.min(len1, len2);

        for (int i = 0; i < n; i++) {
            int s1_code = getCharCode(s1.charAt(i) + "");
            int s2_code = getCharCode(s2.charAt(i) + "");
            if (s1_code != s2_code) {
                return s1_code - s2_code;
            }
        }
        return len1 - len2;
    }
}
