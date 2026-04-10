# Blocksy V6 Day1-7 执行记录（第一轮）

更新时间：2026-04-10

## 已完成

### Day1-2：分类信息后端增强

- 后台分类信息列表改为分页接口：
  - `GET /api/admin/listings?page=&pageSize=`
- 后台批量治理新增失败重试与 CSV 导出：
  - `POST /api/admin/listings/batch-retry`
  - `POST /api/admin/listings/batch-retry/export`
- 分类维度统计新增时间窗口参数：
  - `GET /api/admin/listings/stats/category?days=30`
  - 支持 `startDate/endDate`
- 分类信息审核动作新增通知联动：
  - 审核通过/驳回/下架/恢复后，向发布者写入站内通知

### Day3-4：管理后台联调

- `blocksy-admin` 分类信息页增强：
  - 列表分页
  - 批量处理结果留存
  - 失败项重试
  - 重试结果 CSV 导出
  - 分类统计支持 7/30/90 天窗口

### Day5：用户端“我的发布”与详情增强

- 用户端新增我的发布状态筛选（全部/待审核/已上架/已下架）
- 我的发布新增状态操作：
  - 下架
  - 重新提交审核
  - 删除
- 详情页新增“状态时间线”：
  - `GET /api/listings/mine/{id}/logs`

### Day6：活动治理通知收口

- 后台活动治理动作（下架/恢复/删除）新增发布者通知联动

### Day7：回归与 CI

- 新增脚本：
  - `scripts/regression_v6_listing_event_notification_flow.sh`
  - 覆盖：分类信息发布→后台审核→通知校验，活动下架→通知校验
- 新增 workflow：
  - `.github/workflows/smoke-v6-listing-event-notification.yml`

## 本轮新增接口清单

- `POST /api/listings/mine/{id}/offline`
- `POST /api/listings/mine/{id}/resubmit`
- `POST /api/listings/mine/{id}/delete`
- `GET /api/listings/mine/{id}/logs`
- `POST /api/admin/listings/batch-retry`
- `POST /api/admin/listings/batch-retry/export`

## 待下一轮继续

- 分类信息详情“相关推荐”由占位升级为真实推荐
- 活动治理页补“批量失败项重试结果可视化”
- 通知中心补“审核结果通知模板化”
