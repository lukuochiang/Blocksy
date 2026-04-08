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
          <view class="pill">已报名 {{ events.filter((e) => e.joined).length }}</view>
        </view>
      </view>
      <view class="header">
        <button class="back-btn" @click="goHome">返回首页</button>
        <button class="back-btn" :loading="loading" @click="loadEvents">刷新</button>
      </view>
      <skeleton-list v-if="loading" :count="2" />
      <view class="list">
        <view v-for="item in pagedEvents" :key="item.id" class="card">
          <view class="name">{{ item.title }}</view>
          <view class="meta">时间：{{ item.time }}</view>
          <view class="meta">地点：{{ item.location }}</view>
          <view class="desc">{{ item.description }}</view>
          <button
            class="signup-btn"
            :type="item.joined ? 'default' : 'primary'"
            @click="toggleSignup(item.id)"
          >
            {{ item.joined ? "已报名（点此取消）" : "立即报名" }}
          </button>
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
        v-if="!loading && events.length > pageSize"
        :page="page"
        :page-size="pageSize"
        :total="events.length"
        @prev="prevPage"
        @next="nextPage"
      />
    </view>
    <view class="side-col">
      <view class="side-card">
        <view class="side-title">活动状态</view>
        <view class="side-line">活动总数：{{ events.length }}</view>
        <view class="side-line">已报名：{{ events.filter((e) => e.joined).length }}</view>
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
import { onMounted, ref } from "vue";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import ListPager from "../../components/ListPager.vue";
import { useH5PullRefresh } from "../../utils/pull-refresh";
import { computed } from "vue";

interface EventCard {
  id: number;
  title: string;
  time: string;
  location: string;
  description: string;
  joined: boolean;
}

const sourceEvents: EventCard[] = [
  {
    id: 1,
    title: "周末跳蚤市场",
    time: "4月13日 10:00-16:00",
    location: "社区中心广场",
    description: "邻里闲置互换，欢迎带物品参与。",
    joined: false
  },
  {
    id: 2,
    title: "夜跑打卡",
    time: "每周三 20:00",
    location: "南门集合",
    description: "5 公里轻松夜跑，新手友好。",
    joined: true
  }
];

const events = ref<EventCard[]>([]);
const loading = ref(false);
const page = ref(1);
const pageSize = 4;
const pagedEvents = computed(() => {
  const start = (page.value - 1) * pageSize;
  return events.value.slice(start, start + pageSize);
});

function goHome() {
  uni.reLaunch({ url: "/pages/home/index" });
}

function toggleSignup(eventId: number) {
  events.value = events.value.map((item) => {
    if (item.id !== eventId) {
      return item;
    }
    return { ...item, joined: !item.joined };
  });
  const current = events.value.find((item) => item.id === eventId);
  uni.showToast({ title: current?.joined ? "报名成功" : "已取消报名", icon: "none" });
}

async function loadEvents() {
  loading.value = true;
  try {
    await withMinDuration(async () => {
      events.value = sourceEvents;
    });
    page.value = 1;
  } finally {
    loading.value = false;
  }
}

function prevPage() {
  if (page.value <= 1) {
    return;
  }
  page.value -= 1;
}

function nextPage() {
  const totalPages = Math.ceil(events.value.length / pageSize);
  if (page.value >= totalPages) {
    return;
  }
  page.value += 1;
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
  border: 1px solid var(--line);
  background: #fff;
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
