<template>
  <app-section>
    <h3 class="section-title">操作日志</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="模块">
        <el-input v-model="query.module" placeholder="PERMISSION/RISK" clearable />
      </el-form-item>
      <el-form-item label="动作">
        <el-input v-model="query.action" placeholder="ASSIGN_DATA_PERMISSION" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="module" label="模块" width="140" />
      <el-table-column prop="action" label="动作" width="220" />
      <el-table-column prop="operatorUserId" label="操作人ID" width="120" />
      <el-table-column prop="targetType" label="目标类型" width="120" />
      <el-table-column prop="targetId" label="目标ID" width="120" />
      <el-table-column prop="details" label="详情" min-width="260" show-overflow-tooltip />
      <el-table-column label="时间" min-width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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
import { fetchAdminOperationLogs, type AdminOperationLogItem } from "../../api/permission";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<AdminOperationLogItem[]>([]);
const total = ref(0);
const query = reactive({
  module: "",
  action: "",
  page: 1,
  pageSize: 20
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchAdminOperationLogs({
      module: query.module.trim() || undefined,
      action: query.action.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载操作日志失败");
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  query.page = 1;
  void loadRows();
}

function onPageChange(page: number) {
  query.page = page;
  void loadRows();
}

function onPageSizeChange(size: number) {
  query.pageSize = size;
  query.page = 1;
  void loadRows();
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.section-title { margin: 0 0 12px; }
.toolbar { margin-bottom: 12px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
