<template>
  <app-section>
    <h3 class="section-title">协议政策配置</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="类型">
        <el-select v-model="query.policyType" clearable placeholder="全部" style="width: 180px">
          <el-option label="用户协议" value="USER_AGREEMENT" />
          <el-option label="隐私政策" value="PRIVACY_POLICY" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadRows">刷新</el-button>
        <el-button @click="openCreateDialog">新增版本</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="policyType" label="类型" width="160" />
      <el-table-column prop="version" label="版本号" width="120" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="生效" width="90">
        <template #default="{ row }">
          <el-tag :type="row.active ? 'success' : 'info'">{{ row.active ? "是" : "否" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.publishedAt) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" :disabled="row.active" @click="activate(row.id)">设为生效</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="createVisible" title="新增政策版本" width="560px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="类型">
          <el-select v-model="createForm.policyType" style="width: 100%">
            <el-option label="用户协议" value="USER_AGREEMENT" />
            <el-option label="隐私政策" value="PRIVACY_POLICY" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="createForm.version" placeholder="如 v1.1.0" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="createForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="createForm.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { activatePolicy, fetchPolicies, savePolicy, type PolicyDocumentItem } from "../../api/admin-settings";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const saving = ref(false);
const createVisible = ref(false);
const rows = ref<PolicyDocumentItem[]>([]);
const query = reactive({
  policyType: undefined as string | undefined
});
const createForm = reactive({
  policyType: "USER_AGREEMENT",
  version: "",
  title: "",
  content: ""
});

async function loadRows() {
  loading.value = true;
  try {
    rows.value = await fetchPolicies(query.policyType);
  } catch (error) {
    ElMessage.error((error as Error).message || "加载失败");
  } finally {
    loading.value = false;
  }
}

function openCreateDialog() {
  createVisible.value = true;
}

async function save() {
  if (!createForm.version.trim() || !createForm.title.trim() || !createForm.content.trim()) {
    ElMessage.warning("请填写完整信息");
    return;
  }
  saving.value = true;
  try {
    await savePolicy({
      policyType: createForm.policyType,
      version: createForm.version.trim(),
      title: createForm.title.trim(),
      content: createForm.content.trim()
    });
    ElMessage.success("保存成功");
    createVisible.value = false;
    createForm.version = "";
    createForm.title = "";
    createForm.content = "";
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "保存失败");
  } finally {
    saving.value = false;
  }
}

async function activate(id: number) {
  try {
    await activatePolicy(id);
    ElMessage.success("已设为生效版本");
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "操作失败");
  }
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>
