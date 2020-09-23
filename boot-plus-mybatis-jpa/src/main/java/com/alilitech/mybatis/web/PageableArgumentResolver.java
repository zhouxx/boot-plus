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
package com.alilitech.mybatis.web;

import com.alilitech.mybatis.jpa.domain.Page;
import com.alilitech.mybatis.jpa.domain.Pageable;
import com.alilitech.mybatis.jpa.domain.Sort;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link Pageable} is a abstract class, define the resolver
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PageableArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String DEFAULT_PAGE_PARAMETER = "page";
    private static final String DEFAULT_SIZE_PARAMETER = "size";
    private static final int DEFAULT_MAX_PAGE_SIZE = 2000;

    private static final SortArgumentResolver DEFAULT_SORT_RESOLVER = new SortArgumentResolver();

    private final SortArgumentResolver sortResolver;

    public PageableArgumentResolver(SortArgumentResolver sortResolver) {
        this.sortResolver = sortResolver == null ? DEFAULT_SORT_RESOLVER : sortResolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.equals(parameter.getParameterType()) || Page.class.equals(parameter.getParameterType());
    }

    @Override
    public Page resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String pageString = webRequest.getParameter(DEFAULT_PAGE_PARAMETER);
        String pageSizeString = webRequest.getParameter(DEFAULT_SIZE_PARAMETER);

        //默认分页
        Pageable pageable = new Page();

        //转化加优化
        int page = StringUtils.hasText(pageString) ? parseAndApplyBoundaries(pageString, Integer.MAX_VALUE, false)
                : pageable.getPage();
        int pageSize = StringUtils.hasText(pageSizeString) ? parseAndApplyBoundaries(pageSizeString, DEFAULT_MAX_PAGE_SIZE, false)
                : pageable.getSize();

        // Limit lower bound
        pageSize = pageSize < 1 ? pageable.getSize() : pageSize;
        // Limit upper bound
        pageSize = Math.min(pageSize, DEFAULT_MAX_PAGE_SIZE);

        //Sort解析转化
        Sort sort = sortResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        return new Page(page, pageSize, sort);
    }

    private int parseAndApplyBoundaries(String parameter, int upper, boolean shiftIndex) {

        try {
            int parsed = Integer.parseInt(parameter) - (shiftIndex ? 1 : 0);
            return parsed < 0 ? 0 : Math.min(parsed, upper);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
