# Blocksy V5 第二批真实页面替换（数据权限/操作日志/用户行为日志）

更新时间：2026-04-09

## 1. 本轮范围

将以下菜单从“通用工作台”升级为“真实后端接口页”：

1. 数据权限（`/permissions/data`）
2. 操作日志（`/permissions/logs`）
3. 用户行为日志（`/users/behaviors`）

## 2. 后端能力

### 2.1 新增迁移
- `V10__ops_and_data_permission.sql`
  - `user_behavior_logs`
  - `data_permissions`
  - `admin_operation_logs`

### 2.2 新增接口
- `GET /api/admin/permissions/data`
- `POST /api/admin/permissions/data/assign`
- `GET /api/admin/permissions/operation-logs`
- `GET /api/admin/permissions/user-behavior-logs`

### 2.3 日志联动
- 数据权限分配会写入 `admin_operation_logs`
- 异常行为/申诉处理会写入 `admin_operation_logs`

## 3. 管理后台能力

### 3.1 数据权限页
- 列表分页查询（角色/数据域）
- 批量分配（角色 + 数据域 + 数据值数组）

### 3.2 操作日志页
- 日志分页查询（模块/动作）
- 展示操作者、目标、详情、时间

### 3.3 用户行为日志页
- 分页查询（用户ID/行为类型）
- 展示资源对象、IP、设备、时间

## 4. 关键文件

- 后端：
  - `blocksy-server/src/main/resources/db/migration/V10__ops_and_data_permission.sql`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/admin/controller/AdminPermissionController.java`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/admin/service/impl/AdminPermissionServiceImpl.java`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/admin/service/impl/AdminRiskServiceImpl.java`
- 前端：
  - `blocksy-admin/src/views/permission-data/index.vue`
  - `blocksy-admin/src/views/permission-operation-log/index.vue`
  - `blocksy-admin/src/views/user-behaviors/index.vue`
  - `blocksy-admin/src/api/permission.ts`

## 5. 验收步骤

1. 启动后端并完成 Flyway 到 V10。
2. 后台访问三个页面验证列表分页可用。
3. 在数据权限页执行分配，操作日志页可看到新增记录。
