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
package com.alilitech.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ConditionalOnClass(WebMvcConfigurer.class)
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(value = "swagger.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration implements WebMvcConfigurer, EnvironmentAware {

    public static final String API_PATH = "/api.html";

    private Environment env;

    private final Logger logger = LoggerFactory.getLogger(SwaggerConfiguration.class);

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController(API_PATH, "/swagger-ui/index.html").setContextRelative(false);
    }

    @Bean
    public OpenAPI bootPlusOpenAPI(SwaggerProperties swaggerProperties) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting Swagger");
        }
        Contact contact = new Contact()
                .name(swaggerProperties.getContactName())
                .url(swaggerProperties.getContactUrl())
                .email(swaggerProperties.getContactEmail());
        License license = new License()
                .name(swaggerProperties.getLicense())
                .url(swaggerProperties.getLicenseUrl());
        Info info = new Info()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .termsOfService(swaggerProperties.getTermsOfServiceUrl())
                .contact(contact)
                .license(license);

        OpenAPI openAPI = new OpenAPI().info(info);
        if (StringUtils.hasText(swaggerProperties.getApiHost())) {
            openAPI.addServersItem(new Server().url(swaggerProperties.getApiHost()));
        }

        String protocol = env.getProperty("server.ssl.key-store") != null ? "https" : "http";
        String port = Optional.ofNullable(env.getProperty("server.port")).orElse("8080");
        if (logger.isDebugEnabled()) {
            logger.debug("Swagger UI : {}://localhost:{}{}", protocol, port, API_PATH);
        }
        return openAPI;
    }

    @Bean
    public GroupedOpenApi bootPlusGroupedOpenApi(SwaggerProperties swaggerProperties) {
        String[] paths = swaggerProperties.getDefaultIncludePatterns().toArray(new String[0]);
        return GroupedOpenApi.builder()
                .group(swaggerProperties.getGroupName())
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GlobalOperationCustomizer globalParameterCustomizer(SwaggerProperties swaggerProperties) {
        return (operation, handlerMethod) -> {
            if (CollectionUtils.isEmpty(swaggerProperties.getGlobal())) {
                return operation;
            }
            swaggerProperties.getGlobal().forEach(globalParameter -> operation.addParametersItem(new Parameter()
                    .name(globalParameter.getName())
                    .description(globalParameter.getDescription())
                    .in(globalParameter.getParameterType())
                    .required(globalParameter.isRequired())
                    .schema(new StringSchema())));
            return operation;
        };
    }

    @Bean
    public GlobalOpenApiCustomizer securitySchemeCustomizer(SwaggerProperties swaggerProperties) {
        return openAPI -> {
            if (CollectionUtils.isEmpty(swaggerProperties.getAuthorized()) || openAPI.getPaths() == null) {
                return;
            }
            Components components = openAPI.getComponents() == null ? new Components() : openAPI.getComponents();
            SecurityRequirement requirement = new SecurityRequirement();
            swaggerProperties.getAuthorized().forEach(authorized -> {
                components.addSecuritySchemes(authorized.getName(), new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .name(authorized.getName())
                        .in(toIn(authorized.getIn())));
                requirement.addList(authorized.getName());
            });
            openAPI.components(components);

            List<String> patterns = CollectionUtils.isEmpty(swaggerProperties.getAuthorizedIncludePatterns())
                    ? swaggerProperties.getDefaultIncludePatterns()
                    : swaggerProperties.getAuthorizedIncludePatterns();
            AntPathMatcher matcher = new AntPathMatcher();
            openAPI.getPaths().forEach((path, pathItem) -> {
                boolean matched = patterns.stream().anyMatch(pattern -> matcher.match(pattern, path));
                if (matched) {
                    pathItem.readOperations().forEach(operation -> operation.addSecurityItem(requirement));
                }
            });
        };
    }

    @Bean
    @ConditionalOnClass(name = "com.alilitech.mybatis.jpa.domain.Pageable")
    public MybatisJpaPageableParameterBuilder mybatisJpaPageableParameterBuilder() {
        return new MybatisJpaPageableParameterBuilder();
    }

    private SecurityScheme.In toIn(String value) {
        if (!StringUtils.hasText(value)) {
            return SecurityScheme.In.HEADER;
        }
        return SecurityScheme.In.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}
