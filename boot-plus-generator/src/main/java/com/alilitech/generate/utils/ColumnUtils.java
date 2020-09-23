/*
 *    Copyright 2017-2020 the original author or authors.
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
package com.alilitech.generate.utils;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class ColumnUtils {

    public static String getJavaStyle(String column) {
        StringBuilder ret = new StringBuilder();
        String arr[] = column.split("_");
        for(int i=0; i<arr.length; i++) {
            if(i == 0) {
                ret.append(arr[i]);
            } else {
                ret.append(initUpper(arr[i]));
            }
        }
        return ret.toString();
    }

    /**
     * 功能：将输入字符串的首字母改成大写
     * @param str
     * @return
     */
    public static String initUpper(String str) {

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
    public static String initLower(String str) {

        char[] ch = str.toCharArray();
        if(ch[0] >= 'A' && ch[0] <= 'Z'){
            ch[0] = (char)(ch[0] + 32);
        }

        return new String(ch);
    }


}
