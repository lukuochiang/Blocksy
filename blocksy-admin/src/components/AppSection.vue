<template>
  <el-card class="section-card" shadow="never">
    <slot />
  </el-card>
</template>

<script setup lang="ts"></script>

<style scoped>
.section-card {
  width: 100%;
  border: 1px solid var(--blocksy-border);
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
}

.section-card :deep(.el-card__body) {
  padding: 20px;
}

.section-card :deep(.el-table) {
  width: 100% !important;
}

.section-card :deep(.el-table th.el-table__cell) {
  background: #f8fafc;
}

.section-card :deep(.el-table td.el-table__cell),
.section-card :deep(.el-table th.el-table__cell) {
  border-color: #e2e8f0;
}

.section-card :deep(.section-title) {
  margin: 0 0 14px;
  font-size: 22px;
  color: var(--blocksy-text);
}
</style>
