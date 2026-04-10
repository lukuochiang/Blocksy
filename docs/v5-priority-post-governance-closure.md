# Blocksy V5 优先闭环实施文档（帖子治理 + 分页搜索 + 社区维度统一）

更新时间：2026-04-09

## 1. 仓库现状分析（本轮结论）

### 1.1 已有能力
- 后端已有帖子发布、评论、后台审核接口骨架。
- 后台已有帖子审核页面（`/audit/posts`）与审核动作接口。
- 用户端已接社区筛选发帖与帖子列表展示。

### 1.2 关键缺口
- `GET /api/posts`、`GET /api/posts/mine` 仍为固定 `LIMIT`，缺分页与关键词检索。
- `GET /api/admin/posts` 缺分页返回结构，后台治理页不利于运营规模化处理。
- `blocksy-admin` 帖子管理页仍有 mock 操作（下架/删除提示）未接真实治理。
- 用户端首页/我的帖子/发布页帖子列表是前端本地分页，不适合真实联调场景。

### 1.3 优先级判断
在“帖子治理、分页搜索、社区维度统一、通知中心、CI/smoke”中，优先实现：
- **帖子治理 + 分页搜索 + 社区维度统一**

理由：
1. 直接影响核心内容主链路（看帖/管帖）。
2. 开发风险低于通知中心全套后台页+趋势图。
3. 能同时提升用户体验和运营治理效率。

## 2. 实施范围（本轮）

### 2.1 后端
- 帖子接口分页化：
  - `GET /api/posts`
  - `GET /api/posts/mine`
  - `GET /api/admin/posts`
- 支持 `communityId + keyword + page + pageSize`。
- 新增统一分页返回结构 `PageResponse<T>`。

### 2.2 管理后台
- 帖子管理页接入真实治理链路：
  - 查询筛选（状态/社区/关键词）
  - 分页
  - 恢复/下架（调用 `/api/admin/posts/{id}/review`）
- 去除 mock 操作。

### 2.3 用户端
- 首页、发布页帖子列表、我的帖子改为后端分页。
- 社区维度参数统一透传，保持联调一致。

### 2.4 回归脚本
- 新增 `scripts/regression_post_governance_flow.sh`
- 覆盖“创建帖子 -> 分页搜索 -> 后台下架 -> 前台隐藏 -> 后台恢复 -> 前台可见”。

## 3. 实施进度

1. [x] 完成后端分页查询与接口返回结构改造  
2. [x] 完成后台帖子管理页真实治理联调  
3. [x] 完成用户端帖子分页联调（首页/发布页/我的帖子）  
4. [x] 完成帖子治理 smoke 脚本  
5. [x] 完成 GitHub Actions smoke workflow 接入（`/.github/workflows/smoke-post-governance.yml`）

## 4. 验收清单

1. 启动后端与前端项目。
2. 访问 Swagger，验证以下接口支持分页参数：
   - `/api/posts`
   - `/api/posts/mine`
   - `/api/admin/posts`
3. 在后台帖子管理页执行“下架/恢复”，前台查询结果同步变化。
4. 运行脚本：

```bash
cd Blocksy
./scripts/regression_post_governance_flow.sh
```

预期：脚本输出 `post governance regression flow done.`

## 5. 下一步建议

1. 基于本轮分页能力，补帖子列表“时间范围 + 状态”筛选。
2. 将 `scripts/regression_post_governance_flow.sh` 接入 CI workflow（单独 PR）。
3. 再进入通知中心后台页（公告撤回/二次下发/趋势图 7/30 天）。

### 5.1 CI 触发说明（已接入）

- Workflow: `.github/workflows/smoke-post-governance.yml`
- 触发方式：
  - `push` 到 `main`（命中后端/脚本/workflow 路径）
  - `pull_request`（命中后端/脚本/workflow 路径）
  - `workflow_dispatch` 手动触发
- 核心动作：
  1. 拉起 PostgreSQL/Redis + MinIO
  2. 启动 `blocksy-server`
  3. 执行 `scripts/regression_post_governance_flow.sh`
  4. 上传 `server.log` 供失败排查
