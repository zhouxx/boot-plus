/*
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
package com.alilitech.generate;

import com.alilitech.generate.config.TableConfig;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class XmlParser {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Document document;

    public XmlParser(String xmlPath) {
        //获取解析器
        SAXReader saxReader = new SAXReader();
        try {
            //获取文档对象
            document = saxReader.read(xmlPath);
        } catch (DocumentException e) {
            logger.error(e.getMessage());
        }
    }

    public XmlParser(InputStream inputStream) {
        //获取解析器
        SAXReader saxReader = new SAXReader();
        try {
            //获取文档对象
            document = saxReader.read(inputStream);
        } catch (DocumentException e) {
            logger.error(e.getMessage());
        }
    }

    public <T> T parseText(String elementPath, Class<T> clazz)  {

        String[] nodeArray = elementPath.split("\\.");
        Element element = null;
        for(int i=0; i<nodeArray.length; i++) {
            if(i == 0) {
                element = document.getRootElement();
            } else {
                element = element.element(nodeArray[i]);
            }
        }
        T t ;
        try {
            t = clazz.newInstance();
            for(Field field : clazz.getDeclaredFields()) {
                String text = element.elementText(field.getName());
                field.setAccessible(true);
                field.set(t, covertFieldValue(text, field));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    public <T> T parseAttribute(String elementPath, Class<T> clazz) {

        String[] nodeArray = elementPath.split("\\.");
        Element element = null;
        for(int i=0; i<nodeArray.length; i++) {
            if(i == 0) {
                element = document.getRootElement();
            } else {
                element = element.element(nodeArray[i]);
            }
        }
        T t;
        try {
            t = clazz.newInstance();
            for(Field field : clazz.getDeclaredFields()) {
                String text = element.attributeValue(field.getName());
                field.setAccessible(true);
                field.set(t, covertFieldValue(text, field));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    public <T> List<T> parseListAttribute(String elementPath, Class<T> clazz)  {
        List<T> retList = new ArrayList<>();
        String[] nodeArray = elementPath.split("\\.");
        Element element = null;
        List<Element> elements = null;
        for(int i=0; i<nodeArray.length; i++) {
            if(i == 0) {
                element = document.getRootElement();
            } else if(i >= nodeArray.length - 1){
                elements = element.elements(nodeArray[i]);
            } else {
                element = element.element(nodeArray[i]);
            }
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for(Element temp : elements) {
            T t;
            try {
                t = clazz.newInstance();
                for(Field field : declaredFields) {
                    String text = temp.attributeValue(field.getName());
                    field.setAccessible(true);
                    field.set(t, covertFieldValue(text, field));
                }
                retList.add(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return retList;
    }

    private Object covertFieldValue(String value, Field field) {
        if(field.getType().equals(String.class)) {
            return value;
        } else if(field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    public static void main(String[] args) {
        String path = "D:\\IdeaWorkspace\\BootPlus\\boot-plus-integration-jpa-generator\\src\\main\\resources\\generate.xml";
        List<TableConfig> tableConfigs = new XmlParser(path).parseListAttribute("config.tables.table", TableConfig.class);

        System.out.println(tableConfigs);

    }

}
