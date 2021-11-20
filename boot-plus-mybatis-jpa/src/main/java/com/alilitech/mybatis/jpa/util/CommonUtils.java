/*
 *    Copyright 2017-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.alilitech.mybatis.jpa.util;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class CommonUtils {

    private CommonUtils() {
    }

    /**
     * 将驼峰标识转换为下划线
     */
    public static String camelToUnderline(String text) {
        if (text == null || "".equals(text.trim())) {
            return "";
        }
        StringBuilder result = new StringBuilder(text.length() + 1);
        result.append(text, 0, 1);
        for (int i = 1; i < text.length(); i++) {
            if (!Character.isLowerCase(text.charAt(i))) {
                result.append('_');
            }
            result.append(text, i, i + 1);
        }
        return result.toString().toLowerCase();
    }

    /**
     * 将下划线标识转换为驼峰
     */
    public static String underlineToCamel(String text) {
        if (text == null || "".equals(text.trim())) {
            return "";
        }
        int length = text.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if(i == 0 || text.charAt(i) == '_') {
                continue;
            }
            char c = text.charAt(i-1);
            if (c == '_') {
                result.append(Character.toUpperCase(text.charAt(i)));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 功能：将输入字符串的首字母改成大写
     * @param str
     * @return
     */
    public static String upperFirst(String str) {

        char[] ch = str.toCharArray();
        if(ch[0] >= 'a' && ch[0] <= 'z'){
            ch[0] = (char)(ch[0] - 32);
        }

        return new String(ch);
    }
    /**
     * 功能：将输入字符串的首字母改成小写
     * @param str
     * @return
     */
    public static String lowerFirst(String str) {

        char[] ch = str.toCharArray();
        if(ch[0] >= 'A' && ch[0] <= 'Z'){
            ch[0] = (char)(ch[0] + 32);
        }

        return new String(ch);
    }

}
