<template>
  <app-section>
    <h3 class="section-title">社区管理</h3>
    <el-form :model="form" inline class="toolbar">
      <el-form-item label="编码">
        <el-input v-model="form.code" placeholder="例如 SH-PD-001" />
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="form.name" placeholder="社区名称" />
      </el-form-item>
      <el-form-item label="地址">
        <el-input v-model="form.address" placeholder="地址（可选）" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="create">新增社区</el-button>
        <el-button @click="loadCommunities">刷新</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="communities" border class="page-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="编码" width="180" />
      <el-table-column prop="name" label="名称" min-width="220" />
      <el-table-column prop="address" label="地址" min-width="320" />
      <el-table-column prop="status" label="状态" width="100" />
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
import { CommunityItem, createCommunity, fetchCommunities } from "../../api/community";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const communities = ref<CommunityItem[]>([]);
const form = reactive({
  code: "",
  name: "",
  address: "",
  description: ""
});

async function loadCommunities() {
  loading.value = true;
  try {
    communities.value = await fetchCommunities();
  } catch (error) {
    ElMessage.error((error as Error).message || "加载失败");
  } finally {
    loading.value = false;
  }
}

async function create() {
  if (!form.code.trim() || !form.name.trim()) {
    ElMessage.warning("编码和名称必填");
    return;
  }
  try {
    await createCommunity({
      code: form.code.trim(),
      name: form.name.trim(),
      address: form.address.trim() || undefined,
      description: form.description.trim() || undefined
    });
    form.code = "";
    form.name = "";
    form.address = "";
    form.description = "";
    ElMessage.success("创建成功");
    await loadCommunities();
  } catch (error) {
    ElMessage.error((error as Error).message || "创建失败");
  }
}

onMounted(() => {
  loadCommunities();
});
</script>

<style scoped>
.section-title {
  margin: 0 0 12px;
}

.toolbar {
  margin-bottom: 12px;
}

.page-table {
  width: 100%;
}
</style>
