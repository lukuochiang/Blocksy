# Blocksy Server (MVP)

## 1. 本地启动

前置：
- Java 17
- Maven 3.9+
- PostgreSQL/Redis/MinIO（你已通过 `docker-compose` 启动）

启动：

```bash
mvn spring-boot:run
```

打包验证：

```bash
mvn -DskipTests package
```

服务默认地址：`http://localhost:8080`

## 2. 数据库初始化

已接入 Flyway，启动时会自动执行：
- `src/main/resources/db/migration/V1__init_schema.sql`
- `src/main/resources/db/migration/V2__report_audit.sql`
- `src/main/resources/db/migration/V3__user_punish_audit.sql`
- `src/main/resources/db/migration/V4__listing_admin_audit.sql`
- `src/main/resources/db/migration/V5__event_admin_audit.sql`
- `src/main/resources/db/migration/V6__operation_note_templates.sql`
- `src/main/resources/db/migration/V7__operation_note_template_logs.sql`

## 3. Swagger / OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

默认测试账号（由 `DataInitializer` 自动创建）：
- username: `demo`
- password: `blocksy123`

## 4. 已完成接口（本轮重点）

认证与用户：
- `POST /api/auth/login`
- `GET /api/users/me`

分类信息：
- `GET /api/listings`
  - 支持筛选/排序参数：`communityId` `category` `keyword` `minPrice` `maxPrice` `sortBy(CREATED_AT/PRICE)` `sortOrder(ASC/DESC)`
- `GET /api/listings/{id}`
- `GET /api/listings/mine`
- `GET /api/listings/mine/{id}`
- `POST /api/listings`
- `GET /api/admin/listings`
- `POST /api/admin/listings/{id}/handle`（`APPROVE/REJECT/OFFLINE/RESTORE/DELETE`）
- `POST /api/admin/listings/batch-handle`
- `GET /api/admin/listings/stats/category`
- `GET /api/admin/listings/{id}/logs`

活动：
- `GET /api/events`
- `GET /api/events/{id}`
- `GET /api/events/mine`
- `GET /api/events/my-signups`
- `POST /api/events`
- `POST /api/events/{id}/signup`
- `GET /api/admin/events`
- `POST /api/admin/events/{id}/handle`（`OFFLINE/RESTORE/DELETE`）
- `POST /api/admin/events/batch-handle`
- `POST /api/admin/events/batch-retry`
- `POST /api/admin/events/batch-retry/export`（CSV）
- `GET /api/admin/events/{id}/logs`
- `GET /api/admin/events/handle-logs`（支持 eventId/operatorUserId/action/time）
- `GET /api/admin/events/handle-logs/export`（CSV）

运营配置：
- `GET /api/admin/operation-note-templates`
- `POST /api/admin/operation-note-templates`
- `PUT /api/admin/operation-note-templates/{id}`
- `POST /api/admin/operation-note-templates/{id}/status`
- `GET /api/admin/operation-note-templates/logs`
- `GET /api/admin/operation-note-templates/rules`
- `GET /api/admin/operation-note-templates/permission`

通知：
- `GET /api/notifications`
- `GET /api/notifications/page`
- `GET /api/notifications/unread-count`
- `POST /api/notifications/{id}/read`
- `POST /api/notifications/read-batch`
- `POST /api/notifications/read-all`
- `POST /api/admin/notifications/system-announcement`
- `GET /api/admin/notifications/stats`
- `GET /api/admin/notifications/governance-stats`

举报：
- `POST /api/admin/reports/batch-retry`
- `POST /api/admin/reports/batch-retry/export`（CSV）

文件：
- `POST /api/files/upload`

## 5. 一键联调脚本（1/2/3）

脚本路径：`../scripts/phase123_closure_flow.sh`

作用：
1. 登录拿 token
2. 读取社区
3. 发布分类信息并查询我的分类信息
4. 发布活动并报名
5. 查询通知未读数并执行全部已读

执行：

```bash
bash ../scripts/phase123_closure_flow.sh
```

可选环境变量：
- `BASE_URL`（默认 `http://localhost:8080`）
- `USERNAME`（默认 `demo`）
- `PASSWORD`（默认 `blocksy123`）

## 6. 回归脚本（本轮新增）

- `../scripts/regression_core_flow.sh`
  - 验证：登录 -> 当前用户 -> 发帖 -> 评论 -> 举报 -> 后台处理举报
- `../scripts/regression_listing_admin_flow.sh`
  - 验证：发布分类信息 -> 后台下架 -> 公网列表不可见 -> 后台恢复 -> 查看处理日志
- `../scripts/regression_event_batch_export_flow.sh`
  - 验证：活动批量下架 -> 日志筛选 -> CSV 导出
- `../scripts/regression_template_audit_flow.sh`
  - 验证：模板权限 -> 新增模板 -> 启停模板 -> 操作日志
- `../scripts/regression_listing_batch_stats_flow.sh`
  - 验证：分类信息批量审核 -> 分类维度统计 -> 前台筛选排序查询
- `../scripts/regression_v35_v45_flow.sh`
  - 验证：活动/举报失败重试+导出、系统公告下发、通知触达统计、治理看板指标

执行：

```bash
bash ../scripts/regression_core_flow.sh
bash ../scripts/regression_listing_admin_flow.sh
bash ../scripts/regression_event_batch_export_flow.sh
bash ../scripts/regression_template_audit_flow.sh
bash ../scripts/regression_listing_batch_stats_flow.sh
bash ../scripts/regression_v35_v45_flow.sh
```

## 7. 新计划文档（V2 下一批）

- `../docs/v2-listing-batch-stats-and-sorting-plan.md`
- `../docs/v3-v45-execution-plan.md`

权限配置：
- `BLOCKSY_ADMIN_USERNAMES`：后台管理员账号（默认 `demo`）
- `BLOCKSY_TEMPLATE_MANAGER_USERNAMES`：备注模板管理权限账号（默认 `demo`）
