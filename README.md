# Norvo MVP

Norvo 是一个“本地社区 / 邻里社交 / 分类信息平台”的 MVP 骨架工程。当前版本聚焦于本地开发可运行、结构清晰、方便后续扩展。

## 1. 项目结构

```text
Norvo
├── docker-compose.yml
├── sql
│   └── init.sql
├── blocksy-server                  # Spring Boot 3 + Java 17 后端
├── blocksy-app                     # uni-app + Vue3 + TS + Pinia 用户端骨架
└── blocksy-admin                   # Vue3 + Vite + TS + Pinia + Element Plus 管理后台骨架
```

## 2. 本地依赖服务启动

```bash
docker compose up -d
```

包含服务：

- PostgreSQL: `localhost:5432`，DB=`blocksy`，User=`blocksy`，Password=`blocksy123`
- Redis: `localhost:6379`
- MinIO API: `http://localhost:9000`
- MinIO Console: `http://localhost:9001`，User=`admin`，Password=`Admin@123456`

说明：

- `sql/init.sql` 会在 PostgreSQL 首次初始化时自动执行。
- MinIO bucket `blocksy-media` 会在后端启动时自动检查并创建。
- 如果你已经启动过旧版本数据库，需要重建 PostgreSQL 数据卷以应用最新表结构：

```bash
docker compose down -v
docker compose up -d
```

### 2.1 服务连通性验证

```bash
docker compose ps
docker compose exec -T blocksy-postgres psql -U blocksy -d blocksy -c "SELECT 1;"
docker compose exec -T blocksy-redis redis-cli ping
curl -f http://localhost:9000/minio/health/live
```

验证 MinIO bucket（`blocksy-media`）：

```bash
docker run --rm --entrypoint /bin/sh minio/mc -c \
"mc alias set local http://host.docker.internal:9000 admin Admin@123456 >/dev/null && mc ls local"
```

## 3. 后端启动（blocksy-server）

要求：

- JDK 17+
- Maven 3.9+

启动：

```bash
cd blocksy-server
mvn spring-boot:run
```

默认端口：`8080`

核心配置文件：`blocksy-server/src/main/resources/application.yml`

Swagger / OpenAPI（后端启动后）：

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

后端构建检查：

```bash
cd blocksy-server
mvn -DskipTests package
```

可通过环境变量覆盖：

- `BLOCKSY_DB_HOST` / `BLOCKSY_DB_PORT` / `BLOCKSY_DB_NAME` / `BLOCKSY_DB_USERNAME` / `BLOCKSY_DB_PASSWORD`
- `BLOCKSY_REDIS_HOST` / `BLOCKSY_REDIS_PORT`
- `BLOCKSY_MINIO_ENDPOINT` / `BLOCKSY_MINIO_PUBLIC_ENDPOINT` / `BLOCKSY_MINIO_ACCESS_KEY` / `BLOCKSY_MINIO_SECRET_KEY` / `BLOCKSY_MINIO_BUCKET`
- `BLOCKSY_JWT_SECRET` / `BLOCKSY_JWT_EXPIRE_SECONDS`
- `BLOCKSY_ADMIN_USERNAMES`（后台管理员用户名白名单，逗号分隔，默认 `demo`）

`application-dev.yml` MinIO 配置项：

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: admin
  secret-key: Admin@123456
  bucket-name: blocksy-media
  public-url: http://localhost:9000
```

## 4. MVP 接口骨架

统一前缀：`/api`

- `POST /api/auth/login`
- `GET /api/users/me`
- `GET /api/users/communities`
- `PUT /api/users/community`
- `GET /api/posts`
- `GET /api/posts/mine`
- `GET /api/posts/{id}`
- `POST /api/posts`
- `GET /api/comments?postId=`
- `POST /api/comments`
- `GET /api/communities`
- `GET /api/listings`
- `POST /api/listings`
- `GET /api/events`
- `POST /api/events`
- `POST /api/events/{id}/signup`
- `GET /api/notifications`
- `POST /api/reports`
- `GET /api/admin/reports`
- `POST /api/admin/reports/{id}/handle`
- `GET /api/admin/users`
- `POST /api/admin/users/{id}/ban`
- `POST /api/admin/users/{id}/unban`
- `GET /api/admin/communities`
- `POST /api/admin/communities`
- `POST /api/files/upload`

### 4.3 后端联调快速验证（按 MVP 顺序）

1. 登录拿 token

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"blocksy123"}'
```

