<template>
  <view class="page enter">
    <view class="hero">
      <view class="brand-row">
        <img class="logo" src="/images/logo.png" mode="aspectFit" />
        <view class="brand-text">
          <view class="hero-title">Norvo</view>
          <view class="hero-subtitle">邻里社区 · 本地生活</view>
        </view>
      </view>
      <view class="tag-row">
        <view class="tag">社区认证</view>
        <view class="tag">本地互助</view>
        <view class="tag">活动公告</view>
      </view>
    </view>
    <view class="card enter enter-delay-1">
      <view class="card-title">登录账号</view>
      <input v-model="username" class="input" placeholder="用户名（默认 demo）" />
      <input v-model="password" class="input" password placeholder="密码（默认 blocksy123）" />
      <button class="submit-btn" type="primary" :loading="loading" @click="onLogin">进入社区</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { login } from "../../api/auth";
import { useUserStore } from "../../store/user";

const userStore = useUserStore();
userStore.hydrate();

const username = ref("demo");
const password = ref("blocksy123");
const loading = ref(false);

async function onLogin() {
  if (!username.value || !password.value) {
    uni.showToast({ title: "请输入用户名和密码", icon: "none" });
    return;
  }
  loading.value = true;
  try {
    const result = await login({
      username: username.value.trim(),
      password: password.value
    });
    userStore.setAuth(result.token);
    await userStore.fetchMe();
    await userStore.fetchCommunities();
    uni.showToast({ title: "登录成功", icon: "success" });
    uni.reLaunch({ url: "/pages/home/index" });
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "登录失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 44px 18px 28px;
}

.hero {
  margin-bottom: 16px;
}

.logo {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  border: 1px solid #dce7f8;
  background: #fff;
  box-shadow: var(--shadow);
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-text {
  min-width: 0;
}

.tag-row {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  font-size: 12px;
  color: var(--primary-dark);
  border-radius: 999px;
  padding: 5px 10px;
  background: linear-gradient(135deg, #e4efff 0%, #ebfbf7 100%);
  border: 1px solid #cfe0f8;
}

.hero-title {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 0.4px;
}

.hero-subtitle {
  margin-top: 6px;
  color: var(--text-soft);
  font-size: 14px;
}

.card {
  background: var(--surface);
  border: 1px solid #dce7f8;
  border-radius: 20px;
  padding: 20px 16px 16px;
  backdrop-filter: blur(8px);
  box-shadow: var(--shadow);
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
}

.input {
  box-sizing: border-box;
  width: 100%;
  height: 44px;
  border: 1px solid #d5e2f5;
  border-radius: 12px;
  padding: 0 12px;
  margin-bottom: 12px;
  background: #fff;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 2px;
  border-radius: 10px;
  font-weight: 600;
}

@media (min-width: 960px) {
  .page {
    max-width: 480px;
    margin: 0 auto;
    padding-top: 80px;
  }
}
</style>
