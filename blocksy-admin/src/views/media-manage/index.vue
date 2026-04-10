<template>
  <app-section>
    <h3 class="section-title">媒体管理</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="社区ID">
        <el-input v-model.number="query.communityId" clearable placeholder="例如 1" />
      </el-form-item>
      <el-form-item label="帖子ID">
        <el-input v-model.number="query.postId" clearable placeholder="例如 1001" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
          <el-option label="正常" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="postId" label="帖子ID" width="90" />
      <el-table-column prop="communityId" label="社区ID" width="90" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column label="预览" width="120">
        <template #default="{ row }">
          <el-image :src="row.url" fit="cover" style="width:56px;height:56px;border-radius:6px" />
        </template>
      </el-table-column>
      <el-table-column prop="objectKey" label="ObjectKey" min-width="220" show-overflow-tooltip />
      <el-table-column prop="size" label="大小" width="100" />
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" :disabled="row.status === 0" @click="offline(row.id)">下架</el-button>
        </template>
      </el-table-column>
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
import { fetchPostMediaAssets, offlinePostMediaAsset, type MediaAssetItem } from "../../api/content-ops";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const total = ref(0);
const rows = ref<MediaAssetItem[]>([]);
const query = reactive({
  communityId: undefined as number | undefined,
  postId: undefined as number | undefined,
  status: undefined as number | undefined,
  page: 1,
  pageSize: 20
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchPostMediaAssets({
      communityId: query.communityId,
      postId: query.postId,
      status: query.status,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载媒体失败");
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
async function offline(id: number) {
  try {
    await offlinePostMediaAsset(id);
    ElMessage.success("已下架");
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "下架失败");
  }
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
