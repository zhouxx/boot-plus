# ${title}

|   版本 | ${version}                                |
| -----: | :---------------------------------------- |
|   描述 | ${description}                            |
| 联系人 | ${contactName!''} <#if contactEmail??> [Email:](mailto:${contactEmail}) </#if> |

<#list interfaces as interface>

## ${interface.summary}

### URL

${interface.httpMethod}	${interface.path}

### 入参

<#if interface.parameters?? && (interface.parameters?size > 0)>
| 字段              | 类型              | 是否必须                                  | 描述                        |
| ----------------- | ----------------- | ----------------------------------------- | --------------------------- |
<#list interface.parameters as parameter>
| ${parameter.name} | ${parameter.type!'unknown'} | ${parameter.required?string('Yes', 'No')} | ${parameter.description!''} |
</#list>
<#else>
    无
</#if>

### 出参

<#if interface.responses?? && (interface.responses?size > 0)>
| 字段              | 类型              | 是否必须                                  | 描述                        |
| ----------------- | ----------------- | ----------------------------------------- | --------------------------- |
<#list interface.responses as response>
| ${response.name} | ${response.type!'unknown'} | ${response.required?string('Yes', 'No')} | ${response.description!''} |
</#list>
<#else>
    无
</#if>

</#list>

