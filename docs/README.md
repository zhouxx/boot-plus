# Boot Plus Reference Guide 1.3.x

# Part1 : 简介

​	Boot Plus 是基于spring boot 的增强，但并未修改SpringBoot已有的功能，也就是说完全可以兼容使用SpringBoot的项目。可以任意使用其中一个模块的增强，而不必引入所有模块。让spring boot的使用者更好地关注于业务。

主要增强以下几个方面：

* dynamic datasource based on aop
* cache: spring cache enhancement
* web: json serialize enhancement, cros , validation enhancement
* log management online
* swagger(api online)
* mybatis extension：like jpa, but more smart
* security integration: more simple, support JWT & Stateful Token

# Part2 :  新版本特性

从1.3.0版本开始，做了一些模块上的重构：

* 移除了quartz的增强，你可以继续使用原来的版本，也可以使用xxl-job，它对使用者更友好
* 将mybatis的增强模块合并至mybatis-jpa，让使用者更简化使用
* 添加了对spring cache的增强，spring cache为我们提供了很好的缓存抽象，但未提供cache和tti和ttl配置，在此提供了基于caffeine和基于redis的tti、ttl配置增强
* 同时也升级了spring boot 版本号至 **2.3.5.RELEASE** ，与 spring cloud  **Hoxton.SR9** 的版本号保持一致
* 引用了官方的swagger starter 3.0.0

# Part3 : 起步

## 3.1 系统要求

Boot Plus 1.3.x 至少要求java1.8，Spring Boot 2.3.5.RELEASE.

## 3.2 Maven 依赖

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alilitech</groupId>
            <artifactId>boot-plus-dependencies</artifactId>
            <version>1.3.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 3.3 开发第一个应用

### 3.3.1 创建pom

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
            <groupId>com.alilitech</groupId>
            <artifactId>boot-plus-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alilitech</groupId>
            <artifactId>boot-plus-web</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.alilitech</groupId>
                <artifactId>boot-plus-dependencies</artifactId>
                <version>1.3.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

</project>
```

你可以通过IDE指定启动类：`com.AppStart`来启动项目，也可以通过自定义启动类启动项目。

### 3.3.2 创建一个可执行的jar

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

# Part4 : boot-plus-core

提供了基于spring的依赖（没有web）。提供了以下功能：

* 启动类`com.AppStart`：启动时更优雅地打印更多信息
* 工具类：`BeanUtils`：更高效的深度拷贝。父类属性，聚合属性都可一并拷贝。常用于DTO与Entity之间的对象拷贝，提高开发效率。

# Part5 boot-plus-routing-datasource

动态数据源是指在项目里可以配置多个数据源，并且可以在不同的地方指定使用不同的数据源。

此多数据源的实现未依赖于任何一个持久化的框架，所以一般于用于多库多service。想在同一个事务里操作多个数据源暂时无法实现。

## 5.1 配置多数据源

主数据源还和原来的方式一样，当未指定数据源或找不到指定的数据源时使用主数据源。主数据源的标识是`default`。
其它数据源标识要以`ds`开头，否则无法识别。配置方式和spring boot的方式一样。    

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
@DynamicSource("ds-second")
public void exeService() {
    
}
```

## 5.2 加密密码解析

部分在配置文件里的密码是加密的，但在连数据的时候需要明文。所以提供了`EncryptPropertyResolver`来解析。

```java
@Component
public class MyEncryptPropertyResolver implements EncryptPropertyResolver {
    @Override
    public boolean supportResolve(String key, String value) {
        return key.equals("spring.datasource.ds-second.password");
    }

    @Override
    public String resolve(String value) {
        return value;
    }
}
```

## 5.3 动态添加/移除数据源

代码里可以动态地创建数据源，并添加到多数据源里。

我们可以拿到数据源对象，判断是否是`DefaultDynamicDataSource`此类，可以调用此类的`addDataSource`方法。一般用于数据源实时动态修改切换。

```java
//add datasource
defaultDynamicDataSource.addDataSource(datasourceName, datasourceUrl, datasourceUsername, datasourcePassword);

//remove datasource
defaultDynamicDataSource.remove(datasourceName);
```

