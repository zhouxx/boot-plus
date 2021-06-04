# CHANGELOG

## [v1.3.7] 2021.6.4

* mybatis-jpa: 分离查总数和物理分页查询。
* mybatis-jpa: 如果查总数是0，则物理分页查询忽略
* mybatis-jpa: 如果传入分页对象是null。则表示不分页
* mybatis-jpa: 查总数使用mybatis的executor查询，方便让一级缓存和二级缓存生效
* mybatis-jpa: 优化日志打印，使用mybatis的日志打印
* mybatis-jpa: in的参数支持传入数组
* web: 优化全局异常的日志打印
* web: 优化字典收集的日志打印，方便定位是哪个字段引起的字典收集
* core: BeanUtils支持更多的类型直接拷贝
* security: 解决当token失效后，不能手动logout的问题

## [v1.3.6] 2021.4.12

* mybatis-jpa: 修复bug
* mybatis-jpa: mybatis-starter升级至2.1.3
* web: 重构Jackson序列化分为值转换和值格式化，可任意定义多个转换链
* web: 可配置转换后的字段key

## [v1.3.5] 2021.3.27

* mybatis-jpa: 参数是集合时支持更多的类型（不单单是list)
* mybatis-jpa: 为集合参数支持条件型注解`@Iftest(notEmpty)`
* swagger:  修复版本升级后的bug和优化

## [v1.3.4] 2021.3.3

* mybatis-jpa: 修复了一些bug（自定义主键规则失效、只是定义关联关系未匹配上）
* core:  `BeanUtils`支持忽略源对象的部分属性

## [v1.3.3] 2021.2.25

* mybatis-jpa: 代码级查询提供了orders参数设置，更好地与前端衔接
* core:  `BeanUtils`优化，减少对象的创建
* web: 数字格式化注解`NumberFormat `扩展了属性
* web: 字典格式化注解`DictFormat `扩展了属性

## [v1.3.2] 2020.12.20

* mybatis-jpa: 修复雪花算法的时间回拨的bug
* mybatis-jpa:  给雪花算法策略之修改偏移量提供外部存储器`OffsetRepository `
* web: 给默认的全局异常处理器返回一个空的modalAndView

## [v1.3.1] 2020.12.7

* mybatis-jpa: 主键支持雪花算法
* mybatis-jpa:  select count(1)变成select count(*)
* security: 增加对默认认证的覆盖，覆盖后将拒绝所有认证
* swagger: 移除对swagger的环境控制，交由swagger本身提供的配置控制

## [v1.3.0] 2020.11.27

* quartz: 移除定时任务模式
* mybatis: 将mybatis模块合并至mybatis-jpa模块
* cache: 添加对缓存的增强模块
* springboot升级至 **2.3.5.RELEASE**  
* swagger: 依赖官方starter
* 优化配置文件提示
* 优化部分包结构