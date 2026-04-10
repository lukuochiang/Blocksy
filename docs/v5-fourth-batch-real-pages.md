# Blocksy V5 第四批真实页面替换（社区成员活跃/分类管理/媒体管理）

更新时间：2026-04-10

## 1. 本轮范围

1. 社区成员活跃（`/communities/engagement`）
2. 分类管理（`/categories`）
3. 媒体管理（`/media`）

## 2. 后端实现

### 2.1 新增迁移
- `V12__content_categories.sql`
  - `content_categories`

### 2.2 新增接口（`/api/admin/content`）
- `GET /communities/engagement` 社区活跃分页
- `GET /categories` 分类列表分页
- `POST /categories` 新增分类
- `POST /categories/{id}/toggle` 启停分类
- `GET /media/posts` 帖子媒体分页
- `POST /media/posts/{id}/offline` 媒体下架

### 2.3 初始化数据
- `DataInitializer` 补充默认内容分类种子（POST/LISTING）

## 3. 前端实现（blocksy-admin）

- 新增页面：
  - `views/community-engagement/index.vue`
  - `views/category-manage/index.vue`
  - `views/media-manage/index.vue`
- 新增 API：
  - `api/content-ops.ts`
- 路由替换：
  - `/communities/engagement`
  - `/categories`
  - `/media`

## 4. 验收建议

1. 社区成员活跃页能按关键词分页查看活跃分。
2. 分类管理页能新增分类并启停。
3. 媒体管理页能查询并下架帖子图片。
