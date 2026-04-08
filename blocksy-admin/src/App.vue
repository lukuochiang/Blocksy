<template>
  <router-view v-if="route.meta.hideShell" />
  <el-container v-else class="app-shell">
    <el-header class="header">
      <div class="brand-wrap">
        <img src="/images/logo.png" alt="logo" class="logo" />
        <div class="brand-text">
          <span class="brand">Norvo Admin</span>
          <span class="brand-sub">邻里社区运营后台</span>
        </div>
      </div>
      <div class="right">
        <template v-if="adminStore.isLoggedIn">
          <span class="username">{{ adminStore.username }}</span>
          <el-button size="small" plain @click="logout">退出</el-button>
        </template>
        <router-link v-else to="/login">
          <el-button size="small" type="primary">登录</el-button>
        </router-link>
      </div>
    </el-header>
    <el-container class="content-wrapper">
      <el-aside width="248px" class="aside">
        <div class="sidebar">
          <el-menu :default-active="route.path" router class="menu">
            <el-sub-menu v-for="group in menuGroups" :key="group.label" :index="group.label">
              <template #title>{{ group.label }}</template>
              <el-menu-item v-for="item in group.children" :key="item.path" :index="item.path">
                {{ item.label }}
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </div>
      </el-aside>
      <el-main class="main">
        <div class="breadcrumb-wrapper">
          <el-breadcrumb separator="›" class="page-breadcrumb">
            <el-breadcrumb-item>
              <router-link to="/" class="home-link">
                <span class="home-icon">⌂</span>
                <span>首页</span>
              </router-link>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-for="(item, index) in breadcrumbItems.slice(1)" :key="index">
              <router-link v-if="item.path" :to="item.path">{{ item.label }}</router-link>
              <span v-else>{{ item.label }}</span>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="content-scroll">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAdminStore } from "./store";
import { menuGroups } from "./config/menu";

const route = useRoute();
const router = useRouter();
const adminStore = useAdminStore();

function logout() {
  adminStore.logout();
  router.replace("/login");
}

// 计算完整面包屑路径
const breadcrumbItems = computed(() => {
  const items = [{ label: "首页", path: "/" }];
  const currentPath = route.path;

  for (const group of menuGroups) {
    const child = group.children.find((c) => c.path === currentPath);
    if (child) {
      items.push({ label: group.label, path: "" });
      items.push({ label: child.label, path: currentPath });
      break;
    }
  }
  return items;
});
</script>

<style scoped>
.app-shell {
  height: 100vh;
  overflow: hidden;
  padding-top: 60px;
  box-sizing: border-box;
}

.content-wrapper {
  display: flex;
  height: 100%;
  overflow: hidden;
}

.content-wrapper :deep(.el-container) {
  height: 100%;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 16px;
  border-bottom: 1px solid #0f766e22;
  background: linear-gradient(90deg, #ffffff 0%, #f5fbfa 100%);
  box-sizing: border-box;
}

.brand-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  height: 36px;
  width: auto;
}

.brand-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--blocksy-brand-soft), var(--blocksy-brand));
  box-shadow: 0 0 0 5px #0f766e1a;
}

.brand {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-sub {
  color: #718096;
  font-size: 12px;
  margin-top: 2px;
}

.right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-breadcrumb {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 10px 16px;
  background: #f7fafc;
  border-radius: 8px;
  font-size: 13px;
  box-sizing: border-box;
}

.page-breadcrumb :deep(.el-breadcrumb__item) {
  font-size: 13px;
}

.page-breadcrumb :deep(.el-breadcrumb__inner) {
  color: #718096;
  font-weight: 500;
}

.page-breadcrumb :deep(.el-breadcrumb__inner a) {
  color: #718096;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s;
}

.page-breadcrumb :deep(.el-breadcrumb__inner a:hover) {
  color: var(--blocksy-brand);
}

.page-breadcrumb :deep(.el-breadcrumb__separator) {
  color: #cbd5e0;
  margin: 0 6px;
}

.page-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--blocksy-brand);
  font-weight: 600;
}

.home-icon {
  display: inline-block;
  margin-right: 4px;
  font-size: 14px;
  color: var(--blocksy-brand);
}

.home-link {
  display: inline-flex;
  align-items: center;
}

.username {
  color: var(--blocksy-muted);
}

.aside {
  flex-shrink: 0;
  height: 100%;
  border-right: 1px solid #d9e1ef;
  background: linear-gradient(180deg, #fdfefe 0%, #f6faf9 100%);
  overflow: hidden;
}

.sidebar {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
}

.sidebar {
  scrollbar-width: thin;
  scrollbar-color: #d9e1ef transparent;
}

.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-thumb {
  background: #d9e1ef;
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.menu {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-sub-menu__title) {
  font-weight: 700;
}

.menu :deep(.el-menu-item.is-active) {
  background: #d7f3f1;
  color: var(--blocksy-brand);
  border-right: 3px solid var(--blocksy-brand);
}

.main {
  display: flex !important;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
  box-sizing: border-box;
}

.main :deep(.el-main) {
  display: flex !important;
  flex-direction: column;
  overflow: hidden;
}

.breadcrumb-wrapper {
  flex-shrink: 0;
}

.content-scroll {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}
</style>
