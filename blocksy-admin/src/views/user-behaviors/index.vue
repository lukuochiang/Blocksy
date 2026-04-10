<template>
  <app-section>
    <h3 class="section-title">用户行为日志</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="用户ID">
        <el-input v-model.number="query.userId" placeholder="例如 1" clearable />
      </el-form-item>
      <el-form-item label="行为类型">
        <el-input v-model="query.behaviorType" placeholder="LOGIN/CREATE_POST" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="behaviorType" label="行为类型" width="160" />
      <el-table-column prop="resourceType" label="资源类型" width="120" />
      <el-table-column prop="resourceId" label="资源ID" width="120" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="device" label="设备" width="140" />
      <el-table-column label="时间" min-width="180">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
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
import { fetchUserBehaviorLogs, type UserBehaviorLogItem } from "../../api/permission";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const total = ref(0);
const rows = ref<UserBehaviorLogItem[]>([]);
const query = reactive({
  userId: undefined as number | undefined,
  behaviorType: "",
  page: 1,
  pageSize: 20
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchUserBehaviorLogs({
      userId: query.userId,
      behaviorType: query.behaviorType.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载行为日志失败");
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

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.section-title { margin: 0 0 12px; }
.toolbar { margin-bottom: 12px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
