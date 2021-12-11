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
package com.alilitech.mybatis.jpa.primary.key;

/**
 * snowflake offset repository
 * when using {@link com.alilitech.mybatis.jpa.primary.key.snowflake.TimeCallbackStrategy#OFFSET_MODIFY} strategy
 * @author Zhou Xiaoxiang
 * @since 1.3.2
 */
public interface OffsetRepository {

    /**
     * 存储offset
     * 发生时钟回拨之后，如果是修改偏移量策略，则将新的偏移量存储起来
     * @param entityClass 实体类的类，每个类对应一个偏移量
     * @param offset 偏移量
     * @return
     */
    boolean saveOffset(Class<?> entityClass, Long offset);

    /**
     * 获得offset
     * 在项目重启后会将保存的偏移量拿到内存中
     * @param entityClass 实体类的类，每个类对应一个偏移量
     * @return 偏移量
     */
    Long getOffset(Class<?> entityClass);

}
