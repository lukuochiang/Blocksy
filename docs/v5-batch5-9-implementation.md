# Blocksy V5 Batch 5~9 一体化落地记录

更新时间：2026-04-10

## 1. 本次交付范围

- Batch 5 审核中心剩余菜单真实化
  - `/audit/comments` -> 评论审核真实页（复用评论管理接口）
  - `/audit/users` -> 用户审核真实页（复用认证审核接口）
  - `/audit/media` -> 媒体审核真实页（复用媒体管理接口）
- Batch 6 消息与推送
  - Push 任务列表/创建/立即下发
  - Push 记录分页查询
  - 通知模板分页与新增/更新
- Batch 7 数据分析
  - 用户增长、社区活跃、内容活跃、审核与举报、留存、热门社区排行
- Batch 8 系统设置
  - 平台配置项（分组）列表与保存
  - 协议政策版本新增与激活
- Batch 9 工程化
  - 新增批次回归脚本
  - 新增 CI smoke workflow
  - admin 路由改为动态导入（代码拆分）

## 2. 后端新增接口

### 2.1 Admin-Notification 增量

- `GET /api/admin/notifications/push/tasks`
- `POST /api/admin/notifications/push/tasks`
- `POST /api/admin/notifications/push/tasks/{id}/send`
- `GET /api/admin/notifications/push/records`
- `GET /api/admin/notifications/templates`
- `POST /api/admin/notifications/templates/save`

### 2.2 Admin-Analytics

- `GET /api/admin/analytics/growth`
- `GET /api/admin/analytics/community`
- `GET /api/admin/analytics/content`
- `GET /api/admin/analytics/moderation`
- `GET /api/admin/analytics/retention`
- `GET /api/admin/analytics/ranking`

### 2.3 Admin-Settings

- `GET /api/admin/settings/items`
- `POST /api/admin/settings/items/save`
- `GET /api/admin/settings/policies`
- `POST /api/admin/settings/policies/save`
- `POST /api/admin/settings/policies/{id}/activate`

## 3. 数据与初始化

- 基于 `V13__ops_push_analytics_settings.sql` 表结构，补齐实体与 mapper：
  - `push_tasks`
  - `push_records`
  - `notification_templates`
  - `platform_settings`
  - `policy_documents`
- `DataInitializer` 新增种子：
  - 通知模板种子
  - 平台设置种子（BASIC/UPLOAD/INTEGRATIONS/BRAND）
  - 协议文档种子（用户协议/隐私政策）
  - 默认 push 任务种子

## 4. 前端管理后台新增真实页

- 消息与推送：
  - `src/views/message-push/index.vue`
  - `src/views/message-record/index.vue`
- 数据分析：
  - `src/views/analytics-growth/index.vue`
  - `src/views/analytics-community/index.vue`
  - `src/views/analytics-content/index.vue`
  - `src/views/analytics-moderation/index.vue`
  - `src/views/analytics-retention/index.vue`
  - `src/views/analytics-ranking/index.vue`
- 系统设置：
  - `src/views/settings-basic/index.vue`
  - `src/views/settings-brand/index.vue`
  - `src/views/settings-upload/index.vue`
  - `src/views/settings-integrations/index.vue`
  - `src/views/settings-legal/index.vue`

## 5. 工程化补齐

- 回归脚本：
  - `scripts/regression_admin_batch5_9_flow.sh`
- CI workflow：
  - `.github/workflows/smoke-admin-batch5-9.yml`

## 6. 验证方式

1. 启动依赖与服务：
   - PostgreSQL / Redis / MinIO
   - `blocksy-server`
   - `blocksy-admin`
2. 运行脚本：
   - `bash scripts/regression_admin_batch5_9_flow.sh`
3. 手工验证菜单：
   - 审核中心 3 个菜单非占位
   - 消息与推送 2 个菜单可操作
   - 数据分析 6 个菜单有真实数据
   - 系统设置 5 个菜单可读写

## 7. 下一步建议（进入 V6 前）

- 对 Batch 6/7/8 的核心接口补最小集成测试（`@SpringBootTest` + Testcontainers）
- 优化 analytics 口径（当前 retention 为 MVP 近似值）
- push 任务补“定时发送/取消发送/失败重试”状态流转
