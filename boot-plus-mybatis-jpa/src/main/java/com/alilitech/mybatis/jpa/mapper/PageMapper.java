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
package com.alilitech.mybatis.jpa.mapper;


import com.alilitech.mybatis.jpa.anotation.NoMapperBean;
import com.alilitech.mybatis.jpa.domain.Pageable;
import com.alilitech.mybatis.jpa.domain.Sort;

import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@NoMapperBean
public interface PageMapper<T, ID> extends CrudMapper<T, ID> {

    List<T> findAllPage(Pageable<T> pageable);

    List<T> findAllPageSort(Pageable<T> pageable, Sort sort);

}
