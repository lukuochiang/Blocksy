<template>
  <app-section>
    <h3 class="section-title">推送记录</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="任务ID">
        <el-input v-model.number="query.taskId" clearable placeholder="例如 1" />
      </el-form-item>
      <el-form-item label="发送状态">
        <el-select v-model="query.sendStatus" clearable placeholder="全部" style="width: 150px">
          <el-option label="已发送" value="SENT" />
          <el-option label="失败" value="FAILED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="taskId" label="任务ID" width="100" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="channel" label="渠道" width="120" />
      <el-table-column prop="sendStatus" label="发送状态" width="120" />
      <el-table-column label="已读" width="90">
        <template #default="{ row }">{{ row.readStatus ? "是" : "否" }}</template>
      </el-table-column>
      <el-table-column label="送达时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.deliveredAt) }}</template>
      </el-table-column>
      <el-table-column label="已读时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.readAt) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
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
import { fetchPushRecords, type PushRecordItem } from "../../api/admin-messaging";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<PushRecordItem[]>([]);
const total = ref(0);
const query = reactive({
  taskId: undefined as number | undefined,
  sendStatus: undefined as string | undefined,
  page: 1,
  pageSize: 20
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchPushRecords({
      taskId: query.taskId,
      sendStatus: query.sendStatus,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载推送记录失败");
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
.toolbar {
  margin-bottom: 12px;
}
.pager-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
