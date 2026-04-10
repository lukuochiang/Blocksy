<template>
  <app-section>
    <h3 class="section-title">认证审核</h3>
    <el-form inline :model="query" class="toolbar">
      <el-form-item label="状态">
        <el-select v-model="query.processStatus" clearable placeholder="全部" style="width: 150px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="通过" value="APPROVED" />
          <el-option label="驳回" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户ID">
        <el-input v-model.number="query.userId" clearable placeholder="例如 1" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="verifyType" label="认证类型" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="idCardMask" label="证件号" width="180" />
      <el-table-column prop="processStatus" label="状态" width="120" />
      <el-table-column label="申请时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handle(row.id, 'APPROVED')">通过</el-button>
          <el-button size="small" type="danger" @click="handle(row.id, 'REJECTED')">驳回</el-button>
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
import { fetchVerifications, handleVerification, type VerificationItem } from "../../api/verification";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const rows = ref<VerificationItem[]>([]);
const total = ref(0);
const query = reactive({
  processStatus: undefined as string | undefined,
  userId: undefined as number | undefined,
  page: 1,
  pageSize: 10
});

async function loadRows() {
  loading.value = true;
  try {
    const data = await fetchVerifications({
      processStatus: query.processStatus,
      userId: query.userId,
      page: query.page,
      pageSize: query.pageSize
    });
    rows.value = data.items;
    total.value = data.total;
  } catch (error) {
    ElMessage.error((error as Error).message || "加载认证申请失败");
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

async function handle(id: number, processStatus: "APPROVED" | "REJECTED") {
  try {
    await handleVerification(id, {
      processStatus,
      reviewNote: processStatus === "APPROVED" ? "资料有效，审核通过" : "资料不完整，请补充后重试"
    });
    ElMessage.success("处理成功");
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "处理失败");
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
