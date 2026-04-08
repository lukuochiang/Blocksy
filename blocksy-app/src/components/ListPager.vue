<template>
  <view v-if="total > 0" class="pager">
    <button class="pager-btn" :disabled="page <= 1" @click="$emit('prev')">上一页</button>
    <view class="pager-info">{{ page }} / {{ totalPages }} 页 · 共 {{ total }} 条</view>
    <button class="pager-btn" :disabled="page >= totalPages" @click="$emit('next')">下一页</button>
  </view>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  page: number;
  pageSize: number;
  total: number;
}>();

defineEmits<{
  (e: "prev"): void;
  (e: "next"): void;
}>();

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)));
</script>

<style scoped>
.pager {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.pager-btn {
  border: 1px solid #cfe0f8;
  background: #fff;
  border-radius: 12px;
}

.pager-info {
  font-size: 12px;
  color: var(--text-soft);
}
</style>
