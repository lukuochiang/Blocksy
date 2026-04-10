<template>
  <app-section>
    <h3 class="section-title">异常行为监控</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="处理状态">
        <el-select v-model="query.processStatus" clearable placeholder="全部" style="width: 150px">
          <el-option label="待处理" value="PENDING" />
          <el-option label="已处理" value="RESOLVED" />
          <el-option label="已忽略" value="IGNORED" />
        </el-select>
      </el-form-item>
      <el-form-item label="等级">
        <el-select v-model="query.level" clearable placeholder="全部" style="width: 140px">
          <el-option label="高" value="HIGH" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="低" value="LOW" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="类型/描述" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="anomalyType" label="异常类型" width="140" />
      <el-table-column prop="level" label="等级" width="100" />
      <el-table-column prop="details" label="详情" min-width="220" show-overflow-tooltip />
      <el-table-column prop="processStatus" label="状态" width="110" />
      <el-table-column label="更新时间" min-width="180">
        <template #default="{ row }">{{ formatDateTime(row.processedAt || row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="处理" width="210">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handle(row.id, 'RESOLVED')">标记处理</el-button>
          <el-button size="small" @click="handle(row.id, 'IGNORED')">忽略</el-button>
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
import { fetchRiskAnomalies, handleRiskAnomaly, type RiskAnomalyItem } from "../../api/risk";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<RiskAnomalyItem[]>([]);
const total = ref(0);
const query = reactive({
  processStatus: undefined as string | undefined,
  level: undefined as string | undefined,
  keyword: "",
  page: 1,
  pageSize: 10
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchRiskAnomalies({
      processStatus: query.processStatus,
      level: query.level,
      keyword: query.keyword.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载异常记录失败");
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

async function handle(id: number, processStatus: "RESOLVED" | "IGNORED") {
  try {
    await handleRiskAnomaly(id, {
      processStatus,
      handleNote: processStatus === "RESOLVED" ? "后台确认处理" : "判定低风险忽略"
    });
    ElMessage.success("处理成功");
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "处理失败");
  }
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
