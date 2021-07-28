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
package com.alilitech.generate.config;

import com.alilitech.generate.utils.StyleConvertUtils;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class TableConfig {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 实体类名称
     */
    private String domainName;

    /**
     * 文件存在时是否覆盖针对Domain
     */
    private boolean overrideDomain = true;

    /**
     * 文件存在时是否覆盖针对Mapper
     */
    private boolean overrideMapper = false;

    /**
     * 忽略非标准的下划线
     */
    private boolean ignoreNoStandardUnderscore = false;

    public TableConfig() {
    }

    public TableConfig(String tableName) {
        this(tableName, StyleConvertUtils.getUpperCamelCaseStyle(tableName));
    }

    public TableConfig(String tableName, String domainName) {
        this.tableName = tableName;
        this.domainName = domainName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDomainName() {
        if(domainName == null || domainName.equals("")) {
            return StyleConvertUtils.getUpperCamelCaseStyle(tableName);
        }
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public boolean isOverrideDomain() {
        return overrideDomain;
    }

    public void setOverrideDomain(boolean overrideDomain) {
        this.overrideDomain = overrideDomain;
    }

    public boolean isOverrideMapper() {
        return overrideMapper;
    }

    public void setOverrideMapper(boolean overrideMapper) {
        this.overrideMapper = overrideMapper;
    }

    public boolean isIgnoreNoStandardUnderscore() {
        return ignoreNoStandardUnderscore;
    }

    public void setIgnoreNoStandardUnderscore(boolean ignoreNoStandardUnderscore) {
        this.ignoreNoStandardUnderscore = ignoreNoStandardUnderscore;
    }
}
