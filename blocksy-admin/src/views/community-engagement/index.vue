<template>
  <app-section>
    <h3 class="section-title">社区成员活跃</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="社区名称/编码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="communityId" label="社区ID" width="100" />
      <el-table-column prop="communityName" label="社区名称" min-width="180" />
      <el-table-column prop="memberCount" label="成员数" width="100" />
      <el-table-column prop="postCount" label="发帖数" width="100" />
      <el-table-column prop="commentCount" label="评论数" width="100" />
      <el-table-column prop="activeScore" label="活跃分" width="100" />
    </el-table>

    <div class="pager-wrap">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :total="total"
        :current-page="query.page"
        :page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        @current-change="onPageChange"
        @size-change="onPageSizeChange"
      />
    </div>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchCommunityEngagement, type CommunityEngagementItem } from "../../api/content-ops";

const loading = ref(false);
const total = ref(0);
const rows = ref<CommunityEngagementItem[]>([]);
const query = reactive({ keyword: "", page: 1, pageSize: 10 });

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchCommunityEngagement({
      keyword: query.keyword.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载社区活跃数据失败");
  } finally {
    loading.value = false;
  }
}

function onSearch() {
  query.page = 1;
  void loadRows();
}
function onPageChange(page: number) {
  query.page = page;
  void loadRows();
}
function onPageSizeChange(size: number) {
  query.pageSize = size;
  query.page = 1;
  void loadRows();
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.section-title { margin: 0 0 12px; }
.toolbar { margin-bottom: 12px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
