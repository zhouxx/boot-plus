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
package com.alilitech.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.alilitech.web.ThreadLocalContainer;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.UUID;

/**
 * Return the events thread and requestId (usually the current thread).
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author ZhouXiaoxiang
 * @since 1.3.9
 */
public class ThreadRequestIdConverter extends ClassicConverter {

    public static final ThreadLocal<String> requestIdThreadLocal = new ThreadLocal<>();

    public ThreadRequestIdConverter() {
        if(ClassUtils.isPresent("com.alilitech.web.ThreadLocalContainer", null)) {
            ThreadLocalContainer.getInstance().addThreadLocal(requestIdThreadLocal);
        }
    }

    public String convert(ILoggingEvent event) {
        // 非请求且未设置requestId的情况下，直接返回线程名称
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null && requestIdThreadLocal.get() == null) {
            return event.getThreadName();
        }

        // 一个请求第一次打印日志的时候生成requestId
        if(requestAttributes != null && requestIdThreadLocal.get() == null) {
            requestIdThreadLocal.set(UUID.randomUUID().toString().substring(0, 18));
        }

        // 发现requestId不为空的时候才打印requestId，否则直接打印线程名称
        if(requestIdThreadLocal.get() != null) {
            return event.getThreadName() + "--" + requestIdThreadLocal.get();
        } else {
            return event.getThreadName();
        }
    }

}
