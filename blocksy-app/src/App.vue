<template>
  <view class="app-shell" :class="{ 'with-tabbar': showTabBar }">
    <view v-if="showDesktopShell" class="desktop-topbar">
      <view class="topbar-inner">
        <view class="brand" @click="go('/pages/home/index')">
          <view class="brand-dot">N</view>
          <view class="brand-text">Norvo</view>
        </view>
        <view class="desktop-nav">
          <button
            v-for="item in desktopNav"
            :key="item.path"
            class="desktop-nav-btn"
            :class="{ active: routePath === item.path }"
            @click="go(item.path)"
          >
            {{ item.label }}
          </button>
        </view>
        <view class="topbar-actions">
          <button class="ghost-btn" @click="go('/pages/post/index')">发布</button>
          <button class="ghost-btn" type="primary" @click="go('/pages/mine/index')">我的</button>
        </view>
      </view>
    </view>

    <view v-if="showDesktopShell" class="desktop-stage">
      <view class="desktop-main">
        <component :is="currentView" />
      </view>
    </view>

    <component v-else :is="currentView" />
    <tab-bar v-if="showTabBar" :current-path="routePath" />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from "vue";
import LoginPage from "./pages/login/index.vue";
import HomePage from "./pages/home/index.vue";
import PostPage from "./pages/post/index.vue";
import MinePage from "./pages/mine/index.vue";
import CommunityPage from "./pages/community/index.vue";
import MyPostPage from "./pages/my-post/index.vue";
import ListingPage from "./pages/listing/index.vue";
import MessagePage from "./pages/message/index.vue";
import EventPage from "./pages/event/index.vue";
import TabBar from "./components/TabBar.vue";

const routePath = ref("/pages/login/index");
const isDesktop = ref(false);

function parsePathFromHash(): string {
  const hash = window.location.hash || "";
  const normalized = hash.replace(/^#/, "");
  if (!normalized) {
    return "/pages/login/index";
  }
  return normalized.startsWith("/") ? normalized : `/${normalized}`;
}

function syncRouteFromHash() {
  routePath.value = parsePathFromHash();
}

const viewMap: Record<string, unknown> = {
  "/pages/login/index": LoginPage,
  "/pages/home/index": HomePage,
  "/pages/listing/index": ListingPage,
  "/pages/post/index": PostPage,
  "/pages/message/index": MessagePage,
  "/pages/mine/index": MinePage,
  "/pages/community/index": CommunityPage,
  "/pages/my-post/index": MyPostPage,
  "/pages/event/index": EventPage
};

const currentView = computed(() => viewMap[routePath.value] || LoginPage);
const tabPaths = new Set([
  "/pages/home/index",
  "/pages/listing/index",
  "/pages/post/index",
  "/pages/message/index",
  "/pages/mine/index"
]);
const showTabBar = computed(() => !isDesktop.value && tabPaths.has(routePath.value));
const showDesktopShell = computed(() => isDesktop.value && routePath.value !== "/pages/login/index");
const desktopNav = [
  { label: "首页", path: "/pages/home/index" },
  { label: "分类信息", path: "/pages/listing/index" },
  { label: "社区活动", path: "/pages/event/index" },
  { label: "消息", path: "/pages/message/index" }
];

function go(path: string) {
  if (routePath.value === path) {
    return;
  }
  uni.reLaunch({ url: path });
}

function updateViewportMode() {
  isDesktop.value = window.innerWidth >= 960;
}

onMounted(() => {
  if (!window.location.hash) {
    window.location.hash = "#/pages/login/index";
  }
  updateViewportMode();
  syncRouteFromHash();
  window.addEventListener("resize", updateViewportMode);
  window.addEventListener("hashchange", syncRouteFromHash);
});

onUnmounted(() => {
  window.removeEventListener("resize", updateViewportMode);
  window.removeEventListener("hashchange", syncRouteFromHash);
});
</script>

<style>
html,
body,
#app {
  margin: 0;
  min-height: 100%;
}

