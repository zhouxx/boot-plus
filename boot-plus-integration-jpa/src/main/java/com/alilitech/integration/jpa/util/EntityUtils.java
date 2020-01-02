/**
 * Copyright 2017-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alilitech.integration.jpa.util;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class EntityUtils {

    /**
     * 获取Java对象对应的表名, 默认下划线风格
     * @param clazz
     * @return
     */
    public static String getTableName(Class<?> clazz) {
        // 判断是否有Table注解
        if (clazz.isAnnotationPresent(Table.class)) {
            // 获取注解对象
            Table table = clazz.getAnnotation(Table.class);
            // 设置了name属性
            if (!table.name().trim().equals("")) {
                return table.name();
            }
        }
        // 类名
        String className = clazz.getSimpleName();

        return CommonUtils.camelToUnderline(className);
    }

    public static String getEntityName(Class<?> type) {
        if (type.isAnnotationPresent(Entity.class)) {
            // 获取注解对象
            Entity entity = type.getAnnotation(Entity.class);
            // 设置了mappedBy()属性
            if (!entity.name().trim().equals("")) {
                return entity.name();
            }
        }
        return type.getSimpleName();
    }

    /**
     * 获取持久化字段可以将此方法的返回结果存储到容器中
     */
    public static List<Field> getPersistentFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        Class<?> searchType = clazz;
        while (!Object.class.equals(searchType) && searchType != null) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                if (isPersistentField(field)) {
                    list.add(field);
                }
            }
            searchType = searchType.getSuperclass();
        }
        return list;
    }

    /**
     * 是否为持久化字段{@link Transient}注解为非持久化字段
     * @param field
     * @return
     */
    public static boolean isPersistentField(Field field) {
        return !field.isAnnotationPresent(Transient.class);
    }

}
