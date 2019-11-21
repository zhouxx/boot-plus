/**
 *    Copyright 2017-2019 the original author or authors.
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
package com.alili.core.util;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class JavaBeansUtil {

    /**
     * 根据属性名称和java类型，获取对应的getter方法名
     * @param property
     * @param javaType
     * @return
     */
    public static String getGetterMethodName(String property, String javaType) {
        StringBuilder sb = covertMethodPost(property);
        if ("boolean".equals(javaType)) {
            sb.insert(0, "is");
        } else {
            sb.insert(0, "get");
        }
        return sb.toString();
    }

    /**
     * 根据属性名称获取对应的setter方法名称
     * @param property
     * @return
     */
    public static String getSetterMethodName(String property) {
        StringBuilder sb = covertMethodPost(property);
        sb.insert(0, "set");
        return sb.toString();
    }

    private static StringBuilder covertMethodPost(String property) {
        StringBuilder sb = new StringBuilder();
        sb.append(property);
        if (Character.isLowerCase(sb.charAt(0))) {
            if (sb.length() == 1 || !Character.isUpperCase(sb.charAt(1))) {
                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            }
        }
        return sb;
    }

}
