<template>
  <view class="page enter">
    <view class="hero-card">
      <view class="title">{{ detail?.title || "分类信息详情" }}</view>
      <view class="subtitle">查看发布内容与状态</view>
      <view v-if="detail" class="pill-row">
        <view class="pill">{{ statusText(detail.status) }}</view>
        <view class="pill">{{ detail.category }}</view>
      </view>
    </view>
    <view class="toolbar">
      <button class="toolbar-btn" @click="goBack">返回</button>
      <button class="toolbar-btn" :class="{ 'is-loading': loading }" :disabled="loading" @click="reload">
        {{ loading ? "刷新中..." : "刷新" }}
      </button>
    </view>

    <skeleton-list v-if="loading" :count="2" />
    <view v-if="detail" class="card">
      <view class="row"><text class="label">标题</text><text class="value">{{ detail.title }}</text></view>
      <view class="row"><text class="label">价格</text><text class="value">{{ detail.price == null ? "面议" : `¥${detail.price}` }}</text></view>
      <view class="row"><text class="label">联系方式</text><text class="value">{{ detail.contact || "-" }}</text></view>
      <view class="row"><text class="label">发布时间</text><text class="value">{{ detail.createdAt ? formatDateTime(detail.createdAt) : "-" }}</text></view>
      <view class="content">{{ detail.content }}</view>
      <view v-if="detail.status !== 1" class="warn-tip">当前信息未在公共列表展示（待审核或已下架）。</view>
      <view v-if="mineMode" class="mine-actions">
        <button v-if="detail.status === 1 || detail.status === 2" class="action-btn" @click="onMineAction('offline')">下架</button>
        <button v-if="detail.status === 0" class="action-btn" @click="onMineAction('resubmit')">重新提交审核</button>
        <button class="action-btn danger" @click="onMineAction('delete')">删除</button>
      </view>
      <view v-if="mineMode && logs.length" class="timeline">
        <view class="timeline-title">状态时间线</view>
        <view v-for="item in logs" :key="item.id" class="timeline-item">
          <view class="timeline-head">
            <text class="timeline-action">{{ item.action }}</text>
            <text class="timeline-time">{{ formatDateTime(item.createdAt) }}</text>
          </view>
          <view class="timeline-note">{{ item.note || "-" }}</view>
        </view>
      </view>
    </view>
    <view v-if="!loading && recommendList.length" class="recommend-card">
      <view class="recommend-title">相关推荐</view>
      <view class="recommend-grid">
        <view
          v-for="item in recommendList"
          :key="item.id"
          class="recommend-item"
          @click="goDetail(item.id)"
        >
          <image v-if="item.coverUrl" class="recommend-cover" :src="item.coverUrl" mode="aspectFill" />
          <view class="recommend-name">{{ item.title }}</view>
          <view class="recommend-meta">
            <text>{{ item.price == null ? "面议" : `¥${item.price}` }}</text>
            <text>{{ item.createdAt ? formatDateTime(item.createdAt) : "-" }}</text>
          </view>
        </view>
      </view>
    </view>
    <empty-state
      v-if="!loading && !detail"
      title="信息不存在"
      description="该分类信息可能已删除或无权限访问。"
      cta-text="返回分类页"
      cta-variant="secondary"
      @cta="goListing"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import {
  deleteMyListing,
  getListingDetail,
  getListingRecommendations,
  getMyListingDetail,
  getMyListingLogs,
  offlineMyListing,
  resubmitMyListing,
  type ListingItem,
  type ListingStatusLogItem
} from "../../api/listing";
import { formatDateTime } from "../../utils/datetime";

const listingId = ref<number | null>(null);
const mineMode = ref(false);
const detail = ref<ListingItem | null>(null);
const loading = ref(false);
const logs = ref<ListingStatusLogItem[]>([]);
const recommendList = ref<ListingItem[]>([]);

function statusText(status: number) {
  if (status === 2) return "待审核";
  if (status === 1) return "已上架";
  return "已下架/驳回";
}

function goListing() {
  uni.navigateTo({ url: "/pages/listing/index" });
}

function goBack() {
  uni.navigateBack();
}

