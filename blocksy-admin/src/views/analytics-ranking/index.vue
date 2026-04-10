<template>
  <app-section>
    <h3 class="section-title">热门社区排行</h3>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="communityName" label="社区名称" min-width="180" />
      <el-table-column prop="postCount" label="帖子数" width="120" />
      <el-table-column prop="commentCount" label="评论数" width="120" />
      <el-table-column prop="reportCount" label="举报数" width="120" />
      <el-table-column prop="score" label="综合分" width="120" />
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchCommunityRanking, type CommunityRankingItem } from "../../api/admin-analytics";

const loading = ref(false);
const rows = ref<CommunityRankingItem[]>([]);

async function loadRows() {
  loading.value = true;
  try {
    rows.value = await fetchCommunityRanking();
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
