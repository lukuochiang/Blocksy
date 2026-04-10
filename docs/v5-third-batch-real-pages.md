# Blocksy V5 第三批真实页面替换（认证审核/社区公告/活动报名记录）

更新时间：2026-04-10

## 1. 本轮范围

将以下菜单从通用工作台升级为真实业务页：

1. 认证审核（`/users/verification`）
2. 社区公告（`/communities/notices`）
3. 活动报名记录（`/events/signups`）

## 2. 后端实现

### 2.1 新增迁移
- `V11__verification_notice_signup_admin.sql`
  - `user_verification_applications`
  - `community_notices`

### 2.2 新增接口
- 认证审核：
  - `GET /api/admin/verifications`
  - `POST /api/admin/verifications/{id}/handle`
- 社区公告：
  - `GET /api/admin/communities/notices`
  - `POST /api/admin/communities/notices`
  - `POST /api/admin/communities/notices/{id}/revoke`
- 活动报名记录：
  - `GET /api/admin/events/signups`

### 2.3 种子数据
- `DataInitializer` 补充：
  - 认证申请默认数据
  - 社区公告默认数据
  - 活动及报名默认数据

## 3. 前端实现（blocksy-admin）

- 新增页面：
  - `views/user-verification/index.vue`
  - `views/community-notice/index.vue`
  - `views/event-signups/index.vue`
- 新增 API：
  - `api/verification.ts`
  - `api/community-notice.ts`
  - `api/event-signup.ts`
- 路由替换：
  - `/users/verification`
  - `/communities/notices`
  - `/events/signups`

## 4. 验收建议

1. 后端启动后进入上述三个页面，确认列表可加载。
2. 在认证审核页执行通过/驳回。
3. 在社区公告页执行发布/撤回。
4. 在活动报名记录页按活动ID、社区ID、用户ID筛选。