# Part6 boot-plus-web

## 6.1 跨域支持

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
    exposedHeaders: 
      - message
```

* enabled： 是否启用跨域
* path： 哪些URL可以跨域
* allowedOrigins： 哪些域名可以请求跨域，逗号隔开
* allowedMethods： 哪些方法可以跨域 POST, GET, PUT, DELETE, OPTIONS
* allowCredentials
* maxAge
* exposedHeaders：跨域时哪些头部信息返回

## 6.2 json序列化之null值处理

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

若是对于同一个对象需要在不同线程里实现不同的效果，比如查看详情和修改详情对于null值处理不一样，使用如下：

```java
DefaultNullContextHolder.set(false);   //关闭此次请求/线程对null值序列化的处理
```

## 6.3 json序列化之数字格式化

业务上有很多逻辑运算，运算完了会产生结果，往往不同类型(如int、double)的数据返回的格式不一样，但实际上展示的时候需要统一保留两位或统一格式化。使用`@NumberFormat`注解：

```java
@NumberFormat(pattern = "#,###,##0.00")
private BigDecimal amount;
```

* pattern: 格式化格式，若使用了此样式，通过`DecimalFormat.format(pattern)`去格式化
* scale: 保留位数，默认是2
* round: 取舍模式，默认4舍五入，参考`BigDecimal`里的常量

## 6.4 json序列化之字典

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

## 6.5 流处理

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

## 6.6 参数校验

### 6.6.1 校验

校验是指对客户端请求的参数格式或逻辑校验，根据校验校验结果返回给客户端的不同信息。校验分两种：

- 手工校验

  手工校验是指在代码里的业务校验。校验不通过可以抛出校验异常。如：`throws new ValidateException("名称已存在")`
  
- 自动校验

  在自动映射的字段上，加上相关的注解，从而实现自动校验。如：`@NotEmpty(message="名称不能为空")`

### 6.6.2 自定义校验处理器

​	通过实现`com.alilitech.web.validate.ValidateHandler`接口，并实现处理验证异常。此处理器只能有一个，并暴露给spring。

## 6.7 系统异常处理

框架提供了全局默认异常处理：打印异常信息，在头部信息里返回`服务器内部错误`

也可以自定义异常处理，实现`com.alilitech.web.exception.ExceptionHandler`接口，并交给spring管理。

# Part7 boot-plus-log

集成了spring-boot-starter-actuator。

## 7.1 对Controller进行切面控制

```yaml
logging:
  aspect: true
```

可以控制开启切面是否开启，同时提供了切面控制扩展，可以打印日志或记录日志。

```java
@Service
public class LogOptService implements LogExtension {
    @Override
    public void beforeEnter(Signature signature, Object[] objects) {
        //....
    }
    @Override
    public Object afterExit(Signature signature, Object result) {
        //....
    }
}
```

## 7.2 管理日志级别

生产环境或测试环境有时候需要debug日志，等调试完后又关闭。访问`/log.html`在线管理日志级别。注意加了安全控制需要给相关人员赋于权限。相关的url包括`GET /management/logs`,`PUT /management/logs`

# Part8 boot-plus-swagger

实现了在线API，主要实现以下功能：

* 集成swaggger
* 统一授权配置
* 每个接口添加参数配置
* 简单导出API
* 对Pageable进行参数改写
* 在线UI（/api.html）

可以配置swagger相关配置，如：

```yaml
swagger:
  title: 接口文档
  description: 这是在线生成的API文档
  version: V1.0
  termsOfServiceUrl:
  contactName : David
  contactUrl: 
  contactEmail: 
  license:
  licenseUrl:
  defaultIncludePattern: 
    - "/api/.*"
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
  authorizedIncludePattern:
    - "/api/.*"