:root {
  --bg: #f3f6fc;
  --surface: rgba(255, 255, 255, 0.88);
  --surface-solid: #ffffff;
  --surface-muted: #f6f9ff;
  --text: #0f172a;
  --text-soft: #64748b;
  --line: #d7e1f0;
  --primary: #1473ff;
  --primary-dark: #0e56c9;
  --success: #0f9f74;
  --danger: #de3e45;
  --shadow: 0 10px 34px rgba(29, 45, 84, 0.1);
  --shadow-soft: 0 6px 20px rgba(29, 45, 84, 0.06);
  --radius: 16px;
  --s1: 8px;
  --s2: 16px;
  --s3: 24px;
  --s4: 32px;
}

body {
  background:
    radial-gradient(circle at 8% -20%, #d8ecff 0%, transparent 40%),
    radial-gradient(circle at 95% -10%, #dff8ef 0%, transparent 34%),
    radial-gradient(circle at 50% 120%, #e9edff 0%, transparent 44%),
    var(--bg);
  color: var(--text);
  font-family: "SF Pro Display", "PingFang SC", "Segoe UI", "Helvetica Neue", Arial, sans-serif;
  line-height: 1.45;
}

view {
  display: block;
}

input,
textarea,
button {
  font: inherit;
}

button {
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--surface-solid);
  color: var(--text);
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: var(--shadow-soft);
}

button:hover {
  transform: translateY(-1px);
}

button:disabled,
button[disabled] {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

button[loading="true"] {
  position: relative;
  opacity: 0.92;
  padding-right: 30px;
}

button[loading="true"]::after {
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

button[type="primary"] {
  border: 0;
  background: linear-gradient(135deg, var(--primary) 0%, #18a4ff 100%);
  color: #fff;
}

button[type="warn"] {
  background: #fff1f2;
  color: var(--danger);
}

.app-shell {
  min-height: 100vh;
}

.app-shell.with-tabbar {
  padding-bottom: 86px;
}

@media (min-width: 960px) {
  .app-shell {
    max-width: 1320px;
    margin: 0 auto;
    padding-left: 18px;
    padding-right: 18px;
  }

  .app-shell.with-tabbar {
    padding-bottom: 24px;
  }

  .desktop-topbar {
    position: sticky;
    top: 0;
    z-index: 30;
    padding-top: 12px;
  }

  .topbar-inner {
    display: grid;
    grid-template-columns: 180px 1fr 170px;
    align-items: center;
    gap: 14px;
    padding: 10px 14px;
    border-radius: 16px;
    border: 1px solid #d4e3f7;
    background: rgba(255, 255, 255, 0.86);
    backdrop-filter: blur(12px);
    box-shadow: var(--shadow-soft);
  }

  .brand {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
  }

  .brand-dot {
    width: 30px;
    height: 30px;
    border-radius: 999px;
    display: grid;
    place-items: center;
    color: #fff;
    font-weight: 700;
    background: linear-gradient(135deg, #1274ff 0%, #22c4e4 100%);
  }

  .brand-text {
    font-size: 18px;
    font-weight: 700;
    color: #0f325f;
  }

  .desktop-nav {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .desktop-nav-btn {
    border: 0;
    background: transparent;
    color: #4f6b91;
    min-height: 38px;
    padding: 0 12px;
    box-shadow: none;
  }

  .desktop-nav-btn.active {
    color: #0d4fb4;
    background: linear-gradient(135deg, #e4f0ff 0%, #e7fbf5 100%);
    font-weight: 700;
  }

  .topbar-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }

  .ghost-btn {
    min-height: 36px;
    padding: 0 12px;
    border-radius: 10px;
  }

  .desktop-stage {
    margin-top: 14px;
    display: block;
  }

  .desktop-main {
    min-width: 0;
  }
}

@media (max-width: 959px) {
  .desktop-topbar,
  .desktop-stage {
    display: none;
  }
}

@media (hover: hover) {
  [class*="card"] {
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }

  [class*="card"]:hover {
    transform: translateY(-2px);
    box-shadow: 0 14px 28px rgba(24, 39, 75, 0.12);
  }
}

.enter {
  animation: rise-in 0.48s ease both;
}

.enter-delay-1 {
  animation-delay: 0.06s;
}

.enter-delay-2 {
  animation-delay: 0.12s;
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
