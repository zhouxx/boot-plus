/**
 *    Copyright 2008-2019 the original author or authors.
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
package com.alili.integration.jpa.statement.parser;

import com.alili.integration.jpa.LikeContainer;
import com.alili.integration.jpa.anotation.IfTest;
import com.alili.integration.jpa.definition.MethodDefinition;
import com.alili.integration.jpa.domain.Sort;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Oliver Gierke
 * @author Martin Baumgartner
 * @author Zhou Xiaoxiang
 */
public class Predicate {

    private static final String KEYWORD_TEMPLATE = "(%s)(?=(\\p{Lu}|\\P{InBASIC_LATIN}))";

    private static final String ORDER_BY = "OrderBy";

    private final List<OrPart>  nodes = new ArrayList<OrPart>();

    private OrderBySource orderBySource;

    private int orderIndex = -1;

    private MethodDefinition methodDesc;

    public Predicate(String predicate, Class<?> domainClass, MethodDefinition methodDesc) {

        this.methodDesc = methodDesc;

        String[] parts = split(predicate, ORDER_BY);

        if (parts.length > 2) {
            throw new IllegalArgumentException("OrderBy must not be used more than once in a methodDesc name!");
        }

        buildTree(parts[0], domainClass);
        this.orderBySource = parts.length == 2 ? new OrderBySource(parts[1], domainClass) : null;

        //设置参数index
        int index = 0;
        for(OrPart orPart : nodes) {
            for(int i=0; i<orPart.children.size(); i++) {
                Part part = orPart.children.get(i);
                if(part.getNumberOfArguments() <= 0) {  //没有参数，不处理
                    continue;
                }

                //跳过RowBounds
                if(RowBounds.class.isAssignableFrom(methodDesc.getParameterDefinitions().get(index).getParameterClass())) {
                    index ++;
                    i --;
                    continue;
                }
                //跳过Sort
                if(Sort.class.isAssignableFrom(methodDesc.getParameterDefinitions().get(index).getParameterClass())) {
                    orderIndex = index ++;
                    this.orderBySource = null;
                    i --;
                    continue;
                }

                part.setIndex(index);
                part.setOneParameter(methodDesc.isOneParameter());

                //看看有没有ifTest
                Part.TestCondition testCondition = getCondition(index, methodDesc);
                if(testCondition != null) {
                    part.setTestCondition(testCondition);
                }
                index = index + part.getType().getNumberOfArguments();
            }
        }

    }

    private void buildTree(String source, Class<?> domainClass) {

        String[] split = split(source, "Or");
        for (String part : split) {
            nodes.add(new OrPart(part, domainClass, methodDesc));
        }
    }

    public OrderBySource getOrderBySource() {
        return orderBySource;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    private static String[] split(String text, String keyword) {

        Pattern pattern = Pattern.compile(String.format(KEYWORD_TEMPLATE, keyword));
        return pattern.split(text);
    }

    @Override
    public String toString() {
        OrderBySource orderBySource = this.getOrderBySource();
        return String.format("%s %s", getWhere(this.nodes),
                orderBySource != null ? orderBySource : "");
    }

    private String getWhere(List<OrPart> nodes) {
        if(this.nodes.size() <= 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<where>");
        sb.append(StringUtils.collectionToDelimitedString(this.nodes, "", "<trim prefix=\" OR \" prefixOverrides=\"AND\" suffixOverrides=\"AND\">", "</trim>"));
        sb.append("</where>");

        return sb.toString();
    }

    //根据参数索引获得IfCondition
    private Part.TestCondition getCondition(int index, MethodDefinition methodDesc) {
        //先读取方法的IfTest
        boolean methodIfTest = methodDesc.isMethodIfTest();
        List<Annotation> annotationList = methodDesc.getParameterDefinitions().get(index).getAnnotations();
        if(annotationList.size() <= 0 && !methodIfTest) {
            return null;
        }

        Part.TestCondition testCondition = null;

        for(Annotation annotation : annotationList) {
            //若参数有注解，则以参数注解为准
            if(annotation instanceof IfTest) {
                IfTest ifTest = (IfTest) annotation;
                testCondition = new Part.TestCondition(ifTest.notNull(), ifTest.notEmpty(), ifTest.conditions());
                return testCondition;
            }
        }
        //否则默认方法的注解
        if(methodIfTest) {
            IfTest ifTest = methodDesc.getIfTest();
            testCondition = new Part.TestCondition(ifTest.notNull(), ifTest.notEmpty(), ifTest.conditions());
        }

        return testCondition;
    }

    public static class OrPart implements Iterable<Part> {

        private final List<Part> children = new ArrayList<Part>();

        private MethodDefinition methodDesc;

        /**
         * Creates a new {@link OrPart}.
         * @param source the source to split up into {@literal And} parts in turn.
         * @param domainClass the domain class to check the resulting {@link Part}s against.
         * @param methodDesc
         */
        OrPart(String source, Class<?> domainClass, MethodDefinition methodDesc) {

            String[] split = split(source, "And");
            for (String part : split) {
                if (StringUtils.hasText(part)) {
                    children.add(new Part(part, domainClass));
                }
            }

            this.methodDesc = methodDesc;
        }

        public Iterator<Part> iterator() {

            return children.iterator();
        }

        @Override
        public String toString() {

            for(int i=0; i<children.size(); i++) {
                Part part = children.get(i);
                if(part.getNumberOfArguments() <=0 ) {  //没有参数，不处理
                    continue;
                }

                // 将like的信息放入缓存中，后面需要改变其参数
                if(part.getLikeType() != null) {
                    if(part.isOneParameter()) {
                        String key = methodDesc.getNameSpace() + "." + methodDesc.getMethodName() + "._parameter";
                        LikeContainer.getInstance().put(key, part.getLikeType());
                    } else {
                        String key = methodDesc.getNameSpace() + "." + methodDesc.getMethodName() + ".arg" + part.getIndex();
                        LikeContainer.getInstance().put(key, part.getLikeType());
                    }
                }

            }
            return StringUtils.collectionToDelimitedString(children, " ");
        }


    }

}
