# Admin Service - 快速开始

## 前置条件

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.x

## 数据库初始化

```bash
# 1. 创建数据库并初始化表结构
mysql -u root -p < src/main/resources/sql/schema.sql

# 2. 导入初始化数据
mysql -u root -p < src/main/resources/sql/data.sql
```

## 本地运行

```bash
# 1. 编译打包
mvn clean package -DskipTests

# 2. 运行服务
java -jar target/admin-service-1.0.0.jar --spring.profiles.active=dev
```

## Docker部署

```bash
# 1. 构建镜像
mvn clean package -DskipTests
docker build -t admin-service:latest .

# 2. 使用docker-compose启动
cd ../../
docker-compose up -d admin-service
```

## 默认账号

- 用户名: admin
- 密码: Admin@123

## 健康检查

```bash
curl http://localhost:8081/actuator/health
```

## 技术栈

- Spring Boot 2.7.x
- MyBatis-Plus 3.5.x
- Redis
- JWT
- MySQL 8.0

## 注意事项

1. Service和Controller层需要根据业务需求实现
2. 当前框架已完成,可直接在此基础上开发
3. 配置文件中的密码和密钥请在生产环境修改
