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
package com.alilitech.integration.jpa.anotation;

import com.alilitech.integration.jpa.parameter.TriggerValueType;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.annotation.Documented;


/**
 * trigger with code
 * 当是javaCode的时候通过调用方法，这样通过调用方法可以获得动态对象
 * 当是DatabaseFunction的时候，直接取value
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Documented
public @interface Trigger {

    SqlCommandType triggerType();

    TriggerValueType valueType();

    Class valueClass() default Object.class;

    String methodName() default "";

    String value() default "";

    /**
     * force to set the value, whether the value is present or null
     * only support {@link TriggerValueType#DatabaseFunction}
     * @return
     */
    boolean force() default true;

}
