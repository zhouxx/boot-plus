/*
 * Copyright 2008-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alilitech.integration.jpa.statement.parser;

import com.alilitech.integration.jpa.definition.MethodDefinition;
import com.alilitech.integration.jpa.domain.Sort;
import com.alilitech.integration.jpa.exception.StatementNotSupportException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to parse a {@link String} into a tree or {@link OrPart}s consisting of simple {@link Part} instances in turn.
 * Takes a domain class as well to validate that each of the {@link Part}s are referring to a property of the domain
 * class. The {@link PartTree} can then be used to build queries based on its API instead of parsing the method name for
 * each query execution.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class PartTree implements Render {

    /*
     * We look for a pattern of: keyword followed by
     *
     *  an upper-case letter that has a lower-case variant \p{Lu}
     * OR
     *  any other letter NOT in the BASIC_LATIN Uni-code Block \\P{InBASIC_LATIN} (like Chinese, Korean, Japanese, etc.).
     *
     * @see <a href="http://www.regular-expressions.info/unicode.html">http://www.regular-expressions.info/unicode.html</a>
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#ubc">Pattern</a>
     */
    private static final String KEYWORD_TEMPLATE = "(%s)(?=(\\p{Lu}|\\P{InBASIC_LATIN}))";
    private static final String QUERY_PATTERN = "find|read|get|query|stream";
    private static final String COUNT_PATTERN = "count";
    private static final String EXISTS_PATTERN = "exists";
    private static final String DELETE_PATTERN = "delete|remove";
    public static final Pattern QUERY_PREFIX_TEMPLATE = Pattern.compile(
            "^(" + QUERY_PATTERN + ")((\\p{Lu}.*?))??");
    private static final Pattern PREFIX_TEMPLATE = Pattern.compile(
            "^(" + QUERY_PATTERN + "|" + COUNT_PATTERN + "|" + EXISTS_PATTERN + "|" + DELETE_PATTERN + ")((\\p{Lu}.*?))??By");
    private static final Pattern PREFIX_TEMPLATE_VIRTUAL = Pattern.compile(
            "^(" + QUERY_PATTERN + ")With");
    private static final Pattern PREFIX_TEMPLATE_VIRTUAL_JOIN = Pattern.compile(
            "^(" + QUERY_PATTERN + ")JoinWith");


    /**
     * The subject, for example "findDistinctUserByNameOrderByAge" would have the subject "DistinctUser".
     */
    private final Subject subject;

    /**
     * The subject, for example "findDistinctUserByNameOrderByAge" would have the predicate "NameOrderByAge".
     */
    private final Predicate predicate;

    /**
     * Creates a new {@link PartTree} by parsing the given {@link String}.
     *
     * @param source the {@link String} to parse
     * @param domainClass the domain class to check individual parts against to ensure they refer to a property of the
     *          class
     */
    public PartTree(String source, Class<?> domainClass, MethodDefinition methodDefinition) {

        Assert.notNull(source, "Source must not be null");
        // Assert.notNull(domainClass, "Domain class must not be null");

        Matcher matcher = PREFIX_TEMPLATE.matcher(source);
        Matcher matcherVirtual = PREFIX_TEMPLATE_VIRTUAL.matcher(source);
        Matcher matcherVirtualJoin = PREFIX_TEMPLATE_VIRTUAL_JOIN.matcher(source);

        if (!(matcher.find()
                || PREFIX_TEMPLATE_VIRTUAL.matcher(source).find()
                || PREFIX_TEMPLATE_VIRTUAL_JOIN.matcher(source).find())
        ) {
            //this.subject = new Subject(Optional.empty());
            //this.predicate = new Predicate(source, domainClass, methodDefinition);
            // when can not resolve the source, throw the exception
            throw new StatementNotSupportException(methodDefinition.getNameSpace(), methodDefinition.getMethodName());
        } else if(matcherVirtual.find()) {
            this.subject = new Subject(Optional.of(matcherVirtual.group(0)));
            this.predicate = new Predicate(source.substring(matcherVirtual.group().length()), domainClass, methodDefinition);
        } else if(matcherVirtualJoin.find()) {
            this.subject = new Subject(Optional.of(matcherVirtualJoin.group(0)));
            this.predicate = new Predicate(source.substring(matcherVirtualJoin.group().length()), domainClass, methodDefinition);
        } else {
            this.subject = new Subject(Optional.of(matcher.group(0)));
            this.predicate = new Predicate(source.substring(matcher.group().length()), domainClass, methodDefinition);
        }
    }

    /**
     * Returns the {@link Sort} criteria parsed from the source.
     *
     * @return never {@literal null}.
     */
    /*public Sort getSort() {
        return predicate.getOrderBySource().toSort();
    }*/

    /**
     * Returns whether we indicate distinct lookup of entities.
     *
     * @return {@literal true} if distinct
     */
    public boolean isDistinct() {
        return subject.isDistinct();
    }

    /**
     * Returns whether a count projection shall be applied.
     *
     * @return
     */
    public boolean isCountProjection() {
        return subject.isCountProjection();
    }

    /**
     * Returns whether an existsById projection shall be applied.
     *
     * @return
     * @since 1.13
     */
    public boolean isExistsProjection() {
        return subject.isExistsProjection();
    }

    /**
     * return true if the created {@link PartTree} is meant to be used for deleteById operation.
     *
     * @return
     * @since 1.8
     */
    public boolean isDelete() {
        return subject.isDelete();
    }

    /**
     * Return {@literal true} if the create {@link PartTree} is meant to be used for a query with limited maximal results.
     *
     * @return
     * @since 1.9
     */
    public boolean isLimiting() {
        return getMaxResults() != null;
    }

    /**
     * Return the number of maximal results to return or {@literal null} if not restricted.
     *
     * @return {@literal null} if not restricted.
     * @since 1.9
     */
    @Nullable
    public Integer getMaxResults() {
        return subject.getMaxResults().orElse(null);
    }

    /**
     * Splits the given text at the given keywords. Expects camel-case style to only match concrete keywords and not
     * derivatives of it.
     *
     * @param text the text to split
     * @param keyword the keyword to split around
     * @return an array of split items
     */
    private static String[] split(String text, String keyword) {
        Pattern pattern = Pattern.compile(String.format(KEYWORD_TEMPLATE, keyword));
        return pattern.split(text);
    }

    @Override
    public void render(RenderContext context) {
        predicate.render(context);
    }

    /**
     * A part of the parsed source that results from splitting up the resource around {@literal Or} keywords. Consists of
     * {@link Part}s that have to be concatenated by {@literal And}.
     */
    static class OrPart implements Render {

        private final List<Part> children = new ArrayList<>();

        // private MethodDefinition methodDefinition;

        /**
         * Creates a new {@link OrPart}.
         * @param source the source to split up into {@literal And} parts in turn.
         * @param domainClass the domain class to check the resulting {@link Part}s against.
         * @param argumentIndex
         */
        OrPart(String source, Optional<Class> domainClass, MethodDefinition methodDefinition, AtomicInteger argumentIndex) {
            String[] split = split(source, "And");
            for (String part : split) {
                if (StringUtils.hasText(part)) {
                    children.add(new Part(part, domainClass, methodDefinition, argumentIndex));
                }
            }
        }

        @Override
        public void render(RenderContext context) {

            /*for(int i=0; i<children.size(); i++) {
                Part part = children.get(i);
                if(part.getNumberOfArguments() <=0 ) {  //没有参数，不处理
                    continue;
                }

                // 将like的信息放入缓存中，后面需要改变其参数
                if(part.getLikeType() != null) {
                    if(part.isOneParameter()) {
                        String key = methodDefinition.getNameSpace() + "." + methodDefinition.getMethodName() + "._parameter";
                        LikeContainer.getInstance().put(key, part.getLikeType());
                    } else {
                        String key = methodDefinition.getNameSpace() + "." + methodDefinition.getMethodName() + ".arg" + part.getArgumentIndex();
                        LikeContainer.getInstance().put(key, part.getLikeType());
                    }
                }
            }*/

            String delim = "";
            for(Part part : children) {
                context.renderString(delim);
                part.render(context);
                delim = " ";
            }

        }
    }

    /**
     * Represents the subject part of the query. E.g. {@code findDistinctUserByNameOrderByAge} would have the subject
     * {@code DistinctUser}.
     *
     * @author Phil Webb
     * @author Oliver Gierke
     * @author Christoph Strobl
     * @author Thomas Darimont
     */
    private static class Subject {

        private static final String DISTINCT = "Distinct";
        private static final Pattern COUNT_BY_TEMPLATE = Pattern.compile("^count(\\p{Lu}.*?)??By");
        private static final Pattern EXISTS_BY_TEMPLATE = Pattern.compile("^(" + EXISTS_PATTERN + ")(\\p{Lu}.*?)??By");
        private static final Pattern DELETE_BY_TEMPLATE = Pattern.compile("^(" + DELETE_PATTERN + ")(\\p{Lu}.*?)??By");
        private static final String LIMITING_QUERY_PATTERN = "(First|Top)(\\d*)?";
        private static final Pattern LIMITED_QUERY_TEMPLATE = Pattern
                .compile("^(" + QUERY_PATTERN + ")(" + DISTINCT + ")?" + LIMITING_QUERY_PATTERN + "(\\p{Lu}.*?)??By");

        private final boolean distinct;
        private final boolean count;
        private final boolean exists;
        private final boolean delete;
        private final Optional<Integer> maxResults;

        public Subject(Optional<String> subject) {
            this.distinct = subject.map(it -> it.contains(DISTINCT)).orElse(false);
            this.count = matches(subject, COUNT_BY_TEMPLATE);
            this.exists = matches(subject, EXISTS_BY_TEMPLATE);
            this.delete = matches(subject, DELETE_BY_TEMPLATE);
            this.maxResults = returnMaxResultsIfFirstKSubjectOrNull(subject);
        }

        /**
         * @param subject
         * @return
         * @since 1.9
         */
        private Optional<Integer> returnMaxResultsIfFirstKSubjectOrNull(Optional<String> subject) {

            return subject.map(it -> {
                Matcher grp = LIMITED_QUERY_TEMPLATE.matcher(it);
                if (!grp.find()) {
                    return null;
                }
                return StringUtils.hasText(grp.group(4)) ? Integer.valueOf(grp.group(4)) : 1;
            });

        }

        /**
         * Returns {@literal true} if {@link Subject} matches {@link #DELETE_BY_TEMPLATE}.
         *
         * @return
         * @since 1.8
         */
        public boolean isDelete() {
            return delete;
        }

        public boolean isCountProjection() {
            return count;
        }

        /**
         * Returns {@literal true} if {@link Subject} matches {@link #EXISTS_BY_TEMPLATE}.
         *
         * @return
         * @since 1.13
         */
        public boolean isExistsProjection() {
            return exists;
        }

        public boolean isDistinct() {
            return distinct;
        }

        public Optional<Integer> getMaxResults() {
            return maxResults;
        }

        private boolean matches(Optional<String> subject, Pattern pattern) {
            return subject.map(it -> pattern.matcher(it).find()).orElse(false);
        }
    }

    /**
     * Represents the predicate part of the query.
     *
     * @author Oliver Gierke
     * @author Phil Webb
     */
    private static class Predicate implements Render {

        private static final String KEYWORD_TEMPLATE = "(%s)(?=(\\p{Lu}|\\P{InBASIC_LATIN}))";

        private static final String ORDER_BY = "OrderBy";

        private final List<OrPart> nodes = new ArrayList<>();

        private OrderBySource orderBySource;

        private MethodDefinition methodDefinition;

        public Predicate(String predicate, Class<?> domainClass, MethodDefinition methodDefinition) {

            this.methodDefinition = methodDefinition;

            String[] parts = split(predicate, ORDER_BY);

            if (parts.length > 2) {
                throw new IllegalArgumentException("OrderBy must not be used more than once in a methodDefinition name!");
            }

            Optional<Class> domainClassOptional = Optional.ofNullable(domainClass);

            buildTree(parts[0], domainClassOptional);
            this.orderBySource = (parts.length == 2 ? new OrderBySource(parts[1], domainClassOptional) : OrderBySource.EMPTY);

            //设置参数index
            /*int index = 0;
            for(OrPart orPart : nodes) {
                for(int i=0; i<orPart.children.size(); i++) {
                    Part part = orPart.children.get(i);
                    if(part.getNumberOfArguments() <= 0) {  //没有参数，不处理
                        continue;
                    }

                    //跳过RowBounds
                    if(methodDefinition.getParameterDefinitions().get(index).isPage()) {
                        index ++;
                        i --;
                        continue;
                    }
                    //跳过Sort
                    if(methodDefinition.getParameterDefinitions().get(index).isSort()) {
                        this.orderBySource = null;
                        index ++;
                        i --;
                        continue;
                    }

                    part.setArgumentIndex(index);

                    //看看有没有ifTest
                    Part.TestCondition testCondition = getCondition(index, methodDefinition);
                    if(testCondition != null) {
                        part.setTestCondition(testCondition);
                    }
                    index = index + part.getType().getNumberOfArguments();

                    part.setLikeCache(methodDefinition);
                }
            }*/

        }

        private void buildTree(String source, Optional<Class> domainClassOptional) {
            AtomicInteger argumentIndex = new AtomicInteger();
            String[] split = split(source, "Or");
            for (String part : split) {
                nodes.add(new OrPart(part, domainClassOptional, methodDefinition, argumentIndex));
            }
        }

        public OrderBySource getOrderBySource() {
            return orderBySource;
        }

        private static String[] split(String text, String keyword) {

            Pattern pattern = Pattern.compile(String.format(KEYWORD_TEMPLATE, keyword));
            return pattern.split(text);
        }

        @Override
        public void render(RenderContext context) {
            if(this.nodes.size() > 0) {
                context.renderString("<where>");

                this.nodes.forEach(orPart -> {
                    context.renderString("<trim prefix=\" OR \" prefixOverrides=\"AND\" suffixOverrides=\"AND\">");
                    orPart.render(context);
                    context.renderString("</trim>");
                });

                context.renderString("</where>");
            }

            if(this.getOrderBySource() != null) {
                this.getOrderBySource().render(context);
            }
        }
    }
}
