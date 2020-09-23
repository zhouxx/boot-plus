/*
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
package com.alilitech.generate;

import com.alilitech.generate.utils.ColumnUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据库里的字段信息
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Getter
@Setter
public class TableColumn {

    private Class columnType;

    private Integer columnSize;

    private String columnName;

    private String property;

    private boolean nullAble;

    private Integer scale;

    private String remark;

    private String defaultValue;

    private boolean autoIncrement;

    private boolean generatedColumn;

    private boolean primary;

    public String getProperty() {
        return ColumnUtils.getJavaStyle(columnName);
    }
}
