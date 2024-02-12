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
package com.alilitech.web;

import com.alilitech.web.exception.DefaultExceptionResolver;
import com.alilitech.web.jackson.JacksonInterceptor;
import com.alilitech.web.jackson.ser.CompositeSerializerModifier;
import com.alilitech.web.jackson.ser.dict.DictWithLocaleCacheManager;
import com.alilitech.web.jackson.ser.dict.DictWithoutLocaleCacheManager;
import com.alilitech.web.valid.ValidAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ValidatorFactory;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.2.4
 */
@EnableConfigurationProperties({CorsProperties.class, JsonProperties.class})
@Import({JacksonInterceptor.class, ThreadLocalInterceptor.class, DictWithLocaleCacheManager.class, DictWithoutLocaleCacheManager.class, CompositeSerializerModifier.class, DefaultExceptionResolver.class})
public class WebConfiguration implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    private final DefaultExceptionResolver defaultExceptionResolver;

    public WebConfiguration(CorsProperties corsProperties,
                            DefaultExceptionResolver defaultExceptionResolver) {
        this.corsProperties = corsProperties;
        this.defaultExceptionResolver = defaultExceptionResolver;
    }

    @Bean
    public ThreadLocalContainer threadLocalContainer() {
        return ThreadLocalContainer.getInstance();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JacksonInterceptor());
        registry.addInterceptor(new ThreadLocalInterceptor());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if(corsProperties.isEnabled()) {
            registry.addMapping(corsProperties.getPath())
                    .allowedOrigins(corsProperties.getAllowedOrigins())
                    .allowedOriginPatterns(corsProperties.getAllowedOriginPatterns())
                    .allowedMethods(corsProperties.getAllowedMethods())
                    .allowCredentials(corsProperties.isAllowCredentials())
                    .exposedHeaders(corsProperties.getExposedHeaders())
                    .maxAge(corsProperties.getMaxAge());
        }
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(defaultExceptionResolver);
    }

    @Bean
    @ConditionalOnMissingBean(ResponseEntityExceptionHandler.class)
    public ValidAdvice validAdvice(ValidatorFactory validatorFactory) {
        return new ValidAdvice(validatorFactory);
    }
}
