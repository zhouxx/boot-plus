 <a href="https://www.apache.org/licenses/LICENSE-2.0"><img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square"></a>

# 简介

Sql写完了，字段调整后痛苦；自测接口时都要把复杂的报文写一遍json；因为字典的展示，需要改sql；跨域，校验、异常处理等等，这些痛点将在这里得到解决。

Bootplus是基于Spring Boot构建的框架，使其开发更方便，更简单。但其不改变其原有功能，只做扩展，并且兼容原有的开发。

# 特性

* **解耦：** 每个模块只对部分功能进行增强，可以任意搭配组合
* **兼容：** 兼容已经开发的项目，可以直接使用，不影响原来的功能
* **配置化：** 部分功能通过配置化就能解决项目里的痛点
* **定制化：** 提供了优雅的扩展接口和默认实现，若用户有定制化需求可自行定制

# 主要功能

* **多数据源：** 动态添加数据源，可实现配置或运行时加载新的数据源
* **定时任务：** 动态添加、移除定时任务
* **mybatis扩展：**
  * mapper自动加载：无需提供多余的SQL，即可实现简单的增删改查
  * 自定义查询接口：可通过自定义查询方法，而无需提供SQL，则可实现自定义查询
  * 关联查询：通过指定关联关系，实现一对一，一对多，多对多的关联查询
  * 主键支持：提供自增、序列、UUID主键实现，可自定义主键实现
  * 分页排序：传入分页/排序参数，即可自动分页/排序
  * 代码级触发器：提供代码级的触发器，对插入和更新操作自动赋值，无需手动赋值
  * 自定义数据库扩展：对于不同的数据库，可自定义分页和序列生成器。
* **web扩展：**
  * 跨域配置化：通过配置即可实现跨域
  * jackson扩展： 通过注解或配置可实现字典、数字、空值自动转化
  * 统一校验、异常处理
* **在线文档：** 提供在线API，可通过配置实现全局参数，统一授权
* **Log：** 提供Log UI，在线修改Log级别，为调试提供优雅的定制
* **安全框架集成：** 集成Spring Security，提供JWT和常规Token实现，让使用者更简单，更高效。

# 文档

[中文文档](https://zhouxx.github.io/boot-plus/#/README) 

# License

Boot Plus is under the Apache 2.0 license. See the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0) file for details.
