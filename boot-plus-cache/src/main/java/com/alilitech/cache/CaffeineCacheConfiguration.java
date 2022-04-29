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
package com.alilitech.cache;

import com.alilitech.cache.support.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caffeine cache configuration.
 *
 * @author Eddú Meléndez
 * @author Zhou Xiaoxiang
 */
@ConditionalOnClass({ Caffeine.class, CaffeineCacheManager.class })
@EnableConfigurationProperties(CacheProperties.class)
public class CaffeineCacheConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        return new CacheManagerCustomizers(customizers.orderedStream().collect(Collectors.toList()));
    }

    @Bean
    @Primary
    public CaffeineCacheManager cacheManager(CacheProperties cacheProperties, CacheManagerCustomizers customizers,
                                      ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec,
                                      ObjectProvider<CacheLoader<Object, Object>> cacheLoader) {
        CaffeineCacheManager cacheManager = createCacheManager(cacheProperties, caffeine, caffeineSpec, cacheLoader);
        List<String> cacheNames = cacheProperties.getCacheNames();
        if (!CollectionUtils.isEmpty(cacheNames)) {
            cacheManager.setCacheNames(cacheNames);
        }
        return customizers.customize(cacheManager);
    }

    private CaffeineCacheManager createCacheManager(CacheProperties cacheProperties,
                                                    ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec,
                                                    ObjectProvider<CacheLoader<Object, Object>> cacheLoader) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        setCacheBuilder(cacheProperties, caffeineSpec.getIfAvailable(), caffeine.getIfAvailable(), cacheManager);
        cacheLoader.ifAvailable(cacheManager::setCacheLoader);
        return cacheManager;
    }

    private void setCacheBuilder(CacheProperties cacheProperties, CaffeineSpec caffeineSpec,
                                 Caffeine<Object, Object> caffeine, CaffeineCacheManager cacheManager) {
        String specification = cacheProperties.getCaffeine().getSpec();
        if (StringUtils.hasText(specification)) {
            cacheManager.setCacheSpecification(specification);
        }
        else if (caffeineSpec != null) {
            cacheManager.setCaffeineSpec(caffeineSpec);
        }
        else if (caffeine != null) {
            cacheManager.setCaffeine(caffeine);
        }
    }

}

