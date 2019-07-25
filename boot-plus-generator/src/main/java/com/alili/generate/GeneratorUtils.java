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

import com.alili.generate.config.DataSourceConfig;
import com.alili.generate.config.GlobalConfig;
import com.alili.generate.config.TableConfig;
import com.alili.generate.definition.AnnotationDefinition;
import com.alili.generate.definition.ClassDefinition;
import com.alili.generate.definition.ClassType;
import com.alili.generate.definition.FieldDefinition;
import com.alili.integration.jpa.anotation.GeneratedValue;
import com.alili.integration.jpa.mapper.PageMapper;
import com.alili.integration.jpa.parameter.GenerationType;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import javax.persistence.Id;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class GeneratorUtils {

    public static List<ClassDefinition> process(DataSourceConfig dataSourceConfig, GlobalConfig globalConfig, List<TableConfig> tableConfigList) {

        List<Table> tables = new ArrayList<>();
        List<ClassDefinition> classDefinitions = new ArrayList<>();

        Connection connection = null;

        try {
            Class.forName(dataSourceConfig.getDriverName());

            connection = DriverManager.getConnection(dataSourceConfig.getUrl(), dataSourceConfig.getUsername(), dataSourceConfig.getPassword());

            DatabaseMetaData metaData = connection.getMetaData();

            for (TableConfig tableConfig : tableConfigList) {

                String tableName = tableConfig.getTableName();

                List<TableColumn> tableColumns = new ArrayList<>();
                //List<TableColumn> queryColumns = new ArrayList<>();
                //List<TableColumn> detailColumns = new ArrayList<>();


                /*ResultSet rsTable = metaData.getTables(null, "", tableName, null);
                rsTable.next();
                rsTable.getString("REMARKS");*/
                ResultSet rsPK = metaData.getPrimaryKeys(null, "", tableName);

                String pkColumn = null;

                if(rsPK.next()) {
                    pkColumn = rsPK.getString("COLUMN_NAME");
                    //rsPK.getString("KEY_SEQ");
                    //rsPK.getString("PK_NAME");
                }

                ResultSet rs = metaData.getColumns(null, "", tableName, "%");

                while (rs.next()) {

                    TableColumn tableColumn = new TableColumn();

                    Integer columnType = rs.getInt("DATA_TYPE");
                    Integer columnSize = rs.getInt("COLUMN_SIZE");
                    String columnName = rs.getString("COLUMN_NAME");
                    boolean isNull = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                    Integer scale = rs.getInt("DECIMAL_DIGITS");
                    String remark = rs.getString("REMARKS");
                    String defaultValue = rs.getString("COLUMN_DEF");
                    String isAutoIncrement = rs.getString("IS_AUTOINCREMENT");
                    String isGeneratedColumn = rs.getString("IS_GENERATEDCOLUMN");

                    //String tableCat = rs.getString("TABLE_CAT");
                    //String tableSchem = rs.getString("TABLE_SCHEM");
                    //String tableName1 = rs.getString("TABLE_NAME");

                    tableColumn.setColumnName(columnName);
                    tableColumn.setColumnSize(columnSize);
                    tableColumn.setColumnType(covertDomain(columnType));
                    tableColumn.setDefaultValue(defaultValue);
                    tableColumn.setNullAble(isNull);
                    tableColumn.setPrimary(columnName.equals(pkColumn) ? true : false);
                    tableColumn.setRemark(remark);
                    tableColumn.setAutoIncrement("YES".equals(isAutoIncrement) ? true : false);
                    tableColumn.setGeneratedColumn("YES".equals(isGeneratedColumn) ? true : false);
                    tableColumn.setScale(scale);

                    /*if(Arrays.asList(tableConfig.getQuery().split(",")).contains(columnName)) {
                        queryColumns.add(tableColumn);
                    }
                    if(Arrays.asList(tableConfig.getDetail().split(",")).contains(columnName)) {
                        detailColumns.add(tableColumn);
                    }

                    if(tableConfig.getIgnore().contains(columnName)) {
                        continue;
                    }*/

                    tableColumns.add(tableColumn);

                }

                rsPK.close();
                rs.close();

                Table table = new Table(tableColumns, tableConfig);

                tables.add(table);
            }

        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        tables.forEach(table -> {

            Map<String, byte[]> extendMap = getExtend(table);

            //domain
            ClassDefinition domainClassDefinition = covertDomain(table, globalConfig.getPackageName());
            classDefinitions.add(domainClassDefinition);
            Class<?> domainClass = loadClass(extendMap, classDefinitions);
            domainClassDefinition.addAnnotation("Getter").addAnnotation("Setter").addImport("lombok.Getter").addImport("lombok.Setter");

            //mapper
            ClassDefinition mapperDefinition = covertMapper(table, globalConfig.getPackageName(), domainClass);
            classDefinitions.add(mapperDefinition);

        });
        return classDefinitions;
    }

    private static Class<?> covertDomain(Integer columnType) {

        switch (columnType) {
            case Types.CHAR :
            case Types.VARCHAR :
            case Types.LONGVARCHAR :
                return String.class;
            case Types.DATE :
            case Types.TIMESTAMP:
            case Types.TIME :
                return java.util.Date.class;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return BigDecimal.class;
            case Types.REAL:
                return Float.class;
            case Types.DOUBLE:
            case Types.FLOAT:
                return Double.class;
            case Types.BIGINT:
                return Long.class;
            case Types.INTEGER:
            case Types.TINYINT:
                return Integer.class;
            case Types.BLOB :
            case Types.NCLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return Byte[].class;
                default :
                    return String.class;
        }
    }

    private static Map<String, byte[]> getExtend(Table table) {
        Map<String, byte[]> map = new HashMap<>();
        //set extend
        /*String extend = table.getTableConfig().getExtend();
        byte[] bytes = null;
        try {
            Class<?> aClass = Class.forName(extend);
            String className = aClass.getName();
            String classAsPath = className.replace('.', '/') + ".class";
            InputStream stream = aClass.getClassLoader().getResourceAsStream(classAsPath);
            bytes = new byte[stream.available()];
            IOUtils.readFully(stream, bytes);
            map.put(extend, bytes);
            //classDefinition.addExtend(aClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        return map;
    }

    private static ClassDefinition covertDomain(Table table, String globalPackageName) {
        //生成domain
        ClassDefinition classDefinition = new ClassDefinition(table.getTableConfig().getDomainName());
        classDefinition.setClassType(ClassType.DOMAIN).setPackageName(globalPackageName + "." + ClassType.DOMAIN.getType());
        classDefinition.addAnnotation(new AnnotationDefinition(javax.persistence.Table.class).addProperty("name", table.getTableConfig().getTableName()));

        // classDefinition.addExtend(table.getTableConfig().getExtend());

        table.getTableColumns().forEach(tableColumn -> {
            FieldDefinition fieldDefinition = new FieldDefinition(tableColumn.getColumnType(), tableColumn.getProperty());

            if(tableColumn.isPrimary()) {
                fieldDefinition.addAnnotation(Id.class);
                fieldDefinition.addAnnotation(new AnnotationDefinition(GeneratedValue.class).addProperty("value", GenerationType.AUTO));
            }
            if(tableColumn.getRemark() != null && !tableColumn.getRemark().equals("")) {
                fieldDefinition.addComment(tableColumn.getRemark());
            }
            classDefinition.addFieldDefinition(fieldDefinition);
        });
        return classDefinition;
    }

    private static Class<?> loadClass(Map<String, byte[]> exist, List<ClassDefinition> classDefinitions) {

        List<JavaSrc> javaSrcList = new ArrayList<>();

        String retJavaName = null;

        for(int i=0; i<classDefinitions.size(); i++) {
            ClassDefinition classDefinition = classDefinitions.get(i);

            //这里处理domain并动态加载进内存
            String javaSrc = classDefinition.toString();
            String javaName = classDefinition.getClassName();
            if (classDefinition.getPackageName() != null && !classDefinition.getPackageName().equals("")) {
                javaName = classDefinition.getPackageName() + "." + javaName;
            }
            javaSrcList.add(new JavaSrc(javaName + ".java", javaSrc));

            if(i == classDefinitions.size()-1) {
                retJavaName = javaName;
            }
        }
        try {

            Map<String, byte[]> compile = DynamicLoader.compile(javaSrcList);
            if(exist != null) {
                compile.putAll(exist);
            }
            Class<?> aClass = new DynamicLoader.MemoryClassLoader(compile).findClass(retJavaName);
            return aClass;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ClassDefinition covertMapper(Table table, String globalPackageName, Class domainClass) {
        //生成domain
        ClassDefinition classDefinition = new ClassDefinition(table.getTableConfig().getDomainName() + "Mapper");
        classDefinition.setClassType(ClassType.MAPPER).setPackageName(globalPackageName + "." + ClassType.MAPPER.getType());
        classDefinition.setInterfaced(true);

        Class idClass = null;
        List<Field> fields = Arrays.asList(domainClass.getDeclaredFields());
        for(Field field : fields) {
            if(field.getAnnotation(Id.class) != null) {
                idClass = field.getType();
                break;
            }
        }
        TypeResolver typeResolver = new TypeResolver();
        ResolvedType resolvedType = typeResolver.resolve(PageMapper.class, domainClass, idClass);
        //List<ResolvedType> typeParameters = resolvedType.getTypeBindings().getTypeParameters();
        classDefinition.addExtend(resolvedType);
        //classDefinition.addImport(CurdMapper.class).addImport(domainClass).addImport(idClass);
        //classDefinition.addExtend(CurdMapper.class.getSimpleName() + "<" + domainClass.getSimpleName() + ", " + idClass.getSimpleName() + ">");

        /*if(!CollectionUtils.isEmpty(table.getQueryColumns())) {

            List<String> byNames = new ArrayList<>();

            List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

            table.getQueryColumns().forEach(tableColumn -> {
                byNames.add(ColumnUtils.initUpper(tableColumn.getProperty()));
                parameterDefinitions.add(new ParameterDefinition(tableColumn.getProperty()).setTypeClass(tableColumn.getColumnType()));

            });
            String byName = StringUtils.join(byNames, "And");
            MethodDefinition methodDefinition = new MethodDefinition("findPageBy" + byName);
            methodDefinition.addAnnotation("@IfTest(notEmpty = true)").addImport(IfTest.class);
            methodDefinition.addParameter(new ParameterDefinition("page").setTypeClass(Pageable.class));
            methodDefinition.addParameter(new ParameterDefinition("sort").setTypeClass(Sort.class));
            parameterDefinitions.forEach(parameterDefinition -> {
                methodDefinition.addParameter(parameterDefinition);
            });
            methodDefinition.setReturnValueClass(List.class).setReturnValue("List<" + domainClass.getSimpleName() + ">");
            methodDefinition.setHasBody(false);
            classDefinition.addMethodDefinition(methodDefinition);
        }*/


        return classDefinition;
    }

    public static void main(String[] args) throws IOException {

        InputStream inputStream = GeneratorUtils.class.getClassLoader().getResourceAsStream("generate.xml");
        XmlParser xmlParser = new XmlParser(inputStream);
        DataSourceConfig dataSourceConfig = xmlParser.parseText("config.datasource", DataSourceConfig.class);
        GlobalConfig globalConfig = xmlParser.parseText("config.properties", GlobalConfig.class);

        List<TableConfig> tableConfigs = xmlParser.parseListAttribute("config.tables.table", TableConfig.class);
        List<ClassDefinition> classDefinitions = process(dataSourceConfig, globalConfig, tableConfigs);
        classDefinitions.forEach(classDefinition -> {
            try {
                classDefinition.out(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
