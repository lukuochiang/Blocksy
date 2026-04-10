<template>
  <app-section>
    <h3 class="section-title">备注模板管理</h3>
    <div class="toolbar">
      <el-select v-model="filters.module" clearable placeholder="模块" style="width: 140px">
        <el-option v-for="item in ruleOptions" :key="item.module" :label="item.module" :value="item.module" />
      </el-select>
      <el-select v-model="filters.action" clearable placeholder="动作" style="width: 140px">
        <el-option v-for="action in filterActions" :key="action" :label="action" :value="action" />
      </el-select>
      <el-select v-model="filters.status" clearable placeholder="状态" style="width: 120px">
        <el-option :value="1" label="启用" />
        <el-option :value="0" label="禁用" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="loadData">刷新</el-button>
      <el-button type="success" :disabled="!canManage" @click="openCreate">新增模板</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe class="page-table">
      <el-table-column label="ID" prop="id" width="80" />
      <el-table-column label="模块" prop="module" width="110" />
      <el-table-column label="动作" prop="action" width="120" />
      <el-table-column label="模板内容" prop="content" min-width="360" />
      <el-table-column label="排序" prop="sortNo" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "启用" : "禁用" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggleStatus(row)">
            {{ row.status === 1 ? "禁用" : "启用" }}
          </el-button>
          <el-button size="small" @click="openLogs(row.id)">日志</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑模板' : '新增模板'" width="620px">
      <el-form label-width="90px">
        <el-form-item label="模块">
          <el-select v-model="form.module" placeholder="选择模块" @change="onFormModuleChange">
            <el-option v-for="item in ruleOptions" :key="item.module" :label="item.module" :value="item.module" />
          </el-select>
        </el-form-item>
        <el-form-item label="动作">
          <el-select v-model="form.action" placeholder="选择动作">
            <el-option v-for="action in formActions" :key="action" :label="action" :value="action" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入模板内容" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logDialogVisible" title="模板操作日志" width="760px">
      <div class="toolbar">
        <el-input v-model.number="logFilters.templateId" clearable placeholder="对象(templateId)" style="width: 160px" />
        <el-input v-model.number="logFilters.operatorUserId" clearable placeholder="操作人ID" style="width: 140px" />
        <el-select v-model="logFilters.action" clearable placeholder="动作" style="width: 140px">
          <el-option label="CREATE" value="CREATE" />
          <el-option label="UPDATE" value="UPDATE" />
          <el-option label="ENABLE" value="ENABLE" />
          <el-option label="DISABLE" value="DISABLE" />
        </el-select>
        <el-date-picker
          v-model="logTimeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
        />
        <el-button @click="loadLogs">筛选</el-button>
      </div>
      <el-table v-loading="logLoading" :data="logRows" border>
        <el-table-column label="时间" prop="createdAt" min-width="180" />
        <el-table-column label="动作" prop="action" width="120" />
        <el-table-column label="操作人" prop="operatorUserId" width="110" />
        <el-table-column label="备注" prop="note" min-width="260" />
      </el-table>
    </el-dialog>
  </app-section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import {
  createOperationNoteTemplate,
  fetchOperationNoteTemplates,
  fetchOperationNoteTemplateLogsWithFilter,
  fetchOperationNoteTemplateRules,
  getOperationNoteTemplatePermission,
  type OperationNoteTemplateRuleItem,
  type OperationNoteTemplateLogItem,
  type OperationNoteTemplateItem,
  updateOperationNoteTemplate,
  updateOperationNoteTemplateStatus
} from "../../api/operation-note-template";

const loading = ref(false);
const saving = ref(false);
const logLoading = ref(false);
const rows = ref<OperationNoteTemplateItem[]>([]);
const logRows = ref<OperationNoteTemplateLogItem[]>([]);
const ruleOptions = ref<OperationNoteTemplateRuleItem[]>([]);
const filters = reactive<{ module?: string; action?: string; status?: number }>({});
const dialogVisible = ref(false);
const logDialogVisible = ref(false);
const editingId = ref<number | null>(null);
const canManage = ref(false);
const logFilters = reactive<{ templateId?: number; operatorUserId?: number; action?: string }>({});
const logTimeRange = ref<[string, string] | []>([]);

const form = reactive({
  module: "EVENT",
  action: "OFFLINE",
  content: "",
  sortNo: 10,
  enabled: true
});

