<template>
  <app-section>
    <h3 class="section-title">分类管理</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="模块">
        <el-input v-model="query.module" clearable placeholder="POST / LISTING" />
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="code/name" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="openCreate">新增分类</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="code" label="编码" width="160" />
      <el-table-column prop="name" label="名称" width="160" />
      <el-table-column prop="sortNo" label="排序" width="100" />
      <el-table-column label="启用" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="(val:boolean)=>toggle(row.id, val)" />
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

    <el-dialog v-model="createVisible" title="新增分类" width="520px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="模块">
          <el-input v-model="createForm.module" placeholder="POST / LISTING" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="createForm.code" placeholder="SECOND_HAND" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="createForm.sortNo" :min="0" :max="9999" />
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
  createContentCategory,
  fetchContentCategories,
  toggleContentCategory,
  type ContentCategoryItem
} from "../../api/content-ops";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const creating = ref(false);
const createVisible = ref(false);
const total = ref(0);
const rows = ref<ContentCategoryItem[]>([]);
const query = reactive({ module: "", keyword: "", page: 1, pageSize: 20 });
const createForm = reactive({ module: "LISTING", code: "", name: "", sortNo: 0 });

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchContentCategories({
      module: query.module.trim() || undefined,
      keyword: query.keyword.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载分类失败");
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
function openCreate() {
  createVisible.value = true;
}
async function submitCreate() {
  if (!createForm.module.trim() || !createForm.code.trim() || !createForm.name.trim()) {
    ElMessage.warning("模块/编码/名称不能为空");
    return;
  }
  creating.value = true;
  try {
    await createContentCategory({
      module: createForm.module.trim().toUpperCase(),
      code: createForm.code.trim().toUpperCase(),
      name: createForm.name.trim(),
      sortNo: createForm.sortNo
    });
    ElMessage.success("创建成功");
    createVisible.value = false;
    createForm.code = "";
    createForm.name = "";
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "创建失败");
  } finally {
    creating.value = false;
  }
}
async function toggle(id: number, enabled: boolean) {
  try {
    await toggleContentCategory(id, enabled);
    ElMessage.success("状态已更新");
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "更新失败");
  }
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.section-title { margin: 0 0 12px; }
.toolbar { margin-bottom: 12px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
