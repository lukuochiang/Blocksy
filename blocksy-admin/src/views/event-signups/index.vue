<template>
  <app-section>
    <h3 class="section-title">活动报名记录</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="活动ID">
        <el-input v-model.number="query.eventId" clearable placeholder="例如 1" />
      </el-form-item>
      <el-form-item label="社区ID">
        <el-input v-model.number="query.communityId" clearable placeholder="例如 1" />
      </el-form-item>
      <el-form-item label="用户ID">
        <el-input v-model.number="query.userId" clearable placeholder="例如 1" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="signupId" label="报名ID" width="90" />
      <el-table-column prop="eventId" label="活动ID" width="90" />
      <el-table-column prop="eventTitle" label="活动标题" min-width="180" />
      <el-table-column prop="communityId" label="社区ID" width="90" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column label="报名时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.signupAt) }}</template>
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
import { fetchEventSignups, type EventSignupItem } from "../../api/event-signup";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<EventSignupItem[]>([]);
const total = ref(0);
const query = reactive({
  eventId: undefined as number | undefined,
  communityId: undefined as number | undefined,
  userId: undefined as number | undefined,
  page: 1,
  pageSize: 10
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchEventSignups({
      eventId: query.eventId,
      communityId: query.communityId,
      userId: query.userId,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载活动报名记录失败");
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
