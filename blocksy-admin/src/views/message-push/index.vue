<template>
  <app-section>
    <h3 class="section-title">Push 推送</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="状态">
        <el-select v-model="query.taskStatus" clearable placeholder="全部" style="width: 150px">
          <el-option label="待发送" value="PENDING" />
          <el-option label="已发送" value="SENT" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="标题/内容关键词" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="openCreateDialog">新建任务</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="content" label="内容" min-width="280" show-overflow-tooltip />
      <el-table-column prop="targetType" label="目标人群" width="120" />
      <el-table-column prop="taskStatus" label="状态" width="120" />
      <el-table-column label="发送时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.sentAt) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" :disabled="row.taskStatus === 'SENT'" @click="send(row.id)">立即下发</el-button>
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

    <h4 class="sub-title">通知模板</h4>
    <el-form inline :model="templateQuery" class="toolbar">
      <el-form-item label="模块">
        <el-input v-model="templateQuery.module" clearable placeholder="如 REPORT / EVENT" />
      </el-form-item>
      <el-form-item label="启用">
        <el-select v-model="templateQuery.enabled" clearable placeholder="全部" style="width: 120px">
          <el-option label="启用" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="loadTemplates">刷新模板</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="templateRows" border>
      <el-table-column prop="module" label="模块" width="130" />
      <el-table-column prop="triggerCode" label="触发场景" width="160" />
      <el-table-column prop="titleTemplate" label="标题模板" min-width="180" />
      <el-table-column prop="contentTemplate" label="内容模板" min-width="260" show-overflow-tooltip />
      <el-table-column label="启用" width="100">
        <template #default="{ row }">{{ row.enabled ? "是" : "否" }}</template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="createVisible" title="新建 Push 任务" width="520px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="createForm.title" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="createForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="人群">
          <el-select v-model="createForm.targetType" style="width: 100%">
            <el-option label="全部用户" value="ALL" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import {
  createPushTask,
  fetchNotificationTemplates,
  fetchPushTasks,
  sendPushTask,
  type NotificationTemplateItem,
  type PushTaskItem
} from "../../api/admin-messaging";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<PushTaskItem[]>([]);
const total = ref(0);
const templateRows = ref<NotificationTemplateItem[]>([]);
const createVisible = ref(false);
const creating = ref(false);

const query = reactive({
  taskStatus: undefined as string | undefined,
  keyword: "",
  page: 1,
  pageSize: 10
});
const templateQuery = reactive({
  module: "",
  enabled: undefined as boolean | undefined
});
const createForm = reactive({
  title: "",
  content: "",
  targetType: "ALL"
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchPushTasks({
      taskStatus: query.taskStatus,
      keyword: query.keyword.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载任务失败");
  } finally {
    loading.value = false;
  }
}

async function loadTemplates() {
  try {
    const data = await fetchNotificationTemplates({
      module: templateQuery.module.trim() || undefined,
      enabled: templateQuery.enabled,
      page: 1,
      pageSize: 50
    });
    templateRows.value = data.items;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载模板失败");
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
function openCreateDialog() {
  createVisible.value = true;
}

async function submitCreate() {
  if (!createForm.title.trim() || !createForm.content.trim()) {
    ElMessage.warning("标题和内容不能为空");
    return;
  }
  creating.value = true;
  try {
    await createPushTask({
      title: createForm.title.trim(),
      content: createForm.content.trim(),
      targetType: createForm.targetType
    });
    ElMessage.success("任务创建成功");
    createVisible.value = false;
    createForm.title = "";
    createForm.content = "";
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "创建失败");
  } finally {
    creating.value = false;
  }
}

async function send(id: number) {
  try {
    const count = await sendPushTask(id);
    ElMessage.success(`已下发，发送记录 ${count} 条`);
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "下发失败");
  }
}

onMounted(() => {
  void loadRows();
  void loadTemplates();
});
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.pager-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.sub-title {
  margin: 18px 0 10px;
  font-size: 16px;
}
</style>
