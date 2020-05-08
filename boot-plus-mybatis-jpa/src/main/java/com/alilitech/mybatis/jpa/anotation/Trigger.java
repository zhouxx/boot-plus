/**
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
package com.alilitech.mybatis.jpa.anotation;

import com.alilitech.mybatis.jpa.parameter.TriggerValueType;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.annotation.Documented;


/**
 * trigger with code
 * when {@link TriggerValueType#JavaCode} ,invoke the methodName of valueClass to generate the value
 * otherwise {@link TriggerValueType#DatabaseFunction}, just replace the placeholder with supported value
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Documented
public @interface Trigger {

    SqlCommandType triggerType();

    TriggerValueType valueType();

    Class<?> valueClass() default Object.class;

    String methodName() default "";

    String value() default "";

    /**
     * force to set the value, whether the value of domain's field is present or null
     * only support {@link TriggerValueType#DatabaseFunction}
     */
    boolean force() default true;

}