2. 上传文件（MinIO）

```bash
curl -X POST "http://localhost:8080/api/files/upload" \
  -F "file=@/tmp/test.txt"
```

3. 发帖

```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"communityId":1,"content":"MVP 联调帖子","media":[]}'
```

4. 帖子列表与详情

```bash
curl "http://localhost:8080/api/posts"
curl "http://localhost:8080/api/posts/{id}"
```

5. 评论

```bash
curl -X POST "http://localhost:8080/api/comments" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"postId":1,"content":"评论测试"}'
```

第二阶段已接入：

- 登录使用数据库 `users` 表校验（BCrypt 密码）
- `GET /api/users/me` 读取当前 JWT 用户
- `GET /api/posts` 从数据库读取帖子+图片
- `POST /api/posts` 支持发帖时同时绑定图片

V1.5 第一阶段已接入：

- 社区归属：`GET /api/users/communities`、`PUT /api/users/community`
- 帖子社区筛选：`GET /api/posts?communityId=`
- 我的帖子：`GET /api/posts/mine?communityId=`
- 评论列表：`GET /api/comments?postId=`
- 举报处理后台能力：`GET /api/admin/reports`、`POST /api/admin/reports/{id}/handle`
- 用户封禁后台能力：`POST /api/admin/users/{id}/ban`、`POST /api/admin/users/{id}/unban`
- 社区管理后台能力：`GET/POST /api/admin/communities`
- 封禁用户 token 自动失效（JWT 过滤器按用户状态校验）

第三阶段已接入：

- 分类信息模块查库版骨架（`GET /api/listings`、`POST /api/listings`）
- 活动模块查库版骨架（`GET /api/events`、`POST /api/events`）
- 管理后台 `listing/event` 页面联调后端接口并展示基础列表

MinIO 上传示例：

```bash
curl -X POST "http://localhost:8080/api/files/upload" \
  -F "file=@/path/to/demo.png" \
  -F "bizPath=common"
```

返回字段至少包含：

- `bucket`
- `objectKey`
- `url`
- `size`
- `originalFilename`
- `contentType`

MinIO 控制台：

- `http://localhost:9001`（admin / Admin@123456）

如何确认上传成功：

1. 调用上传接口返回 `code=0` 且包含 `objectKey/url`
2. 打开 MinIO 控制台，进入 `blocksy-media` bucket
3. 按返回的 `objectKey` 路径可看到上传对象

帖子图片联调流程（MVP）：

1. 调 `POST /api/files/upload` 上传图片，拿到 `objectKey/url/size`
2. 调 `POST /api/posts` 携带 `media` 数组创建帖子

`POST /api/posts` 请求示例：

```json
{
  "communityId": 1,
  "content": "周末有人一起骑行吗？",
  "media": [
    {
      "objectKey": "8e05f1f6-demo.png",
      "url": "http://localhost:9000/blocksy-media/8e05f1f6-demo.png",
      "size": 24567
    }
  ]
}
```

## 4.1 默认测试账号（自动初始化）

后端启动后若 `users` 表没有 `demo` 用户，会自动创建：

- username: `demo`
- password: `blocksy123`

