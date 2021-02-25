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
package com.alilitech.web;

import com.alilitech.web.exception.DefaultExceptionResolver;
import com.alilitech.web.jackson.BootPlusModule;
import com.alilitech.web.jackson.ser.DictCacheManager;
import com.alilitech.web.jackson.ser.DictFormatSerializerModifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.2.4
 */
@EnableConfigurationProperties({CorsProperties.class, JsonProperties.class})
@Import({DictCacheManager.class, DictFormatSerializerModifier.class, DefaultExceptionResolver.class, BootPlusModule.class})
public class WebConfiguration implements WebMvcConfigurer {

    public static final String TIP_KEY = "message";

    private final CorsProperties corsProperties;

    private final BootPlusModule bootPlusModule;

    private final DefaultExceptionResolver defaultExceptionResolver;

    public WebConfiguration(CorsProperties corsProperties,
                            DefaultExceptionResolver defaultExceptionResolver,
                            BootPlusModule bootPlusModule) {
        this.corsProperties = corsProperties;
        this.defaultExceptionResolver = defaultExceptionResolver;
        this.bootPlusModule = bootPlusModule;
    }

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.registerModule(bootPlusModule);
        return objectMapper;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if(corsProperties.isEnabled()) {
            registry.addMapping(corsProperties.getPath())
                    .allowedOrigins(corsProperties.getAllowedOrigins())
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
}
