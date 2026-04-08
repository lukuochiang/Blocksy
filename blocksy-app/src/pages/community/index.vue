<template>
  <view class="page desktop-layout enter">
    <view class="main-col">
      <view v-if="pullStatus !== 'idle'" class="pull-indicator" :style="{ height: `${pullOffset}px` }">
        <view class="pull-text">{{ pullText }}</view>
      </view>
      <view class="hero-card">
        <view class="title">社区选择</view>
        <view class="subtitle">加入你的真实社区，优先看到身边内容</view>
      </view>
      <view class="header">
        <button class="back-btn" @click="goHome">返回首页</button>
        <button class="back-btn" :loading="loading" @click="loadCommunities">刷新</button>
      </view>
    <skeleton-list v-if="loading" :count="3" />
    <view v-for="item in pagedCommunities" :key="item.communityId" class="card">
      <view class="name">{{ item.communityName }}</view>
      <view class="code">{{ item.communityCode }}</view>
      <button
        class="select-btn"
        :type="item.communityId === userStore.currentCommunityId ? 'primary' : 'default'"
        @click="selectCommunity(item.communityId)"
      >
        {{ item.communityId === userStore.currentCommunityId ? "当前社区" : "切换到此社区" }}
      </button>
    </view>
    <empty-state
      v-if="!loading && !communities.length"
      title="暂无可选社区"
      description="请联系管理员开通社区或稍后刷新重试。"
      cta-text="去首页"
      cta-variant="secondary"
      @cta="goHome"
    />
    <list-pager
      v-if="!loading && communities.length > pageSize"
      :page="page"
      :page-size="pageSize"
      :total="communities.length"
      @prev="prevPage"
      @next="nextPage"
    />
    </view>
    <view class="side-col">
      <view class="side-card">
        <view class="side-title">当前状态</view>
        <view class="side-line">默认社区：{{ userStore.currentCommunityId || "-" }}</view>
        <view class="side-line">可选社区：{{ communities.length }}</view>
      </view>
      <view class="side-card">
        <view class="side-title">操作建议</view>
        <view class="side-line">优先选择你常住社区</view>
        <view class="side-line">切换后首页会按社区更新内容</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useUserStore } from "../../store/user";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import ListPager from "../../components/ListPager.vue";
import { useH5PullRefresh } from "../../utils/pull-refresh";

const userStore = useUserStore();
userStore.hydrate();
const loading = ref(false);
const page = ref(1);
const pageSize = 6;

const communities = computed(() => userStore.communities);
const pagedCommunities = computed(() => {
  const start = (page.value - 1) * pageSize;
  return communities.value.slice(start, start + pageSize);
});

async function loadCommunities() {
  if (!userStore.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    uni.navigateTo({ url: "/pages/login/index" });
    return;
  }
  loading.value = true;
  try {
    await withMinDuration(async () => {
      await userStore.fetchCommunities();
    });
    page.value = 1;
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "加载社区失败", icon: "none" });
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
  const totalPages = Math.ceil(communities.value.length / pageSize);
  if (page.value >= totalPages) {
    return;
  }
  page.value += 1;
}

function goHome() {
  uni.reLaunch({ url: "/pages/home/index" });
}

const { pullStatus, pullOffset, pullText } = useH5PullRefresh(async () => {
  await loadCommunities();
});

async function selectCommunity(communityId: number) {
  try {
    await userStore.changeCommunity(communityId);
    uni.showToast({ title: "切换成功", icon: "success" });
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "切换失败", icon: "none" });
  }
}

onMounted(() => {
  loadCommunities();
});
</script>

<style scoped>
.page {
  padding: 16px;
  box-sizing: border-box;
}

.title {
  font-size: 20px;
  font-weight: 800;
}

.hero-card {
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #0d6be8 0%, #1ea2ff 56%, #4ec7c2 100%);
  box-shadow: var(--shadow);
}

.subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
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

.card {
  border: 1px solid #dce7f7;
  border-radius: 18px;
  background: var(--surface);
  backdrop-filter: blur(8px);
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: var(--shadow-soft);
}

.name {
  font-weight: 700;
  margin-bottom: 6px;
}

.code {
  color: var(--text-soft);
  margin-bottom: 10px;
}

.select-btn {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 8px 12px;
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
}

</style>
