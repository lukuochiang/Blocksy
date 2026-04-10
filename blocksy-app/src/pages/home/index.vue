<template>
  <view class="page desktop-layout enter">
    <view class="main-col">
      <view v-if="pullStatus !== 'idle'" class="pull-indicator" :style="{ height: `${pullOffset}px` }">
        <view class="pull-text">{{ pullText }}</view>
      </view>
      <view class="hero-card enter enter-delay-1">
        <view class="title">社区动态</view>
        <view class="user">Hi, {{ userStore.displayName || "访客" }}</view>
        <view class="community">当前社区：{{ currentCommunityName || "未选择" }}</view>
      </view>

      <view class="quick-grid">
        <button class="quick-btn" @click="goCommunity">切换社区</button>
        <button class="quick-btn" @click="goPost">发布帖子</button>
        <button class="quick-btn" @click="goEvent">社区活动</button>
        <button class="quick-btn" :class="{ 'is-loading': loading }" :disabled="loading" @click="reload">
          {{ loading ? "刷新中..." : "刷新动态" }}
        </button>
      </view>

      <view class="subtitle">最新帖子</view>
      <skeleton-list v-if="loading" :count="3" />
      <view v-for="item in posts" :key="item.id" class="card">
        <view class="content">{{ item.content }}</view>
        <view class="meta">#{{ item.id }} · 评论 {{ item.commentCount }} · {{ formatDateTime(item.createdAt) }}</view>
        <view class="ops">
          <button size="mini" @click="goPost">评论</button>
          <button size="mini" type="warn" @click="reportPost(item.id)">举报</button>
        </view>
      </view>
      <empty-state
        v-if="!loading && !posts.length"
        title="当前社区暂无帖子"
        description="成为第一个发帖的人，分享你的邻里生活。"
        cta-text="去发布"
        cta-variant="primary"
        @cta="goPost"
      />
      <list-pager
        v-if="!loading && total > pageSize"
        :page="page"
        :page-size="pageSize"
        :total="total"
        @prev="onPrevPage"
        @next="onNextPage"
      />
    </view>
    <view class="side-col">
      <view class="side-card enter enter-delay-1">
        <view class="side-title">社区面板</view>
        <view class="side-line">当前社区：{{ currentCommunityName || "未选择" }}</view>
        <view class="side-line">动态数量：{{ total }}</view>
        <view class="side-line">登录用户：{{ userStore.displayName || "未登录" }}</view>
      </view>
      <view class="side-card enter enter-delay-2">
        <view class="side-title">快捷入口</view>
        <button class="side-btn" @click="goCommunity">社区选择</button>
        <button class="side-btn" @click="goPost">立即发帖</button>
        <button class="side-btn" @click="goEvent">活动中心</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getPostList, PostItem } from "../../api/post";
import { createReport } from "../../api/report";
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
const pageSize = ref(5);
const total = ref(0);

const currentCommunityName = computed(() => {
  const community = userStore.communities.find((item) => item.communityId === userStore.currentCommunityId);
  return community?.communityName;
});

async function reload() {
  await loadPage(1);
}

async function loadPage(targetPage: number) {
  loading.value = true;
  try {
    await withMinDuration(async () => {
      if (userStore.token) {
        await userStore.fetchMe();
        await userStore.fetchCommunities();
      }
      const response = await getPostList({
        communityId: userStore.currentCommunityId || undefined,
        page: targetPage,
        pageSize: pageSize.value
      });
      posts.value = response.items || [];
      total.value = response.total || 0;
      page.value = response.page || targetPage;
      pageSize.value = response.pageSize || pageSize.value;
    });
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function onPrevPage() {
  if (page.value <= 1 || loading.value) {
    return;
  }
  void loadPage(page.value - 1);
}

function onNextPage() {
  const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value));
  if (page.value >= maxPage || loading.value) {
    return;
  }
  void loadPage(page.value + 1);
}

async function reportPost(postId: number) {
  if (!userStore.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    return;
  }
  try {
    await createReport({
      targetType: "POST",
      targetId: postId,
      reason: "不当内容"
    });
    uni.showToast({ title: "举报已提交", icon: "success" });
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "举报失败", icon: "none" });
  }
}

function goCommunity() {
  uni.navigateTo({ url: "/pages/community/index" });
}

function goPost() {
  uni.navigateTo({ url: "/pages/post/index" });
}

function goEvent() {
  uni.navigateTo({ url: "/pages/event/index" });
}

onMounted(() => {
  reload();
});

const { pullStatus, pullOffset, pullText } = useH5PullRefresh(async () => {
  await reload();
});
</script>

<style scoped>
.page {
  padding: 16px;
  box-sizing: border-box;
}

.hero-card {
  background: linear-gradient(135deg, #0f65eb 0%, #18a4ff 55%, #2cc9bf 100%);
  color: #fff;
  padding: 20px 18px;
  border-radius: 20px;
  box-shadow: var(--shadow);
  margin-bottom: 14px;
  position: relative;
  overflow: hidden;
}

.hero-card::after {
  content: "";
  position: absolute;
  right: -30px;
  top: -30px;
  width: 120px;
  height: 120px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
}

.title {
  font-size: 24px;
  font-weight: 700;
}

.user, .community {
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.95);
}

.quick-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 14px;
}

.quick-btn {
  position: relative;
  background: var(--surface);
  backdrop-filter: blur(8px);
  border: 1px solid #d5e2f5;
  border-radius: 12px;
  padding: 12px;
  font-weight: 600;
  box-shadow: var(--shadow-soft);
}

.quick-btn.is-loading {
  padding-right: 28px;
}

.quick-btn.is-loading::after {
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

.subtitle {
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 10px;
}

.card {
  border: 1px solid #dce7f7;
  border-radius: 18px;
  padding: 14px;
  margin-bottom: 12px;
  background: var(--surface);
  backdrop-filter: blur(8px);
  box-shadow: var(--shadow-soft);
}

.content {
  margin-bottom: 8px;
  font-size: 15px;
  color: #111827;
}

.meta {
  color: var(--text-soft);
  font-size: 12px;
  margin-bottom: 8px;
}

.ops {
  display: flex;
  gap: 8px;
}

.ops button {
  background: #fff;
  border-color: #d5e2f5;
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
    margin-bottom: 8px;
    border: 1px solid var(--line);
    background: #fff;
  }
}
</style>
