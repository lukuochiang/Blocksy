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
          <view class="stat">总消息 {{ total }}</view>
          <view class="stat">未读 {{ unreadCount }}</view>
        </view>
      </view>
      <view class="toolbar">
        <button class="toolbar-btn" :class="{ 'is-loading': loading }" :disabled="loading" @click="reloadRows">
          {{ loading ? "刷新中..." : "刷新" }}
        </button>
        <button class="toolbar-btn" :disabled="!unreadCount" @click="markAllRead">全部已读</button>
        <button class="toolbar-btn" :disabled="!pagedRows.some((item) => !item.isRead)" @click="markVisibleRead">当前页已读</button>
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

      <skeleton-list v-if="loading" :count="3" />
      <view class="list">
        <view v-for="item in pagedRows" :key="item.id" class="row" :class="{ unread: !item.isRead }">
          <view class="row-head">
            <view class="row-title">{{ item.title }}</view>
            <view class="time">{{ item.time }}</view>
          </view>
          <view class="content">{{ item.content }}</view>
          <view class="meta">{{ item.typeText }}</view>
          <view class="row-actions">
            <button v-if="!item.isRead" class="inline-btn" @click="markOneRead(item.id)">标记已读</button>
          </view>
        </view>
      </view>
      <empty-state
        v-if="!loading && !pagedRows.length"
        title="暂无消息"
        description="有新的互动和通知时会第一时间出现在这里。"
        cta-text="去首页"
        cta-variant="secondary"
        @cta="goHome"
      />
      <list-pager
        v-if="!loading && total > pageSize"
        :page="page"
        :page-size="pageSize"
        :total="total"
        @prev="prevPage"
        @next="nextPage"
      />
    </view>
    <view class="side-col">
      <view class="side-card">
        <view class="side-title">消息概览</view>
        <view class="side-line">全部消息：{{ total }}</view>
        <view class="side-line">当前页：{{ rows.length }}</view>
        <view class="side-line">未读：{{ unreadCount }}</view>
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
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import {
  getNotificationPage,
  getNotificationUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
  markNotificationsReadBatch,
  type NotificationItem
} from "../../api/notification";
import { formatDateTime } from "../../utils/datetime";
import { useUserStore } from "../../store/user";

type TabCode = "all" | "interaction" | "system" | "activity";
interface MessageRow {
  id: number;
  type: string;
  typeText: string;
  title: string;
  content: string;
  time: string;
  isRead: boolean;
}

const tabs: Array<{ code: TabCode; name: string }> = [
  { code: "all", name: "全部" },
  { code: "interaction", name: "互动" },
  { code: "system", name: "系统" },
  { code: "activity", name: "活动" }
];

const userStore = useUserStore();
userStore.hydrate();
const rows = ref<MessageRow[]>([]);
const loading = ref(false);
const unreadCount = ref(0);
const activeTab = ref<TabCode>("all");
const page = ref(1);
const pageSize = 4;
const total = ref(0);

const pagedRows = computed(() => {
  return rows.value;
});

watch(activeTab, () => {
  page.value = 1;
  void reloadRows();
});

watch(page, () => {
  void reloadRows();
});

function prevPage() {
  if (page.value <= 1) {
    return;
  }
  page.value -= 1;
}

function nextPage() {
  const totalPages = Math.ceil(total.value / pageSize);
  if (page.value >= totalPages) {
    return;
  }
  page.value += 1;
}

const { pullStatus, pullOffset, pullText } = useH5PullRefresh(async () => {
  await reloadRows();
});

function goHome() {
  uni.reLaunch({ url: "/pages/home/index" });
}

function toRow(item: NotificationItem): MessageRow {
  const map: Record<string, { type: TabCode; text: string }> = {
    COMMENT: { type: "interaction", text: "评论通知" },
    LIKE: { type: "interaction", text: "点赞通知" },
    EVENT_SIGNUP: { type: "activity", text: "活动通知" },
    SYSTEM: { type: "system", text: "系统通知" }
  };
  const matched = map[item.type] || { type: "system", text: item.type };
  return {
    id: item.id,
    type: matched.type,
    typeText: matched.text,
    title: item.title || matched.text,
    content: item.content || "",
    time: formatDateTime(item.createdAt),
    isRead: item.isRead
  };
}

async function refreshUnreadCount() {
  const result = await getNotificationUnreadCount();
  unreadCount.value = result.unreadCount || 0;
}

async function markOneRead(id: number) {
  try {
    await markNotificationRead(id);
    rows.value = rows.value.map((item) => (item.id === id ? { ...item, isRead: true } : item));
    await refreshUnreadCount();
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "操作失败", icon: "none" });
  }
}

async function markAllRead() {
  try {
    await markAllNotificationsRead();
    rows.value = rows.value.map((item) => ({ ...item, isRead: true }));
    unreadCount.value = 0;
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "操作失败", icon: "none" });
  }
}

async function markVisibleRead() {
  const unreadIds = pagedRows.value.filter((item) => !item.isRead).map((item) => item.id);
  if (!unreadIds.length) {
    return;
  }
  try {
    await markNotificationsReadBatch(unreadIds);
    rows.value = rows.value.map((item) => (unreadIds.includes(item.id) ? { ...item, isRead: true } : item));
    await refreshUnreadCount();
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "操作失败", icon: "none" });
  }
}

function mapTabToType(tab: TabCode): "INTERACTION" | "SYSTEM" | "ACTIVITY" | undefined {
  if (tab === "interaction") return "INTERACTION";
  if (tab === "system") return "SYSTEM";
  if (tab === "activity") return "ACTIVITY";
  return undefined;
}

async function reloadRows() {
  if (!userStore.token) {
    rows.value = [];
    unreadCount.value = 0;
    return;
  }
  loading.value = true;
  try {
    await withMinDuration(async () => {
      const result = await getNotificationPage({
        page: page.value,
        pageSize,
        type: mapTabToType(activeTab.value)
      });
      total.value = result.total || 0;
      rows.value = (result.items || []).map(toRow);
      await refreshUnreadCount();
    });
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

reloadRows();
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

.toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 10px;
}

.toolbar-btn {
  position: relative;
  border: 1px solid var(--line);
  background: #fff;
}

.toolbar-btn.is-loading {
  padding-right: 30px;
}

.toolbar-btn.is-loading::after {
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

.row.unread {
  border-color: #9bc3ff;
  box-shadow: 0 10px 18px rgba(60, 120, 220, 0.14);
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

.row-actions {
  margin-top: 8px;
}

.inline-btn {
  border: 1px solid #d5e2f5;
  background: #fff;
  border-radius: 10px;
  padding: 6px 10px;
  font-size: 12px;
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
