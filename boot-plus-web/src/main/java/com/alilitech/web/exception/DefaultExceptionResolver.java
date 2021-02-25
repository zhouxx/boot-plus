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
package com.alilitech.web.exception;

import com.alilitech.web.WebConfiguration;
import com.alilitech.util.UnicodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DefaultExceptionResolver implements HandlerExceptionResolver {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExceptionHandler exceptionHandler;

    public DefaultExceptionResolver(@Nullable ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if(exceptionHandler != null) {
            return exceptionHandler.resolveException(request, response, handler, ex);
        }
        logger.error(ex.getMessage());

        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.addHeader(WebConfiguration.TIP_KEY, UnicodeUtils.stringToUnicode("服务器内部错误"));

        // return empty mv
        return new ModelAndView();
    }
}