```

defaultIncludePattern： 哪些url的API会在在线文档里显示

apiHost：在线测试api时实际访问的API。由于代理映射问题，有时候访问html的地址和访问API的地址是不一致的

global：每个接口添加参数，在线API里，每个接口都会体现。数组，可配置多个。

authorized：统一授权，会在需要授权的API上加锁，显示需要授权。数组，可配置多个。

authorizedIncludePattern： 哪个URL需要授权，逗号隔开

> 在实际开发过程中如果有安全控制，也要将以下url分配相关权限：`GET /swagger-resources/**`, `GET /swagger-ui/**`, `GET /v2/**`

# Part9 boot-plus-security

集成了spring scurity，但由于spring scurity比较复杂，使用起来比较繁琐，故做了一些减法，对常用的保留，对不常用的暂时去除。若需要其它功能的，请自行集成。

此次集成主要实现了以下特性：

* 可快速实现授权与鉴权，无需关注复杂的各种过滤器
* 集成JWT
* 实现Stateful Token（有状态Token）
* 支持多因素认证
* 支持本地缓存和分布式缓存一键切换

> 注意：由于security模块引入spring cache，所以一定要配置cache名称：
>
> ```yaml
> spring:
>   cache:
>     cache-names:
>       - security
> ```

## 9.1 JWT

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
    ignorePatterns: 
      - pattern: "/*.ico"
        method: GET
      - pattern: "/css/**"
      - pattern: "/fonts/**"
    permitAllPatterns: 
    permitAllUserNames: 
      - admin
    bizUserClassName: 
    authenticationPrefix: "/authentication"
    jwt:
      secret:                    # 加密串
      timeoutMin:                # Token超时, 单位：分钟
      refreshSeconds:            # Token还有多久失败时刷新Token， 单位：秒
```

type：目前支持JWT，ST

**ignorePatterns： 哪些url不需要授权和鉴权，这些url拿不到上下文**

**permitAllPatterns：哪些url，所有用户都有权限**

**permitAllUserNames： 哪些用户有全部url的权限**

**authenticationPrefix: 认证（登录、登出）uri前缀**

bizUserClassName:  需要存储的业务用户类全路径名，默认是BizUser.class.getName()

jwt.secret: 加密串

jwt.timeoutMin:  Token超时, 单位：分钟

jwt.refreshSeconds: Token还有多久失败时刷新Token， 单位：秒

## 9.2 Stateful Token（有状态token)

在spring Scurity 基础上实现了有状态的token, token对应的用户信息在缓存里存储。

配置如下：

```yaml
security:
  token:
    type: ST
    ignorePatterns: 
      - pattern: "/*.ico"
        method: GET
      - pattern: "/css/**"
      - pattern: "/fonts/**"
    permitAllPatterns: 
    permitAllUserNames: 
      - admin
    bizUserClassName: 
    authenticationPrefix: "/authentication"
```

> 两种风格只需要切换配置即可。

## 9.3 如何开发

* 登录url: ${authenticationPrefix}/login

* 登出uri: ${authenticationPrefix}/logout

* 开发者扩展类`ExtensibleSecurity` ，可自定义认证授权与鉴权部分

  * validTokenExtension: 校验token扩展，可自定义校验Token扩展，也可以刷新缓存期限

  * loginSuccess: 登录成功处理

  * loginFailure: 登录失败处理

  * logoutSuccess: 登出成功处理

  * loadUserByUsername: 

    为用户名和密码方式的认证提供方法。
  
    根据用户名加载信息，包括用户名，密码。若需要鉴权的用户则需要加入角色信息
  
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

  * resolveToken 根据请求解析Token
  
  * obtainResource 根据request（资源）获得关联的角色信息，以验证此用户是否有权限
  
  * authorizationFailure 鉴权失败处理
  
  * addVirtualFilterDefinitions 
  
    我们可能有这样的需求，一个认证请求会同时验证好多数据，比如登陆时附带验证码的时候，我们需要先验证验证码，再验证用户名和密码（多因素认证，不是多次认证）。我们可以定义多个`VirtualFilterDefinition`，如：
  
    ```java
    virtualFilterDefinitions.add(VirtualFilterDefinition.get().supportedPredicate((servletRequest, servletResponse) -> {
        		// 此filter是否支持验证判断
                AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/authentication/login", "POST");
                RequestMatcher.MatchResult matcher = requestMatcher.matcher((HttpServletRequest) servletRequest);
                return matcher.isMatch();
            }).authenticationFunction((servletRequest, servletResponse) -> {
        		// 如何验证，验证失败时可以抛出AuthenticationException
                String code = servletRequest.getParameter("code");
                if("1".equals(code)) {
                    return null;
                }
                throw new UsernameNotFoundException("验证码错误");
            }).endAuthentication((authentication, servletRequest, servletResponse) -> {	
        		// 此filter认证成功后，是否结束整个流程的认证
                return false;
            }).alias("验证码验证"));  //别名，在日志里会看到经过了哪些filter
    ```
  
    > 这些定义的filter会在用户名和密码认证的filter之前。用户名和密码的认证filter，只支持post请求。
    >
    > `VirtualFilterDefinition` 提供了构建Authentication的方法
  
  * authenticationExtension 授权原始框架扩展，默认情况下实现以下功能
  
    * 开启跨域支持
    * session管理禁用
    * csrf 禁用
  
  * authorizationExtension 鉴权原始框架扩展，默认情况下实现以下功能
  
    * 开启跨域支持
    * session管理禁用
    * csrf 禁用
    * anonymous 禁用
    * securityContext 禁用
    * requestCache 禁用
  
  > 对于`authenticationExtension ` 和`authorizationExtension `原始默认实现功能不符合要求，可完全覆盖默认实现

# Part10 boot-plus-mybatis-jpa

mybatis本身为我们做了很多事情，又不失灵活性，我们可以完全自主的方便的自定义sql。但有些通用的表数据增删改查也要我们来提供sql。为此市面上出了很多generator，代码生成器可以帮我们解决部分需求，但在表字段增加、修改、删减后又要重新执行一遍。如果我们已经有很多定制的开发了，则无法重新执行sql生成。为此我们为mybatis提供了增强，保留了原来的灵活性，更为开发者减少了工作量。由于此次增强实现了部分JPA规范，固命名为mybatis-jpa。

## 10.1 文件配置mapper扫描路径

首先增强了mybatis配置，传统的mybatis都需要搞个configuration类，然后写个`@mapperScan`, 感觉多此一举，这样也不能用统一的启动类。所以定义了在配置文件里配置mapper扫描

```yaml
mybatis:
  mapper-locations: classpath*:com/mapping/*.xml
  type-aliases-package: com.**.domain
  mapper-scan:
    basePackages: "com.**mapper,org.**mapper"     
```

mapper-scan.basePackages 多个路径，逗号隔开

## 10.1 Crud自动加载SQL

通用的crud再也不用我们自己手写或生成sql了，框架为我们提供了自动装载sql。

实现了`Mapper`的接口或实现了`Mapper`的子接口(如`CrudMapper`等)的接口可以自动了生成对应的sql statement，无需重复编写。举例：

定义mapper

```java
// 泛型一定要定义，第一个指实体类的类型，第二指主键类型
public interface UserMapper extends CrudMapper<User, Long> {
    
}
```

定义实体类

```java
@Table(name = "t_user")  //表名
public class User {
    
    @Id  										//指定主键
    @GeneratedValue(GenerationType.IDENTITY)    //指定主键生成的规则
    private Long userId;

    //...
    
    // 字段名会自动映射为库里的dept_no
    private String deptNo;

    //也可强制指定表里的字段名称
    @Column(name = "memo1")
  	private String memo1;
}
```

## 10.2 方法名称定义查询

如果需要根据条件进行查询，可根据jpa规范实现，无需编写sql。如：

```java
/**
 * 框架为你装载如下sql：select ... from xxx where name = #{name} and age = #{age} order by name desc
 * 您只需要定义方法名称，无需写sql
 */
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
| In(传入list)                                | xx in (?, ?, ? ....)         |

