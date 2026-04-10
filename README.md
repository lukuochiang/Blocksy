# Norvo MVP

Norvo 是一个“本地社区 / 邻里社交 / 分类信息平台”的 MVP 骨架工程。当前版本聚焦于本地开发可运行、结构清晰、方便后续扩展。

## 0. 开发推进文档

- 模块状态矩阵与两周排期（2026-04-09 ~ 2026-04-22）：
  - `docs/module-status-and-2week-plan.md`
- V5 优先闭环（帖子治理 + 分页搜索 + 社区维度统一）：
  - `docs/v5-priority-post-governance-closure.md`
- V5 通知中心后台实施计划：
  - `docs/v5-notification-center-admin-plan.md`
- V5 通知中心已实现与后台菜单去占位化：
  - `docs/v5-notification-center-implementation.md`
- V5 Top3 菜单真实业务页：
  - `docs/v5-top3-admin-real-pages.md`
- V5 第二批菜单真实业务页：
  - `docs/v5-second-batch-real-pages.md`
- V5 第三批菜单真实业务页：
  - `docs/v5-third-batch-real-pages.md`
- V5 第四批菜单真实业务页：
  - `docs/v5-fourth-batch-real-pages.md`
- V5 后续批次安排：
  - `docs/v5-next-batches-plan.md`
- V5 Batch 5~9 一体化落地记录：
  - `docs/v5-batch5-9-implementation.md`
- V6 Day1-7 执行记录：
  - `docs/v6-day1-7-execution.md`

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

数据库迁移（已接入 Flyway）：

- 后端启动时会自动执行 `blocksy-server/src/main/resources/db/migration` 下的迁移脚本
- 当前基线脚本：`V1__init_schema.sql`
- 可通过以下 SQL 验证迁移记录：

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
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
- `POST /api/listings/mine/{id}/offline`
- `POST /api/listings/mine/{id}/resubmit`
- `POST /api/listings/mine/{id}/delete`
- `GET /api/listings/mine/{id}/logs`
- `GET /api/events`
- `POST /api/events`
- `POST /api/events/{id}/signup`
- `GET /api/notifications`
- `POST /api/reports`
- `GET /api/admin/reports`
- `POST /api/admin/reports/{id}/handle`
- `POST /api/admin/reports/batch-handle`
- `GET /api/admin/reports/{id}/logs`
- `GET /api/admin/users`
- `POST /api/admin/users/{id}/ban`
- `POST /api/admin/users/{id}/unban`
- `GET /api/admin/users/{id}/punish-logs`
- `GET /api/admin/communities`
- `POST /api/admin/communities`
- `GET /api/admin/notifications/announcements`
- `POST /api/admin/notifications/{id}/revoke`
- `POST /api/admin/notifications/{id}/redispatch`
- `GET /api/admin/notifications/trend?days=7|30`
- `GET /api/admin/risk/anomalies`
- `POST /api/admin/risk/anomalies/{id}/handle`
- `GET /api/admin/risk/appeals`
- `POST /api/admin/risk/appeals/{id}/handle`
- `GET /api/admin/permissions/menus`
- `POST /api/admin/permissions/menus/assign`
- `GET /api/admin/permissions/logs`
- `GET /api/admin/permissions/data`
- `POST /api/admin/permissions/data/assign`
- `GET /api/admin/permissions/operation-logs`
- `GET /api/admin/permissions/user-behavior-logs`
- `GET /api/admin/verifications`
- `POST /api/admin/verifications/{id}/handle`
- `GET /api/admin/communities/notices`
- `POST /api/admin/communities/notices`
- `POST /api/admin/communities/notices/{id}/revoke`
- `GET /api/admin/events/signups`
- `GET /api/admin/listings`
- `POST /api/admin/listings/batch-handle`
- `POST /api/admin/listings/batch-retry`
- `POST /api/admin/listings/batch-retry/export`
- `GET /api/admin/listings/stats/category`
- `GET /api/admin/content/communities/engagement`
- `GET /api/admin/content/categories`
- `POST /api/admin/content/categories`
- `POST /api/admin/content/categories/{id}/toggle`
- `GET /api/admin/content/media/posts`
- `POST /api/admin/content/media/posts/{id}/offline`
- `GET /api/admin/notifications/push/tasks`
- `POST /api/admin/notifications/push/tasks`
- `POST /api/admin/notifications/push/tasks/{id}/send`
- `GET /api/admin/notifications/push/records`
- `GET /api/admin/notifications/templates`
- `POST /api/admin/notifications/templates/save`
- `GET /api/admin/analytics/growth`
- `GET /api/admin/analytics/community`
- `GET /api/admin/analytics/content`
- `GET /api/admin/analytics/moderation`
- `GET /api/admin/analytics/retention`
- `GET /api/admin/analytics/ranking`
- `GET /api/admin/settings/items`
- `POST /api/admin/settings/items/save`
- `GET /api/admin/settings/policies`
- `POST /api/admin/settings/policies/save`
- `POST /api/admin/settings/policies/{id}/activate`
- `POST /api/files/upload`

