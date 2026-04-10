<template>
  <app-section>
    <h3 class="section-title">通知中心</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="标题/内容" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 150px">
          <el-option label="有效" :value="1" />
          <el-option label="已撤回" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="openPublishDialog">发布公告</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border class="page-table">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? "有效" : "已撤回" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="dispatchCount" label="累计下发" width="110" />
      <el-table-column label="最近下发" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.lastDispatchedAt) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" type="warning" :disabled="row.status === 0" @click="onRevoke(row.id)">撤回</el-button>
          <el-button size="small" type="primary" :disabled="row.status === 0" @click="onRedispatch(row.id)">二次下发</el-button>
        </template>
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

    <el-card class="trend-card" shadow="never">
      <template #header>
        <div class="trend-head">
          <span>指标趋势图</span>
          <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
            <el-radio-button :label="7">近7天</el-radio-button>
            <el-radio-button :label="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div class="trend-bars">
        <div v-for="point in trendData" :key="point.day" class="bar-col">
          <div class="bar-wrap">
            <div class="bar total" :style="{ height: `${calcHeight(point.totalCount)}px` }"></div>
            <div class="bar read" :style="{ height: `${calcHeight(point.readCount)}px` }"></div>
          </div>
          <div class="bar-day">{{ point.day.slice(5) }}</div>
          <div class="bar-meta">{{ point.totalCount }}/{{ point.readCount }}</div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="publishVisible" title="发布系统公告" width="520px">
      <el-form :model="publishForm" label-width="70px">
        <el-form-item label="标题">
          <el-input v-model="publishForm.title" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="publishForm.content" type="textarea" :rows="5" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="submitPublish">发布并下发</el-button>
      </template>
    </el-dialog>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import {
  fetchAnnouncements,
  fetchNotificationTrend,
  publishSystemAnnouncement,
  redispatchAnnouncement,
  revokeAnnouncement,
  type AnnouncementItem,
  type NotificationTrendPoint
} from "../../api/notification";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<AnnouncementItem[]>([]);
const total = ref(0);
const trendDays = ref<7 | 30>(7);
const trendData = ref<NotificationTrendPoint[]>([]);
const publishVisible = ref(false);
const publishing = ref(false);
const publishForm = reactive({
  title: "",
  content: ""
});

const query = reactive<{
  keyword?: string;
  status?: number;
  page: number;
  pageSize: number;
}>({
  keyword: "",
  status: undefined,
  page: 1,
  pageSize: 10
});

async function loadAnnouncements() {
  loading.value = true;
  try {
    const data = await fetchAnnouncements({
      keyword: query.keyword?.trim() || undefined,
      status: query.status,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items || [];
    total.value = data.total || 0;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载公告失败");
  } finally {
    loading.value = false;
  }
}

async function loadTrend() {
  try {
    trendData.value = await fetchNotificationTrend(trendDays.value);
  } catch (error) {
    ElMessage.error((error as Error).message || "加载趋势失败");
  }
}

function onSearch() {
  query.page = 1;
  void loadAnnouncements();
}

function onPageChange(page: number) {
  query.page = page;
  void loadAnnouncements();
}

function onPageSizeChange(size: number) {
  query.pageSize = size;
  query.page = 1;
  void loadAnnouncements();
}

async function onRevoke(id: number) {
  try {
    await ElMessageBox.confirm("撤回后该公告将不再作为有效公告展示，是否继续？", "撤回确认", {
      type: "warning"
    });
    await revokeAnnouncement(id);
    ElMessage.success("已撤回");
    await loadAnnouncements();
    await loadTrend();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error((error as Error).message || "撤回失败");
    }
  }
}

async function onRedispatch(id: number) {
  try {
    const count = await redispatchAnnouncement(id);
    ElMessage.success(`二次下发成功，新增 ${count} 条通知`);
    await loadAnnouncements();
    await loadTrend();
  } catch (error) {
    ElMessage.error((error as Error).message || "二次下发失败");
  }
}

function openPublishDialog() {
  publishVisible.value = true;
}

async function submitPublish() {
  if (!publishForm.title.trim() || !publishForm.content.trim()) {
    ElMessage.warning("标题和内容不能为空");
    return;
  }
  publishing.value = true;
  try {
    const count = await publishSystemAnnouncement({
      title: publishForm.title.trim(),
      content: publishForm.content.trim()
    });
    ElMessage.success(`公告发布成功，已下发 ${count} 条通知`);
    publishVisible.value = false;
    publishForm.title = "";
    publishForm.content = "";
    await loadAnnouncements();
    await loadTrend();
  } catch (error) {
    ElMessage.error((error as Error).message || "发布失败");
  } finally {
    publishing.value = false;
  }
}

function calcHeight(value: number): number {
  const max = Math.max(...trendData.value.map((i) => i.totalCount), 1);
  return Math.max(6, Math.round((value / max) * 96));
}

onMounted(() => {
  void loadAnnouncements();
  void loadTrend();
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

.pager-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.trend-card {
  margin-top: 14px;
}

.trend-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.trend-bars {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(48px, 1fr));
  gap: 8px;
  align-items: end;
  min-height: 140px;
}

.bar-col {
  text-align: center;
}

.bar-wrap {
  display: flex;
  align-items: end;
  justify-content: center;
  gap: 2px;
  height: 106px;
}

.bar {
  width: 10px;
  border-radius: 4px 4px 0 0;
}

.bar.total {
  background: #6ea8fe;
}

.bar.read {
  background: #67c23a;
}

.bar-day {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.bar-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #94a3b8;
}
</style>
