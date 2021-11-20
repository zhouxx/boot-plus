/**
 *    Copyright 2009-2020 the original author or authors.
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
package com.alilitech.mybatis.jpa.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The return value type of {@link Boolean} type handler
 * @author Clinton Begin
 * @author Zhou Xiaoxiang
 */
public class BooleanTypeHandler extends BaseTypeHandler<Boolean> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setBoolean(i, parameter);
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        Object object = rs.getObject(columnName);
        Boolean x = getBooleanData(object);
        if (x != null) return x;
        return rs.getBoolean(columnName);
    }

    @SuppressWarnings("all")
    private Boolean getBooleanData(Object object) {
        if(object instanceof Integer) {
            int i = Integer.parseInt(object.toString());
            return i > 0;
        }
        return null;
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        Object object = rs.getObject(columnIndex);
        Boolean x = getBooleanData(object);
        if (x != null) return x;
        return rs.getBoolean(columnIndex);
    }

    @Override
    public Boolean getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        Object object = cs.getObject(columnIndex);
        Boolean x = getBooleanData(object);
        if (x != null) return x;
        return cs.getBoolean(columnIndex);
    }
}