function goDetail(id: number) {
  uni.navigateTo({
    url: `/pages/listing-detail/index?id=${id}`
  });
}

async function reload() {
  if (!listingId.value) return;
  loading.value = true;
  try {
    await withMinDuration(async () => {
      detail.value = mineMode.value ? await getMyListingDetail(listingId.value as number) : await getListingDetail(listingId.value as number);
      if (mineMode.value) {
        logs.value = await getMyListingLogs(listingId.value as number);
      } else {
        logs.value = [];
      }
      recommendList.value = await getListingRecommendations(listingId.value as number, 6);
    });
  } catch (error) {
    detail.value = null;
    recommendList.value = [];
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function onMineAction(action: "offline" | "resubmit" | "delete") {
  if (!listingId.value) return;
  try {
    if (action === "offline") {
      await offlineMyListing(listingId.value, "详情页下架");
    } else if (action === "resubmit") {
      await resubmitMyListing(listingId.value, "详情页重新提交");
    } else {
      await deleteMyListing(listingId.value, "详情页删除");
    }
    uni.showToast({ title: "操作成功", icon: "none" });
    await reload();
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "操作失败", icon: "none" });
  }
}

onLoad((query) => {
  const id = Number(query?.id);
  if (!Number.isFinite(id) || id <= 0) {
    uni.showToast({ title: "参数错误", icon: "none" });
    return;
  }
  listingId.value = id;
  mineMode.value = query?.mine === "1";
  void reload();
});
</script>

<style scoped>
.page { padding: 16px; box-sizing: border-box; }
.hero-card {
  margin-bottom: 10px;
  padding: 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #0963e6 0%, #18a4ff 58%, #32cab9 100%);
  box-shadow: var(--shadow);
}
.title { font-size: 22px; font-weight: 800; }
.subtitle { margin-top: 6px; font-size: 13px; color: rgba(255,255,255,0.9); }
.pill-row { margin-top: 10px; display: flex; gap: 8px; }
.pill {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.35);
}
.toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 10px; }
.toolbar-btn { border: 1px solid var(--line); background: #fff; position: relative; }
.toolbar-btn.is-loading { padding-right: 30px; }
.toolbar-btn.is-loading::after {
  content: ""; position: absolute; right: 10px; top: 50%; width: 12px; height: 12px; margin-top: -6px;
  border-radius: 999px; border: 2px solid currentColor; border-right-color: transparent; animation: spin 0.7s linear infinite;
}
.card { border: 1px solid #dce7f7; border-radius: 18px; background: var(--surface); padding: 12px; box-shadow: var(--shadow-soft); }
.row { display: flex; justify-content: space-between; gap: 10px; font-size: 14px; margin-bottom: 8px; }
.label { color: var(--text-soft); }
.value { color: #0f172a; text-align: right; }
.content { margin-top: 10px; color: #334155; font-size: 14px; line-height: 1.5; }
.warn-tip {
  margin-top: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 12px;
  border: 1px solid #fed7aa;
}
.mine-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.action-btn {
  border: 1px solid #d5e2f5;
  background: #fff;
}
.action-btn.danger {
  border-color: #fecaca;
  color: #b91c1c;
}
.timeline {
  margin-top: 14px;
  border-top: 1px solid #e2e8f0;
  padding-top: 10px;
}
.timeline-title {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}
.timeline-item {
  margin-top: 8px;
  padding: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
}
.timeline-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
}
.timeline-action {
  color: #0f172a;
  font-weight: 700;
}
.timeline-time {
  color: #64748b;
}
.timeline-note {
  margin-top: 4px;
  color: #475569;
  font-size: 12px;
}
.recommend-card {
  margin-top: 12px;
  border: 1px solid #dce7f7;
  border-radius: 18px;
  background: var(--surface);
  padding: 12px;
  box-shadow: var(--shadow-soft);
}
.recommend-title {
  font-size: 14px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 8px;
}
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}
.recommend-item {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 8px;
  background: #f8fafc;
}
.recommend-cover {
  width: 100%;
  height: 82px;
  border-radius: 8px;
  margin-bottom: 6px;
}
.recommend-name {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.3;
}
.recommend-meta {
  margin-top: 4px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 11px;
  color: #64748b;
}
</style>
