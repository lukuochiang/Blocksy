<template>
  <app-section>
    <h3 class="section-title">敏感词管理</h3>
    <el-form inline :model="form" class="toolbar">
      <el-form-item label="敏感词">
        <el-input v-model="form.word" placeholder="例如：加微信" />
      </el-form-item>
      <el-form-item label="等级">
        <el-select v-model="form.level" style="width: 140px">
          <el-option label="低" value="LOW" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="高" value="HIGH" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="createWord">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" border class="page-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="word" label="敏感词" min-width="280" />
      <el-table-column label="等级" width="120">
        <template #default="{ row }">
          <el-tag :type="levelTagType(row.level)">{{ row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="启用" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="(val) => toggle(row.id, Boolean(val))" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="220">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="removeWord(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import {
  createSensitiveWord,
  fetchSensitiveWords,
  removeSensitiveWord,
  toggleSensitiveWord,
  type SensitiveWordItem
} from "../../api/sensitive-word";
import { formatDateTime } from "../../utils/datetime";

const rows = ref<SensitiveWordItem[]>([]);
const form = reactive<{
  word: string;
  level: "LOW" | "MEDIUM" | "HIGH";
}>({
  word: "",
  level: "MEDIUM"
});

function levelTagType(level: SensitiveWordItem["level"]) {
  if (level === "HIGH") {
    return "danger";
  }
  if (level === "MEDIUM") {
    return "warning";
  }
  return "success";
}

async function loadRows() {
  rows.value = await fetchSensitiveWords();
}

async function createWord() {
  if (!form.word.trim()) {
    ElMessage.warning("请输入敏感词");
    return;
  }
  await createSensitiveWord(form.word.trim(), form.level);
  form.word = "";
  await loadRows();
  ElMessage.success("新增成功");
}

async function toggle(id: number, enabled: boolean) {
  await toggleSensitiveWord(id, enabled);
  await loadRows();
}

async function removeWord(id: number) {
  await removeSensitiveWord(id);
  await loadRows();
  ElMessage.success("删除成功");
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}

.page-table {
  width: 100%;
}
</style>
