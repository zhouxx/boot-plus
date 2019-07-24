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
package com.alili.generate;

import com.alili.generate.config.DataSourceConfig;
import com.alili.generate.config.GlobalConfig;
import com.alili.generate.config.TableConfig;
import com.alili.generate.definition.ClassDefinition;
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
        URL url[] = ((PluginDescriptor) getPluginContext().get("pluginDescriptor")).getClassRealm().getURLs();
        DynamicLoader.classpaths = url;

        //获得src路径
        MavenProject mavenProject = (MavenProject) getPluginContext().get("project");
        List compileSourceRoots = mavenProject.getCompileSourceRoots();
        if(compileSourceRoots == null || compileSourceRoots.size() == 0) {
            throw new MojoExecutionException("Plugin was not found Java Source path.");
        }
        String srcPath = compileSourceRoots.get(0).toString();
        //获得resource路径
        List resources = mavenProject.getResources();
        if(resources == null || resources.size() == 0) {
            throw new MojoExecutionException("Plugin was not found Java Resource path.");
        }
        AtomicReference<String> resourcePath = new AtomicReference<>();
        resources.forEach(resource -> {
            String directory = ((Resource) resource).getDirectory();
            if(new File(directory + "/" + xmlPath).exists()) {
                resourcePath.set(directory);
                return;
            }
        });
        //String resourcePath = ((org.apache.maven.model.Resource)resources.get(0)).getDirectory();

        //解析
        //InputStream inputStream = new FileInputStream(new File(resourcePath + "/" + xmlPath));
        XmlParser xmlParser = new XmlParser(resourcePath.get() + "/" + xmlPath);
        DataSourceConfig dataSourceConfig = xmlParser.parseText("config.datasource", DataSourceConfig.class);
        GlobalConfig globalConfig = xmlParser.parseText("config.properties", GlobalConfig.class);

        List<TableConfig> tableConfigs = xmlParser.parseListAttribute("config.tables.table", TableConfig.class);
        List<ClassDefinition> classDefinitions = GeneratorUtils.process(dataSourceConfig, globalConfig, tableConfigs);
        String finalSrcPath = srcPath;
        classDefinitions.forEach(classDefinition -> {
            try {
                //建立文件夹
                String fileDirectory = finalSrcPath + "/" + classDefinition.getPackageName().replaceAll("\\.", "/");
                File fileDir = new File(fileDirectory);
                if(!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                //输出文件
                File fileJava = new File(fileDirectory + "/" + classDefinition.getClassName() + ".java");
                //classDefinition.out(System.out);
                classDefinition.out(new FileOutputStream(fileJava));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
