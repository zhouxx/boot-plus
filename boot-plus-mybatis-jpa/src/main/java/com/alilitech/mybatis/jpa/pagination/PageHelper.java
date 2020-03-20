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
package com.alilitech.mybatis.jpa.pagination;


/**
 * 分页工具类
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PageHelper {

    /**
     * 计算当前分页偏移量
     *
     * @param current 当前页
     * @param size 每页显示数量
     * @return
     */
    public static int offsetCurrent(int current, int size) {
        if (current > 0) {
            return (current - 1) * size;
        }
        return 0;
    }


    /**
     *
     * @param pagination
     * @return
     */
    public static int offsetCurrent(Pagination pagination) {
        if (null == pagination) {
            return 0;
        }
        return offsetCurrent(pagination.getPage(), pagination.getSize());
    }

}
