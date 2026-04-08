<template>
  <app-section>
    <h3 class="section-title">用户管理</h3>
    <div class="toolbar">
      <el-button type="primary" @click="loadUsers">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="users" border class="page-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="220" />
      <el-table-column prop="nickname" label="昵称" min-width="220" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? "正常" : "封禁" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="260">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="danger" @click="doBan(row.id)">封禁</el-button>
          <el-button v-else size="small" type="success" @click="doUnban(row.id)">解封</el-button>
        </template>
      </el-table-column>
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { banUser, fetchUsers, unbanUser, UserItem } from "../../api/user";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const users = ref<UserItem[]>([]);

async function loadUsers() {
  loading.value = true;
  try {
    users.value = await fetchUsers();
  } catch (error) {
    ElMessage.error((error as Error).message || "加载失败");
  } finally {
    loading.value = false;
  }
}

async function doBan(userId: number) {
  try {
    await banUser(userId);
    ElMessage.success("封禁成功");
    await loadUsers();
  } catch (error) {
    ElMessage.error((error as Error).message || "封禁失败");
  }
}

async function doUnban(userId: number) {
  try {
    await unbanUser(userId);
    ElMessage.success("解封成功");
    await loadUsers();
  } catch (error) {
    ElMessage.error((error as Error).message || "解封失败");
  }
}

onMounted(() => {
  loadUsers();
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
</style>
