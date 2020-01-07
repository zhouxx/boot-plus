# Boot Plus Reference Guide 1.1.x

# Part1 : 简介

​	Boot Plus 是基于spring boot 的增强，但并未修改原来的功能。让spring boot的使用者更好地关注于业务。主要增强以下几个方面：

* dynamic datasource
* quartz
* web: json serialize, cros and so on
* log management
* swagger(api online)
* mybatis extension
* security integration

​	**Boot Plus**  包含四部分:

- **core** 
- **web** 
- **biz** 
- **integration** 



# Part2 : 起步

## 2.1 系统要求

Boot Plus 1.1.0 至少要求java1.8，Spring Boot 2.2.1.RELEASE.

## 2.2 Maven 依赖

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alili</groupId>
            <artifactId>boot-plus-dependencies</artifactId>
            <version>1.1.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 2.3 开发第一个应用

### 2.3.1 创建pom

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>myproject</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>com.alili</groupId>
            <artifactId>boot-plus-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alili</groupId>
            <artifactId>boot-plus-web</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.alili</groupId>
                <artifactId>boot-plus-dependencies</artifactId>
                <version>1.1.x</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

</project>
```

你可以通过IDE指定启动类：`com.AppStart`来启动项目，也可以通过自定义启动类启动项目。

### 2.3.2 创建一个可执行的jar

需要添加 spring-boot-maven-plugin 至 pom.xml :

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <mainClass>com.AppStart</mainClass>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

至此，你已成功搭建和部署一个项目了。



# Part3 : core

核心层是基于spring的。基于核心层扩展了组件：

## 3.1 boot-plus-core-datasource

动态数据源是指在项目里可以配置多个数据源，并且可以在不同的地方指定使用不同的数据源。

* 配置多个数据源

主数据源还和原来的方式一样，当未指定数据源或找不到指定的数据源时使用主数据源。
其它数据源名称要以`ds`开头，否则无法识别。配置方式和spring boot的方式一样。    

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/test
    username: test
    password: MyNewPass4!
    ds-second:
      url: jdbc:mysql://127.0.0.1:3306/test1
      username: test
      password: MyNewPass4! 
```

代码里动态使用数据源的方式采用注解，一般在service层。比如：

```java
@DynamicSource("second")
public void exeService() {
    
}
```

* 动态添加/移除数据源

代码里可以动态地创建数据源，并添加到多数据源里。

我们可以拿到数据源对象，判断是否是`DefaultDynamicDataSource`此类，可以调用此类的`addDataSource`方法。

```java
//add datasource
defaultDynamicDataSource.addDataSource(datasourceName, datasourceUrl, datasourceUsername, datasourcePassword);

//remove datasource
defaultDynamicDataSource.remove(datasourceName);
```

## 3.2 boot-plus-core-quartz

定时任务是指可以在项目里的任意时间段里添加或修改定时任务。使用方法如下：
```java
 //定义jop，或其子类
QuartzJob quartzJob = new QuartzJob();
quartzJob.setClassName("com.alili.service.MyJobService");
quartzJob.setMethodName("start");
quartzJob.setEnabled(true);
quartzJob.setCronExpression("* * * * * ? *");
quartzJob.setSpringInstantiated(1);

try {
	quartzManager.saveOrUpdateJob(quartzJob);
} catch (Exception e) {
e.printStackTrace();
}
```

> 当把Job的enabled定义为false，即job失效。



# Part4 : web

web层是基于spring mvc 的框架，但他提供了更多的支持。

### 4.1 boot-plus-web

#### 4.1.1 跨域支持

跨域只需要在application.yml里配置：

```yaml
mvc:
  cors:
    enabled: true              
    path : "/**"               
    allowedOrigins: "*"        
    allowedMethods: "*"
    allowCredentials: true
    maxAge: 3600
    exposedHeaders: message
```

