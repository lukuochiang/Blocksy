<template>
  <view class="empty-wrap">
    <view class="illustration">
      <view class="blob blob-a"></view>
      <view class="blob blob-b"></view>
      <view class="dot"></view>
    </view>
    <view class="title">{{ title }}</view>
    <view class="desc">{{ description }}</view>
    <button
      v-if="ctaText"
      class="cta-btn"
      :class="{
        'cta-primary': ctaVariant === 'primary',
        'cta-secondary': ctaVariant === 'secondary',
        'cta-weak': ctaVariant === 'weak'
      }"
      @click="$emit('cta')"
    >
      {{ ctaText }}
    </button>
  </view>
</template>

<script setup lang="ts">
defineEmits<{
  (e: "cta"): void;
}>();

withDefaults(
  defineProps<{
    title?: string;
    description?: string;
    ctaText?: string;
    ctaVariant?: "primary" | "secondary" | "weak";
  }>(),
  {
    title: "暂无数据",
    description: "稍后再来看看，或者先发布一条内容。",
    ctaText: "",
    ctaVariant: "primary"
  }
);
</script>

<style scoped>
.empty-wrap {
  margin-top: 8px;
  padding: 24px 14px;
  border-radius: 16px;
  border: 1px dashed var(--line);
  background: rgba(255, 255, 255, 0.72);
  text-align: center;
}

.illustration {
  margin: 0 auto 10px;
  width: 70px;
  height: 44px;
  position: relative;
}

.blob {
  position: absolute;
  border-radius: 999px;
}

.blob-a {
  width: 44px;
  height: 26px;
  left: 6px;
  top: 10px;
  background: linear-gradient(135deg, #d8e8ff 0%, #d3f6ee 100%);
}

.blob-b {
  width: 36px;
  height: 20px;
  right: 6px;
  top: 14px;
  background: linear-gradient(135deg, #e6efff 0%, #e9fbf7 100%);
}

.dot {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  left: 29px;
  top: 4px;
  background: #4e8dff;
}

.title {
  font-size: 14px;
  font-weight: 700;
}

.desc {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-soft);
}

.cta-btn {
  margin-top: 12px;
  font-weight: 700;
}

.cta-primary {
  border: 0;
  background: linear-gradient(135deg, var(--primary) 0%, #18a4ff 100%);
  color: #fff;
}

.cta-secondary {
  border: 1px solid #cddcf5;
  background: linear-gradient(135deg, #e8f1ff 0%, #eafaf7 100%);
  color: var(--primary-dark);
}

.cta-weak {
  border: 1px solid #d8e4f6;
  background: #fff;
  color: var(--text-soft);
}
</style>
