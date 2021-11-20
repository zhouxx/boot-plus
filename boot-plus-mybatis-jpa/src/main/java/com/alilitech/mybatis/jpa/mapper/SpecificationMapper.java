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
import com.alilitech.mybatis.jpa.criteria.Specification;
import com.alilitech.mybatis.jpa.domain.Pageable;

import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
@NoMapperBean
public interface SpecificationMapper<T, ID> extends Mapper<T, ID> {

    List<T> findSpecification(Specification<T> specification);

    List<T> findPageSpecification(Pageable<T> pageable, Specification<T> specification);

}
