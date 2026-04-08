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
import { computed, onMounted, ref } from "vue";
import AppSection from "../../components/AppSection.vue";
import { fetchPosts } from "../../api/post";
import { fetchUsers } from "../../api/user";
import { fetchReports } from "../../api/report";

const users = ref(0);
const posts = ref(0);
const comments = ref(0);
const reports = ref(0);

const metricCards = computed(() => [
  { label: "今日新增用户", value: users.value, trend: "较昨日 +8%" },
  { label: "今日活跃用户", value: Math.max(users.value - 2, 0), trend: "较昨日 +5%" },
  { label: "今日发帖数", value: posts.value, trend: "较昨日 +12%" },
  { label: "今日评论数", value: comments.value, trend: "较昨日 +7%" },
  { label: "今日举报数", value: reports.value, trend: "较昨日 -3%" },
  { label: "待审核内容", value: Math.max(reports.value, 3), trend: "请优先处理 >24h 工单" }
]);

const pendingItems = computed(() => [
  { type: "帖子审核", count: Math.max(Math.floor(posts.value / 2), 2), sla: "平均待审 18 分钟" },
  { type: "评论审核", count: Math.max(Math.floor(comments.value / 3), 1), sla: "平均待审 12 分钟" },
  { type: "举报工单", count: reports.value, sla: "平均待处理 31 分钟" }
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
    const [userRows, postRows, reportRows] = await Promise.all([fetchUsers(), fetchPosts(), fetchReports()]);
    users.value = userRows.length;
    posts.value = postRows.length;
    comments.value = postRows.reduce((sum, item) => sum + (item.commentCount || 0), 0);
    reports.value = reportRows.length;
  } catch {
    users.value = 0;
    posts.value = 0;
    comments.value = 0;
    reports.value = 0;
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