const filterActions = computed(() => {
  if (!filters.module) {
    return ruleOptions.value.flatMap((item) => item.actions);
  }
  return ruleOptions.value.find((item) => item.module === filters.module)?.actions ?? [];
});

const formActions = computed(() => {
  return ruleOptions.value.find((item) => item.module === form.module)?.actions ?? [];
});

function resetForm() {
  const firstModule = ruleOptions.value[0]?.module || "EVENT";
  const firstAction = ruleOptions.value.find((item) => item.module === firstModule)?.actions[0] || "OFFLINE";
  form.module = firstModule;
  form.action = firstAction;
  form.content = "";
  form.sortNo = 10;
  form.enabled = true;
}

function onFormModuleChange() {
  const firstAction = formActions.value[0];
  if (firstAction) {
    form.action = firstAction;
  }
}

function openCreate() {
  if (!canManage.value) {
    ElMessage.warning("当前账号无模板管理权限");
    return;
  }
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(row: OperationNoteTemplateItem) {
  if (!canManage.value) {
    ElMessage.warning("当前账号无模板管理权限");
    return;
  }
  editingId.value = row.id;
  form.module = row.module;
  form.action = row.action;
  form.content = row.content;
  form.sortNo = row.sortNo ?? 0;
  form.enabled = row.status === 1;
  dialogVisible.value = true;
}

async function loadData() {
  loading.value = true;
  try {
    canManage.value = await getOperationNoteTemplatePermission();
    ruleOptions.value = await fetchOperationNoteTemplateRules();
    if (filters.module && !ruleOptions.value.some((item) => item.module === filters.module)) {
      filters.module = undefined;
      filters.action = undefined;
    }
    if (filters.action && filters.module && !filterActions.value.includes(filters.action)) {
      filters.action = undefined;
    }
    rows.value = await fetchOperationNoteTemplates(filters);
  } catch (error) {
    ElMessage.error(`加载失败: ${String(error)}`);
  } finally {
    loading.value = false;
  }
}

async function submit() {
  if (!canManage.value) {
    ElMessage.warning("当前账号无模板管理权限");
    return;
  }
  if (!form.content.trim()) {
    ElMessage.warning("模板内容不能为空");
    return;
  }
  saving.value = true;
  try {
    const payload = {
      module: form.module.trim().toUpperCase(),
      action: form.action.trim().toUpperCase(),
      content: form.content.trim(),
      sortNo: form.sortNo,
      status: form.enabled ? 1 : 0
    };
    if (editingId.value) {
      await updateOperationNoteTemplate(editingId.value, payload);
    } else {
      await createOperationNoteTemplate(payload);
    }
    ElMessage.success("保存成功");
    dialogVisible.value = false;
    await loadData();
  } catch (error) {
    ElMessage.error(`保存失败: ${String(error)}`);
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(row: OperationNoteTemplateItem) {
  if (!canManage.value) {
    ElMessage.warning("当前账号无模板管理权限");
    return;
  }
  try {
    await updateOperationNoteTemplateStatus(row.id, row.status === 1 ? 0 : 1);
    ElMessage.success("状态更新成功");
    await loadData();
  } catch (error) {
    ElMessage.error(`状态更新失败: ${String(error)}`);
  }
}

async function openLogs(templateId: number) {
  logDialogVisible.value = true;
  logFilters.templateId = templateId;
  logFilters.operatorUserId = undefined;
  logFilters.action = undefined;
  logTimeRange.value = [];
  await loadLogs();
}

async function loadLogs() {
  logLoading.value = true;
  try {
    const startAt = Array.isArray(logTimeRange.value) && logTimeRange.value.length === 2 ? logTimeRange.value[0] : undefined;
    const endAt = Array.isArray(logTimeRange.value) && logTimeRange.value.length === 2 ? logTimeRange.value[1] : undefined;
    logRows.value = await fetchOperationNoteTemplateLogsWithFilter({
      templateId: logFilters.templateId,
      operatorUserId: logFilters.operatorUserId,
      action: logFilters.action,
      startAt,
      endAt
    });
  } catch (error) {
    ElMessage.error(`日志加载失败: ${String(error)}`);
  } finally {
    logLoading.value = false;
  }
}

onMounted(() => {
  void loadData();
});

watch(
  () => filters.module,
  () => {
    if (filters.action && !filterActions.value.includes(filters.action)) {
      filters.action = undefined;
    }
  }
);
</script>

<style scoped>
.section-title { margin: 0 0 12px; }
.toolbar { display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
.page-table { width: 100%; }
</style>
