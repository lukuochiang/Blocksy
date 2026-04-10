<template>
  <app-section>
    <div class="head">
      <h3 class="section-title">留存转化分析（MVP）</h3>
      <el-radio-group v-model="days" size="small" @change="loadRows">
        <el-radio-button :label="7">近7天</el-radio-button>
        <el-radio-button :label="30">近30天</el-radio-button>
      </el-radio-group>
    </div>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="day" label="日期" width="160" />
      <el-table-column prop="value" label="活跃用户数" />
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchRetentionTrend, type TrendPoint } from "../../api/admin-analytics";

const days = ref<7 | 30>(7);
const loading = ref(false);
const rows = ref<TrendPoint[]>([]);

async function loadRows() {
  loading.value = true;
  try {
    rows.value = await fetchRetentionTrend(days.value);
  } catch (error) {
    ElMessage.error((error as Error).message || "加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
