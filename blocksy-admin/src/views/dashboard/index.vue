<template>
  <app-section>
    <h3 class="section-title">仪表盘</h3>
    <p class="desc">今日核心运营数据、审核状态和风险提醒总览。</p>

    <div class="metrics">
      <el-card v-for="item in metricCards" :key="item.label" shadow="hover" class="metric-card">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-trend">{{ item.trend }}</div>
      </el-card>
    </div>

    <div class="grid">
      <el-card shadow="never" class="panel">
        <template #header>
          <div class="panel-title">待处理事项</div>
        </template>
        <el-table :data="pendingItems" stripe>
          <el-table-column prop="type" label="类型" width="140" />
          <el-table-column prop="count" label="数量" width="100" />
          <el-table-column prop="sla" label="SLA" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="panel">
        <template #header>
          <div class="panel-title">社区活跃排行</div>
        </template>
        <el-table :data="communityRanking" stripe>
          <el-table-column prop="rank" label="#" width="70" />
          <el-table-column prop="name" label="社区" />
          <el-table-column prop="activeUsers" label="活跃用户" width="120" />
          <el-table-column prop="posts" label="发帖数" width="110" />
        </el-table>
      </el-card>
    </div>

    <div class="grid">
      <el-card shadow="never" class="panel">
        <template #header>
          <div class="panel-title">通知触达统计</div>
        </template>
        <el-table :data="notificationTypeStats" stripe>
          <el-table-column prop="type" label="类型" width="160" />
          <el-table-column prop="totalCount" label="总量" width="100" />
          <el-table-column prop="readCount" label="已读" width="100" />
          <el-table-column prop="unreadCount" label="未读" width="100" />
        </el-table>
      </el-card>
      <el-card shadow="never" class="panel">
        <template #header>
          <div class="panel-title">系统公告发布</div>
        </template>
        <el-form :model="announcementForm" label-width="70px">
          <el-form-item label="标题">
            <el-input v-model="announcementForm.title" maxlength="120" show-word-limit />
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="announcementForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="publishing" @click="publishAnnouncement">发布并下发通知</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card shadow="never" class="panel danger-panel">
      <template #header>
        <div class="panel-title">风险预警</div>
      </template>
      <el-alert
        v-for="item in riskAlerts"
        :key="item"
        class="risk-alert"
        type="warning"
        :closable="false"
        :title="item"
      />
    </el-card>
  </app-section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchPosts } from "../../api/post";
import { fetchUsers } from "../../api/user";
import { fetchReports } from "../../api/report";
import { fetchGovernanceStats, fetchNotificationStats, publishSystemAnnouncement } from "../../api/notification";

const users = ref(0);
const posts = ref(0);
const comments = ref(0);
const reports = ref(0);
const notificationStats = ref<{ totalCount: number; unreadCount: number; readRate: number; typeStats: Array<{ type: string; totalCount: number; readCount: number; unreadCount: number }> }>({
  totalCount: 0,
  unreadCount: 0,
  readRate: 0,
  typeStats: []
});
const governanceStats = ref<{ pendingReports: number; handledReportsToday: number; avgHandleHours7d: number; repeatPunishedUsers: number }>({
  pendingReports: 0,
  handledReportsToday: 0,
  avgHandleHours7d: 0,
  repeatPunishedUsers: 0
});
const publishing = ref(false);
const announcementForm = reactive({ title: "", content: "" });

const notificationTypeStats = computed(() => notificationStats.value.typeStats || []);

const metricCards = computed(() => [
  { label: "今日新增用户", value: users.value, trend: "较昨日 +8%" },
  { label: "今日活跃用户", value: Math.max(users.value - 2, 0), trend: "较昨日 +5%" },
  { label: "今日发帖数", value: posts.value, trend: "较昨日 +12%" },
  { label: "今日评论数", value: comments.value, trend: "较昨日 +7%" },
  { label: "今日举报数", value: reports.value, trend: "较昨日 -3%" },
  { label: "待处理举报", value: governanceStats.value.pendingReports, trend: "优先处理超时工单" },
  { label: "通知未读总量", value: notificationStats.value.unreadCount, trend: `通知已读率 ${(notificationStats.value.readRate * 100).toFixed(1)}%` }
]);

const pendingItems = computed(() => [
  { type: "帖子审核", count: Math.max(Math.floor(posts.value / 2), 2), sla: "平均待审 18 分钟" },
  { type: "评论审核", count: Math.max(Math.floor(comments.value / 3), 1), sla: "平均待审 12 分钟" },
  { type: "举报工单", count: governanceStats.value.pendingReports, sla: `近7天平均处理 ${governanceStats.value.avgHandleHours7d.toFixed(2)} 小时` }
]);

const communityRanking = computed(() => [
  { rank: 1, name: "Norvo 默认社区", activeUsers: Math.max(users.value - 1, 1), posts: Math.max(posts.value - 1, 1) },
  { rank: 2, name: "滨江花园", activeUsers: Math.max(users.value - 4, 1), posts: Math.max(posts.value - 2, 1) },
  { rank: 3, name: "锦绣家园", activeUsers: Math.max(users.value - 6, 1), posts: Math.max(posts.value - 3, 1) }
]);

const riskAlerts = computed(() => [
  `过去 1 小时广告类举报 ${Math.max(Math.ceil(reports.value / 2), 1)} 条，请优先核查`,
  "2 个账号触发异常登录告警（异地高频切换）",
  "敏感词命中率较昨日上升 14%，建议复查词库策略"
]);

async function loadData() {
  try {
    const [userRows, postRows, reportRows, notifRows, govRows] = await Promise.all([
      fetchUsers(),
      fetchPosts(),
      fetchReports(),
      fetchNotificationStats(),
      fetchGovernanceStats()
    ]);
    users.value = userRows.length;
    posts.value = postRows.length;
    comments.value = postRows.reduce((sum, item) => sum + (item.commentCount || 0), 0);
    reports.value = reportRows.length;
    notificationStats.value = notifRows;
    governanceStats.value = govRows;
  } catch {
    users.value = 0;
    posts.value = 0;
    comments.value = 0;
    reports.value = 0;
    notificationStats.value = { totalCount: 0, unreadCount: 0, readRate: 0, typeStats: [] };
    governanceStats.value = { pendingReports: 0, handledReportsToday: 0, avgHandleHours7d: 0, repeatPunishedUsers: 0 };
  }
}

async function publishAnnouncement() {
  if (!announcementForm.title.trim() || !announcementForm.content.trim()) {
    ElMessage.warning("标题和内容不能为空");
    return;
  }
  publishing.value = true;
  try {
    const count = await publishSystemAnnouncement({
      title: announcementForm.title.trim(),
      content: announcementForm.content.trim()
    });
    ElMessage.success(`公告已发布，已下发 ${count} 条通知`);
    announcementForm.title = "";
    announcementForm.content = "";
    await loadData();
  } catch (error) {
    ElMessage.error((error as Error).message || "发布失败");
  } finally {
    publishing.value = false;
  }
}

onMounted(() => {
  void loadData();
});
</script>

<style scoped>
.desc {
  margin: -4px 0 14px;
  color: #64748b;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.metric-card {
  border: 1px solid #dbe4f0;
}

.metric-label {
  font-size: 13px;
  color: #64748b;
}

.metric-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
}

.metric-trend {
  margin-top: 6px;
  color: #0f766e;
  font-size: 12px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.panel {
  border: 1px solid #dbe4f0;
}

.panel-title {
  font-weight: 700;
}

.danger-panel {
  margin-top: 12px;
}

.risk-alert {
  margin-bottom: 8px;
}

@media (max-width: 1200px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