排序：

```java
findByXXOrderByXXXAsc
findByXXOrderByXXXDescAndXXX
```

查询条件扩展

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

## 10.3 方法名称定义查询（动态条件）

在传统jpa里，若是实用jpa规范的接口，不能根据条件不同自定义不同条件的查询。但实际使用过程中，经常有若条件是空的，则查全部的。这个时候如果还是用条件匹配是不适合的。

故为解决此问题，定义的注解`@IfTest`。举例：

```java
/**
 * 
 * 此注解相当于mybatis里的动态sql标签
 * <if test="name != null and name != ''">and name = #{name}</if>
 * <if test="age > 0">and age = #{age}</if>
 * <if test="deptNo != null and deptNo != ''">and dept_no = #{deptNo}</if>
 */
@IfTest(notEmpty = true)
List<TestUser> findPageByNameAndAgeOrDeptNo(String name, @IfTest(notEmpty = true, conditions = {"> 0"})Integer age, String DeptNo);
```

> 参数上定义了注解，则使用参数定义的注解。参数上没有定义的，则使用方法上的注解。
>
> `notEmpty`表示 `xx != null and  xx != ''`，`notNull`表示`xx != null`

## 10.4 自动关联查询

在查询时，往往会关联多表查询。但在使用jpa的时候，可以定义关联关系。通过自定义关联关系，可自动关联查询。mybatis-jpa也提供了关联查询，您只需要在实体类上定义关联关系即可，举例如下：

