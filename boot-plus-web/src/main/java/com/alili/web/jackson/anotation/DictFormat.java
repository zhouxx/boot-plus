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
package com.alili.web.jackson.anotation;

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
     * 字典的属性原始值是否转String输出
     */
    boolean dictKeyToString() default false;


    /**
     * 目标属性, 一个字典对应有个值，这个也要有个属性存在，默认为dicKey+"Name"
     */
    String targetFiled() default "";

    /**
     * 默认值，当字典里没有对应的值时，显示的字典值
     */
    String defaultValue() default "";

    /**
     * 禁用缓存  暂时未实现
     */
    boolean disableCache() default true;

}
