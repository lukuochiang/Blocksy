# Blocksy V5 扩展：Top3 菜单真实业务页落地

更新时间：2026-04-09

## 1. 范围

本次将以下三个高优先级菜单从“工作台占位”升级为“真实后端接口页”：

1. 异常行为监控（`/risk/anomaly`）
2. 申诉处理（`/risk/appeals`）
3. 菜单权限（`/permissions/menus`）

## 2. 后端实现

### 2.1 数据库迁移
- `V9__risk_and_permission_modules.sql`
  - `risk_anomalies`
  - `risk_appeals`
  - `menu_permissions`
  - `permission_op_logs`

### 2.2 接口
- 风控与申诉：
  - `GET /api/admin/risk/anomalies`
  - `POST /api/admin/risk/anomalies/{id}/handle`
  - `GET /api/admin/risk/appeals`
  - `POST /api/admin/risk/appeals/{id}/handle`
- 菜单权限：
  - `GET /api/admin/permissions/menus`
  - `POST /api/admin/permissions/menus/assign`
  - `GET /api/admin/permissions/logs`

### 2.3 初始化数据
- `DataInitializer` 增加风控示例记录和默认菜单权限种子，确保本地页面可直接联调。

## 3. 管理后台实现

- 新增页面：
  - `views/risk-anomaly/index.vue`
  - `views/risk-appeals/index.vue`
  - `views/permission-menu/index.vue`
- 新增 API：
  - `api/risk.ts`
  - `api/permission.ts`
- 路由替换：
  - `/risk/anomaly`
  - `/risk/appeals`
  - `/permissions/menus`

## 4. 验收步骤

1. 启动后端（Flyway 自动执行到 V9）。
2. 进入后台页面，验证：
   - 异常行为可筛选、分页、处理。
   - 申诉可筛选、分页、处理。
   - 菜单权限可查询、批量分配，并能看到操作日志。
3. Swagger 验证新增接口可调用。
