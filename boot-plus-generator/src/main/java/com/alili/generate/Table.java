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
package com.alili.generate;

import com.alili.generate.config.TableConfig;

import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class Table {

    private List<TableColumn> tableColumns;

    private List<TableColumn> queryColumns;

    private List<TableColumn> detailColumns;

    private TableConfig tableConfig;

    private TableColumn primaryKeyColumn;

    public Table(List<TableColumn> tableColumns, List<TableColumn> queryColumns, List<TableColumn> detailColumns, TableConfig tableConfig) {
        this.tableColumns = tableColumns;
        this.queryColumns = queryColumns;
        this.detailColumns = detailColumns;
        this.tableConfig = tableConfig;
    }

    public List<TableColumn> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<TableColumn> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public TableConfig getTableConfig() {
        return tableConfig;
    }

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }

    public List<TableColumn> getQueryColumns() {
        return queryColumns;
    }

    public void setQueryColumns(List<TableColumn> queryColumns) {
        this.queryColumns = queryColumns;
    }

    public List<TableColumn> getDetailColumns() {
        return detailColumns;
    }

    public void setDetailColumns(List<TableColumn> detailColumns) {
        this.detailColumns = detailColumns;
    }

    public TableColumn getPrimaryKeyColumn() {
        for(TableColumn tableColumn : tableColumns) {
            if(tableColumn.isPrimary()) {
                return tableColumn;
            }
        }
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(TableColumn primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

}
