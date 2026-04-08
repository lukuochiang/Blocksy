<template>
  <view class="tabbar">
    <button
      v-for="item in tabs"
      :key="item.path"
      class="tab-item"
      :class="{ active: currentPath === item.path }"
      @click="switchTab(item.path)"
    >
      <view class="icon">{{ item.icon }}</view>
      <view class="label">{{ item.label }}</view>
    </button>
  </view>
</template>

<script setup lang="ts">
const props = defineProps<{
  currentPath: string;
}>();

const tabs = [
  { label: "首页", path: "/pages/home/index", icon: "N" },
  { label: "分类", path: "/pages/listing/index", icon: "F" },
  { label: "发布", path: "/pages/post/index", icon: "+" },
  { label: "消息", path: "/pages/message/index", icon: "M" },
  { label: "我的", path: "/pages/mine/index", icon: "U" }
];

function switchTab(path: string) {
  if (props.currentPath === path) {
    return;
  }
  uni.reLaunch({ url: path });
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  z-index: 20;
  left: 50%;
  transform: translateX(-50%);
  width: min(860px, calc(100vw - 20px));
  bottom: 10px;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(10px);
  border: 1px solid #d3e1f4;
  box-shadow: 0 12px 36px rgba(25, 44, 84, 0.16);
  border-radius: 20px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 4px;
  padding: 6px;
}

.tab-item {
  border: 0;
  background: transparent;
  border-radius: 10px;
  min-height: 44px;
  padding: 6px 4px;
  color: var(--text-soft);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.icon {
  width: 18px;
  height: 18px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  font-size: 11px;
  font-weight: 700;
  background: #eef4ff;
  color: #5c7aa7;
}

.tab-item.active {
  background: linear-gradient(135deg, #dfeeff 0%, #e4fbf3 100%);
  color: #0a4eb6;
  font-weight: 700;
}

.tab-item.active .icon {
  background: linear-gradient(135deg, #1a79ff 0%, #34d2bb 100%);
  color: #fff;
}

.label {
  font-size: 12px;
}

@media (min-width: 960px) {
  .tabbar {
    display: none;
  }
}
</style>
