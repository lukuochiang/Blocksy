<template>
  <view class="page enter">
    <view class="hero-card">
      <view class="title">{{ detail?.title || "活动详情" }}</view>
      <view class="subtitle">查看活动信息并完成报名</view>
      <view class="status-pills">
        <view class="pill">{{ isOffline ? "已下架" : "正常展示" }}</view>
        <view class="pill">{{ isEnded ? "已结束" : "进行中" }}</view>
        <view class="pill">{{ isFull ? "名额已满" : "可报名" }}</view>
        <view class="pill">{{ joined ? "已报名" : "未报名" }}</view>
      </view>
    </view>
    <view class="toolbar">
      <button class="toolbar-btn" @click="goEvent">返回活动列表</button>
      <button class="toolbar-btn" :class="{ 'is-loading': loading }" :disabled="loading" @click="reload">
        {{ loading ? "刷新中..." : "刷新" }}
      </button>
    </view>
    <skeleton-list v-if="loading" :count="2" />
    <view v-if="detail" class="card">
      <view class="row"><text class="label">标题</text><text class="value">{{ detail.title }}</text></view>
      <view class="row"><text class="label">开始时间</text><text class="value">{{ formatDateTime(detail.startTime) }}</text></view>
      <view class="row"><text class="label">结束时间</text><text class="value">{{ detail.endTime ? formatDateTime(detail.endTime) : "-" }}</text></view>
      <view class="row"><text class="label">地点</text><text class="value">{{ detail.location || "-" }}</text></view>
      <view class="row"><text class="label">报名人数</text><text class="value">{{ detail.signupCount || 0 }} / {{ detail.signupLimit || "不限" }}</text></view>
      <view class="content">{{ detail.content }}</view>
      <view v-if="!canJoin" class="offline-tip">{{ unavailableReason }}</view>
      <view class="actions">
        <button class="action-btn" @click="goEvent">返回活动列表</button>
        <button
          class="action-btn"
          type="primary"
          :disabled="!canJoin || signing"
          :loading="signing"
          @click="join"
        >
          {{ joined ? "已报名" : isOffline ? "活动已下架" : isEnded ? "活动已结束" : isFull ? "名额已满" : signing ? "报名中..." : "立即报名" }}
        </button>
      </view>
    </view>
    <empty-state
      v-if="!loading && !detail"
      title="活动不存在"
      description="该活动可能已下架或被删除。"
      cta-text="返回活动页"
      cta-variant="secondary"
      @cta="goEvent"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import { getEventDetail, getMyEventSignups, signupEvent, type EventItem } from "../../api/event";
import { useUserStore } from "../../store/user";
import { formatDateTime } from "../../utils/datetime";

const userStore = useUserStore();
userStore.hydrate();

const eventId = ref<number | null>(null);
const detail = ref<EventItem | null>(null);
const mySignupIds = ref<Set<number>>(new Set());
const loading = ref(false);
const signing = ref(false);
const joined = computed(() => !!(detail.value && mySignupIds.value.has(detail.value.id)));
const isOffline = computed(() => !!(detail.value && detail.value.status !== 1));
const isEnded = computed(() => !!(detail.value?.endTime && new Date(detail.value.endTime).getTime() < Date.now()));
const isFull = computed(() => {
  if (!detail.value) return false;
  if (detail.value.signupLimit == null) return false;
  return (detail.value.signupCount || 0) >= detail.value.signupLimit;
});
const canJoin = computed(() => !isOffline.value && !joined.value && !isEnded.value && !isFull.value);
const unavailableReason = computed(() => {
  if (joined.value) return "你已报名该活动，无需重复提交。";
  if (isOffline.value) return "该活动已被下架，当前仅提供查看，不可报名。";
  if (isEnded.value) return "该活动已结束，报名入口已关闭。";
  if (isFull.value) return "该活动报名名额已满，当前不可继续报名。";
  return "";
});

function goEvent() {
  uni.navigateTo({ url: "/pages/event/index" });
}

async function reload() {
  if (!eventId.value) {
    return;
  }
  loading.value = true;
  try {
    await withMinDuration(async () => {
      detail.value = await getEventDetail(eventId.value as number);
      if (userStore.token) {
        const signups = await getMyEventSignups();
        mySignupIds.value = new Set(signups.map((item) => item.id));
      } else {
        mySignupIds.value = new Set();
      }
    });
  } catch (error) {
    detail.value = null;
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function join() {
  if (!detail.value) {
    return;
  }
  if (!userStore.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    return;
  }
  signing.value = true;
  try {
    await signupEvent(detail.value.id, "来自详情页报名");
    uni.showToast({ title: "报名成功", icon: "success" });
    await reload();
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "报名失败", icon: "none" });
  } finally {
    signing.value = false;
  }
}

onLoad((query) => {
  const id = Number(query?.id);
  if (!Number.isFinite(id) || id <= 0) {
    uni.showToast({ title: "活动ID无效", icon: "none" });
    return;
  }
  eventId.value = id;
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
  background: linear-gradient(135deg, #0f67e7 0%, #2e8aff 55%, #4fc4cf 100%);
  box-shadow: var(--shadow);
}
.title { font-size: 22px; font-weight: 800; }
.subtitle { margin-top: 6px; font-size: 13px; color: rgba(255, 255, 255, 0.9); }
.status-pills { margin-top: 10px; display: flex; gap: 8px; flex-wrap: wrap; }
.pill {
  font-size: 12px; padding: 4px 10px; border-radius: 999px;
  background: rgba(255, 255, 255, 0.18); border: 1px solid rgba(255, 255, 255, 0.32);
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
.offline-tip {
  margin-top: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 12px;
  border: 1px solid #fed7aa;
}
.actions { margin-top: 12px; display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.action-btn { border: 1px solid #d5e2f5; background: #fff; }
</style>
