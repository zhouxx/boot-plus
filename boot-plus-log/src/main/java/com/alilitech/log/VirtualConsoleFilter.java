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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 2.0.0
 */
public class VirtualConsoleFilter extends Filter<ILoggingEvent> {

    private static final VirtualConsoleFilter virtualConsoleFilter = new VirtualConsoleFilter();

    private VirtualConsoleFilter() {
    }

    public static VirtualConsoleFilter getInstance() {
        return virtualConsoleFilter;
    }

    private final List<Filter<ILoggingEvent>> filters = new ArrayList<>();

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!filters.isEmpty()) {
            for (Filter<ILoggingEvent> filter: filters) {
                FilterReply reply = filter.decide(event);
                if(FilterReply.ACCEPT.compareTo(reply) > 0) {
                    return reply;
                }
            }
        }
        return FilterReply.ACCEPT;
    }

    public boolean addFilter(Filter<ILoggingEvent> filter) {
        if(filter == this) {
            return false;
        }
        return filters.add(filter);
    }
}
