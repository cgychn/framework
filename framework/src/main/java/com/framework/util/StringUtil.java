package com.framework.util;

public class StringUtil {

    /**
     * @param rawString 待处理字符串
     * @param replaceWhat 替换什么
     * @param replaceTo 替换成什么
     * @return
     */
    public static String replaceFirstWithOutReg (String rawString, String replaceWhat, String replaceTo) {
        for (int i = 0; i < rawString.length(); i++) {
            if (rawString.charAt(i) == replaceWhat.charAt(0)) {
                // 向后找
                for (int j = 1; j < replaceWhat.length(); j++) {
                    if (replaceWhat.charAt(j) != rawString.charAt(i + j)) {
                        // 如果在replacewhat的遍历中发现rawString中有一个字符与其不同，就停止
                        break;
                    }
                    if (j == replaceWhat.length() - 1) {
                        // 匹配到了
                        // 字符串的下标是 i 到 j
                        rawString = rawString.substring(0, i) + rawString.substring(i + j + 1);
                        StringBuffer stringBuffer = new StringBuffer(rawString);
                        System.out.println(stringBuffer);
                        for (int k = replaceTo.length() - 1; k >= 0; k--) {
                            stringBuffer.insert(i, replaceTo.charAt(k));
                        }
                        return stringBuffer.toString();
                    }
                }
            }
        }
        return rawString;
    }

    /**
     * 驼峰
     * @param val
     * @return
     */
    public static String firstCharLowerCase (String val) {
        char[] chars = val.toCharArray();
        chars[0] = (chars[0] + "").toLowerCase().toCharArray()[0];
        return String.valueOf(chars);
    }

    /**
     * 判断字符串是否为空串或者null
     * @return
     */
    public static boolean isEmpty (String str) {
        return str == null || "".equals(str);
    }

    public static void main (String[] args ) {
        replaceFirstWithOutReg("select user_name as userName, login_name as loginName, password from t_user where user_name like '%#{userName}%'", "#{userName}", "?");
    }

}
