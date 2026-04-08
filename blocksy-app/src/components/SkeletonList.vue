<template>
  <view class="skeleton-list">
    <view v-for="idx in count" :key="idx" class="skeleton-card">
      <view class="line line-lg shimmer"></view>
      <view class="line line-md shimmer"></view>
      <view class="line line-sm shimmer"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    count?: number;
  }>(),
  {
    count: 3
  }
);
</script>

<style scoped>
.skeleton-list {
  display: grid;
  gap: 10px;
}

.skeleton-card {
  border: 1px solid #dce7f7;
  border-radius: 18px;
  background: var(--surface);
  backdrop-filter: blur(8px);
  padding: 12px;
}

.line {
  height: 12px;
  border-radius: 999px;
  background: #e9eff8;
  margin-bottom: 10px;
}

.line-lg {
  width: 78%;
}

.line-md {
  width: 92%;
}

.line-sm {
  width: 40%;
  margin-bottom: 0;
}

.shimmer {
  position: relative;
  overflow: hidden;
}

.shimmer::after {
  content: "";
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.75), transparent);
  animation: shimmer 1.2s infinite;
}

@keyframes shimmer {
  100% {
    transform: translateX(100%);
  }
}
</style>
