<template>
  <view class="page desktop-layout enter">
    <view class="main-col">
      <view v-if="pullStatus !== 'idle'" class="pull-indicator" :style="{ height: `${pullOffset}px` }">
        <view class="pull-text">{{ pullText }}</view>
      </view>
      <view class="hero-card">
        <view class="title">消息中心</view>
        <view class="hero-subtitle">互动通知、系统公告与活动提醒</view>
        <view class="hero-stats">
          <view class="stat">总消息 {{ rows.length }}</view>
          <view class="stat">未读 {{ Math.max(rows.length - 1, 0) }}</view>
        </view>
      </view>
      <view class="tab-row">
        <button
          v-for="tab in tabs"
          :key="tab.code"
          class="tab-btn"
          :class="{ active: activeTab === tab.code }"
          @click="activeTab = tab.code"
        >
          {{ tab.name }}
        </button>
      </view>

      <view class="list">
        <view v-for="item in pagedRows" :key="item.id" class="row">
          <view class="row-head">
            <view class="row-title">{{ item.title }}</view>
            <view class="time">{{ item.time }}</view>
          </view>
          <view class="content">{{ item.content }}</view>
          <view class="meta">{{ item.typeText }}</view>
        </view>
      </view>
      <view v-if="!filteredRows.length" class="empty">暂无消息</view>
      <list-pager
        v-if="filteredRows.length > pageSize"
        :page="page"
        :page-size="pageSize"
        :total="filteredRows.length"
        @prev="prevPage"
        @next="nextPage"
      />
    </view>
    <view class="side-col">
      <view class="side-card">
        <view class="side-title">消息概览</view>
        <view class="side-line">全部消息：{{ rows.length }}</view>
        <view class="side-line">互动通知：{{ rows.filter((r) => r.type === 'interaction').length }}</view>
        <view class="side-line">系统通知：{{ rows.filter((r) => r.type === 'system').length }}</view>
      </view>
      <view class="side-card">
        <view class="side-title">快捷筛选</view>
        <button class="side-btn" @click="activeTab = 'all'">查看全部</button>
        <button class="side-btn" @click="activeTab = 'interaction'">仅互动</button>
        <button class="side-btn" @click="activeTab = 'activity'">仅活动</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import ListPager from "../../components/ListPager.vue";
import { useH5PullRefresh } from "../../utils/pull-refresh";

type TabCode = "all" | "interaction" | "system" | "activity";

const tabs: Array<{ code: TabCode; name: string }> = [
  { code: "all", name: "全部" },
  { code: "interaction", name: "互动" },
  { code: "system", name: "系统" },
  { code: "activity", name: "活动" }
];

const rows = [
  { id: 1, type: "interaction", typeText: "评论通知", title: "有人回复了你的帖子", content: "“周末拼车去商圈吗？”收到 2 条新评论。", time: "刚刚" },
  { id: 2, type: "system", typeText: "系统通知", title: "社区守则更新", content: "请关注最新内容发布规范，违规信息将下架处理。", time: "今天 10:22" },
  { id: 3, type: "activity", typeText: "活动提醒", title: "活动即将开始", content: "你报名的“春季邻里市集”将在明天 09:30 开始。", time: "昨天 18:05" }
];

const activeTab = ref<TabCode>("all");
const page = ref(1);
const pageSize = 4;

const filteredRows = computed(() => {
  if (activeTab.value === "all") {
    return rows;
  }
  return rows.filter((item) => item.type === activeTab.value);
});

const pagedRows = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredRows.value.slice(start, start + pageSize);
});

watch(activeTab, () => {
  page.value = 1;
});

function prevPage() {
  if (page.value <= 1) {
    return;
  }
  page.value -= 1;
}

function nextPage() {
  const totalPages = Math.ceil(filteredRows.value.length / pageSize);
  if (page.value >= totalPages) {
    return;
  }
  page.value += 1;
}

const { pullStatus, pullOffset, pullText } = useH5PullRefresh(async () => {
  page.value = 1;
});
</script>

<style scoped>
.page {
  padding: 16px;
  box-sizing: border-box;
}

.title {
  font-size: 22px;
  font-weight: 800;
}

.hero-card {
  margin-bottom: 10px;
  padding: 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(140deg, #0f63de 0%, #3879ff 60%, #41b7ff 100%);
  box-shadow: var(--shadow);
}

.hero-subtitle {
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

.hero-stats {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.stat {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.32);
}

.tab-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tab-btn {
  border: 1px solid #d5e2f5;
  border-radius: 999px;
  background: var(--surface);
  backdrop-filter: blur(8px);
  color: var(--text-soft);
  padding: 7px 12px;
  font-size: 12px;
}

.tab-btn.active {
  background: linear-gradient(135deg, #e8f1ff 0%, #ebfbf7 100%);
  color: var(--primary-dark);
  border-color: #bed3f6;
  font-weight: 700;
}

.list {
  display: grid;
  gap: 10px;
}

.row {
  border: 1px solid #dce7f7;
  border-radius: 18px;
  padding: 12px;
  background: var(--surface);
  backdrop-filter: blur(8px);
  box-shadow: var(--shadow-soft);
}

.row-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.row-title {
  font-size: 15px;
  font-weight: 700;
}

.time {
  font-size: 12px;
  color: var(--text-soft);
}

.content {
  margin-top: 6px;
  font-size: 14px;
  color: #334155;
}

.meta {
  margin-top: 8px;
  font-size: 12px;
  color: var(--primary-dark);
}

.empty {
  margin-top: 10px;
  color: var(--text-soft);
  text-align: center;
  padding: 24px 12px;
  border: 1px dashed var(--line);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
}

.pull-indicator {
  overflow: hidden;
  transition: height 0.18s ease;
}

.pull-text {
  height: 32px;
  display: grid;
  place-items: center;
  font-size: 12px;
  color: var(--primary-dark);
}

.side-col {
  display: none;
}

@media (min-width: 960px) {
  .desktop-layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 260px;
    gap: 14px;
  }

  .side-col {
    display: grid;
    align-content: start;
    gap: 10px;
  }

  .side-card {
    border: 1px solid #dce7f7;
    border-radius: 18px;
    background: var(--surface);
    backdrop-filter: blur(8px);
    padding: 12px;
    box-shadow: var(--shadow-soft);
  }

  .side-title {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 8px;
  }

  .side-line {
    color: var(--text-soft);
    font-size: 13px;
    margin-bottom: 6px;
  }

  .side-btn {
    width: 100%;
    border: 1px solid var(--line);
    background: #fff;
    margin-top: 8px;
  }
}
</style>