分析接口时间窗口参数（新增）：

- 支持 `days`（如 `7`/`30`）
- 也支持显式窗口：`startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- 示例：
  - `GET /api/admin/analytics/growth?days=30`
  - `GET /api/admin/analytics/community?limit=20&startDate=2026-03-01&endDate=2026-03-31`
  - `GET /api/admin/analytics/retention?startDate=2026-03-15&endDate=2026-03-31`

启动报错排查（`risk_anomalies does not exist`）：

1. 已新增兼容迁移：`blocksy-server/src/main/resources/db/migration/V14__compat_missing_admin_tables.sql`
2. 重启后端让 Flyway 自动执行迁移
3. 若历史库迁移记录异常，先备份，再执行：
   - `DELETE FROM flyway_schema_history WHERE version='14';`（仅在重复失败时）
   - 重启应用重新迁移

帖子分页查询参数（本轮新增）：

- `GET /api/posts?communityId=&keyword=&page=1&pageSize=10`
- `GET /api/posts/mine?communityId=&keyword=&page=1&pageSize=10`
- `GET /api/admin/posts?status=&communityId=&keyword=&page=1&pageSize=10`

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
curl "http://localhost:8080/api/posts?page=1&pageSize=10"
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
- 举报处理备注与操作审计：`handler_note` 字段 + `report_handle_logs` 审计表 + `GET /api/admin/reports/{id}/logs`
- 用户封禁后台能力：`POST /api/admin/users/{id}/ban`、`POST /api/admin/users/{id}/unban`
- 用户处罚增强：封禁原因、封禁时长（小时）、到期自动解封、处罚日志 `GET /api/admin/users/{id}/punish-logs`
- 举报批量处理增强：仅处理 `PENDING` 记录，返回成功/跳过/失败明细
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
- `home/community/event/my-post/listing/message` 已统一分页、下拉刷新、空状态 CTA 行为

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
  - `GET /api/admin/posts`
  - `POST /api/admin/posts/{id}/review`
- 已实现操作：帖子“下架/恢复”真实治理（筛选+分页+状态流转）

V1.5 管理后台新增页面能力：

- 举报管理：处理举报（通过/驳回/通过并封禁）
- 用户管理：封禁/解封用户
- 社区管理：社区列表与新增社区

## 7. V1.5 一键联调脚本（curl）

已提供脚本：

- `scripts/v1_5_flow.sh`
- `scripts/user_punish_flow.sh`
- `scripts/regression_post_governance_flow.sh`

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

`scripts/user_punish_flow.sh` 会按顺序执行：

1. 管理员登录
2. 目标用户登录
3. 管理员按“原因+时长”封禁目标用户
4. 验证目标用户 token 被拦截
5. 查询处罚日志
6. 管理员解封
7. 用户重新登录并验证恢复访问

查看某条举报的处理审计日志：

```bash
curl "http://localhost:8080/api/admin/reports/<reportId>/logs" \
  -H "Authorization: Bearer <admin_token>"
```

批量处理举报：

```bash
curl -X POST "http://localhost:8080/api/admin/reports/batch-handle" \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"reportIds":[1,2,3],"action":"RESOLVED","note":"批量处理","banTargetUser":false}'
```

封禁用户（支持原因+时长）：

```bash
curl -X POST "http://localhost:8080/api/admin/users/<userId>/ban" \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"reason":"恶意刷屏","durationHours":24}'
```

查看用户处罚日志：

```bash
curl "http://localhost:8080/api/admin/users/<userId>/punish-logs" \
  -H "Authorization: Bearer <admin_token>"
```

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

帖子治理分页回归：

```bash
cd /Users/workstation/os-code/Blocksy
./scripts/regression_post_governance_flow.sh
```

CI Smoke（GitHub Actions）：

- Workflow：`.github/workflows/smoke-post-governance.yml`
- 自动触发：`main` 分支 push / PR（命中后端与脚本路径）
- 手动触发：Actions -> `smoke-post-governance` -> Run workflow

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
