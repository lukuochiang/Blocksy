<template>
  <el-card shadow="never" class="panel">
    <template #header>
      <div class="panel-head">
        <span>分析口径治理</span>
        <el-button size="small" :loading="loading" @click="reload">刷新</el-button>
      </div>
    </template>
    <div class="meta">
      <el-tag type="info">默认窗口 {{ config.defaultWindowDays }} 天</el-tag>
      <el-tag type="success">最大窗口 {{ config.maxWindowDays }} 天</el-tag>
      <el-tag>可选 {{ config.allowedWindowDays.join(" / ") }}</el-tag>
      <span v-if="config.updatedAt" class="updated">更新时间：{{ formatDateTime(config.updatedAt) }}</span>
    </div>

    <el-form v-if="editable" inline class="edit-form">
      <el-form-item label="默认窗口">
        <el-input-number v-model="form.defaultWindowDays" :min="1" :max="365" />
      </el-form-item>
      <el-form-item label="最大窗口">
        <el-input-number v-model="form.maxWindowDays" :min="1" :max="365" />
      </el-form-item>
      <el-form-item label="可选窗口">
        <el-input v-model="allowedWindowText" style="width: 260px" placeholder="如 7,14,30,90" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSave">保存配置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="definitionRows" border size="small">
      <el-table-column prop="metricName" label="指标" min-width="150" />
      <el-table-column prop="definition" label="口径说明" min-width="240" />
      <el-table-column prop="formula" label="计算公式" min-width="280" />
      <el-table-column prop="dataSource" label="数据来源" min-width="220" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  fetchAnalyticsConfig,
  saveAnalyticsConfig,
  type AnalyticsConfig,
  type AnalyticsMetricDefinition
} from "../api/admin-analytics";
import { formatDateTime } from "../utils/datetime";

const props = defineProps<{
  editable?: boolean;
  metricCodes?: string[];
}>();

const emit = defineEmits<{
  (e: "updated", config: AnalyticsConfig): void;
}>();

const loading = ref(false);
const saving = ref(false);
const config = reactive<AnalyticsConfig>({
  defaultWindowDays: 7,
  maxWindowDays: 90,
  allowedWindowDays: [7, 14, 30, 90],
  metricDefinitions: []
});
const form = reactive({
  defaultWindowDays: 7,
  maxWindowDays: 90
});
const allowedWindowText = ref("7,14,30,90");

const definitionRows = computed<AnalyticsMetricDefinition[]>(() => {
  if (!props.metricCodes?.length) {
    return config.metricDefinitions;
  }
  const codeSet = new Set(props.metricCodes);
  return config.metricDefinitions.filter((item) => codeSet.has(item.metricCode));
});

function applyConfig(next: AnalyticsConfig) {
  config.defaultWindowDays = next.defaultWindowDays;
  config.maxWindowDays = next.maxWindowDays;
  config.allowedWindowDays = next.allowedWindowDays || [];
  config.metricDefinitions = next.metricDefinitions || [];
  config.updatedAt = next.updatedAt;
  form.defaultWindowDays = config.defaultWindowDays;
  form.maxWindowDays = config.maxWindowDays;
  allowedWindowText.value = config.allowedWindowDays.join(",");
  emit("updated", {
    defaultWindowDays: config.defaultWindowDays,
    maxWindowDays: config.maxWindowDays,
    allowedWindowDays: [...config.allowedWindowDays],
    metricDefinitions: [...config.metricDefinitions],
    updatedAt: config.updatedAt
  });
}

async function reload() {
  loading.value = true;
  try {
    const res = await fetchAnalyticsConfig();
    applyConfig(res);
  } catch (error) {
    ElMessage.error((error as Error).message || "加载分析口径配置失败");
  } finally {
    loading.value = false;
  }
}

function parseAllowedDays(raw: string): number[] {
  return raw
    .split(",")
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isFinite(item) && item > 0)
    .map((item) => Math.floor(item));
}

async function onSave() {
  const allowedWindowDays = parseAllowedDays(allowedWindowText.value);
  if (!allowedWindowDays.length) {
    ElMessage.warning("请输入至少一个可选窗口");
    return;
  }
  saving.value = true;
  try {
    const res = await saveAnalyticsConfig({
      defaultWindowDays: form.defaultWindowDays,
      maxWindowDays: form.maxWindowDays,
      allowedWindowDays
    });
    applyConfig(res);
    ElMessage.success("分析口径配置已保存");
  } catch (error) {
    ElMessage.error((error as Error).message || "保存失败");
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void reload();
});
</script>

<style scoped>
.panel {
  margin-bottom: 12px;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.meta {
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.updated {
  font-size: 12px;
  color: #64748b;
}
.edit-form {
  margin-bottom: 8px;
}
</style>
