<template>
  <app-section>
    <h3 class="section-title">审核与举报分析</h3>
    <div class="stats">
      <el-card shadow="never">
        <div class="label">举报总量</div>
        <div class="value">{{ data.totalReports }}</div>
      </el-card>
      <el-card shadow="never">
        <div class="label">待处理</div>
        <div class="value">{{ data.pendingReports }}</div>
      </el-card>
      <el-card shadow="never">
        <div class="label">已处理</div>
        <div class="value">{{ data.handledReports }}</div>
      </el-card>
      <el-card shadow="never">
        <div class="label">处理率</div>
        <div class="value">{{ (data.handledRate * 100).toFixed(1) }}%</div>
      </el-card>
    </div>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchModerationOverview } from "../../api/admin-analytics";

const data = reactive({
  totalReports: 0,
  pendingReports: 0,
  handledReports: 0,
  pendingRate: 0,
  handledRate: 0
});

async function loadData() {
  try {
    Object.assign(data, await fetchModerationOverview());
  } catch (error) {
    ElMessage.error((error as Error).message || "加载失败");
  }
}

onMounted(() => {
  void loadData();
});
</script>

<style scoped>
.stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: 12px;
}
.label {
  font-size: 13px;
  color: #64748b;
}
.value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
}
@media (max-width: 1200px) {
  .stats {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
}
</style>
