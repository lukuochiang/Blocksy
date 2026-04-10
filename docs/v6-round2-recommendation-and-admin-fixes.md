# V6 第二轮：分类推荐与后台问题修复执行记录

更新时间：2026-04-10 10:50 +0800  
适用分支：`main`

## 本轮目标

1. 分类详情“相关推荐占位”升级为真实推荐逻辑。  
2. 活动治理页补“失败项重试结果可视化”。  
3. 修复线上高频报错并补齐后台真实菜单能力：
   - 社区成员活跃 SQL 语法报错
   - 平台公告/系统通知 `NotificationEntity.read` lambda cache 报错
   - 权限与角色新增登录日志菜单
   - 用户中心黑名单管理真实落地

## 已完成项

### 1) 社区成员活跃 SQL 报错修复

- 文件：`blocksy-server/src/main/java/com/blocksy/server/modules/admin/service/impl/AdminContentOpsServiceImpl.java`
- 处理：
  - 将 `WHERE (? IS NULL OR ...)` 改为动态 SQL 拼接，仅在 `keyword` 存在时追加 `WHERE`
  - 总数统计改为同口径动态查询
- 结果：
  - 消除 PostgreSQL 预编译参数类型导致的 bad SQL grammar 报错

### 2) 通知模块 lambda cache 报错修复

- 文件：`blocksy-server/src/main/java/com/blocksy/server/modules/notification/entity/NotificationEntity.java`
- 处理：
  - 字段从 `isRead` 改为 `read`，保留数据库映射 `@TableField("is_read")`
- 结果：
  - 修复 `can not find lambda cache for this property [read]`（平台公告、系统通知菜单）

### 3) 用户社区默认字段 lambda cache 隐患修复

- 文件：
  - `blocksy-server/src/main/java/com/blocksy/server/modules/user/entity/UserCommunityEntity.java`
  - `blocksy-server/src/main/java/com/blocksy/server/modules/user/service/impl/UserServiceImpl.java`
- 处理：
  - 字段 `isDefault` 改为 `defaultFlag`（仍映射 `is_default`）
  - 全量替换 LambdaQueryWrapper 的方法引用
- 结果：
  - 避免 `property [default]` 相关缓存异常再次出现

### 4) 分类详情相关推荐（真实逻辑）

- 后端：
  - `GET /api/listings/{id}/recommendations?limit=6`
  - 同社区 + 同分类优先，按发布时间倒序；不足时同社区兜底补齐
- 文件：
  - `ListingController.java`
  - `ListingService.java`
  - `ListingServiceImpl.java`
- 前端：
  - `blocksy-app` 详情页接入相关推荐卡片与跳转
  - 文件：
    - `blocksy-app/src/api/listing.ts`
    - `blocksy-app/src/pages/listing-detail/index.vue`

### 5) 活动治理失败项重试结果可视化

- 页面：`blocksy-admin/src/views/event/index.vue`
- 增强：
  - 批量处理结果卡片（总数/成功/跳过/失败）
  - 失败项列表展示（eventId + message）
  - 一键重试失败项
  - 重试结果导出 CSV

### 6) 权限与角色新增登录日志菜单

- 菜单与路由：
  - `blocksy-admin/src/config/menu.ts`
  - `blocksy-admin/src/router/index.ts`
- 新页面：
  - `blocksy-admin/src/views/permission-login-log/index.vue`
- 数据来源：
  - 复用 `GET /api/admin/permissions/user-behavior-logs?behaviorType=LOGIN`
- 同步补充：
  - 登录成功后落库行为日志（含 IP / User-Agent）
  - 文件：`blocksy-server/src/main/java/com/blocksy/server/modules/auth/controller/AuthController.java`

### 7) 黑名单管理真实落地

- 后端：
  - 新增接口：`GET /api/admin/users/blacklist`
  - 文件：
    - `AdminUserController.java`
    - `UserService.java`
    - `UserServiceImpl.java`
- 后台：
  - 新页面：`blocksy-admin/src/views/user-blacklist/index.vue`
  - 路由替换原占位页
  - API：`blocksy-admin/src/api/user.ts` 新增 `fetchBlacklistedUsers`

## 构建验证

- `blocksy-server`：`mvn -DskipTests package` 通过  
- `blocksy-admin`：`npm run build` 通过  
- `blocksy-app`：`npm run build` 通过

## 回归建议（手工最小集）

1. 社区成员活跃页：带关键字与不带关键字各查一次。  
2. 平台公告页、系统通知页：列表与趋势图均可加载。  
3. 分类详情页：查看相关推荐、点击跳转详情。  
4. 活动管理页：执行一次批量处理并触发失败项重试。  
5. 权限与角色 -> 登录日志：登录一次后刷新看到新记录。  
6. 用户中心 -> 黑名单管理：封禁用户后可见，解封后消失。
