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

import com.fasterxml.classmate.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.*;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ConditionalOnClass(WebMvcConfigurer.class)
@EnableConfigurationProperties(SwaggerProperties.class)
@Import({BeanValidatorPluginsConfiguration.class})
@EnableOpenApi
@ConditionalOnProperty(value = "springfox.documentation.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfiguration implements WebMvcConfigurer, EnvironmentAware {

    public static final String API_PATH = "/api.html";

    private Environment env;

    private final Logger logger = LoggerFactory.getLogger(SwaggerConfiguration.class);

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("api.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController(API_PATH, "/swagger-ui/index.html").setContextRelative(false);
    }

    @Bean
    public Docket swaggerSpringfoxDocket(SwaggerProperties swaggerProperties) {
        if(logger.isDebugEnabled()) {
            logger.debug("Starting Swagger");
        }
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
                new ArrayList<>());


        List<RequestParameter> parameters = new ArrayList<>();
        if(swaggerProperties.getGlobal() != null) {
            swaggerProperties.getGlobal().forEach(globalParameter -> {
                RequestParameterBuilder parameterBuilder = new RequestParameterBuilder();
                parameterBuilder.name(globalParameter.getName())
                        .description(globalParameter.getDescription())
                        //.modelRef(new ModelRef(globalParameter.getType()))
                        .in(globalParameter.getParameterType())
                        .required(globalParameter.isRequired())
                        .build();
                parameters.add(parameterBuilder.build());
            });
        }

        Docket docket = new Docket(DocumentationType.OAS_30)
                .groupName(Docket.DEFAULT_GROUP_NAME)
                .apiInfo(apiInfo)
                .forCodeGeneration(true)
                .directModelSubstitute(java.nio.ByteBuffer.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .select()
                .paths(paths(swaggerProperties.getDefaultIncludePatterns()))
                .build();
        //actual api invoke addr
        if(!StringUtils.isEmpty(swaggerProperties.getApiHost())) {
            docket.host(swaggerProperties.getApiHost());
        }

        //every api will add parameter
        if(!CollectionUtils.isEmpty(parameters)) {
            docket.globalRequestParameters(parameters);
        }
        //global authorized
        if(!CollectionUtils.isEmpty(swaggerProperties.getAuthorized())) {
            docket.securitySchemes(securitySchemes(swaggerProperties.getAuthorized()))
                    .securityContexts(securityContexts(
                            swaggerProperties.getAuthorized(),
                            CollectionUtils.isEmpty(swaggerProperties.getAuthorizedIncludePatterns()) ? swaggerProperties.getDefaultIncludePatterns() : swaggerProperties.getAuthorizedIncludePatterns()));
        }
        watch.stop();
        if(logger.isDebugEnabled()) {
            logger.debug("Started Swagger in {} ms", watch.getTotalTimeMillis());
        }
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String port = Optional.ofNullable(env.getProperty("server.port")).orElse("8080");
        if(logger.isDebugEnabled()) {
            logger.debug("Swagger UI : {}://localhost:{}{}", protocol, port, API_PATH);
        }
        return docket;
    }

    //Here is an example where we select any api that matches one of these paths
    private Predicate<String> paths(List<String> patterns) {
        List<Predicate<String>> predicateList = patterns.stream().map(PathSelectors::ant).collect(toList());

        if(predicateList.size() == 1) {
            return predicateList.get(0);
        } else {
            Predicate<String> predicate = predicateList.get(0);

            for(int i=1; i<predicateList.size(); i++) {
                predicate = predicate.or(predicateList.get(i));
            }
            return predicate;
        }
    }

    private List<SecurityScheme> securitySchemes(List<Authorized> authorizeds) {
        return authorizeds.stream().map(authorized -> new ApiKey(authorized.getName(), authorized.getName(), authorized.getIn())).collect(toList());
    }

    private List<SecurityContext> securityContexts(List<Authorized> authorizeds, List<String> patterns) {
        return Collections.singletonList(
                SecurityContext.builder()
                        .securityReferences(defaultAuth(authorizeds))
                        .forPaths(paths(patterns))
                        .build()
        );
    }

    private List<SecurityReference> defaultAuth(List<Authorized> authorizeds) {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return authorizeds.stream().map(authorized -> new SecurityReference(authorized.getName(), authorizationScopes)).collect(Collectors.toList());
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Bean
    @ConditionalOnClass(name = "com.alilitech.mybatis.jpa.domain.Pageable")
    public MybatisJpaPageableParameterBuilder mybatisJpaPageableParameterBuilder(TypeResolver resolver) {
        return new MybatisJpaPageableParameterBuilder(resolver);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.domain.Pageable")
    public DataPageableParameterBuilder dataPageableParameterBuilder(TypeResolver resolver) {
        return new DataPageableParameterBuilder(resolver);
    }

    @Bean
    @ConditionalOnClass(WebMvcEndpointHandlerMapping.class)
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier, ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier, EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties, WebEndpointProperties webEndpointProperties, Environment environment) {
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes, corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath), shouldRegisterLinksMapping, null);
    }

    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment, String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath) || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }

}
