 [![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)  [![Maven central](https://img.shields.io/maven-central/v/com.alilitech/boot-plus?color=brightgreen)](https://maven-badges.herokuapp.com/maven-central/com.alilitech/boot-plus) 

# 简介

Spring Boot虽然提供了方便地开发，但实际在项目开发中还是有太多的痛点需要我们手动处理，如：

* 项目里的一些字典与字典值的转化都要手动处理
* 跨域、校验、异常处理等
* 权限复杂且不灵活，能不能用一个类就能实现权限的灵活扩展

为了解决项目里的痛点，让开发者更专注于业务，而不是分心解决很多与业务无关的事情，我们提供了boot-plus。

Boot-plus是基于Spring Boot构建的框架，提供了全套解决方案，使其开发更方便，更简单。但其不改变其原有功能，只做扩展。可单独引用各个子模块，而且对原来已有业务完全不影响。

> 🎉 **欢迎右上角 Star，你的鼓励是我们不断更新代码的动力**
>
> mybatis-jpa 移至 https://github.com/zhouxx/mybatis-jpa-parent

# 链接

  [⚡️在线文档](https://zhouxx.github.io/boot-plus/#/README)

# 特性

* **解耦：** 每个模块只对部分功能进行增强，可以任意搭配组合,不随意入侵不同层次模块的代码
* **兼容：** 兼容已经开发的项目，可以直接使用，不影响原来的功能
* **配置化：** 部分功能通过配置化就能解决项目里的痛点
* **定制化：** 提供了优雅的扩展接口和默认实现，若用户有定制化需求可自行定制

# 主要功能

* **多数据源：** 动态添加数据源，可实现配置或运行时加载新的数据源
* **web扩展：**
  * 跨域配置化：通过配置即可实现跨域
  * jackson扩展： 通过注解或配置可实现字典、数字、空值自动转化
  * 统一校验、异常处理
* **在线文档：** 提供在线API，可通过配置实现全局参数，统一授权
* **Log：** 提供Log UI，在线修改Log级别，为调试提供优雅的定制
* **安全框架集成：** 集成Spring Security，提供JWT和有状态Token实现，并支持多因素认证，更简单，更高效。

# License

Boot Plus is under the Apache 2.0 license. See the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0) file for details.