```java
@Table(name = "t_user")
public class User {
    
    @Id
    private Long userId;

    //...
    
    private String deptNo

    @ManyToOne
    @JoinColumn(name = "deptNo", referencedColumnName = "deptNo")
    private Dept dept;
}

@Table(name = "t_dept")
public class Detp {
    
    @Id
    private Long deptId;
    
    private String deptNo;
    
    //...
    
    @OneToMany(mappedBy = "dept")
    private List<User> users;
}

```

目前提供三种关联

* OneToOne

  ```java
  public class User {
      @Id
      private Long userId;
  
      //...
  	
      //User表的userId和UserInfo表的userId一对一关联
      @OneToOne
      @JoinColumn(name = "userId", referencedColumnName = "userId")
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
  	
      //Dict表的doctId与DictVal表里的dictId关联
     	@OneToMany(mappedBy = "dict")
  	private List<DictVal> dictVals;
  }
  
  public class DictVal {
      @Id
      private Long dictValId;
      
      private String dictId;
  
      //...
  	
      //DictVal的dictId与Dict主键关联
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
  
      //定义中间表user_role
      //定义User表的userId与中间表的userId字段关联
      //定义Role表的roleId与中间表的roleId字段关联
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

## 10.5 关联查询优化

若每次查询都需要关联查询，有时候消耗较大，会影响性能（N+1问题）。我们可以通过注解来实现，哪些需要，哪些不需要从而优化部分性能。如：

```java
public class User {
	@Id
	private Long id;
	
	//...
    
    private String deptNo
	
	@MappedStatement(exclude = {"findById"})     //findById方法时不关联查询Dept
	@ManyToOne
	@JoinColumn(name = "deptNo", referencedColumnName = "deptNo")
	private Dept dept;
}
```

MappedStatement有include(哪些需要关联)，exclude(哪些不需要关联)。若同时存在exclude，include。以exclude为准。

## 10.6 关联查询(子查询)自定义

子查询时有时候需要定义排序，或者需要自定义部分字段过滤，比如有些删除是逻辑删除，子查询的时候我们不希望把已经删除的查出来。参考：

```java
public class User {
	@Id
	private Long id;
	
	//...
	
    private String deptNo;
    
