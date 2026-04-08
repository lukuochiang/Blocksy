<template>
  <view class="page desktop-layout enter">
    <view class="main-col">
      <view class="hero-card">
        <view class="hero-title">个人中心</view>
        <view class="hero-subtitle">管理你的社区身份与内容资产</view>
      </view>
      <view class="profile-card">
        <view class="title">账号信息</view>
        <view class="status-chip">{{ userStore.isLoggedIn ? "已登录" : "未登录" }}</view>
        <view class="profile-item"><text>用户名</text><text>{{ userStore.profile?.username || "-" }}</text></view>
        <view class="profile-item"><text>昵称</text><text>{{ userStore.profile?.nickname || "-" }}</text></view>
        <view class="profile-item"><text>用户 ID</text><text>#{{ userStore.profile?.id || "-" }}</text></view>
      </view>
      <view class="actions-grid">
        <button @click="refreshMe">刷新用户信息</button>
        <button @click="goMyPosts">我的帖子</button>
        <button @click="goCommunity">切换社区</button>
        <button v-if="userStore.isLoggedIn" type="warn" @click="logout">退出登录</button>
        <button v-else @click="goLogin">去登录</button>
      </view>
    </view>
    <view class="side-col">
      <view class="side-card">
        <view class="side-title">账号状态</view>
        <view class="side-line">登录状态：{{ userStore.isLoggedIn ? "已登录" : "未登录" }}</view>
        <view class="side-line">默认社区：{{ userStore.currentCommunityId || "-" }}</view>
        <view class="side-line">可选社区：{{ userStore.communities.length }}</view>
      </view>
      <view class="side-card">
        <view class="side-title">快捷入口</view>
        <button class="side-btn" @click="goMyPosts">我的动态</button>
        <button class="side-btn" @click="goCommunity">社区选择</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useUserStore } from "../../store/user";

const userStore = useUserStore();
userStore.hydrate();

async function refreshMe() {
  if (!userStore.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    return;
  }
  try {
    await userStore.fetchMe();
    uni.showToast({ title: "刷新成功", icon: "success" });
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "刷新失败", icon: "none" });
  }
}

function goLogin() {
  uni.navigateTo({ url: "/pages/login/index" });
}

function goMyPosts() {
  uni.navigateTo({ url: "/pages/my-post/index" });
}

function goCommunity() {
  uni.navigateTo({ url: "/pages/community/index" });
}

function logout() {
  userStore.logout();
  uni.showToast({ title: "已退出", icon: "success" });
  setTimeout(() => {
    uni.reLaunch({ url: "/pages/login/index" });
  }, 1000);
}

onMounted(() => {
  if (userStore.token && !userStore.profile) {
    refreshMe();
  }
});
</script>

<style scoped>
.page {
  padding: 16px;
  box-sizing: border-box;
}

.profile-card {
  background: var(--surface);
  backdrop-filter: blur(8px);
  border: 1px solid #dce7f7;
  border-radius: 20px;
  padding: 14px;
  box-shadow: var(--shadow);
}

.hero-card {
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #0d6be8 0%, #3a7bff 56%, #52b1ff 100%);
  box-shadow: var(--shadow);
}

.hero-title {
  font-size: 22px;
  font-weight: 800;
}

.hero-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

.title {
  font-size: 20px;
  font-weight: 700;
}

.status-chip {
  display: inline-block;
  margin-top: 8px;
  margin-bottom: 10px;
  font-size: 12px;
  border-radius: 999px;
  padding: 4px 10px;
  background: #e8f1ff;
  color: var(--primary-dark);
}

.profile-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  padding: 10px 0;
  border-top: 1px solid #eef2f6;
}

.profile-item text:first-child {
  color: var(--text-soft);
}

.actions-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.actions-grid button {
  background: var(--surface-solid);
  border: 1px solid #d5e2f5;
  border-radius: 12px;
  padding: 10px 12px;
  font-weight: 600;
}

.actions-grid button[type="warn"] {
  grid-column: 1 / -1;
  background: #fff1f2;
  color: var(--danger);
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