* enabled： 是否启用跨域
* path： 哪些URL可以跨域
* allowedOrigins： 哪些域名可以请求跨域，逗号隔开
* allowedMethods： 哪些方法可以跨域 POST, GET, PUT, DELETE, OPTIONS
* allowCredentials
* maxAge
* exposedHeaders：跨域时哪个头部信息返回

#### 4.1.2 json序列化之null值处理

对于部分null值，前端展示不友好，又不能直接去掉，若在业务里处理量大且麻烦，通过配置或注解可以解决此问题。

```yaml
mvc:
  json:
    defaultNull: true
    defaultNullValue: "-"
```

* defaultNull: 是否空字段（null）返回默认值，默认值如下：

  | null字段类型 | 默认值 |
  | ------------ | ------ |
  | String       | ""     |
  | Array        | []     |
  | Object       | {}     |
  | Date         | ""     |
  | Double       | 0.0    |
  | Integer      | 0      |
  | Map          | {}     |
  | BigDecimal   | 0.0    |

* defaultNullValue： 若配置了这个，则全部用此代替空字段（null）的返回默认值


以上都是对json的全局配置，我们对空值的默认值可以部分定义，如定义在某个需要序列化的类上：

```java
@NullFormat(defaultNull = true, defaultNullValue = "-")
```

>所有对null值默认值的处理不能对map等非常规bean起作用。如果是部分定义，只对当前类有效，其聚合的类无效。

若是对于同一个对象需要在不同线程里实现不同的效果，比如查看详情和修改详情对于null值处理不一样，可以通过以下

```java
DefaultNullContextHolder.set(false);   //关闭此次请求/线程对null值序列化的处理
```

#### 4.1.3 json序列化之数字格式化

业务上有很多逻辑运算，运算完了会产生结果，往往不同类型(如int、double)的数据返回的格式不一样，但实际上展示的时候需要统一保留两位或统一格式化。使用`@NumberFormat`注解：

```java
@NumberFormat(pattern = "#,###,##0.00")
private BigDecimal amount;
```

* pattern: 格式化格式，若使用了此样式，通过`DecimalFormat.format(pattern)`去格式化
* scale: 保留位数，默认是2
* round: 取舍模式，默认4舍五入，参考`BigDecimal`里的常量

#### 4.1.4 json序列化之字典

字典在数据库里表示不同的含义，从数据库里查出的是字典的key，与显示的值有一一对应关系。在实际展示给用户时必须是用户能理解的含义。以往的解决方式是通过在业务里单独处理或数据库查询的时候关联查询，若有新的字典含义可能还需要改代码，给项目带来了风险与不便。

Boot Plus提供了解决方法：

 - 定义字典和字典值。实现`DictCollector`并暴露给spring，`DictCollector`是字典收集器，可以收集所有的字典。可以定义多个字典收集器。
 - 使用`@DicFormat`注解。无需添加其它字段即可实现字段值的显示。
   * dictKey: 字典key，默认为当前属性的名称
   * dictKeyToString: 字典key是否转String输出
   * targetField: 目标属性名称，默认值是dicKey+"Name"
   * defaultValue: 默认值，当字典里没有对应的值时，显示的字典值
   * disableCache 禁用缓存（暂未实现）

字典序列化默认采取了缓存策略，若字典key未找到，会从每个收集器再收集一次，故请在各个收集器里做好缓存策略，避免大面积将查询打到数据库。

> 以上对json的扩展是基于jackson（spring mvc默认序列化）的，若在项目里未使用jackson序列化，无法扩展。

#### 4.1.5 流处理

springmvc为我们提供了很好的文件上传的支持。但文件返回未实现。

文件下载：

```java
@GetMapping("boot/fileDownload")
public ResponseEntity<AbstractStreamingResponseBody> fileDownload() {
    return new FileDownloadStreamingResponseBody(new File("xx.pdf"))
        .fileName("xxx.pdf")
        .mediaType(MediaType.APPLICATION_PDF)
        .toResponseEntity();
}
```

文件查看：

```java
@GetMapping("boot/fileView")
public ResponseEntity<AbstractStreamingResponseBody> fileView() {
    return new FileViewStreamingResponseBody(new File("xx.pdf"))
            .toResponseEntity();
}
```

