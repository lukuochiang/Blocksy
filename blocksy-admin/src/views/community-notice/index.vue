<template>
  <app-section>
    <h3 class="section-title">社区公告</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="社区ID">
        <el-input v-model.number="query.communityId" clearable placeholder="例如 1" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 150px">
          <el-option label="有效" :value="1" />
          <el-option label="已撤回" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="标题/内容" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="openCreate">发布公告</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="communityId" label="社区ID" width="100" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "有效" : "已撤回" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="warning" :disabled="row.status === 0" @click="revoke(row.id)">撤回</el-button>
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

    <el-dialog v-model="createVisible" title="发布社区公告" width="520px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="社区ID">
          <el-input v-model.number="createForm.communityId" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="createForm.title" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="createForm.content" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">发布</el-button>
      </template>
    </el-dialog>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import {
  createCommunityNotice,
  fetchCommunityNotices,
  revokeCommunityNotice,
  type CommunityNoticeItem
} from "../../api/community-notice";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const creating = ref(false);
const createVisible = ref(false);
const rows = ref<CommunityNoticeItem[]>([]);
const total = ref(0);
const query = reactive({
  communityId: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: "",
  page: 1,
  pageSize: 10
});
const createForm = reactive({
  communityId: 1,
  title: "",
  content: ""
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchCommunityNotices({
      communityId: query.communityId,
      status: query.status,
      keyword: query.keyword.trim() || undefined,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载社区公告失败");
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

function openCreate() {
  createVisible.value = true;
}

async function submitCreate() {
  if (!createForm.communityId || !createForm.title.trim() || !createForm.content.trim()) {
    ElMessage.warning("社区ID、标题、内容不能为空");
    return;
  }
  creating.value = true;
  try {
    await createCommunityNotice({
      communityId: createForm.communityId,
      title: createForm.title.trim(),
      content: createForm.content.trim()
    });
    ElMessage.success("发布成功");
    createVisible.value = false;
    createForm.title = "";
    createForm.content = "";
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "发布失败");
  } finally {
    creating.value = false;
  }
}

async function revoke(id: number) {
  try {
    await revokeCommunityNotice(id);
    ElMessage.success("已撤回");
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "撤回失败");
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
