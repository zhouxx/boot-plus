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
package com.alili.web.config;

import com.alili.web.config.properties.CorsProperties;
import com.alili.web.config.properties.JsonProperties;
import com.alili.web.exception.DefaultExceptionResolver;
import com.alili.web.jackson.CustomObjectMapper;
import com.alili.web.jackson.DictFormatSerializerModifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.TimeZone;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Configuration
@Import({CorsProperties.class, JsonProperties.class, DictFormatSerializerModifier.class, DefaultExceptionResolver.class})
public class WebConfig implements WebMvcConfigurer {

    public static final String TIP_KEY = "message";

    private CorsProperties corsProperties;

    private JsonProperties jsonProperties;

    private DictFormatSerializerModifier dictFormatSerializerModifier;

    private DefaultExceptionResolver defaultExceptionResolver;

    public WebConfig(CorsProperties corsProperties, JsonProperties jsonProperties, DictFormatSerializerModifier dictFormatSerializerModifier, DefaultExceptionResolver defaultExceptionResolver) {
        this.corsProperties = corsProperties;
        this.jsonProperties = jsonProperties;
        this.dictFormatSerializerModifier = dictFormatSerializerModifier;
        this.defaultExceptionResolver = defaultExceptionResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if(corsProperties.isEnabled()) {
            registry.addMapping(corsProperties.getPath())
                    .allowedOrigins(corsProperties.getAllowedOrigins())
                    .allowedMethods(corsProperties.getAllowedMethods())
                    .allowCredentials(corsProperties.isAllowCredentials())
                    .exposedHeaders(StringUtils.tokenizeToStringArray(corsProperties.getExposedHeaders(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS))
                    .maxAge(corsProperties.getMaxAge());
        }
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();

        CustomObjectMapper objectMapper = new CustomObjectMapper(jsonProperties.isDefaultNull(), jsonProperties.getDefaultNullValue());
        objectMapper.setDateFormat(jsonProperties.getDateFormat());
        objectMapper.setFilterNull(jsonProperties.isFilterNull());
        objectMapper.setIgnoreUnknown(jsonProperties.isIgnoreUnknown());
        objectMapper.setTimeZone(TimeZone.getTimeZone(jsonProperties.getTimezone()));
        objectMapper.setDicFormatSerializerModifier(dictFormatSerializerModifier);

        for(HttpMessageConverter converter : converters) {
            if(converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter) converter;
                jackson2HttpMessageConverter.setObjectMapper(objectMapper);
            }
        }

        //放到第一个
        //converters.add(0, jackson2HttpMessageConverter);
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(defaultExceptionResolver);
    }
}
