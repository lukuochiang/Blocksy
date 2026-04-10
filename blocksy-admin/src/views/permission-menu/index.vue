<template>
  <app-section>
    <h3 class="section-title">菜单权限</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="角色">
        <el-input v-model="query.roleCode" placeholder="例如 ADMIN" clearable />
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="菜单key/name/path" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-card shadow="never" class="assign-card">
      <template #header>批量分配菜单</template>
      <el-form inline :model="assignForm">
        <el-form-item label="角色">
          <el-input v-model="assignForm.roleCode" placeholder="ADMIN" />
        </el-form-item>
        <el-form-item label="菜单Keys">
          <el-input v-model="assignForm.menuKeysText" style="width: 460px" placeholder="dashboard,risk.anomaly,risk.appeals" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="assigning" @click="submitAssign">提交分配</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleCode" label="角色" width="120" />
      <el-table-column prop="menuKey" label="菜单Key" width="180" />
      <el-table-column prop="menuName" label="菜单名" width="160" />
      <el-table-column prop="menuPath" label="路径" min-width="180" />
      <el-table-column label="启用" width="80">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? "是" : "否" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="180">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
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

    <el-card shadow="never" class="log-card">
      <template #header>权限操作日志</template>
      <el-table :data="logs" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleCode" label="角色" width="120" />
        <el-table-column prop="operatorUserId" label="操作人ID" width="120" />
        <el-table-column prop="action" label="动作" width="180" />
        <el-table-column prop="details" label="详情" min-width="280" show-overflow-tooltip />
        <el-table-column label="时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import {
  assignRoleMenus,
  fetchMenuPermissions,
  fetchPermissionLogs,
  type MenuPermissionItem,
  type PermissionLogItem
} from "../../api/permission";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const assigning = ref(false);
const rows = ref<MenuPermissionItem[]>([]);
const logs = ref<PermissionLogItem[]>([]);
const total = ref(0);
const query = reactive({
  roleCode: "",
  keyword: "",
  page: 1,
  pageSize: 20
});
const assignForm = reactive({
  roleCode: "ADMIN",
  menuKeysText: "dashboard,risk.anomaly,risk.appeals,permissions.menus"
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchMenuPermissions({
      roleCode: query.roleCode.trim() || undefined,
      keyword: query.keyword.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载菜单权限失败");
  } finally {
    loading.value = false;
  }
}

async function loadLogs() {
  try {
    logs.value = await fetchPermissionLogs({ roleCode: query.roleCode.trim() || undefined, limit: 30 });
  } catch (error) {
    ElMessage.error((error as Error).message || "加载权限日志失败");
  }
}

function onSearch() {
  query.page = 1;
  void Promise.all([loadRows(), loadLogs()]);
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

async function submitAssign() {
  const roleCode = assignForm.roleCode.trim().toUpperCase();
  const menuKeys = assignForm.menuKeysText.split(",").map((item) => item.trim()).filter(Boolean);
  if (!roleCode || menuKeys.length === 0) {
    ElMessage.warning("角色和菜单Keys不能为空");
    return;
  }
  assigning.value = true;
  try {
    const count = await assignRoleMenus({ roleCode, menuKeys });
    ElMessage.success(`分配完成，影响 ${count} 项`);
    await Promise.all([loadRows(), loadLogs()]);
  } catch (error) {
    ElMessage.error((error as Error).message || "分配失败");
  } finally {
    assigning.value = false;
  }
}

onMounted(() => {
  void Promise.all([loadRows(), loadLogs()]);
});
</script>

<style scoped>
.section-title { margin: 0 0 12px; }
.toolbar { margin-bottom: 12px; }
.assign-card { margin-bottom: 12px; border: 1px solid #dbe4f0; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
.log-card { margin-top: 14px; border: 1px solid #dbe4f0; }
</style>
