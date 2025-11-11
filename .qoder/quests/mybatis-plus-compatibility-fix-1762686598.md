# MyBatis-Plus与Spring Boot兼容性问题修复方案

## 问题描述

admin-service在Docker环境中部署失败，启动时抛出如下异常：

```
Invalid value type for attribute 'factoryBeanObjectType': java.lang.String
```

该问题源于MyBatis-Plus 3.5.x系列与Spring Boot 3.2.0版本之间的兼容性缺陷。已尝试的常规修复手段（升级MyBatis-Plus至3.5.5/3.5.7、自定义SqlSessionFactory配置、排除自动配置类、调整@MapperScan注解位置）均未能解决问题。

## 问题根源分析

### 技术层面原因

Spring Boot 3.x引入了对Spring Framework 6.0的依赖，该版本对Bean定义元数据的处理机制进行了重构。MyBatis-Plus在Bean工厂初始化阶段尝试注册SqlSessionFactory时，其内部使用的Bean元数据属性类型与Spring 6.0的期望类型不匹配，导致类型转换异常。

具体表现为：
- MyBatis-Plus尝试将factoryBeanObjectType属性设置为字符串类型
- Spring Boot 3.2.0期望该属性为Class类型或其他特定类型
- 类型校验失败触发IllegalArgumentException异常

### 版本兼容性矩阵

| Spring Boot版本 | MyBatis-Plus版本 | 兼容性状态 |
|----------------|-----------------|-----------|
| 3.2.0 | 3.5.5 | 不兼容 |
| 3.2.0 | 3.5.7 | 不兼容 |
| 2.7.18 | 3.5.3.1 | 稳定兼容 |
| 2.7.x | 3.5.x | 兼容 |

## 解决方案设计

### 方案选择：降级策略

鉴于MyBatis-Plus官方尚未发布完全兼容Spring Boot 3.2.0的稳定版本，采用降级Spring Boot版本的方案是当前最可靠的解决路径。该方案具有以下优势：

1. 技术成熟度高：Spring Boot 2.7.18为LTS长期支持版本，已经过大规模生产环境验证
2. 生态兼容性好：MyBatis-Plus 3.5.3.1与Spring Boot 2.7.x有完整的适配测试覆盖
3. 风险可控：降级路径清晰，不涉及底层架构重构

### 版本调整方案

#### 目标版本配置

| 组件 | 当前版本 | 目标版本 | 调整原因 |
|------|---------|---------|----------|
| Spring Boot | 3.2.0 | 2.7.18 | 规避兼容性问题，采用稳定LTS版本 |
| Spring Cloud | 2023.0.0 | 2021.0.8 | 与Spring Boot 2.7.x版本对齐 |
| Spring Cloud Alibaba | 2022.0.0.0 | 2021.0.5.0 | 匹配Spring Cloud版本 |
| MyBatis-Plus | 3.5.7 | 3.5.3.1 | 使用经验证的稳定版本 |

#### 依赖协调说明

降级后需确保以下依赖的版本一致性：
- Nacos客户端版本保持2.3.0不变（兼容2.7.x）
- MySQL驱动版本保持8.3.0不变
- Hutool工具库版本保持5.8.32不变
- Lombok版本保持1.18.32不变

### 配置调整策略

#### MyBatis-Plus自动配置依赖

移除所有自定义的MyBatis-Plus Bean配置类（如MybatisPlusConfig），完全依赖Spring Boot的自动装配机制。Spring Boot 2.7.x对MyBatis-Plus的自动配置支持更加成熟，可避免手动配置导致的Bean冲突。

#### Redis配置冲突处理

如果项目同时使用了Spring Data Redis和MyBatis-Plus，需要在启动类上排除RedisRepositoriesAutoConfiguration：

```
@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class})
```

该配置可防止Redis的Repository自动配置与MyBatis-Plus的Bean定义产生冲突。

### 验证策略

#### 本地环境验证

1. 清理Maven本地缓存：删除.m2/repository中的相关依赖缓存
2. 重新构建项目：执行`mvn clean package -DskipTests`
3. 启动服务验证：确认应用可正常启动，无Bean初始化异常
4. 功能测试：验证数据库连接、MyBatis-Plus CRUD操作、Nacos服务注册等核心功能

#### Docker环境验证

1. 重新构建Docker镜像：确保镜像中的JAR包使用新版本依赖
2. 启动容器验证：通过`docker-compose up -d admin-service`启动服务
3. 健康检查：访问`/actuator/health`端点确认服务健康状态
4. 日志检查：确认无版本冲突或兼容性警告日志

## 实施步骤

### 1. 修改父POM依赖版本

在`backend/pom.xml`中调整以下属性：

| 属性名 | 修改前 | 修改后 |
|-------|--------|--------|
| spring-boot.version | 3.2.0 | 2.7.18 |
| spring-cloud.version | 2023.0.0 | 2021.0.8 |
| spring-cloud-alibaba.version | 2022.0.0.0 | 2021.0.5.0 |
| mybatis-plus.version | 3.5.7 | 3.5.3.1 |

### 2. 清理MyBatis-Plus自定义配置

检查admin-service中是否存在自定义的MyBatis-Plus配置类（通常命名为MybatisPlusConfig），若存在则完全删除，依赖自动装配机制。

### 3. 检查并调整启动类注解

确保AdminServiceApplication启动类的注解配置正确：
- 保留@SpringBootApplication注解
- @MapperScan注解放置在启动类上
- 如使用Redis，添加exclude配置排除RedisRepositoriesAutoConfiguration

### 4. 清理并重新构建

依次执行以下操作：
1. 执行`mvn clean`清理构建缓存
2. 执行`mvn package -DskipTests`构建项目
3. 检查构建日志，确认依赖版本已正确更新

### 5. 容器化部署验证

1. 重新构建admin-service的Docker镜像
2. 通过docker-compose启动服务
3. 查看容器日志，确认启动成功
4. 访问健康检查端点验证服务可用性

## 风险评估与缓解

### 潜在风险

1. API兼容性风险：Spring Boot 2.7.x与3.2.0在部分API上存在差异
2. 依赖传递风险：降级可能导致间接依赖版本变化
3. 功能特性差异：3.x版本的某些新特性在2.7.x中不可用

### 缓解措施

1. 全面回归测试：对所有已实现的功能模块进行功能验证
2. 依赖冲突检查：使用`mvn dependency:tree`分析依赖树，排查版本冲突
3. 渐进式迁移：优先修复admin-service，验证稳定后再推广至其他服务
4. 备份回滚机制：保留当前代码分支，确保可快速回滚

## 成功标准

1. admin-service在本地环境可正常启动，无Bean初始化异常
2. Docker容器中admin-service启动成功，健康检查通过
3. 数据库连接正常，MyBatis-Plus的Mapper可正确执行SQL
4. Nacos服务注册成功，服务实例在Nacos控制台可见
