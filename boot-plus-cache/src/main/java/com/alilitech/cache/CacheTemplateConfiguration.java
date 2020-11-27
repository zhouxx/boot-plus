/*
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
package com.alilitech.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.0
 */
@EnableCaching
@ConditionalOnClass(CacheManager.class)
public class CacheTemplateConfiguration {

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.redis.connection.RedisConnectionFactory")
    @ConditionalOnMissingBean(CacheTemplate.class)
    public RedisCacheTemplate redisCacheTemplate() {
        return new RedisCacheTemplate();
    }

    @Bean
    @ConditionalOnClass(name = "com.github.benmanes.caffeine.cache.Caffeine")
    @ConditionalOnMissingBean(CacheTemplate.class)
    public CaffeineCacheTemplate caffeineCacheTemplateCaffeine() {
        return new CaffeineCacheTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(CacheTemplate.class)
    public DefaultCacheTemplate defaultCacheTemplate() {
        return new DefaultCacheTemplate();
    }

}
