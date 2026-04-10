# Blocksy V2 下一批计划（分类信息）

更新时间：2026-04-09

## 1. 目标范围

本批次只做三件事，先保证联调闭环：

1. 后台批量审核（分类信息）
2. 分类维度统计（按分类输出总量/待审核/上架/下架）
3. 前台筛选排序增强（关键词、价格区间、排序）

不进入 V3（活动增强）与 V4（通知增强）。

## 2. 本批次已完成（Server）

### 2.1 后台批量审核

- 新增接口：`POST /api/admin/listings/batch-handle`
- 请求体：`listingIds[] + action + note`
- 支持动作：`APPROVE/REJECT/OFFLINE/RESTORE/DELETE`
- 每条处理都会写入 `listing_handle_logs`
- 返回批量处理汇总与逐条结果

### 2.2 分类维度统计

- 新增接口：`GET /api/admin/listings/stats/category`
- 支持参数：`communityId`（可选）
- 返回字段：
  - `category`
  - `totalCount`
  - `pendingCount`
  - `onlineCount`
  - `offlineCount`

### 2.3 前台筛选排序增强（接口层）

- 增强接口：`GET /api/listings`
- 新增参数：
  - `keyword`
  - `minPrice`
  - `maxPrice`
  - `sortBy`（`CREATED_AT/PRICE`）
  - `sortOrder`（`ASC/DESC`）

## 3. 回归验证

- 新增脚本：`scripts/regression_listing_batch_stats_flow.sh`
- 覆盖链路：
  1. 登录
  2. 创建两条待审核分类信息
  3. 后台批量通过
  4. 查询分类统计
  5. 走前台筛选排序接口验证

## 4. 下一步排期（V2 收尾）

### 4.1 Admin（blocksy-admin）

1. 分类信息管理页接入勾选批量操作（通过/驳回/下架/恢复/删除）
2. 页头增加分类统计卡片（3 类 + 汇总）
3. 列表筛选补充关键词/价格区间/排序，并与后端参数对齐

### 4.2 App（blocksy-app）

1. 分类信息列表增加排序控件（最新/价格升序/价格降序）
2. 筛选栏补关键词与价格区间输入
3. 空状态文案按筛选态区分（无数据 vs 筛选无结果）

### 4.3 验收标准

1. 后台能批量审核并有日志
2. 后台能查看分类统计并按社区切换
3. 前台能通过参数稳定拿到筛选/排序结果
4. 三端回归脚本可重复执行
