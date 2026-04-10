# Blocksy V5 实作进展：通知中心 + 菜单去占位化

更新时间：2026-04-09

## 1. 本轮目标

1. 公告列表（分页/筛选）
2. 公告撤回
3. 公告二次下发
4. 7/30 天指标趋势图（后台联调）
5. `blocksy-admin` 菜单页不再是静态占位说明

## 2. 已落地能力

### 2.1 后端（blocksy-server）
- 新增系统公告表：`system_announcements`（Flyway `V8__system_announcements.sql`）
- 新增后台通知中心接口：
  - `GET /api/admin/notifications/announcements`
  - `POST /api/admin/notifications/{id}/revoke`
  - `POST /api/admin/notifications/{id}/redispatch`
  - `GET /api/admin/notifications/trend?days=7|30`
- 发布公告时写入公告主表并下发站内通知；二次下发会累计 `dispatch_count`。

### 2.2 管理后台（blocksy-admin）
- 新增通知中心页面：
  - 列表筛选 + 分页
  - 撤回
  - 二次下发
  - 7/30 天趋势图（柱状可视）
  - 公告发布弹窗
- 路由接入：
  - `/announcements`
  - `/messages/system`（复用通知中心页）

### 2.3 全菜单去占位化
- 将通用占位页升级为“模块工作台”：
  - 执行项统计卡
  - 执行项列表（筛选、状态流转、删除）
  - 新增执行项
  - 每个菜单独立 localStorage 持久化
- 效果：所有仍走通用页的菜单都具备可操作能力，不再是纯说明占位。

## 3. 影响文件

- 后端：
  - `blocksy-server/src/main/resources/db/migration/V8__system_announcements.sql`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/admin/controller/AdminNotificationController.java`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/notification/service/NotificationService.java`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/notification/service/impl/NotificationServiceImpl.java`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/notification/entity/SystemAnnouncementEntity.java`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/notification/mapper/SystemAnnouncementMapper.java`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/notification/dto/AdminAnnouncementItemResponse.java`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/notification/dto/NotificationTrendPointResponse.java`
- 管理后台：
  - `blocksy-admin/src/views/announcement/index.vue`
  - `blocksy-admin/src/api/notification.ts`
  - `blocksy-admin/src/router/index.ts`
  - `blocksy-admin/src/views/_shared/module-placeholder.vue`

## 4. 验收建议

1. 启动后端，打开 Swagger 验证新增四个通知中心接口。
2. 打开后台：
   - 进入“平台公告”或“系统通知”页，完成发布->撤回->二次下发。
   - 切换 7/30 天趋势，检查曲线柱状变化。
3. 打开任意仍走通用页的菜单（如“异常行为监控”），验证执行项新增、状态流转和刷新后持久化。
