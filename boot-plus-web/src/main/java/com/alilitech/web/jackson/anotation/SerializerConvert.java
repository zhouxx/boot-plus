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
package com.alilitech.web.jackson.anotation;

import com.alilitech.web.jackson.ser.SerializerConverter;

import java.lang.annotation.*;

/**
 * convert original value to target value before real serialize
 * support Class array to form the chain of converters
 * @author Zhou Xiaoxiang
 * @since 1.3.6
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SerializerConvert {

    Class<? extends SerializerConverter>[] convertClasses() default {};

}