#### 4.1.6 校验

校验可以自定义校验结果字段，和根据校验校验结果返回给客户端的不同信息。校验分两种：
- 手工校验

  手工校验是指在代码里的业务校验。校验不通过可以抛出校验异常。如：`throws new ValidateException("名称已存在")`
  - 自定义校验处理器
    
      通过实现`ValidateHandler`接口，并实现处理验证异常。此处理器只能有一个，并暴露给spring。

- 自动校验

  在自动映射的字段上，加上相关的注解，从而实现自动校验。如：`@NotEmpty(message="名称不能为空")`

#### 4.1.7 系统异常处理

提供了系统异常处理，也可以自定义异常处理，实现`ExceptionHandler`接口。

## 4.2 boot-plus-web-log

集成了spring-boot-starter-actuator。

对开发环境提供了基于controller层的切面日志打印，同时提供了在线日志级别管理，访问`/log.html`

## 4.3 boot-plus-web-swagger

实现了在线API，主要实现以下功能：

* 集成swaggger
* 统一授权配置
* 每个接口添加参数配置
* 简单导出API
* 对Pageable进行参数改写
* 在线UI（/api.html）

可以配置swagger相关配置，如：

```yaml
swagger :
  title : 接口文档
  description : 这是在线生成的API文档
  version : V1.0
  termsOfServiceUrl :
  contactName : David
  contactUrl :
  contactEmail :
  license :
  licenseUrl :
  defaultIncludePattern : /swagger.*,/mvc.*    #/.* 全部url,多个URL用逗号隔开
  apiHost: localhost:8080                      #ui上测试操作的时候访问的实际url
  global:                                      #每个接口添加参数，在线API里，每个接口都会体现
    - name: Authorization
      description: 授权
      type: string
      parameterType: header
      required: false
  authorized:                                  #统一处理授权
    - name: Authorization
      in: header
  authorizedIncludePattern:                    #哪些URL需要权限
```

defaultIncludePattern： 哪些url的API会在在线文档里显示，逗号隔开

apiHost：在线测试api时实际访问的API。由于代理映射问题，有时候访问html的地址和访问API的地址是不一致的

global：每个接口添加参数，在线API里，每个接口都会体现。数组，可配置多个。

authorized：统一授权，会在需要授权的API上加锁，显示需要授权。数组，可配置多个。

authorizedIncludePattern： 哪个URL需要授权，逗号隔开

# Part5 : biz

## 5.1 boot-plus-biz-security

集成了spring scurity，但由于spring scurity比较复杂，使用起来比较繁琐，故做了一些减法，对常用的保留，对不常用的暂时去除。若需要其它功能的，请自行集成。

此次集成主要实现了以下特性：

* 可快速实现授权与鉴权，无需关注复杂的各种过滤器
* 集成JWT
* 实现Stateful Token

### 5.1.1 JWT

在Spring Scurity基本上实现无状态的JWT Token。主要实现以下功能：

* 分离授权和鉴权
* 用户可自定义扩展生成Token，校验Token
* 用户可自定义扩展登陆成功，登陆失败，登出成功，根据登陆key查用户
* 用户可自定义解析拿到Token，解析根据请求（uri）拿到资源（resource）对应的角色
* 用户可自定义鉴权失败的返回
* 登出Token黑名单功能（利用缓存，用户可自定义缓存）
* 自动刷新Token(由于是无状态的，所以在Token快失效时，返回一个新的Token)

对于其它配置可以通过以下配置来实现

```yaml
security:
  token:
    type: JWT
    ignorePatterns: "/*.ico,/css/**,/fonts/**"
    permitAllPatterns: 
    permitAllUserNames: admin
    bizUserClassName: 
    jwt:
      secret:                    # 加密串
      timeoutMin:                # Token超时, 单位：分钟
      refreshSeconds:            # Token还有多久失败时刷新Token， 单位：秒
```

type：目前支持JWT，ST

