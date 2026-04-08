<template>
  <app-section>
    <h3 class="section-title">举报管理</h3>
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="处理状态" style="width: 180px">
        <el-option label="全部" value="" />
        <el-option label="待处理" value="PENDING" />
        <el-option label="已通过" value="RESOLVED" />
        <el-option label="已驳回" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="loadReports">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="reports" border class="page-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="reporterUserId" label="举报人" width="110" />
      <el-table-column prop="targetType" label="目标类型" width="120" />
      <el-table-column prop="targetId" label="目标ID" width="110" />
      <el-table-column prop="reason" label="原因" min-width="320" />
      <el-table-column prop="processStatus" label="状态" width="100" />
      <el-table-column label="创建时间" min-width="220">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="resolve(row.id, false)">通过</el-button>
          <el-button size="small" type="warning" @click="resolve(row.id, true)">通过并封禁</el-button>
          <el-button size="small" type="danger" @click="reject(row.id)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchReports, handleReport, ReportItem } from "../../api/report";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const statusFilter = ref("");
const reports = ref<ReportItem[]>([]);

async function loadReports() {
  loading.value = true;
  try {
    reports.value = await fetchReports(statusFilter.value || undefined);
  } catch (error) {
    ElMessage.error((error as Error).message || "加载失败");
  } finally {
    loading.value = false;
  }
}

async function resolve(reportId: number, banTargetUser: boolean) {
  try {
    await handleReport(reportId, "RESOLVED", banTargetUser);
    ElMessage.success("处理成功");
    await loadReports();
  } catch (error) {
    ElMessage.error((error as Error).message || "处理失败");
  }
}

async function reject(reportId: number) {
  try {
    await handleReport(reportId, "REJECTED");
    ElMessage.success("处理成功");
    await loadReports();
  } catch (error) {
    ElMessage.error((error as Error).message || "处理失败");
  }
}

onMounted(() => {
  loadReports();
});
</script>

<style scoped>
.section-title {
  margin: 0 0 12px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.page-table {
  width: 100%;
}
</style>
