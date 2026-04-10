<template>
  <view class="page enter">
    <view class="hero-card">
      <view class="title">我的分类信息</view>
      <view class="subtitle">查看你发布的二手、失物和求助信息</view>
    </view>
    <view class="toolbar">
      <button class="toolbar-btn" @click="goMine">返回我的</button>
      <button class="toolbar-btn" :class="{ 'is-loading': loading }" :disabled="loading" @click="reload">
        {{ loading ? "刷新中..." : "刷新" }}
      </button>
    </view>
    <view class="status-tabs">
      <button
        v-for="item in statusFilters"
        :key="item.value"
        class="status-tab"
        :class="{ active: currentStatus === item.value }"
        @click="switchStatus(item.value)"
      >
        {{ item.label }}
      </button>
    </view>
    <skeleton-list v-if="loading" :count="3" />
    <view class="list">
      <view v-for="item in rows" :key="item.id" class="card">
        <view class="badge" :class="statusClass(item.status)">{{ statusText(item.status) }}</view>
        <view class="card-title">{{ item.title }}</view>
        <view class="card-meta">{{ item.category }} · {{ item.createdAt ? formatDateTime(item.createdAt) : "-" }}</view>
        <view class="card-content">{{ item.content }}</view>
        <view class="card-price">{{ item.price == null ? "面议" : `¥${item.price}` }}</view>
        <view class="action-row">
          <button class="detail-btn" @click="goDetail(item.id)">查看详情</button>
          <button v-if="item.status === 1 || item.status === 2" class="detail-btn" @click="handleMine(item.id, 'offline')">下架</button>
          <button v-if="item.status === 0" class="detail-btn" @click="handleMine(item.id, 'resubmit')">重新提交</button>
          <button class="detail-btn danger" @click="handleMine(item.id, 'delete')">删除</button>
        </view>
      </view>
    </view>
    <empty-state
      v-if="!loading && !rows.length"
      title="你还没发布分类信息"
      description="去分类信息页发布第一条内容。"
      cta-text="去分类信息"
      cta-variant="primary"
      @cta="goListing"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import { deleteMyListing, getMyListings, offlineMyListing, resubmitMyListing, type ListingItem } from "../../api/listing";
import { useUserStore } from "../../store/user";
import { formatDateTime } from "../../utils/datetime";

const userStore = useUserStore();
userStore.hydrate();

const rows = ref<ListingItem[]>([]);
const loading = ref(false);
const currentStatus = ref<number | undefined>(undefined);
const statusFilters = [
  { label: "全部", value: undefined },
  { label: "待审核", value: 2 },
  { label: "已上架", value: 1 },
  { label: "已下架", value: 0 }
];

function goMine() {
  uni.navigateTo({ url: "/pages/mine/index" });
}

function goListing() {
  uni.navigateTo({ url: "/pages/listing/index" });
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/listing-detail/index?id=${id}&mine=1` });
}

function statusText(status: number) {
  if (status === 2) return "待审核";
  if (status === 1) return "已上架";
  return "已下架/驳回";
}

function statusClass(status: number) {
  if (status === 2) return "pending";
  if (status === 1) return "online";
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
      rows.value = await getMyListings(userStore.currentCommunityId || undefined, currentStatus.value);
    });
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function switchStatus(status: number | undefined) {
  currentStatus.value = status;
  void reload();
}

async function handleMine(id: number, action: "offline" | "resubmit" | "delete") {
  try {
    if (action === "offline") {
      await offlineMyListing(id, "用户端主动下架");
    } else if (action === "resubmit") {
      await resubmitMyListing(id, "用户端重新提交审核");
    } else {
      await deleteMyListing(id, "用户端删除");
    }
    uni.showToast({ title: "操作成功", icon: "none" });
    await reload();
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "操作失败", icon: "none" });
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
  background: linear-gradient(135deg, #0d6be8 0%, #3a7bff 56%, #52b1ff 100%);
  box-shadow: var(--shadow);
}
.title { font-size: 22px; font-weight: 800; }
.subtitle { margin-top: 6px; font-size: 13px; color: rgba(255, 255, 255, 0.9); }
.toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 10px; }
.status-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}
.status-tab {
  border: 1px solid #d5e2f5;
  background: #fff;
  color: #64748b;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
}
.status-tab.active {
  border-color: #bed3f6;
  background: linear-gradient(135deg, #e8f1ff 0%, #ebfbf7 100%);
  color: var(--primary-dark);
}
.toolbar-btn { border: 1px solid var(--line); background: #fff; position: relative; }
.toolbar-btn.is-loading { padding-right: 30px; }
.toolbar-btn.is-loading::after {
  content: ""; position: absolute; right: 10px; top: 50%; width: 12px; height: 12px; margin-top: -6px;
  border-radius: 999px; border: 2px solid currentColor; border-right-color: transparent; animation: spin 0.7s linear infinite;
}
.list { display: grid; gap: 10px; }
.card { border: 1px solid #dce7f7; border-radius: 18px; background: var(--surface); padding: 12px; box-shadow: var(--shadow-soft); }
.badge {
  display: inline-block;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
  margin-bottom: 8px;
}
.badge.pending { background: #fff7ed; color: #b45309; border: 1px solid #fdba74; }
.badge.online { background: #e7f8f0; color: #137a4f; border: 1px solid #86efac; }
.badge.offline { background: #eef2f7; color: #475569; border: 1px solid #cbd5e1; }
.card-title { font-size: 16px; font-weight: 700; }
.card-meta { margin-top: 6px; color: var(--text-soft); font-size: 12px; }
.card-content { margin-top: 8px; color: #334155; font-size: 14px; }
.card-price { margin-top: 8px; color: var(--primary-dark); font-weight: 700; font-size: 14px; }
.action-row { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 8px; }
.detail-btn { border: 1px solid #d5e2f5; background: #fff; }
.detail-btn.danger { border-color: #fecaca; color: #b91c1c; }
</style>