ignorePatterns： 哪些url不需要授权和鉴权，这些url拿不到上下文

permitAllPatterns：哪些url，所有用户都有权限

permitAllUserNames： 哪些用户有全部url的权限

bizUserClassName:  需要存储的业务用户类全路径名，默认是BizUser.class.getName()

jwt.secret: 加密串

jwt.timeoutMin:  Token超时, 单位：分钟

jwt.refreshSeconds: Token还有多久失败时刷新Token， 单位：秒

### 5.1.2 Stateful Token

在spring Scurity 基本上实现了有状态的token, token对应的用户信息在缓存里存储。

配置如下：

```yaml
security:
  token:
    type: ST
    ignorePatterns: "/*.ico,/css/**,/fonts/**"
    permitAllPatterns: 
    permitAllUserNames: admin
    bizUserClassName: 
```

> 两种风格只需要切换配置即可。

### 5.1.3 devlepement

* 登录url:/authentication/login

* 登出uri:/authentication/logout

* 用户扩展类`ExtensibleSecurity` `，可自定义授权与鉴权部分

  * validateToken: 校验token扩展，可自定义校验Token扩展，也可以刷新缓存期限

  * loginSuccess: 登录成功处理

  * loginFailure: 登录失败处理

  * logoutSuccess: 登出成功处理

  * loadUserByUsername: 根据用户名加载信息，包括用户名，密码。若需要鉴权的用户则需要加入角色信息

    ```java
    if(maxAuth) {
        BizUser bizUser = new BizUser(user.getUserName(), user.getPassword(), new ArrayList<>());
        return bizUser;
    } else {
        List<String> roleCodes = ....
        BizUser bizUser = new BizUser(user.getUserName(), user.getPassword(), roleCodes);
        return bizUser;
    }
    ```

  * resolveToken 解析Token

  * obtainResource 根据request获得关联的角色信息

  * authorizationFailure 鉴权失败处理



# Part6 : integration

## 6.1 boot-plus-integration

集成层是使用Mybatis实现对数据库访问。引入的mysql数据库驱动。
除了常规配置，可以自定义mapper扫描，无需额外的java配置。

```yaml
mybatis:
  mapper-locations: classpath*:com/mapping/*.xml
  type-aliases-package: com.**.domain
  mapper-scan:
    basePackages: com.**mapper
```

mapper-scan.basePackages 多个路径，逗号隔开

## 6.2 boot-plus-integration-jpa

base-plus-integration-jpa是基于mybatis实现的jpa。既实现了部分jpa的规范，又不失灵活性，也可以用传统的方式在xml或注解方式添加自己的sql。

### 6.2.1 CrudMapper自动加载SQL

传统虽然有增删改查的代码生成器，但在添加或删除字段或修改字段特别麻烦，需要把所有的sql都需要修改。有些已经生成的代码也需要手动修改，容易出错。

实现了`Mapper`的接口或实现了`Mapper`的子接口(如`CrudMapper`等)的接口可以自动了生成对应的sql statement，无需重复编写。

### 6.2.2 接口定义查询条件

如果需要根据条件进行查询，可根据jpa规范实现，无需编写sql。如：

```java
// 
List<User> findByNameAndAgeOrderByNameDesc(String name, Integer age)
```

目前提供了以下几种查询：

| 关键字                                      | 查询效果                     |
| ------------------------------------------- | ---------------------------- |
| IsBetween;Between                           | xx between val1 and val2     |
| IsNotBetween;NotBetween                     | xx not between val1 and val2 |
| IsNotNull; NotNull                          | xx is not null               |
| IsNull; Null                                | xx is null                   |
| IsLessThan; LessThan                        | xx < val                     |
| IsLessThanEqual; LessThanEqual              | xx <= val                    |
| IsGreaterThan; GreaterThan                  | xx > val                     |
| IsGreaterThanEqual; GreaterThanEqual        | xx >= val                    |
| IsBefore; Before                            | xx < val                     |
| IsAfter; After                              | xx > val                     |
| IsNotLike; NotLike                          | xx not like %val%            |
| IsLike; Like                                | xx like %val%                |
| IsStartingWith; StartingWith; StartsWith    | xx like val%                 |
| IsEndingWith; EndingWith; EndsWith          | xx like %val                 |
| IsNotContaining; NotContaining; NotContains | xx not like %val%            |
| IsContaining; Containing; Contains          | xx like %val%                |
| IsTrue; True                                | xx is true                   |
| IsFalse; False                              | xx is false                  |
| IsNot; Not                                  | xx <> val                    |
| Is; Equals                                  | xx = val                     |

排序：

```java
findByXXOrderByXXXAsc
findByXXOrderByXXXDescAndXXX
```

条件查询扩展

* ```
  find..By..
  findByNameOrDeptNo
  ```

* ```
  get..By..
  getByNameOrDeptNo
  ```

* ```
  query..By..
  queryByNameOrDeptNo
  ```

* ```
  count..By..   //查询数量
  countByNameOrDeptNo
  ```

* ```
  exists..By.. //判断是否存在
  existsByNameOrDeptNo
  ```

* ```
  delete..By..  //根据条件查询
  deleteByNameOrDeptNo
  ```

### 6.2.3 接口定义查询条件过滤

在传统jpa里，若是实用jpa规范的接口，不能根据条件不同自定义不同条件的查询。但实际使用过程中，经常有若条件是空的，则查全部的。这个时候如果还是用条件匹配是不适合的。

故为解决此问题，定义的注解`@IfTest`。如：

```java
@IfTest(notEmpty = true)
List<TestUser> findPageByNameAndAgeOrDeptNo(String name, @IfTest(notEmpty = true, conditions = {"> 0"})Integer age, String DeptNo);
```

> 参数上定义了注解，则使用参数定义的注解。参数上没有定义的，则使用方法上的注解。

### 6.2.4 面向对象关联查询

在查询时，往往会关联多表查询。但在使用jpa的时候，可以定义关联关系。通过自定义关联关系，可自动关联查询。

```java
public class User {
    @Id
    private Long id;

    //...

    @ManyToOne
    @JoinColumn(name = "deptNo", referencedColumnName = "userId")
    private Dept dept;
}
```

目前提供三种关联

* OneToOne

  ```java
  public class User {
      @Id
      private Long userId;
  
      //...
  	
      @OneToOne
      private UserInfo userInfo;
  }
  
  public class UserInfo {
      @Id
      private Long userInfoId;
      
      private Long userId;
  
      //...
  	
  }
  ```

* ManyToOne

  ```java
  public class Dict {
      @Id
      private Long dictId;
  
      //...
  
     	@OneToMany(mappedBy = "dict")
  	private List<DictVal> dictVals;
  }
  
  public class DictVal {
      @Id
      private Long dictValId;
      
      private String dictId;
  
      //...
  
     	@ManyToOne
  	private Dict dict;
  }
  ```

* ManyToMany

  ```java
  public class User {
      @Id
      private Long userId;
  
      //...
  
      @ManyToMany
  	@JoinTable(name = "user_role",
              joinColumns = @JoinColumn(name = "userId", referencedColumnName = "userId"),
              inverseJoinColumns = @JoinColumn(name = "roleId", referencedColumnName = "roleId"))
      List<Role> roles;
  }
  
  public class Role {
      @Id
      private Long roleId;
  
      //...
  
      @ManyToMany(mappedBy = "roles")
  	private List<User> users;
  }
  
  ```
  
  > 目前只有一层关联，则只是做查询关联，并未做更新关联。
  
  > 通过JoinColumn可定义关联字段
  
  > 当时ManyToMany的时候，可定义关联表JoinTable

### 6.2.5 关联查询优化

若每次查询都需要关联查询，有时候消耗较大，会影响性能（N+1问题）。我们可以通过注解来实现，哪些需要，哪些不需要从而优化部分性能。如：

```java
public class User {
	@Id
	private Long id;
	
	//...
	
	@MappedStatement(exclude = {"findById"})     //findById方法时不查询dept
	@ManyToOne
	@JoinColumn(name = "deptNo")
	private Dept dept;
}
```

MappedStatement有include(哪些需要关联)，exclude(哪些不需要关联)。若同时存在exclude，include。以exclude为准。

### 6.2.6 代码构建复杂查询

通过实现SpecificationMapper接口，可以利用Specification构建复杂条件查询

```java
// WHERE ( dept_no = ? AND ( age > ? AND name like ?) ) order by name ASC
testUserMapper.findSpecification(Specifications.and()
                .equal("deptNo", "002")
                .nested(builder -> {
                    builder.and()
                            .greaterThan("age", 18)
                            .like("name", "Jack");
                })
                .order().asc("name").build());
```

```java
//page and order
testUserMapper.findPageSpecification(page, Specifications.and()
                .equal("deptNo", "002")
                .order().asc("name").build());
```

或者自定义构建

```java
testUserMapper.findSpecification((cb, query) -> {
    PredicateExpression expression = cb.and(cb.in("deptNo", "002", "003"), cb.isNull("createTime"));
            PredicateExpression expression1 = cb.or(cb.lessThan("age", 18), expression);
            query.where(cb.equal("name", "Jackson"), expression1);
            query.orderBy(cb.asc("deptNo"), cb.desc("id"));
            return null;
        });
```



### 6.2.7 主键支持

目前支持三种主键类型：自增（IDENTITY）、序列（SEQUENCE）、UUID（32位）。可如下定义：

```java
public class TestUser {

    @GeneratedValue(GenerationType.IDENTITY)
    @Id
    private Long id;

    //...
}
```

### 6.2.8 分页排序支持

已经实现了自动物理分页。但对orderBy没有做优化（数据库本身会优化）。

在方法里传`Pagination`即可。如：

```java
List<TestUser> findPageByName(Page page, Sort sort， String name);
```

返回的total也在此对象里，拿到即可。

> 若使用传入参数排序，则不要用接口定义的方式定义排序。只会选一种。

### 6.2.9 默认值触发

默认值触发是指类似于触发器，在我们插入或更新时候指定某些字段的默认值。而不需要我们每次处理的时候去设置值。

默认值分两种：java代码、系统函数。java代码可直接定义，也可定义类和方法。系统函数分直接替换占位。

可使用@TriggerValue注解实现。如：

```java
@TriggerValue(triggers = @Trigger(triggerType = SqlCommandType.INSERT, valueType = TriggerValueType.DatabaseFunction, value = "sysdate"))
private Date createTime;

@TriggerValue(triggers = @Trigger(triggerType = SqlCommandType.UPDATE, valueType = TriggerValueType.JavaCode, valueClass = TestSeqService.class, methodName = "getDate"))
private Date updateTime;
```

### 6.2.10 Pageable入参解析。

让排序传入更简单更优雅。若集成swagger，则可以通过swagger-ui查看具体的参数信息

### 6.2.11 自定义数据库扩展

不可能实现所有的关系型数据库，故将数据库的扩展功能交给使用者。

通过实现`MybatisJpaConfigurer#addDatabase`方法，添加定制化数据库，同时需要定义分页`PaginationDialect`。若有序列生成id的方法，也需实现`KeySqlGenerator`

```java
@Override
public void addDatabase(DatabaseRegistry databaseRegistry) {
    databaseRegistry.addDatabase(DatabaseType.valueOf("CustomDatabaseId"))
        .paginationDialect(new MyPaginationDialect())
        .keySqlGenerator(new MyPaginationDialect());
}
```

### 6.2.12 自定义id生成器

现阶段生成id的方式特别多，特别是基于分布式的情况，所以提供了扩展给使用者，让使用者自定义id生成规则。

通过实现`KeyGenerator#generate`，然后在定义id的时候可以如下定义：

```java
@GeneratedValue(generatorClass = MyGenerator.class)
@Id
private String id;
```
