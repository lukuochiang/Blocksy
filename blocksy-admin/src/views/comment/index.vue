<template>
  <app-section>
    <h3 class="section-title">评论管理</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="帖子ID">
        <el-input v-model.number="query.postId" placeholder="例如 1001" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option label="正常" :value="1" />
          <el-option label="已删除" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="评论内容关键词" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadRows">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border class="page-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="postId" label="帖子ID" width="100" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="content" label="评论内容" min-width="380" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "正常" : "已删除" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="200">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" :disabled="row.status === 0" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { deleteAdminComment, fetchAdminComments, type AdminCommentItem } from "../../api/comment";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<AdminCommentItem[]>([]);
const query = reactive<{
  postId?: number;
  status?: number;
  keyword?: string;
}>({
  postId: undefined,
  status: undefined,
  keyword: ""
});

async function loadRows() {
  loading.value = true;
  try {
    rows.value = await fetchAdminComments({
      postId: query.postId || undefined,
      status: query.status,
      keyword: query.keyword?.trim() || undefined
    });
  } catch (error) {
    ElMessage.error((error as Error).message || "加载评论失败");
  } finally {
    loading.value = false;
  }
}

async function remove(commentId: number) {
  try {
    await deleteAdminComment(commentId);
    ElMessage.success("删除成功");
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "删除失败");
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