    // 关联查Dept的时候加条件 deleted = 0 order by dept_no
	@ManyToOne
	@JoinColumn(name = "deptNo")
    @SubQuery(
            predicates = @SubQuery.Predicate(property = "deleted",condition = "= 0"),
            orders = @SubQuery.Order(property = "deptNo"))   
	private Dept dept;
}
```

通过注解`@SubQuery` 可实现自定义子查询

## 10.7 代码构建复杂查询

如果一直通过Mapper的方法来直接定义查询条件和排序，有时候会让方法变地太长，这样对于维护其实是不利的，我们提供了代码级构造复杂查询，通过继承`SpecificationMapper`接口，可以利用`Specification`构建复杂条件查询。参考：

```java
// WHERE ( dept_no = ? AND ( age > ? AND name like ?) ) order by name ASC
userMapper.findSpecification(Specifications.and()
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
userMapper.findSpecification((cb, query) -> {
    PredicateExpression expression = cb.and(cb.in("deptNo", "002", "003"), cb.isNull("createTime"));
            PredicateExpression expression1 = cb.or(cb.lessThan("age", 18), expression);
            query.where(cb.equal("name", "Jack"), expression1);
            query.orderBy(cb.asc("deptNo"), cb.desc("id"));
            return null;
        });
```

## 10.8 主键支持

目前支持四种主键类型：自增（IDENTITY）、序列（SEQUENCE）、UUID（32位），SNOWFLAKE（雪花）。可如下定义：

```java
public class User {

    @GeneratedValue(GenerationType.IDENTITY)
    @Id
    private Long id;

    //...
}
```

### 10.8.1 雪花算法主键

关于雪花算法的主键配置如下：

```yml
mybatis:
  snowflake:
    group-id: 1                         # 组id
    worker-id: 1					    # 工作id
    time-callback-strategy: waiting     # 时间回拨策略（等待，备用工作id，修改偏移量）
    max-back-time: 1                    # 最大可回拨时间，超出则报异常，表示生成id失败
    extra-worker-id: 2                  # 备用工作id 只有回拨策略是备用工作id时，必须配置此属性
    offset:                             # 默认时间偏移量
```

雪花算法的主键为时间回拨提供了3种处理方式

* WAITING 

  等待回拨时间，此策略适用于客户端允许时间等待的情况

* 备用workerId

  如果workerId有多余的情况，可以启用备用id。备用id只是在时间段重叠部分换了备用workerId，这样备用workerId变成了主workerId，原来的主workerId变成备用workerId。注意此策略不适应用于回拨时间段在同一区间的，若两次回拨的时间区间重叠了，这时候两个workerId都被使用过了，会造成主键重复

* 修改偏移量

  我们设计时间偏移量，在取得当前时间后，会减去偏移量，从而达到较小数值，从而让ID的生成时间更久。时间回拨，无非就是让最终生成的ID变小了，导致ID重复，我们可以考虑修改偏移量来达到ID变大。让偏移量减去回拨时间区间，从而让最终的ID变成和原来的轨迹。

  > 注意，时间偏移量的修改只是在内存中修改了。如果项目重启了，请确认项目的重启间隔大于回拨时间。

  由于现在很多项目使用云原生，会导致项目不间断，也就是说没有重启间隔时间。我们提供了外部存储时间偏移量接口`OffsetRepository`,在实现接口后让spring管理，示例如下：

  ```java
  @Component
  public class MyOffsetRepository implements OffsetRepository, EnvironmentAware {
  
      private Environment env;
  
      @Autowired
      private RedisTemplate<Object, Object> redisTemplate;
  
      @Override
      public boolean saveOffset(Class<?> entityClass, Long offset) {
          // 由于是多应用，我们可以采用应用名称来区分不同项目，也可以采用`groupId+workerId`来区分
          String applicationName = env.getProperty("spring.application.name");
          redisTemplate.opsForValue().set(applicationName + "." + entityClass.getName(), offset);
          return true;
      }
  
      @Override
      public Long getOffset(Class<?> entityClass) {
          String applicationName = env.getProperty("spring.application.name");
          Object o = redisTemplate.opsForValue().get(applicationName + "." + entityClass.getName());
          return (Long) o;
      }
  
      @Override
      public void setEnvironment(Environment env) {
          this.env = env;
      }
  }
  
  ```

### 10.8.2 自定义主键生成规则

现阶段生成id的方式特别多，特别是基于分布式的情况，所以提供了扩展给使用者，让使用者自定义id生成规则。

通过实现`KeyGenerator#generate`，然后在定义id生成规则的时候指定generatorClass：

```java
// 定义主键生成类
public class MyGenerator implements KeyGenerator {
    @Override
    public String generate() {
        return System.currentTimeMillis() + "";
    }
}
```

```java
// 指定用这个类生成这个主键，在插入的时候会自动调用此类的generate方法
@Id
@GeneratedValue(generatorClass = MyGenerator.class)
private String id;
```

## 10.9 分页排序支持

已经实现了自动物理分页。

在方法里传`Page`， `Sort`即可。如：

```java
List<TestUser> findPageByName(Page page, Sort sort， String name);
```

返回的total也在此对象里，拿到即可。

排序传入`Sort`对象，`Sort`对象可以构造多个排序

* 如果只是分页，不查询总数，可设置`page.setSelectCount(false)`
* 为应对多数据源，可手动设置DatabaseType：`page.setDatabaseType(databaseType)`
* 如果想自动根据不同数据源设置不同的type，可在配置里设置`mybatis.page.autoDialect=true`

注意：

> 若使用传入参数排序，则不要用接口定义的方式定义排序。只能选一种。
>
> 分页中，只对框架生成的sql提供了count sql优化，自定义的sql暂未做解析优化

## 10.10 默认值触发

默认值触发是指类似于触发器，在我们插入或更新时候指定某些字段的默认值。而不需要我们每次处理的时候去设置值。

默认值分两种：java代码、系统函数。java代码可直接定义，也可定义类和方法。系统函数分直接替换占位。

可使用@TriggerValue注解实现。如：

```java
@TriggerValue(triggers = @Trigger(triggerType = SqlCommandType.INSERT, valueType = TriggerValueType.DatabaseFunction, value = "sysdate"))
private Date createTime;

@TriggerValue(triggers = @Trigger(triggerType = SqlCommandType.UPDATE, valueType = TriggerValueType.JavaCode, valueClass = TestSeqService.class, methodName = "getDate"))
private Date updateTime;
```

## 10.11 自定义数据库扩展

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

## 10.12 Pageable入参解析

让排序传入更简单更优雅。若集成swagger，则可以通过swagger-ui查看具体的参数信息

## 10.13 MybatisJpaStartedEvent

提供一个Event，在jpa加载完成后发布一个事件。可以利用此事件执行一些后置方法。

比如我们需要在框架启动后去调用jpa构建的statement，不然会报找不到statement的异常

# Part11 boot-plus-generator

这是一个maven插件，方便生成实体类和mapper，使用方式如下：

* 在需要生成代码的项目或模块里，定义一个xml文件`generate.xml`：

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <config>
      <datasource>
          <url>jdbc:mysql://localhost:3306/test?characterEncoding=utf-8</url>
          <driverName>com.mysql.jdbc.Driver</driverName>
          <username>root</username>
          <password>root</password>
      </datasource>
  
      <properties>
          <projectPath>src/main/java</projectPath>
          <packageName>com.alilitech</packageName>
      </properties>
  
      <tables>
          <table tableName="t_user" domainName="User" />
          <!-- 可以多个table -->
      </tables>
  </config>
  ```

​		将此文件置于classpath下，注意此项目本身要有相关的数据库连接驱动，否则无法连接数据库。

* 配置插件

  ```xml
  <build>
          <plugins>
              <plugin>
                  <groupId>com.alilitech</groupId>
                  <artifactId>boot-plus-generator</artifactId>
                  <version>1.2.0</version>
                  <dependencies>
                      <dependency>
                          <groupId>com.alilitech</groupId>
                          <artifactId>boot-plus-mybatis-jpa</artifactId>
                          <version>1.3.0</version>
                      </dependency>
                  </dependencies>
              </plugin>
          </plugins>
  </build>
  ```

* 运行插件

# Part12 boot-plus-cache

spring cache为我们提供了多个缓存的抽象，我们只要调用cacheManager就可以操作不同的缓存，但并未提供缓存的tti、ttl的抽象。本模块致力于提供缓存的tti、ttl抽象。目前支持caffeine和redis。这样我们在本地开发的时候可以使用caffeine，不需要依赖redis。上生产可以切换成基于redis分布式缓存。

操作缓存的类`CacheTemplate`。
