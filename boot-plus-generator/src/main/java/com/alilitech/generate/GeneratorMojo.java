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
package com.alilitech.generate;

import com.alilitech.generate.config.DataSourceConfig;
import com.alilitech.generate.config.GlobalConfig;
import com.alilitech.generate.config.TableConfig;
import com.alilitech.generate.definition.ClassDefinition;
import com.alilitech.generate.definition.ClassType;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE)
public class GeneratorMojo extends AbstractMojo {

    private String xmlPath = "generate.xml";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //设置动态编译需要的classpath
        URL urls[] = ((PluginDescriptor) getPluginContext().get("pluginDescriptor")).getClassRealm().getURLs();
        if(urls != null) {
            for (URL url : urls) {
                DynamicLoader.classpaths.add(url.getPath());
            }
        }
        DynamicLoader.log = getLog();

        //获得src路径
        MavenProject mavenProject = (MavenProject) getPluginContext().get("project");
        List<?> compileSourceRoots = mavenProject.getCompileSourceRoots();
        if(compileSourceRoots == null || compileSourceRoots.isEmpty()) {
            throw new MojoExecutionException("Plugin was not found Java Source path.");
        }
        String srcPath = compileSourceRoots.get(0).toString();
        //获得resource路径
        List<?> resources = mavenProject.getResources();
        if(resources == null || resources.isEmpty()) {
            throw new MojoExecutionException("Plugin was not found Java Resource path.");
        }
        AtomicReference<String> resourcePath = new AtomicReference<>();
        resources.forEach(resource -> {
            String directory = ((Resource) resource).getDirectory();
            if(new File(directory + File.separator + xmlPath).exists()) {
                resourcePath.set(directory);
                return;
            }
        });

        //解析
        XmlParser xmlParser = new XmlParser(resourcePath.get() + File.separator + xmlPath);
        DataSourceConfig dataSourceConfig = xmlParser.parseText("config.datasource", DataSourceConfig.class);
        GlobalConfig globalConfig = xmlParser.parseText("config.properties", GlobalConfig.class);

        List<TableConfig> tableConfigs = xmlParser.parseListAttribute("config.tables.table", TableConfig.class);

        GeneratorUtils.log = getLog();
        List<ClassDefinition> classDefinitions = GeneratorUtils.process(dataSourceConfig, globalConfig, tableConfigs);
        for (int i=0; i<classDefinitions.size(); i++) {
            ClassDefinition classDefinition = classDefinitions.get(i);
            try {
                //建立文件夹
                String fileDirectory = srcPath + File.separator + classDefinition.getPackageName().replaceAll("\\.", "/");
                File fileDir = new File(fileDirectory);
                if (!fileDir.exists()) {
                    boolean mkdirs = fileDir.mkdirs();
                    if(!mkdirs) {
                        throw new MojoFailureException("无法根据包路径建立文件夹");
                    }
                }
                //输出文件
                File fileJava = new File(fileDirectory + File.separator + classDefinition.getClassName() + ".java");

                // 判断是否要输出磁盘文件
                int tableIndex = i/2;
                TableConfig tableConfig = tableConfigs.get(tableIndex);
                if(!fileJava.exists()) {
                    classDefinition.out(new FileOutputStream(fileJava));
                    continue;
                }
                if(fileJava.exists() && classDefinition.getClassType() == ClassType.DOMAIN && tableConfig.isOverrideDomain()) {
                    classDefinition.out(new FileOutputStream(fileJava));
                }
                if(fileJava.exists() && classDefinition.getClassType() == ClassType.MAPPER && tableConfig.isOverrideMapper()) {
                    classDefinition.out(new FileOutputStream(fileJava));
                }
            } catch (IOException e) {
                throw new MojoFailureException("生成文件失败", e);
            }
        }
    }
}
