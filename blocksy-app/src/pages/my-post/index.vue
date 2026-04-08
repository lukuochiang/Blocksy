<template>
  <view class="page desktop-layout enter">
    <view class="main-col">
      <view v-if="pullStatus !== 'idle'" class="pull-indicator" :style="{ height: `${pullOffset}px` }">
        <view class="pull-text">{{ pullText }}</view>
      </view>
      <view class="hero-card">
        <view class="title">我的帖子</view>
        <view class="subtitle">管理你在社区发布过的内容</view>
      </view>
      <view class="header">
        <button class="refresh-btn" @click="reload">刷新</button>
      </view>
      <skeleton-list v-if="loading" :count="2" />
      <view v-for="item in pagedPosts" :key="item.id" class="card">
        <view class="content">{{ item.content }}</view>
        <view class="meta">#{{ item.id }} · 评论 {{ item.commentCount }} · {{ formatDateTime(item.createdAt) }}</view>
      </view>
      <empty-state
        v-if="!loading && !posts.length"
        title="暂无发布记录"
        description="先去发一条帖子，再回来查看你的内容。"
        cta-text="去发布"
        cta-variant="primary"
        @cta="goPost"
      />
      <list-pager
        v-if="!loading && posts.length > pageSize"
        :page="page"
        :page-size="pageSize"
        :total="posts.length"
        @prev="prevPage"
        @next="nextPage"
      />
    </view>
    <view class="side-col">
      <view class="side-card">
        <view class="side-title">数据面板</view>
        <view class="side-line">发布数量：{{ posts.length }}</view>
        <view class="side-line">当前社区：{{ userStore.currentCommunityId || "-" }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getMyPosts, PostItem } from "../../api/post";
import { useUserStore } from "../../store/user";
import { formatDateTime } from "../../utils/datetime";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import ListPager from "../../components/ListPager.vue";
import { useH5PullRefresh } from "../../utils/pull-refresh";

const userStore = useUserStore();
userStore.hydrate();
const posts = ref<PostItem[]>([]);
const loading = ref(false);
const page = ref(1);
const pageSize = 5;
const pagedPosts = computed(() => {
  const start = (page.value - 1) * pageSize;
  return posts.value.slice(start, start + pageSize);
});

async function reload() {
  if (!userStore.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    uni.navigateTo({ url: "/pages/login/index" });
    return;
  }
  loading.value = true;
  try {
    await withMinDuration(async () => {
      posts.value = await getMyPosts(userStore.currentCommunityId || undefined);
    });
    page.value = 1;
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function goPost() {
  uni.reLaunch({ url: "/pages/post/index" });
}

function prevPage() {
  if (page.value <= 1) {
    return;
  }
  page.value -= 1;
}

function nextPage() {
  const totalPages = Math.ceil(posts.value.length / pageSize);
  if (page.value >= totalPages) {
    return;
  }
  page.value += 1;
}

const { pullStatus, pullOffset, pullText } = useH5PullRefresh(async () => {
  await reload();
});

onMounted(() => {
  reload();
});
</script>

<style scoped>
.page {
  padding: 16px;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.title {
  font-size: 20px;
  font-weight: 800;
}

.hero-card {
  margin-bottom: 10px;
  padding: 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #126fea 0%, #3392ff 55%, #4ec3d9 100%);
  box-shadow: var(--shadow);
}

.subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

.refresh-btn {
  border: 1px solid #d5e2f5;
  border-radius: 12px;
  padding: 8px 12px;
  background: var(--surface);
}

.card {
  border: 1px solid #dce7f7;
  border-radius: 18px;
  padding: 12px;
  margin-top: 10px;
  background: var(--surface);
  backdrop-filter: blur(8px);
  box-shadow: var(--shadow-soft);
}

.content {
  color: #111827;
}

.meta {
  color: var(--text-soft);
  font-size: 12px;
  margin-top: 6px;
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
