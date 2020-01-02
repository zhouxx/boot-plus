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


import com.alilitech.integration.jpa.parameter.GenerationType;
import com.alilitech.integration.jpa.primary.key.KeyGenerator;
import com.alilitech.integration.jpa.primary.key.KeyGenerator4Auto;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Documented
public @interface GeneratedValue {

    GenerationType value() default GenerationType.AUTO;

    String sequenceName() default "";

    Class<? extends KeyGenerator> generatorClass() default KeyGenerator4Auto.class;

}
