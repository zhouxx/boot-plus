/*
 *    Copyright 2017-2022 the original author or authors.
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
package com.alilitech.web.jackson.anotation;

import java.lang.annotation.*;


/**
 * 字典序列化扩展，此注解必须用在属性上，不用在getter方法上
 * 实现DicService接口并被spring管理，这样利于注入字典值
 * 注意：所有的字典接口每次遇到字典序列化时，每次都调用所有的字典接口。所以必须做好缓存策略，不然每次都调用数据库很累
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictFormat {

    /**
     * 字典名称编码, 默认为当前属性的名称
     */
    String dictKey() default "";

    /**
     * 禁用缓存  暂时未实现
     */
    boolean disableCache() default false;

    // ----- 默认提供了格式化的所有属性，如果SerializerFormat和DictFormat同时存在，则SerializerFormat无效
    /**
     * 属性原始值是否转String输出
     */
    boolean originalValueToString() default false;

    /**
     * 格式化后的值是否用新的属性输出
     */
    boolean newTarget() default true;

    /**
     * 新的目标属性，默认为 原始属性名称+"Format"
     */
    String targetFiled() default "";

    /**
     * 前置拼接，比如单位 $ 等
     */
    String pre() default "";

    /**
     * 后置拼接，比如 % 万元等
     */
    String post() default "";

    /**
     * 是否开启默认值转换
     */
    boolean defaultNull() default false;

    /**
     * 默认值，当值是转换后的值是null的时候显示的默认值
     */
    String defaultNullValue() default "";


}
