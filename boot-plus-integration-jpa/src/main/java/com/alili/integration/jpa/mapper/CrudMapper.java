/**
 *    Copyright 2017-2019 the original author or authors.
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
package com.alili.integration.jpa.mapper;

import com.alili.integration.jpa.domain.Sort;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public interface CrudMapper<T, ID extends Serializable> extends Mapper<T, ID> {

    List<T> findAll();

    List<T> findAllSort(Sort sort);

    Optional<T> findOne(ID id);

    List<T> findAllIds(Collection<ID> ids);

    int insert(T entity);

    int insertSelective(T entity);

    int insertBatch(List<T> list);

    int update(T entity);

    int updateSelective(T entity);

    int updateBatch(List<T> list);

    int delete(ID id);

    int deleteBatch(Collection<ID> ids);

    Boolean exists(ID id);

}
