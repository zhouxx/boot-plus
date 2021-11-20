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
package com.alilitech.web.valid;

import org.springframework.http.ResponseEntity;

/**
 * 校验异常处理接口，此接口实现类只能有一个被spring管理
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public interface ValidHandler {

    /**
     * handler exception, 根据抛出的校验异常处理校验不通过返回
     * @param e just exception
     * @return 请求返回
     */
    <T> ResponseEntity<T> handle(Exception e);
}
