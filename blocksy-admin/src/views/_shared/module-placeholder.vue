<template>
  <app-section>
    <div class="head">
      <h3 class="title">{{ title }}</h3>
      <p class="desc">{{ description }}</p>
    </div>

    <div class="stat-grid">
      <el-card shadow="never" class="stat-card">
        <div class="stat-label">执行项总数</div>
        <div class="stat-value">{{ taskRows.length }}</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-label">进行中</div>
        <div class="stat-value">{{ taskRows.filter((item) => item.status === "DOING").length }}</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-label">已完成</div>
        <div class="stat-value">{{ taskRows.filter((item) => item.status === "DONE").length }}</div>
      </el-card>
    </div>

    <el-form inline :model="filters" class="toolbar">
      <el-form-item label="关键词">
        <el-input v-model="filters.keyword" placeholder="执行项名称/说明" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px">
          <el-option label="待开始" value="TODO" />
          <el-option label="进行中" value="DOING" />
          <el-option label="已完成" value="DONE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="openCreateDialog">新增执行项</el-button>
        <el-button @click="resetToDefaults">重置默认项</el-button>
      </el-form-item>
    </el-form>

    <div class="cards">
      <el-card v-for="item in features" :key="item.name" shadow="hover" class="feature-card">
        <h4>{{ item.name }}</h4>
        <p>{{ item.usage }}</p>
        <p class="ops"><strong>典型操作：</strong>{{ item.action }}</p>
        <p class="role"><strong>涉及角色：</strong>{{ item.role }}</p>
      </el-card>
    </div>

    <el-table :data="filteredRows" border class="table">
      <el-table-column prop="name" label="执行项" min-width="180" />
      <el-table-column prop="usage" label="说明" min-width="260" show-overflow-tooltip />
      <el-table-column prop="owner" label="负责人" width="120" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="180">
        <template #default="{ row }">{{ row.updatedAt }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="moveStatus(row, 'TODO')">待开始</el-button>
          <el-button size="small" type="warning" @click="moveStatus(row, 'DOING')">进行中</el-button>
          <el-button size="small" type="success" @click="moveStatus(row, 'DONE')">完成</el-button>
          <el-button size="small" type="danger" @click="removeTask(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="createVisible" title="新增执行项" width="520px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="执行项">
          <el-input v-model="createForm.name" maxlength="60" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="createForm.usage" type="textarea" :rows="3" maxlength="300" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="createForm.owner" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="createTask">创建</el-button>
      </template>
    </el-dialog>
  </app-section>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";

interface FeatureItem {
  name: string;
  usage: string;
  action: string;
  role: string;
}

interface TaskItem {
  id: number;
  name: string;
  usage: string;
  owner: string;
  status: "TODO" | "DOING" | "DONE";
  updatedAt: string;
}

const route = useRoute();

const title = computed(() => String(route.meta.title || "模块页面"));
const description = computed(() => String(route.meta.description || ""));
const features = computed<FeatureItem[]>(() => (route.meta.features as FeatureItem[]) || []);

const filters = reactive<{ keyword: string; status?: "TODO" | "DOING" | "DONE" }>({
  keyword: "",
  status: undefined
});

const createVisible = ref(false);
const createForm = reactive({
  name: "",
  usage: "",
  owner: ""
});

const storageKey = computed(() => `blocksy_admin_ops_${route.path}`);
const taskRows = ref<TaskItem[]>([]);

function nowString(): string {
  return new Date().toISOString().slice(0, 19).replace("T", " ");
}

function toDefaultTasks(): TaskItem[] {
  return features.value.map((item, index) => ({
    id: Date.now() + index,
    name: item.name,
    usage: `${item.usage} / ${item.action}`,
    owner: item.role.split("、")[0] || "运营",
    status: "TODO",
    updatedAt: nowString()
  }));
}

function loadTasks() {
  const raw = localStorage.getItem(storageKey.value);
  if (raw) {
    try {
      const parsed = JSON.parse(raw) as TaskItem[];
      taskRows.value = Array.isArray(parsed) ? parsed : toDefaultTasks();
      return;
    } catch {
      taskRows.value = toDefaultTasks();
      return;
    }
  }
  taskRows.value = toDefaultTasks();
}

function persist() {
  localStorage.setItem(storageKey.value, JSON.stringify(taskRows.value));
}

const filteredRows = computed(() => {
  return taskRows.value.filter((row) => {
    if (filters.status && row.status !== filters.status) {
      return false;
    }
    if (!filters.keyword.trim()) {
      return true;
    }
    const keyword = filters.keyword.trim().toLowerCase();
    return row.name.toLowerCase().includes(keyword) || row.usage.toLowerCase().includes(keyword);
  });
});

function statusLabel(status: TaskItem["status"]): string {
  if (status === "DOING") {
    return "进行中";
  }
  if (status === "DONE") {
    return "已完成";
  }
  return "待开始";
}

function statusType(status: TaskItem["status"]): "info" | "warning" | "success" {
  if (status === "DOING") {
    return "warning";
  }
  if (status === "DONE") {
    return "success";
  }
  return "info";
}

function moveStatus(task: TaskItem, status: TaskItem["status"]) {
  task.status = status;
  task.updatedAt = nowString();
  persist();
}

function removeTask(id: number) {
  taskRows.value = taskRows.value.filter((item) => item.id !== id);
  persist();
}

function openCreateDialog() {
  createVisible.value = true;
}

function createTask() {
  if (!createForm.name.trim() || !createForm.usage.trim()) {
    ElMessage.warning("执行项和说明不能为空");
    return;
  }
  taskRows.value.unshift({
    id: Date.now(),
    name: createForm.name.trim(),
    usage: createForm.usage.trim(),
    owner: createForm.owner.trim() || "运营",
    status: "TODO",
    updatedAt: nowString()
  });
  createForm.name = "";
  createForm.usage = "";
  createForm.owner = "";
  createVisible.value = false;
  persist();
}

function resetToDefaults() {
  taskRows.value = toDefaultTasks();
  persist();
  ElMessage.success("已重置为默认执行项");
}

watch(
  () => route.path,
  () => loadTasks(),
  { immediate: true }
);
</script>

<style scoped>
.head {
  margin-bottom: 14px;
}

.title {
  margin: 0;
  font-size: 22px;
}

.desc {
  margin: 8px 0 0;
  color: #64748b;
}

.stat-grid {
  margin-bottom: 14px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}

.stat-card {
  border: 1px solid #dbe4f0;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}

.stat-value {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 700;
}

.toolbar {
  margin-bottom: 12px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}

.feature-card h4 {
  margin: 0 0 8px;
  font-size: 16px;
}

.feature-card p {
  margin: 0 0 8px;
  color: #475569;
  line-height: 1.6;
}

.ops,
.role {
  font-size: 13px;
}

.table {
  margin-top: 14px;
}
</style>
