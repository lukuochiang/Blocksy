<template>
  <app-section>
    <h3 class="section-title">分类信息管理</h3>
    <div class="stats-grid">
      <div v-for="item in statsRows" :key="item.category" class="stat-card">
        <div class="stat-title">{{ categoryLabel(item.category) }}</div>
        <div class="stat-main">总计 {{ item.totalCount }}</div>
        <div class="stat-sub">
          <span>待审 {{ item.pendingCount }}</span>
          <span>上架 {{ item.onlineCount }}</span>
          <span>下架 {{ item.offlineCount }}</span>
        </div>
      </div>
    </div>
    <div class="toolbar">
      <el-select v-model="filters.status" clearable placeholder="状态" style="width: 120px">
        <el-option :value="2" label="待审核" />
        <el-option :value="1" label="正常" />
        <el-option :value="0" label="下架" />
      </el-select>
      <el-select v-model="filters.category" clearable placeholder="分类" style="width: 140px">
        <el-option value="SECOND_HAND" label="二手交易" />
        <el-option value="LOST_FOUND" label="失物招领" />
        <el-option value="HELP_WANTED" label="求助求购" />
      </el-select>
      <el-input v-model="filters.keyword" clearable placeholder="关键词搜索标题/内容" style="width: 280px" />
      <el-select v-model="statsDays" style="width: 140px">
        <el-option :value="7" label="近7天统计" />
        <el-option :value="30" label="近30天统计" />
        <el-option :value="90" label="近90天统计" />
      </el-select>
      <el-button :loading="loading" type="primary" @click="loadData">刷新</el-button>
    </div>
    <div class="batch-row">
      <el-button size="small" type="success" :disabled="!selectedIds.length" @click="handleBatch('APPROVE')">批量通过</el-button>
      <el-button size="small" type="danger" :disabled="!selectedIds.length" @click="handleBatch('REJECT')">批量驳回</el-button>
      <el-button size="small" type="warning" :disabled="!selectedIds.length" @click="handleBatch('OFFLINE')">批量下架</el-button>
      <el-button size="small" :disabled="!selectedIds.length" @click="handleBatch('RESTORE')">批量恢复</el-button>
      <span class="batch-tip">已选 {{ selectedIds.length }} 条</span>
    </div>
    <el-table v-loading="loading" :data="rows" stripe border class="page-table" @selection-change="onSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column label="ID" prop="id" width="90" />
      <el-table-column label="标题" prop="title" min-width="260" />
      <el-table-column label="内容" prop="content" min-width="360" />
      <el-table-column label="分类" prop="category" width="140" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'info'">
            {{ row.status === 1 ? "正常" : row.status === 2 ? "待审核" : "下架" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handleRow(row, 'APPROVE')" :disabled="row.status !== 2">通过</el-button>
          <el-button size="small" type="danger" @click="handleRow(row, 'REJECT')" :disabled="row.status !== 2">驳回</el-button>
          <el-button size="small" type="warning" @click="handleRow(row, 'OFFLINE')" :disabled="row.status !== 1">下架</el-button>
          <el-button size="small" type="success" @click="handleRow(row, 'RESTORE')" :disabled="row.status !== 0">恢复</el-button>
          <el-button size="small" type="danger" @click="handleRow(row, 'DELETE')">删除</el-button>
          <el-button size="small" @click="openLogs(row.id)">日志</el-button>
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
    <el-card v-if="lastBatchResult" class="batch-result-card" shadow="never">
      <div class="batch-result-head">
        <strong>最近批量结果</strong>
        <div class="batch-result-actions">
          <el-button size="small" :disabled="!failedIds.length" @click="retryFailed">重试失败项</el-button>
          <el-button size="small" type="primary" :disabled="!failedIds.length" @click="exportRetryResult">重试并导出 CSV</el-button>
        </div>
      </div>
      <div class="batch-result-meta">
        总数 {{ lastBatchResult.total }}，成功 {{ lastBatchResult.successCount }}，失败 {{ lastBatchResult.failCount }}
      </div>
    </el-card>
    <el-dialog v-model="logDialogVisible" title="处理日志" width="700px">
      <el-table :data="logs" border>
        <el-table-column label="时间" prop="createdAt" min-width="180" />
        <el-table-column label="动作" prop="action" width="130" />
        <el-table-column label="操作人" prop="operatorUserId" width="110" />
        <el-table-column label="备注" prop="note" min-width="220" />
      </el-table>
    </el-dialog>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  batchHandleAdminListings,
  exportRetryBatchAdminListings,
  fetchAdminListingCategoryStats,
  fetchAdminListingLogs,
  fetchAdminListings,
  handleAdminListing,
  retryBatchAdminListings,
  type ListingCategoryStats,
  type ListingHandleLog,
  type ListingItem
} from "../../api/listing";
import AppSection from "../../components/AppSection.vue";

const loading = ref(false);
const rows = ref<ListingItem[]>([]);
const total = ref(0);
const logs = ref<ListingHandleLog[]>([]);
const statsRows = ref<ListingCategoryStats[]>([]);
const logDialogVisible = ref(false);
const filters = ref<{ status?: number; keyword?: string; category?: string }>({});
const selectedIds = ref<number[]>([]);
const statsDays = ref(30);
const query = ref({
  page: 1,
  pageSize: 20
});
const lastBatchResult = ref<{ total: number; successCount: number; failCount: number; items: Array<{ listingId: number; success: boolean; message: string }> } | null>(null);
const lastBatchAction = ref<"APPROVE" | "REJECT" | "OFFLINE" | "RESTORE" | "DELETE">("APPROVE");
const failedIds = ref<number[]>([]);

function categoryLabel(category: string): string {
  if (category === "SECOND_HAND") return "二手交易";
  if (category === "LOST_FOUND") return "失物招领";
  if (category === "HELP_WANTED") return "求助求购";
  return category;
}

function onSelectionChange(selection: ListingItem[]) {
  selectedIds.value = selection.map((item) => item.id);
}

async function loadData() {
  loading.value = true;
  try {
    const [listingRes, statsRes] = await Promise.all([
      fetchAdminListings({
        ...filters.value,
        page: query.value.page,
        pageSize: query.value.pageSize
      }),
      fetchAdminListingCategoryStats({ days: statsDays.value })
    ]);
    rows.value = listingRes.items;
    total.value = listingRes.total;
    statsRows.value = statsRes;
    selectedIds.value = [];
  } catch (error) {
    ElMessage.error(`加载分类信息失败: ${String(error)}`);
  } finally {
    loading.value = false;
  }
}

async function handleRow(row: ListingItem, action: "APPROVE" | "REJECT" | "OFFLINE" | "RESTORE" | "DELETE") {
  try {
    await handleAdminListing(row.id, { action, note: `后台${action}操作` });
    ElMessage.success("操作成功");
    await loadData();
  } catch (error) {
    ElMessage.error(`操作失败: ${String(error)}`);
  }
}

async function handleBatch(action: "APPROVE" | "REJECT" | "OFFLINE" | "RESTORE" | "DELETE") {
  if (!selectedIds.value.length) {
    ElMessage.warning("请先选择要操作的数据");
    return;
  }
  try {
    const res = await batchHandleAdminListings({
      listingIds: selectedIds.value,
      action,
      note: `后台批量${action}操作`
    });
    ElMessage.success(`批量处理完成：成功 ${res.successCount}，失败 ${res.failCount}`);
    lastBatchResult.value = res;
    lastBatchAction.value = action;
    failedIds.value = (res.items || []).filter((item) => !item.success && !!item.listingId).map((item) => item.listingId);
    await loadData();
  } catch (error) {
    ElMessage.error(`批量操作失败: ${String(error)}`);
  }
}

async function retryFailed() {
  if (!failedIds.value.length) {
    ElMessage.warning("暂无失败项");
    return;
  }
  try {
    const res = await retryBatchAdminListings({
      failedListingIds: failedIds.value,
      action: lastBatchAction.value,
      note: `重试${lastBatchAction.value}`
    });
    lastBatchResult.value = res;
    failedIds.value = (res.items || []).filter((item) => !item.success && !!item.listingId).map((item) => item.listingId);
    ElMessage.success(`重试完成：成功 ${res.successCount}，失败 ${res.failCount}`);
    await loadData();
  } catch (error) {
    ElMessage.error(`重试失败: ${String(error)}`);
  }
}

async function exportRetryResult() {
  if (!failedIds.value.length) {
    ElMessage.warning("暂无失败项");
    return;
  }
  try {
    const blob = await exportRetryBatchAdminListings({
      failedListingIds: failedIds.value,
      action: lastBatchAction.value,
      note: `重试导出${lastBatchAction.value}`
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "listing-batch-retry-result.csv";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  } catch (error) {
    ElMessage.error(`导出失败: ${String(error)}`);
  }
}

async function openLogs(id: number) {
  try {
    logs.value = await fetchAdminListingLogs(id);
    logDialogVisible.value = true;
  } catch (error) {
    ElMessage.error(`加载日志失败: ${String(error)}`);
  }
}

function onPageChange(page: number) {
  query.value.page = page;
  void loadData();
}

function onPageSizeChange(pageSize: number) {
  query.value.pageSize = pageSize;
  query.value.page = 1;
  void loadData();
}

onMounted(() => {
  void loadData();
});
</script>

<style scoped>
.section-title {
  margin: 0 0 12px;
}

.toolbar {
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stats-grid {
  margin-bottom: 12px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
}

.stat-card {
  border: 1px solid #dde6f5;
  border-radius: 10px;
  padding: 10px 12px;
  background: #f8fbff;
}

.stat-title {
  color: #57617a;
  font-size: 12px;
}

.stat-main {
  margin-top: 4px;
  color: #1f2a44;
  font-size: 18px;
  font-weight: 700;
}

.stat-sub {
  margin-top: 8px;
  color: #6a7389;
  font-size: 12px;
  display: flex;
  gap: 10px;
}

.batch-row {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.batch-tip {
  color: #6a7389;
  font-size: 12px;
}

.page-table {
  width: 100%;
}

.pager-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.batch-result-card {
  margin-top: 12px;
}

.batch-result-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.batch-result-actions {
  display: flex;
  gap: 8px;
}

.batch-result-meta {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;
}
</style>
