<template>
  <app-section>
    <h3 class="section-title">黑名单管理</h3>
    <div class="toolbar">
      <el-button type="primary" :loading="loading" @click="loadRows">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="用户ID" width="90" />
      <el-table-column prop="username" label="用户名" min-width="220" />
      <el-table-column prop="nickname" label="昵称" min-width="180" />
      <el-table-column label="封禁原因" min-width="260">
        <template #default="{ row }">{{ row.banReason || "-" }}</template>
      </el-table-column>
      <el-table-column label="封禁时间" min-width="180">
        <template #default="{ row }">{{ formatDateTime(row.bannedAt) }}</template>
      </el-table-column>
      <el-table-column label="到期时间" min-width="180">
        <template #default="{ row }">{{ row.bannedUntil ? formatDateTime(row.bannedUntil) : "永久" }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="onUnban(row.id)">解封</el-button>
        </template>
      </el-table-column>
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchBlacklistedUsers, type UserItem, unbanUser } from "../../api/user";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<UserItem[]>([]);

async function loadRows() {
  loading.value = true;
  try {
    rows.value = await fetchBlacklistedUsers();
  } catch (error) {
    ElMessage.error((error as Error).message || "加载黑名单失败");
  } finally {
    loading.value = false;
  }
}

async function onUnban(userId: number) {
  try {
    await unbanUser(userId, { reason: "黑名单管理页解封" });
    ElMessage.success("解封成功");
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "解封失败");
  }
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.section-title { margin: 0 0 12px; }
.toolbar { margin-bottom: 12px; }
</style>
