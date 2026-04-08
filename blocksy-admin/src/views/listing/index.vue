<template>
  <app-section>
    <h3 class="section-title">分类信息管理</h3>
    <div class="toolbar">
      <el-button :loading="loading" type="primary" @click="loadData">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" stripe border class="page-table">
      <el-table-column label="ID" prop="id" width="90" />
      <el-table-column label="标题" prop="title" min-width="260" />
      <el-table-column label="描述" prop="description" min-width="420" />
      <el-table-column label="价格" min-width="180">
        <template #default="{ row }">
          {{ row.price == null ? "-" : `¥${row.price}` }}
        </template>
      </el-table-column>
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { fetchListings, type ListingItem } from "../../api/listing";
import AppSection from "../../components/AppSection.vue";

const loading = ref(false);
const rows = ref<ListingItem[]>([]);

async function loadData() {
  loading.value = true;
  try {
    rows.value = await fetchListings();
  } catch (error) {
    ElMessage.error(`加载分类信息失败: ${String(error)}`);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadData();
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
