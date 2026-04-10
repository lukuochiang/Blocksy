<template>
  <app-section>
    <h3 class="section-title">申诉处理</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="处理状态">
        <el-select v-model="query.processStatus" clearable placeholder="全部" style="width: 150px">
          <el-option label="待处理" value="PENDING" />
          <el-option label="通过" value="APPROVED" />
          <el-option label="驳回" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="申诉原因/内容" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="appealReason" label="申诉原因" width="150" />
      <el-table-column prop="appealContent" label="申诉内容" min-width="220" show-overflow-tooltip />
      <el-table-column prop="processStatus" label="状态" width="110" />
      <el-table-column label="更新时间" min-width="180">
        <template #default="{ row }">{{ formatDateTime(row.processedAt || row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="处理" width="220">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handle(row.id, 'APPROVED')">通过</el-button>
          <el-button size="small" type="danger" @click="handle(row.id, 'REJECTED')">驳回</el-button>
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
import { fetchRiskAppeals, handleRiskAppeal, type RiskAppealItem } from "../../api/risk";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<RiskAppealItem[]>([]);
const total = ref(0);
const query = reactive({
  processStatus: undefined as string | undefined,
  keyword: "",
  page: 1,
  pageSize: 10
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchRiskAppeals({
      processStatus: query.processStatus,
      keyword: query.keyword.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载申诉失败");
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

async function handle(id: number, processStatus: "APPROVED" | "REJECTED") {
  try {
    await handleRiskAppeal(id, {
      processStatus,
      resultNote: processStatus === "APPROVED" ? "申诉通过，已解除处罚" : "申诉驳回，维持原处罚"
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
