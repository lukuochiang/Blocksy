<template>
  <view class="page desktop-layout enter">
    <view class="main-col">
      <view v-if="pullStatus !== 'idle'" class="pull-indicator" :style="{ height: `${pullOffset}px` }">
        <view class="pull-text">{{ pullText }}</view>
      </view>
      <view class="hero-card">
        <view class="title">社区活动</view>
        <view class="subtitle">发现本地活动，报名参与邻里互动</view>
        <view class="hero-pills">
          <view class="pill">活动总数 {{ events.length }}</view>
          <view class="pill">我的报名 {{ mySignupIds.size }}</view>
        </view>
      </view>
      <view class="header">
        <button class="back-btn" @click="goHome">返回首页</button>
        <button class="back-btn" :class="{ 'is-loading': loading }" :disabled="loading" @click="loadEvents">
          {{ loading ? "刷新中..." : "刷新" }}
        </button>
      </view>
      <skeleton-list v-if="loading" :count="2" />
      <view class="list">
        <view v-for="item in pagedEvents" :key="item.id" class="card">
          <view class="name">{{ item.title }}</view>
          <view class="meta">时间：{{ formatDateTime(item.startTime) }}</view>
          <view class="meta">地点：{{ item.location }}</view>
          <view class="desc">{{ item.content }}</view>
          <button
            class="signup-btn"
            :type="canSignup(item) ? 'primary' : 'default'"
            :disabled="!canSignup(item)"
            @click="toggleSignup(item.id)"
          >
            {{ signupLabel(item) }}
          </button>
          <button class="detail-btn" @click="goDetail(item.id)">查看详情</button>
        </view>
      </view>
      <empty-state
        v-if="!loading && !events.length"
        title="暂无活动"
        description="你可以先去首页看看社区最新动态。"
        cta-text="去首页"
        cta-variant="secondary"
        @cta="goHome"
      />
      <list-pager
        v-if="!loading && events.length > pager.pageSize"
        :page="pager.page"
        :page-size="pager.pageSize"
        :total="events.length"
        @prev="pager.prevPage"
        @next="pager.nextPage"
      />
    </view>
    <view class="side-col">
      <view class="side-card">
        <view class="side-title">活动状态</view>
        <view class="side-line">活动总数：{{ events.length }}</view>
        <view class="side-line">已报名：{{ mySignupIds.size }}</view>
      </view>
      <view class="side-card">
        <view class="side-title">参与建议</view>
        <view class="side-line">活动开始前 10 分钟到场签到。</view>
        <view class="side-line">如无法参加请及时取消报名。</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import ListPager from "../../components/ListPager.vue";
import { useH5PullRefresh } from "../../utils/pull-refresh";
import { usePagedList } from "../../utils/paged-list";
import { getEventList, getMyEventSignups, signupEvent, type EventItem } from "../../api/event";
import { useUserStore } from "../../store/user";
import { formatDateTime } from "../../utils/datetime";

const userStore = useUserStore();
userStore.hydrate();
const events = ref<EventItem[]>([]);
const mySignupIds = ref<Set<number>>(new Set());
const loading = ref(false);
const pager = usePagedList(events, 4);
const pagedEvents = computed(() => pager.pagedItems.value);

function goHome() {
  uni.reLaunch({ url: "/pages/home/index" });
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/event-detail/index?id=${id}` });
}

async function toggleSignup(eventId: number) {
  if (!userStore.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    return;
  }
  if (mySignupIds.value.has(eventId)) {
    uni.showToast({ title: "当前仅支持报名，不支持取消", icon: "none" });
    return;
  }
  try {
    await signupEvent(eventId, "来自移动端报名");
    uni.showToast({ title: "报名成功", icon: "success" });
    await loadEvents();
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "报名失败", icon: "none" });
  }
}

function isEventEnded(item: EventItem): boolean {
  return !!item.endTime && new Date(item.endTime).getTime() < Date.now();
}

function isEventFull(item: EventItem): boolean {
  if (item.signupLimit == null) {
    return false;
  }
  return (item.signupCount || 0) >= item.signupLimit;
}

function canSignup(item: EventItem): boolean {
  if (item.status !== 1) {
    return false;
  }
  if (mySignupIds.value.has(item.id)) {
    return false;
  }
  if (isEventEnded(item)) {
    return false;
  }
  if (isEventFull(item)) {
    return false;
  }
  return true;
}

function signupLabel(item: EventItem): string {
  if (mySignupIds.value.has(item.id)) {
    return "已报名";
  }
  if (item.status !== 1) {
    return "活动已下架";
  }
  if (isEventEnded(item)) {
    return "活动已结束";
  }
  if (isEventFull(item)) {
    return "名额已满";
  }
  return "立即报名";
}

async function loadEvents() {
  loading.value = true;
  try {
    await withMinDuration(async () => {
      events.value = await getEventList(userStore.currentCommunityId || undefined);
      if (userStore.token) {
        const signups = await getMyEventSignups();
        mySignupIds.value = new Set(signups.map((item) => item.id));
      } else {
        mySignupIds.value = new Set();
      }
    });
    pager.resetPage();
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadEvents();
});

const { pullStatus, pullOffset, pullText } = useH5PullRefresh(async () => {
  await loadEvents();
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
  background: linear-gradient(135deg, #0f67e7 0%, #2e8aff 55%, #4fc4cf 100%);
  box-shadow: var(--shadow);
}

.subtitle {
  margin-top: 6px;
  margin-bottom: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

.hero-pills {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.pill {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.32);
}

.header {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 10px;
}

.back-btn {
  position: relative;
  border: 1px solid var(--line);
  background: #fff;
}

.back-btn.is-loading {
  padding-right: 30px;
}

.back-btn.is-loading::after {
  content: "";
  position: absolute;
  right: 10px;
  top: 50%;
  width: 12px;
  height: 12px;
  margin-top: -6px;
  border-radius: 999px;
  border: 2px solid currentColor;
  border-right-color: transparent;
  animation: spin 0.7s linear infinite;
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

.list {
  display: grid;
  gap: 10px;
}

.card {
  border: 1px solid #dce7f7;
  border-radius: 18px;
  background: var(--surface);
  backdrop-filter: blur(8px);
  padding: 12px;
  box-shadow: var(--shadow-soft);
}

.name {
  font-size: 16px;
  font-weight: 700;
}

.meta {
  margin-top: 6px;
  color: var(--text-soft);
  font-size: 12px;
}

.desc {
  margin-top: 8px;
  font-size: 14px;
  color: #334155;
}

.signup-btn {
  margin-top: 10px;
  border: 1px solid var(--line);
}

.signup-btn[disabled] {
  opacity: 0.7;
}

.detail-btn {
  margin-top: 8px;
  border: 1px solid #d5e2f5;
  background: #fff;
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
}
</style>
