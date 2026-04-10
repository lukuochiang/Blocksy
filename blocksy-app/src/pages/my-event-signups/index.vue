<template>
  <view class="page enter">
    <view class="hero-card">
      <view class="title">我的活动报名</view>
      <view class="subtitle">查看你已报名的社区活动</view>
    </view>
    <view class="toolbar">
      <button class="toolbar-btn" @click="goMine">返回我的</button>
      <button class="toolbar-btn" :class="{ 'is-loading': loading }" :disabled="loading" @click="reload">
        {{ loading ? "刷新中..." : "刷新" }}
      </button>
    </view>
    <view class="filter-row">
      <button class="chip-btn" :class="{ active: statusFilter === 'ALL' }" @click="statusFilter = 'ALL'">全部</button>
      <button class="chip-btn" :class="{ active: statusFilter === 'UPCOMING' }" @click="statusFilter = 'UPCOMING'">未开始</button>
      <button class="chip-btn" :class="{ active: statusFilter === 'ONGOING' }" @click="statusFilter = 'ONGOING'">进行中</button>
      <button class="chip-btn" :class="{ active: statusFilter === 'ENDED' }" @click="statusFilter = 'ENDED'">已结束</button>
      <button class="chip-btn" :class="{ active: statusFilter === 'OFFLINE' }" @click="statusFilter = 'OFFLINE'">已下架</button>
    </view>
    <view class="filter-row">
      <button class="chip-btn" :class="{ active: timeFilterDays === 0 }" @click="timeFilterDays = 0">全部时间</button>
      <button class="chip-btn" :class="{ active: timeFilterDays === 30 }" @click="timeFilterDays = 30">近30天</button>
      <button class="chip-btn" :class="{ active: timeFilterDays === 90 }" @click="timeFilterDays = 90">近90天</button>
    </view>
    <skeleton-list v-if="loading" :count="3" />
    <view v-if="displayRows.length" class="section-title">活动列表</view>
    <view class="list">
      <view v-for="item in displayRows" :key="`row-${item.id}`" class="card">
        <view class="badge" :class="badgeClass(item)">{{ badgeText(item) }}</view>
        <view class="card-title">{{ item.title }}</view>
        <view class="card-meta">开始时间：{{ formatDateTime(item.startTime) }}</view>
        <view class="card-meta">地点：{{ item.location || "-" }}</view>
        <view class="card-content">{{ item.content }}</view>
        <button class="detail-btn" @click="goDetail(item.id)">查看详情</button>
      </view>
    </view>
    <empty-state
      v-if="!loading && !displayRows.length"
      title="你还没有报名活动"
      description="去活动页看看社区近期活动。"
      cta-text="去活动页"
      cta-variant="primary"
      @cta="goEvent"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import { getMyEventSignups, type EventItem } from "../../api/event";
import { useUserStore } from "../../store/user";
import { formatDateTime } from "../../utils/datetime";

const userStore = useUserStore();
userStore.hydrate();

const rows = ref<EventItem[]>([]);
const loading = ref(false);
const statusFilter = ref<"ALL" | "UPCOMING" | "ONGOING" | "ENDED" | "OFFLINE">("ALL");
const timeFilterDays = ref<0 | 30 | 90>(0);
const displayRows = computed(() => {
  const now = Date.now();
  const minTs = timeFilterDays.value === 0 ? 0 : now - timeFilterDays.value * 24 * 60 * 60 * 1000;
  return rows.value.filter((item) => {
    const startTs = new Date(item.startTime).getTime();
    if (minTs && startTs < minTs) {
      return false;
    }
    if (statusFilter.value === "ALL") {
      return true;
    }
    if (statusFilter.value === "OFFLINE") {
      return item.status !== 1;
    }
    if (item.status !== 1) {
      return false;
    }
    const endTs = item.endTime ? new Date(item.endTime).getTime() : Number.MAX_SAFE_INTEGER;
    if (statusFilter.value === "UPCOMING") {
      return startTs > now;
    }
    if (statusFilter.value === "ONGOING") {
      return startTs <= now && endTs >= now;
    }
    return endTs < now;
  });
});

function goMine() {
  uni.navigateTo({ url: "/pages/mine/index" });
}

function goEvent() {
  uni.navigateTo({ url: "/pages/event/index" });
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/event-detail/index?id=${id}` });
}

function badgeText(item: EventItem): string {
  if (item.status !== 1) {
    return "已下架";
  }
  const now = Date.now();
  const startTs = new Date(item.startTime).getTime();
  const endTs = item.endTime ? new Date(item.endTime).getTime() : Number.MAX_SAFE_INTEGER;
  if (startTs > now) {
    return "未开始";
  }
  if (endTs < now) {
    return "已结束";
  }
  return "进行中";
}

function badgeClass(item: EventItem): string {
  const text = badgeText(item);
  if (text === "进行中") return "running";
  if (text === "未开始") return "upcoming";
  if (text === "已结束") return "ended";
  return "offline";
}

async function reload() {
  if (!userStore.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    return;
  }
  loading.value = true;
  try {
    await withMinDuration(async () => {
      rows.value = await getMyEventSignups();
    });
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

reload();
</script>

<style scoped>
.page { padding: 16px; box-sizing: border-box; }
.hero-card {
  margin-bottom: 10px;
  padding: 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #0f67e7 0%, #2e8aff 55%, #4fc4cf 100%);
  box-shadow: var(--shadow);
}
.title { font-size: 22px; font-weight: 800; }
.subtitle { margin-top: 6px; font-size: 13px; color: rgba(255, 255, 255, 0.9); }
.toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 10px; }
.toolbar-btn { border: 1px solid var(--line); background: #fff; position: relative; }
.toolbar-btn.is-loading { padding-right: 30px; }
.toolbar-btn.is-loading::after {
  content: ""; position: absolute; right: 10px; top: 50%; width: 12px; height: 12px; margin-top: -6px;
  border-radius: 999px; border: 2px solid currentColor; border-right-color: transparent; animation: spin 0.7s linear infinite;
}
.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.chip-btn {
  border: 1px solid #d5e2f5;
  background: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  color: #5b6882;
  font-size: 12px;
}
.chip-btn.active {
  background: linear-gradient(135deg, #e8f1ff 0%, #ebfbf7 100%);
  border-color: #bed3f6;
  color: var(--primary-dark);
  font-weight: 700;
}
.list { display: grid; gap: 10px; }
.card { border: 1px solid #dce7f7; border-radius: 18px; background: var(--surface); padding: 12px; box-shadow: var(--shadow-soft); }
.card-title { font-size: 16px; font-weight: 700; }
.card-meta { margin-top: 6px; color: var(--text-soft); font-size: 12px; }
.card-content { margin-top: 8px; color: #334155; font-size: 14px; }
.detail-btn { margin-top: 8px; border: 1px solid #d5e2f5; background: #fff; }
.section-title { margin: 12px 0 8px; font-size: 14px; font-weight: 700; color: #334155; }
.badge {
  display: inline-block;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
  margin-bottom: 8px;
}
.badge.running { background: #e7f8f0; color: #137a4f; }
.badge.upcoming { background: #eef6ff; color: #1d4ed8; }
.badge.ended { background: #eef2f7; color: #475569; }
.badge.offline { background: #fff7ed; color: #9a3412; }
</style>
