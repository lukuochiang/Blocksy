<template>
  <app-section>
    <h3 class="section-title">帖子管理</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option label="正常" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="社区ID">
        <el-input v-model.number="query.communityId" placeholder="例如 1" clearable />
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="帖子内容关键词" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="loadPosts">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="posts" border class="page-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="作者ID" width="100" />
      <el-table-column prop="communityId" label="社区ID" width="100" />
      <el-table-column label="内容" min-width="280">
        <template #default="{ row }">
          <div class="content">{{ row.content }}</div>
          <div v-if="row.media?.length" class="media">
            <el-image
              v-for="item in row.media"
              :key="item.objectKey"
              :src="item.url"
              fit="cover"
              class="thumb"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="commentCount" label="评论数" width="90" />
      <el-table-column prop="likeCount" label="点赞数" width="90" />
      <el-table-column label="创建时间" min-width="220">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="success" :disabled="row.status === 1" @click="review(row.id, 'APPROVE')">恢复</el-button>
          <el-button size="small" type="danger" :disabled="row.status === 0" @click="review(row.id, 'REJECT')">下架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager-wrap">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :total="total"
        :current-page="query.page"
        :page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        @current-change="onPageChange"
        @size-change="onPageSizeChange"
      />
    </div>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchAdminPosts, reviewAdminPost, type AdminPostItem } from "../../api/admin-post";
import { formatDateTime } from "../../utils/datetime";

const posts = ref<AdminPostItem[]>([]);
const loading = ref(false);
const total = ref(0);
const query = reactive<{
  status?: number;
  communityId?: number;
  keyword?: string;
  page: number;
  pageSize: number;
}>({
  status: undefined,
  communityId: undefined,
  keyword: "",
  page: 1,
  pageSize: 10
});

async function loadPosts() {
  loading.value = true;
  try {
    const response = await fetchAdminPosts({
      status: query.status,
      communityId: query.communityId || undefined,
      keyword: query.keyword?.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    posts.value = response.items || [];
    total.value = response.total || 0;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载帖子失败");
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  query.page = 1;
  void loadPosts();
}

function onPageChange(page: number) {
  query.page = page;
  void loadPosts();
}

function onPageSizeChange(size: number) {
  query.pageSize = size;
  query.page = 1;
  void loadPosts();
}

async function review(postId: number, action: "APPROVE" | "REJECT") {
  try {
    await reviewAdminPost(postId, action);
    ElMessage.success(action === "APPROVE" ? "已恢复" : "已下架");
    await loadPosts();
  } catch (error) {
    ElMessage.error((error as Error).message || "操作失败");
  }
}

onMounted(() => {
  void loadPosts();
});
</script>

<style scoped>
.section-title {
  margin: 0 0 12px;
}

.toolbar {
  margin-bottom: 12px;
}
.page-table {
  width: 100%;
}

.content {
  margin-bottom: 8px;
  white-space: pre-wrap;
}

.media {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.thumb {
  width: 56px;
  height: 56px;
  border-radius: 4px;
}

.pager-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
