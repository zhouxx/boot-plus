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
package com.alilitech.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * covert common String to unicode or covert unicode to common String
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class UnicodeUtils {

    public static String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }

    public static String stringToUnicode(String s) {
        String prifix = "\\u";
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String code = prifix + format(Integer.toHexString(c));
            unicode.append(code);
        }
        return unicode.toString();
    }


    /**
     * 为长度不足4位的unicode 值补零
     */
    private static String format(String str) {
        StringBuilder strBuilder = new StringBuilder(str);
        for (int i = 0, l = 4 - strBuilder.length(); i < l; i++) {
            strBuilder.insert(0, "0");
        }
        str = strBuilder.toString();
        return str;
    }

}
