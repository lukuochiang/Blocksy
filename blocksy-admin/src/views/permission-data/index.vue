<template>
  <app-section>
    <h3 class="section-title">数据权限</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="角色">
        <el-input v-model="query.roleCode" placeholder="ADMIN" clearable />
      </el-form-item>
      <el-form-item label="数据域">
        <el-input v-model="query.dataScope" placeholder="COMMUNITY" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-card shadow="never" class="assign-card">
      <template #header>批量分配数据权限</template>
      <el-form inline :model="assignForm">
        <el-form-item label="角色">
          <el-input v-model="assignForm.roleCode" placeholder="ADMIN" />
        </el-form-item>
        <el-form-item label="数据域">
          <el-input v-model="assignForm.dataScope" placeholder="COMMUNITY" />
        </el-form-item>
        <el-form-item label="数据值">
          <el-input v-model="assignForm.dataValuesText" style="width: 360px" placeholder="1,2,3" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="assigning" @click="submitAssign">提交分配</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleCode" label="角色" width="140" />
      <el-table-column prop="dataScope" label="数据域" width="140" />
      <el-table-column prop="dataValue" label="数据值" width="180" />
      <el-table-column label="启用" width="90">
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
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { assignDataPermissions, fetchDataPermissions, type DataPermissionItem } from "../../api/permission";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const assigning = ref(false);
const total = ref(0);
const rows = ref<DataPermissionItem[]>([]);
const query = reactive({
  roleCode: "",
  dataScope: "",
  page: 1,
  pageSize: 20
});
const assignForm = reactive({
  roleCode: "ADMIN",
  dataScope: "COMMUNITY",
  dataValuesText: "1,2"
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchDataPermissions({
      roleCode: query.roleCode.trim() || undefined,
      dataScope: query.dataScope.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载数据权限失败");
  } finally {
    loading.value = false;
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

async function submitAssign() {
  const roleCode = assignForm.roleCode.trim().toUpperCase();
  const dataScope = assignForm.dataScope.trim().toUpperCase();
  const dataValues = assignForm.dataValuesText.split(",").map((v) => v.trim()).filter(Boolean);
  if (!roleCode || !dataScope || dataValues.length === 0) {
    ElMessage.warning("角色、数据域、数据值不能为空");
    return;
  }
  assigning.value = true;
  try {
    const count = await assignDataPermissions({ roleCode, dataScope, dataValues });
    ElMessage.success(`分配成功，影响 ${count} 项`);
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "分配失败");
  } finally {
    assigning.value = false;
  }
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.section-title { margin: 0 0 12px; }
.toolbar { margin-bottom: 12px; }
.assign-card { margin-bottom: 12px; border: 1px solid #dbe4f0; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
