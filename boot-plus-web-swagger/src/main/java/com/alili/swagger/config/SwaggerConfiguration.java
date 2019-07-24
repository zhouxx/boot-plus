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
package com.alili.swagger.config;

import com.alili.core.constants.Profiles;
import com.alili.swagger.config.properties.SwaggerProperties;
import com.alili.swagger.config.properties.Authorized;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Predicates.or;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
@EnableSwagger2
@Profile("!" + Profiles.SPRING_PROFILE_PRODUCTION)
public class SwaggerConfiguration implements WebMvcConfigurer, EnvironmentAware {

    private static final String API_PATH = "api.html";

    private Environment env;

    private final Logger logger = LoggerFactory.getLogger(SwaggerConfiguration.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController(API_PATH, "swagger-ui.html");
    }

    @Bean
    public Docket swaggerSpringfoxDocket(SwaggerProperties swaggerProperties) {
        logger.debug("Starting Swagger");
        StopWatch watch = new StopWatch();
        watch.start();
        Contact contact = new Contact(
                swaggerProperties.getContactName(),
                swaggerProperties.getContactUrl(),
                swaggerProperties.getContactEmail());

        ApiInfo apiInfo = new ApiInfo(
                swaggerProperties.getTitle(),
                swaggerProperties.getDescription(),
                swaggerProperties.getVersion(),
                swaggerProperties.getTermsOfServiceUrl(),
                contact,
                swaggerProperties.getLicense(),
                swaggerProperties.getLicenseUrl(),
                new ArrayList());


        List<Parameter> parameters = new ArrayList<>();
        if(swaggerProperties.getGlobal() != null) {
            swaggerProperties.getGlobal().forEach(globalParameter -> {
                ParameterBuilder parameterBuilder = new ParameterBuilder();
                parameterBuilder.name(globalParameter.getType())
                        .description(globalParameter.getDescription())
                        .modelRef(new ModelRef(globalParameter.getType()))
                        .parameterType(globalParameter.getParameterType())
                        .required(globalParameter.isRequired())
                        .build();
                parameters.add(parameterBuilder.build());
            });

        }

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName(Docket.DEFAULT_GROUP_NAME)
                .apiInfo(apiInfo)
                .forCodeGeneration(true)
                .directModelSubstitute(java.nio.ByteBuffer.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .select()
                .paths(paths(swaggerProperties.getDefaultIncludePattern()))
                .build();
        //actual api invoke addr
        if(!StringUtils.isEmpty(swaggerProperties.getApiHost())) {
            docket.host(swaggerProperties.getApiHost());
        }
        //every api will add parameter
        if(!CollectionUtils.isEmpty(parameters)) {
            docket.globalOperationParameters(parameters);
        }
        //global authorized
        if(!CollectionUtils.isEmpty(swaggerProperties.getAuthorized())) {
            docket.securitySchemes(securitySchemes(swaggerProperties.getAuthorized()))
                    .securityContexts(securityContexts(
                            StringUtils.isEmpty(swaggerProperties.getAuthorizedIncludePattern()) ? swaggerProperties.getDefaultIncludePattern() : swaggerProperties.getAuthorizedIncludePattern()));
        }
        watch.stop();
        logger.debug("Started Swagger in {} ms", watch.getTotalTimeMillis());
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String port = Optional.ofNullable(env.getProperty("server.port")).orElse("8080");
        logger.debug("Swagger UI : {}://localhost:{}/{}", protocol, port, API_PATH);
        return docket;
    }

    //Here is an example where we select any api that matches one of these paths
    private Predicate<String> paths(String defaultIncludePattern) {
        List<Predicate> listRet = Stream.of(defaultIncludePattern.split(",")).map(PathSelectors::regex).collect(toList());
        Predicate[] arr = listRet.toArray(new Predicate[listRet.size()]);
        return or(arr);
    }

    private List<ApiKey> securitySchemes(List<Authorized> authorizeds) {

        List<ApiKey> apiKeys = authorizeds.stream().map(authorized -> {
            return new ApiKey(authorized.getName(), authorized.getName(), authorized.getIn());
        }).collect(toList());

        return apiKeys;
    }

    private List<SecurityContext> securityContexts(String authorizedIncludePattern) {
        return Arrays.asList(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .forPaths(paths(authorizedIncludePattern))
                        .build()
        );
    }
    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(
                new SecurityReference("Authorization", authorizationScopes));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }


}
