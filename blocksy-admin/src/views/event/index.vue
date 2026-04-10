<template>
  <app-section>
    <h3 class="section-title">活动管理</h3>
    <div class="toolbar">
      <el-select v-model="filters.status" clearable placeholder="状态" style="width: 120px">
        <el-option :value="1" label="正常" />
        <el-option :value="0" label="下架" />
      </el-select>
      <el-input v-model="filters.keyword" clearable placeholder="关键词搜索标题/内容" style="width: 280px" />
      <el-select v-model="actionForTemplate" placeholder="治理动作" style="width: 140px" @change="onActionForTemplateChange">
        <el-option label="OFFLINE" value="OFFLINE" />
        <el-option label="RESTORE" value="RESTORE" />
        <el-option label="DELETE" value="DELETE" />
      </el-select>
      <el-select v-model="noteTemplate" clearable placeholder="备注模板" style="width: 180px">
        <el-option v-for="item in noteTemplates" :key="item" :label="item" :value="item" />
      </el-select>
      <el-input v-model="manualNote" clearable placeholder="处理备注（可选）" style="width: 220px" />
      <el-button :loading="loading" type="primary" @click="loadData">刷新</el-button>
      <el-button :disabled="!selectedIds.length" type="warning" @click="batchHandle('OFFLINE')">批量下架</el-button>
      <el-button :disabled="!selectedIds.length" type="success" @click="batchHandle('RESTORE')">批量恢复</el-button>
      <el-button :disabled="!selectedIds.length" type="danger" @click="batchHandle('DELETE')">批量删除</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe border class="page-table" @selection-change="onSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column label="ID" prop="id" width="90" />
      <el-table-column label="标题" prop="title" min-width="260" />
      <el-table-column label="内容" prop="content" min-width="420" />
      <el-table-column label="开始时间" min-width="220">
        <template #default="{ row }">
          {{ formatDateTime(row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "正常" : "下架" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="warning" @click="handleRow(row, 'OFFLINE')" :disabled="row.status === 0">下架</el-button>
          <el-button size="small" type="success" @click="handleRow(row, 'RESTORE')" :disabled="row.status === 1">恢复</el-button>
          <el-button size="small" type="danger" @click="handleRow(row, 'DELETE')">删除</el-button>
          <el-button size="small" @click="openLogs(row.id)">日志</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-card v-if="lastBatchResult" class="batch-result" shadow="never">
      <template #header>
        <div class="batch-result-head">
          <span>批量处理结果</span>
          <div class="batch-result-actions">
            <el-button
              size="small"
              type="warning"
              :disabled="!lastBatchResult.failedItems.length"
              :loading="retryLoading"
              @click="retryFailed"
            >
              重试失败项
            </el-button>
            <el-button
              size="small"
              :disabled="!lastBatchResult.failedItems.length"
              :loading="exportingRetry"
              @click="exportRetryResult"
            >
              导出重试结果
            </el-button>
          </div>
        </div>
      </template>
      <div class="batch-result-summary">
        <el-tag type="info">总数 {{ lastBatchResult.totalCount }}</el-tag>
        <el-tag type="success">成功 {{ lastBatchResult.successCount }}</el-tag>
        <el-tag type="warning">跳过 {{ lastBatchResult.skippedIds.length }}</el-tag>
        <el-tag type="danger">失败 {{ lastBatchResult.failedItems.length }}</el-tag>
      </div>
      <el-table v-if="lastBatchResult.failedItems.length" :data="lastBatchResult.failedItems" border>
        <el-table-column prop="eventId" label="活动ID" width="120" />
        <el-table-column prop="message" label="失败原因" min-width="280" />
      </el-table>
    </el-card>
    <el-dialog v-model="logDialogVisible" title="处理日志" width="700px">
      <div class="toolbar">
        <el-input v-model.number="logFilters.operatorUserId" clearable placeholder="操作人ID" style="width: 140px" />
        <el-select v-model="logFilters.action" clearable placeholder="动作" style="width: 140px">
          <el-option label="OFFLINE" value="OFFLINE" />
          <el-option label="RESTORE" value="RESTORE" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
        <el-button @click="loadLogs">筛选</el-button>
        <el-button type="primary" @click="exportLogs">导出CSV</el-button>
      </div>
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
import { ElMessage, ElMessageBox } from "element-plus";
import {
  batchHandleAdminEvents,
  exportEventHandleLogsCsv,
  fetchAdminEventHandleLogs,
  fetchAdminEvents,
  handleAdminEvent,
  retryBatchHandleAdminEvents,
  retryBatchHandleAdminEventsExport,
  type EventHandleLog,
  type EventBatchHandleResponse,
  type EventItem
} from "../../api/event";
import { fetchOperationNoteTemplates } from "../../api/operation-note-template";
import AppSection from "../../components/AppSection.vue";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<EventItem[]>([]);
const logs = ref<EventHandleLog[]>([]);
const logDialogVisible = ref(false);
const filters = ref<{ status?: number; keyword?: string }>({});
const selectedIds = ref<number[]>([]);
const noteTemplates = ref<string[]>([]);
const noteTemplate = ref("");
const manualNote = ref("");
const actionForTemplate = ref<"OFFLINE" | "RESTORE" | "DELETE">("OFFLINE");
const lastBatchAction = ref<"OFFLINE" | "RESTORE" | "DELETE">("OFFLINE");
const lastBatchResult = ref<EventBatchHandleResponse | null>(null);
const retryLoading = ref(false);
const exportingRetry = ref(false);
const currentLogEventId = ref<number | null>(null);
const logFilters = ref<{ operatorUserId?: number; action?: string }>({});

function onSelectionChange(selection: EventItem[]) {
  selectedIds.value = selection.map((item) => item.id);
}

function resolveNote(action: "OFFLINE" | "RESTORE" | "DELETE") {
  if (manualNote.value?.trim()) {
    return manualNote.value.trim();
  }
  if (noteTemplate.value) {
    return noteTemplate.value;
  }
  return `后台${action}操作`;
}

async function loadNoteTemplates() {
  const templates = await fetchOperationNoteTemplates({
    module: "EVENT",
    action: actionForTemplate.value,
    status: 1
  });
  noteTemplates.value = templates.map((item) => item.content);
  if (noteTemplates.value.length && !noteTemplate.value) {
    noteTemplate.value = noteTemplates.value[0];
  }
}

async function onActionForTemplateChange() {
  noteTemplate.value = "";
  await loadNoteTemplates();
}

async function loadData() {
  loading.value = true;
  try {
    rows.value = await fetchAdminEvents(filters.value);
    await loadNoteTemplates();
  } catch (error) {
    ElMessage.error(`加载活动失败: ${String(error)}`);
  } finally {
    loading.value = false;
  }
}

async function handleRow(row: EventItem, action: "OFFLINE" | "RESTORE" | "DELETE") {
  try {
    await handleAdminEvent(row.id, { action, note: resolveNote(action) });
    ElMessage.success("操作成功");
    await loadData();
  } catch (error) {
    ElMessage.error(`操作失败: ${String(error)}`);
  }
}

async function batchHandle(action: "OFFLINE" | "RESTORE" | "DELETE") {
  if (!selectedIds.value.length) {
    ElMessage.warning("请先选择活动");
    return;
  }
  try {
    const result = await batchHandleAdminEvents({
      eventIds: selectedIds.value,
      action,
      note: resolveNote(action)
    });
    lastBatchAction.value = action;
    lastBatchResult.value = result;
    await ElMessageBox.alert(
      `总数：${result.totalCount}\n成功：${result.successCount}\n跳过：${result.skippedIds.length}\n失败：${result.failedItems.length}`,
      "批量处理结果",
      { confirmButtonText: "知道了" }
    );
    selectedIds.value = [];
    await loadData();
  } catch (error) {
    ElMessage.error(`批量处理失败: ${String(error)}`);
  }
}

async function retryFailed() {
  if (!lastBatchResult.value || !lastBatchResult.value.failedItems.length) {
    ElMessage.warning("没有失败项可重试");
    return;
  }
  retryLoading.value = true;
  try {
    const failedEventIds = lastBatchResult.value.failedItems
      .map((item) => item.eventId)
      .filter((id): id is number => typeof id === "number");
    const retried = await retryBatchHandleAdminEvents({
      failedEventIds,
      action: lastBatchAction.value,
      note: resolveNote(lastBatchAction.value)
    });
    lastBatchResult.value = retried;
    ElMessage.success(`重试完成：成功 ${retried.successCount}，失败 ${retried.failedItems.length}`);
    await loadData();
  } catch (error) {
    ElMessage.error(`重试失败: ${String(error)}`);
  } finally {
    retryLoading.value = false;
  }
}

async function exportRetryResult() {
  if (!lastBatchResult.value || !lastBatchResult.value.failedItems.length) {
    ElMessage.warning("没有失败项可导出");
    return;
  }
  exportingRetry.value = true;
  try {
    const failedEventIds = lastBatchResult.value.failedItems
      .map((item) => item.eventId)
      .filter((id): id is number => typeof id === "number");
    const blob = await retryBatchHandleAdminEventsExport({
      failedEventIds,
      action: lastBatchAction.value,
      note: resolveNote(lastBatchAction.value)
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "event-batch-retry-result.csv";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  } catch (error) {
    ElMessage.error(`导出失败: ${String(error)}`);
  } finally {
    exportingRetry.value = false;
  }
}

async function openLogs(id: number) {
  currentLogEventId.value = id;
  logFilters.value = {};
  logDialogVisible.value = true;
  await loadLogs();
}

async function loadLogs() {
  if (!currentLogEventId.value) {
    return;
  }
  try {
    logs.value = await fetchAdminEventHandleLogs({
      eventId: currentLogEventId.value,
      operatorUserId: logFilters.value.operatorUserId,
      action: logFilters.value.action
    });
  } catch (error) {
    ElMessage.error(`加载日志失败: ${String(error)}`);
  }
}

async function exportLogs() {
  if (!currentLogEventId.value) {
    return;
  }
  try {
    const blob = await exportEventHandleLogsCsv({
      eventId: currentLogEventId.value,
      operatorUserId: logFilters.value.operatorUserId,
      action: logFilters.value.action
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "event-handle-logs.csv";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  } catch (error) {
    ElMessage.error(`导出失败: ${String(error)}`);
  }
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
}

.page-table {
  width: 100%;
}
.batch-result {
  margin-top: 12px;
}
.batch-result-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.batch-result-actions {
  display: flex;
  gap: 8px;
}
.batch-result-summary {
  margin-bottom: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
