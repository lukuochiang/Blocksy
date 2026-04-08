<template>
  <app-section>
    <h3 class="section-title">帖子审核</h3>
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
        <el-button type="primary" @click="loadRows">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border class="page-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="作者ID" width="100" />
      <el-table-column prop="communityId" label="社区ID" width="100" />
      <el-table-column prop="content" label="帖子内容" min-width="420" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? "正常" : "下架" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="commentCount" label="评论数" width="100" />
      <el-table-column label="创建时间" min-width="200">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="success" :disabled="row.status === 1" @click="review(row.id, 'APPROVE')">通过</el-button>
          <el-button size="small" type="danger" :disabled="row.status === 0" @click="review(row.id, 'REJECT')">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchAdminPosts, reviewAdminPost, type AdminPostItem } from "../../api/admin-post";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<AdminPostItem[]>([]);
const query = reactive<{
  status?: number;
  communityId?: number;
  keyword?: string;
}>({
  status: undefined,
  communityId: undefined,
  keyword: ""
});

async function loadRows() {
  loading.value = true;
  try {
    rows.value = await fetchAdminPosts({
      status: query.status,
      communityId: query.communityId || undefined,
      keyword: query.keyword?.trim() || undefined
    });
  } catch (error) {
    ElMessage.error((error as Error).message || "加载帖子审核列表失败");
  } finally {
    loading.value = false;
  }
}

async function review(postId: number, action: "APPROVE" | "REJECT") {
  try {
    await reviewAdminPost(postId, action);
    ElMessage.success(action === "APPROVE" ? "审核通过" : "已下架");
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "审核失败");
  }
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}

.page-table {
  width: 100%;
}
</style>
