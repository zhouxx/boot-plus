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
package com.alili.integration.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Configuration
public class MybatisJpaConfiguration implements WebMvcConfigurer {

    @Bean
    public PageableArgumentResolver mybatisPageableResolver() {
        return new PageableArgumentResolver(mybatisSortResolver());
    }

    @Bean
    public SortArgumentResolver mybatisSortResolver() {
        return new SortArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(mybatisSortResolver());
        argumentResolvers.add(mybatisPageableResolver());
    }

}
