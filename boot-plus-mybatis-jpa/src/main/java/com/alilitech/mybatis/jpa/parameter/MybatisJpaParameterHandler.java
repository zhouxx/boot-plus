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
package com.alilitech.mybatis.jpa.parameter;

import com.alilitech.mybatis.jpa.LikeContainer;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
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

    @Override
    public Object getParameterObject() {
        return super.getParameterObject();
    }

    private static Object process(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        //对like参数进行转换，防止sql注入，前提必须是有参数
        if(parameterObject != null && mappedStatement.getSqlCommandType().equals(SqlCommandType.SELECT)) {
            String methodId = mappedStatement.getId();
            LikeContainer likeContainer = LikeContainer.getInstance();

            //多参数
            if(parameterObject instanceof MapperMethod.ParamMap) {
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
                    }
                });
            } else { // 单参数
                String likeKey = methodId + "._parameter";
                if(likeContainer.isExist(likeKey)) {
                    switch (likeContainer.get(likeKey)) {
                        case BEFORE: {
                            parameterObject = "%" + parameterObject;
                            break;
                        }
                        case AFTER: {
                            parameterObject = parameterObject + "%";
                            break;
                        }
                        case CONTAIN: {
                            parameterObject = "%" + parameterObject + "%";
                            break;
                        }
                    }
                    // 单个参数，必须手动设置，否则无效。因为传参的是值，不是引用
                    boundSql.setAdditionalParameter("_parameter", parameterObject);
                }
            }
        }
        return parameterObject;
    }
}
