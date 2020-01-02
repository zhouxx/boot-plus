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
package com.alilitech.integration.jpa.parameter;

import com.alilitech.integration.jpa.LikeContainer;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class MybatisJpaParameterHandler extends DefaultParameterHandler {

    public MybatisJpaParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        super(mappedStatement, process(mappedStatement, parameterObject, boundSql), boundSql);
    }

    private static Object process(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {

        //对like参数进行转换，防止sql注入
        if(parameterObject instanceof MapperMethod.ParamMap) {
            String methodId = mappedStatement.getId();
            LikeContainer likeContainer = LikeContainer.getInstance();

            MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) parameterObject;
            paramMap.forEach((key, value) -> {
                String likeKey = methodId + "." + key;
                if(likeContainer.isExist(likeKey) && value != null) {
                    switch (likeContainer.get(likeKey)) {
                        case BEFORE: {
                            paramMap.put(key, "%" + value);
                            break;
                        }
                        case AFTER: {
                            paramMap.put(key, value + "%");
                            break;
                        }
                        case CONTAIN: {
                            paramMap.put(key, "%" + value + "%");
                            break;
                        }
                    }
                };
            });
        }

        return parameterObject;

    }

}