登录接口：

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"blocksy123"}'
```

鉴权规则（当前）：

- 放行：`/api/auth/**`、`/api/files/**`、`GET /api/posts/**`、`GET /api/listings`、`GET /api/events`、`GET /api/communities`、Swagger 文档路径
- 其余接口默认需要 `Authorization: Bearer <token>`

## 4.2 数据库表（MVP）

`sql/init.sql` 当前包含以下核心表：

- 用户与认证：`users`、`user_profile`、`user_community`
- 社区：`communities`
- 帖子：`posts`、`post_media`、`post_comments`
- 分类信息：`listings`
- 活动：`events`、`event_signups`
- 通知：`notifications`
- 举报：`reports`

## 5. 前端项目启动

### 5.1 用户端（blocksy-app）

```bash
cd blocksy-app
npm install
npm run dev:h5
```

`blocksy-app` H5 访问地址：

- 本机：`http://localhost:5174/#/pages/login/index`
- 局域网：`http://<你的电脑IP>:5174/#/pages/login/index`

说明：

- 如果你之前打开 `http://localhost:5173` 看到空白，通常是端口错了（5173 常被 admin 占用）或旧依赖缓存导致。
- 当前已固定 app 端口为 `5174`，并修复了 `App.vue` 入口和 Vue/Pinia 依赖兼容问题。

用户端最小联调范围（已接线）：

- 页面：`pages/login`、`pages/home`、`pages/post`、`pages/mine`
- API：`src/api/auth.ts`、`src/api/user.ts`、`src/api/post.ts`、`src/api/file.ts`
- Store：`src/store/user.ts`

联调顺序：

1. 登录：`POST /api/auth/login`
2. 获取当前用户：`GET /api/users/me`
3. 获取帖子列表：`GET /api/posts`
4. 上传图片：`POST /api/files/upload`
5. 发布帖子：`POST /api/posts`
6. 发布评论：`POST /api/comments`

V1.5 用户端新增页面：

- `pages/community/index`：社区选择页
- `pages/my-post/index`：我的帖子页

V1.5 用户端新增能力：

- 首页按当前社区拉取帖子
- 首页支持举报帖子
- 发帖默认绑定当前所选社区

### 5.2 管理后台（blocksy-admin）

```bash
cd blocksy-admin
npm install
npm run dev
```

管理后台最小联调范围（已接线）：

- 登录页：`/login`
- 帖子管理页：`/posts`
- 已接接口：
  - `POST /api/auth/login`
  - `GET /api/posts`
- 已预留操作：帖子“下架/删除”按钮（当前为占位提示，待接后台管理接口）

V1.5 管理后台新增页面能力：

- 举报管理：处理举报（通过/驳回/通过并封禁）
- 用户管理：封禁/解封用户
- 社区管理：社区列表与新增社区

## 7. V1.5 一键联调脚本（curl）

已提供脚本：`scripts/v1_5_flow.sh`

执行：

```bash
cd /Users/workstation/os-code/Blocksy
./scripts/v1_5_flow.sh
```

该脚本会按顺序执行：

1. 用户登录
2. 管理员登录
3. 获取并切换社区
4. 发帖
5. 举报帖子
6. 后台处理举报
7. 封禁验证（`BAN_TARGET_USER=true` 时）

可选环境变量：

```bash
BASE_URL=http://localhost:8080/api
USER_NAME=demo
USER_PASSWORD=blocksy123
ADMIN_NAME=demo
ADMIN_PASSWORD=blocksy123
COMMUNITY_ID=1
BAN_TARGET_USER=true
```

示例（不做封禁验证）：

```bash
BAN_TARGET_USER=false ./scripts/v1_5_flow.sh
```

默认本地访问（Vite）：`http://localhost:5173/login`

## 6. 当前阶段说明

已完成第一阶段核心目标：

- Docker 基础环境编排
- Spring Boot 可启动骨架
- PostgreSQL/Redis/MinIO 接入配置
- MinIO 文件上传最小可用接口
- MVP 核心表初始化 SQL
- 用户端与管理后台基础目录与页面骨架

暂未引入（按要求）：

- 微服务、MongoDB、RabbitMQ、Elasticsearch、即时聊天、推荐系统、复杂审核流、复杂地图、支付系统
