<template>
  <app-section>
    <h3 class="section-title">角色管理</h3>
    <el-form inline :model="form" class="toolbar">
      <el-form-item label="角色名">
        <el-input v-model="form.name" placeholder="例如：客服" />
      </el-form-item>
      <el-form-item label="角色编码">
        <el-input v-model="form.code" placeholder="例如：SERVICE" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" placeholder="角色职责描述" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="createOne">新增角色</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" border class="page-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="角色名" min-width="180" />
      <el-table-column prop="code" label="角色编码" min-width="160" />
      <el-table-column prop="description" label="描述" min-width="320" />
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
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { createRole, fetchRoles, toggleRoleEnabled, type RoleItem } from "../../api/role";
import { formatDateTime } from "../../utils/datetime";

const rows = ref<RoleItem[]>([]);
const form = reactive({
  name: "",
  code: "",
  description: ""
});

async function loadRows() {
  rows.value = await fetchRoles();
}

async function createOne() {
  if (!form.name.trim() || !form.code.trim()) {
    ElMessage.warning("角色名和角色编码必填");
    return;
  }
  await createRole({
    name: form.name.trim(),
    code: form.code.trim().toUpperCase(),
    description: form.description.trim()
  });
  form.name = "";
  form.code = "";
  form.description = "";
  await loadRows();
  ElMessage.success("新增角色成功");
}

async function toggle(id: number, enabled: boolean) {
  await toggleRoleEnabled(id, enabled);
  await loadRows();
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
