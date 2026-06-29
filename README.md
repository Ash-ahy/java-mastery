# Java Mastery v2.0 Pro

> Spring Boot 3.2 + MyBatis-Plus + Redis + RabbitMQ + Security + JWT + Knife4j + EasyExcel

## Quick Start

```batch
:: 1. 启动 MySQL / Redis
docker_start.bat

:: 2. 构建并启动应用（显式使用 docker profile）
rebuild.bat

:: 3. 只体验基础功能时，可直接使用 H2 模式启动
java -jar target\java-mastery-2.0.0.jar --spring.profiles.active=h2
```

`docker` 模式默认连接 `localhost:13306` 的 MySQL 和 `localhost:6380` 的 Redis，并支持环境变量覆盖：
`JWT_SECRET`、`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`

浏览器访问：`http://localhost:8080`

账号：`admin` / `admin123`

## Module

| 功能 | 路径 | 认证 |
|------|------|------|
| 管理后台 | `/` | ❌ |
| API 文档 | `/doc.html` | ❌ |
| 登录 | `POST /api/auth/login` | ❌ |
| 商品列表 | `GET /api/product` | ❌ |
| 购物车 | `GET /api/cart` | ✅ |
| 创建订单 | `POST /api/order` | ✅ |
| 秒杀 | `POST /api/seckill/{id}` | ✅ (`docker` profile) |
| 文件上传 | `POST /api/file/upload` | ✅ |
| Excel 导出 | `GET /api/excel/export/users` | ✅ |
| 用户管理 | `GET /api/sys/user` | ✅ (admin) |
| H2 控制台 | `/h2-console` | ❌ |

## Structure

```
src/main/java/com/mastery/
├── config/        SecurityConfig RedisConfig RabbitConfig Knife4jConfig ...
├── controller/    Auth User Product Order Cart Seckill File Excel (8个)
├── service/       impl/ (10+ 服务类)
├── entity/        User Product MallOrder CartItem UploadFile (5个)
├── mapper/        (5个 Mapper)
├── common/        Result PageResult
├── exception/     GlobalExceptionHandler
├── security/      JwtAuthenticationFilter
└── util/          JwtUtil

src/main/resources/
├── static/index.html      管理后台前端
├── application.yml        通用基础配置
├── application-h2.yml     H2 快速模式
├── application-docker.yml Docker 依赖模式
└── schema-h2.sql          12张表自动建表
```

## Profiles

| Profile | Database | Cache | MQ | Command |
|---------|----------|-------|-----|---------|
| `h2` | H2 内存 | - | - | `java -jar target\java-mastery-2.0.0.jar --spring.profiles.active=h2` |
| `docker` | MySQL 8 | Redis 7 | 可选 | `docker_start.bat` + `rebuild.bat` |

## Accounts

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ROLE_ADMIN |
| zhangsan | admin123 | ROLE_USER |
| lisi | admin123 | ROLE_USER |
