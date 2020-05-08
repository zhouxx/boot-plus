/**
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
package com.alilitech.swagger.web;

import com.alilitech.core.constants.Profiles;
import com.alilitech.swagger.web.entity.Definition;
import com.alilitech.swagger.web.entity.Parameter;
import com.alilitech.swagger.web.entity.SwaggerEntity;
import com.alilitech.swagger.web.model.DocInterface;
import com.alilitech.swagger.web.model.DocParameter;
import com.alilitech.swagger.web.model.DocResponse;
import com.alilitech.swagger.web.model.WordDocument;
import com.alilitech.web.file.FileDownloadStreamingResponseBody;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.models.Swagger;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@RestController
@ApiIgnore
@Profile("!" + Profiles.SPRING_PROFILE_PRODUCTION)
public class SwaggerCustomController {

    private final DocumentationCache documentationCache;
    private final ServiceModelToSwagger2Mapper mapper;
    private final JsonSerializer jsonSerializer;

    public SwaggerCustomController(DocumentationCache documentationCache,
                                   ServiceModelToSwagger2Mapper mapper,
                                   JsonSerializer jsonSerializer) {
        this.documentationCache = documentationCache;
        this.mapper = mapper;
        this.jsonSerializer = jsonSerializer;
    }

    @RequestMapping("v2/export")
    public ResponseEntity<FileDownloadStreamingResponseBody> exportDocument(
            @RequestParam(value = "group", required = false) String swaggerGroup) throws IOException, TemplateException {

        //=====================引用swagger部分
        String groupName = Optional.ofNullable(swaggerGroup).orElse(Docket.DEFAULT_GROUP_NAME);
        Documentation documentation = documentationCache.documentationByGroup(groupName);
        //if (documentation == null) {
            //return new ResponseEntity<Json>(HttpStatus.NOT_FOUND);
        //}
        Swagger swagger = mapper.mapDocumentation(documentation);

        //Map<String, Path> paths = swagger.getPaths();

        /*paths.forEach((s, path) -> {
            //path.getDocParameters()
        });*/

        Json json = jsonSerializer.toJson(swagger);

        //=====================引用swagger结束

        //转换成我们需要的对象

        String value = json.value();

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SwaggerEntity swaggerEntity = objectMapper.readValue(value, SwaggerEntity.class);

        //转换结束

        //拼装word需要的对象
        WordDocument wordDocument = new WordDocument();
        wordDocument.setTitle(swaggerEntity.getInfo().getTitle());
        wordDocument.setVersion(swaggerEntity.getInfo().getVersion());
        wordDocument.setDescription(swaggerEntity.getInfo().getDescription());
        if(swagger.getInfo().getContact() != null) {
            wordDocument.setContactName(swagger.getInfo().getContact().getName());
            wordDocument.setContactEmail(swagger.getInfo().getContact().getEmail());
            wordDocument.setContactUrl(swagger.getInfo().getContact().getUrl());
        }

        //设置接口列表
        List<DocInterface> docInterfaces = new ArrayList<>();
        wordDocument.setInterfaces(docInterfaces);

        Map<String, Map<String, com.alilitech.swagger.web.entity.Path>> pathsMap = swaggerEntity.getPaths();

        pathsMap.forEach((pathValue, pathMap) -> {
            pathMap.forEach((method, path) -> {
                //对单个接口处理
                DocInterface docInterface = new DocInterface();
                docInterface.setHttpMethod(method);
                docInterface.setPath(pathValue);
                docInterface.setSummary(path.getSummary());

                docInterfaces.add(docInterface);
                List<DocParameter> docParameters = new ArrayList<>();
                List<DocResponse> docResponses = new ArrayList<>();

                //接口的请求与返回
                docInterface.setParameters(docParameters);
                docInterface.setResponses(docResponses);

                List<Parameter> parameters = path.getParameters();


                if(!CollectionUtils.isEmpty(parameters)) {
                    //请求参数
                    parameters.forEach(parameter -> {
                        //request body
                        if("body".equals(parameter.getIn())) {
                            String definitionRef = parameter.getSchema().get$ref();

                            if("array".equals(parameter.getSchema().getType())) {
                                definitionRef = parameter.getSchema().getItems().get$ref();
                            }

                            if(definitionRef == null) {
                                return;
                            }

                            definitionRef = definitionRef.replace("#/definitions/", "");

                            Definition definition = swaggerEntity.getDefinitions().get(definitionRef);

                            if(definition.getProperties() == null) {
                                return;
                            }

                            definition.getProperties().forEach(((propertyName, property) -> {
                                DocParameter docParameter = new DocParameter();
                                docParameter.setName(propertyName);
                                docParameter.setType(property.getType());
                                docParameter.setDescription(property.getDescription());
                                docParameter.setRequired(property.isRequired());

                                docParameters.add(docParameter);
                            }));
                        } else {
                            DocParameter docParameter = new DocParameter();
                            docParameter.setName(parameter.getName());
                            docParameter.setType(parameter.getType());
                            docParameter.setDescription(parameter.getDescription());
                            docParameter.setRequired(parameter.isRequired());

                            docParameters.add(docParameter);
                        }

                    });
                }


                if(!CollectionUtils.isEmpty(path.getResponses())) {
                    //返回参数
                    path.getResponses().forEach((s, response) -> {
                        if (s.startsWith("200")) {

                            String definitionRef = response.getSchema().get$ref();

                            if("array".equals(response.getSchema().getType())) {
                                definitionRef = response.getSchema().getItems().get$ref();
                            }

                            if(definitionRef == null) {
                                return;
                            }

                            definitionRef = definitionRef.replace("#/definitions/", "");

                            if ("ResponseEntity".equals(definitionRef)) {
                                return;
                            }

                            Definition definition = swaggerEntity.getDefinitions().get(definitionRef);
                            docInterface.setResponseType(definition.getType());

                            if(definition.getProperties() == null) {
                                return;
                            }

                            definition.getProperties().forEach(((propertyName, property) -> {
                                DocResponse docResponse = new DocResponse();
                                docResponse.setName(propertyName);
                                docResponse.setType(property.getType());
                                docResponse.setDescription(property.getDescription());
                                docResponse.setRequired(property.isRequired());
                                docResponses.add(docResponse);
                            }));

                        }
                    });
                }
            });
        });

        //利用freemarker生成word

        // step1 创建freeMarker配置实例
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

        configuration.setClassForTemplateLoading(this.getClass(), "/");
        // step2 获取模版路径
        //configuration.setDirectoryForTemplateLoading(new File(path));
        // step4 加载模版文件
        Template template = configuration.getTemplate("doc.ftl");

        // 新建字节输出流,Freemarker操作此输出流写入生成的业务文件.
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        template.process(wordDocument, new BufferedWriter(new OutputStreamWriter(out)));

        // 将outputstream转成inputstream
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        return new FileDownloadStreamingResponseBody(in).fileName(wordDocument.getTitle() + ".md").toResponseEntity();

    }


}
