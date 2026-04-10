<template>
  <app-section>
    <h3 class="section-title">用户管理</h3>
    <div class="toolbar">
      <el-button type="primary" @click="loadUsers">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="users" border class="page-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="220" />
      <el-table-column prop="nickname" label="昵称" min-width="220" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? "正常" : "封禁" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="封禁信息" min-width="300">
        <template #default="{ row }">
          <div v-if="row.status === 0">
            <div>原因：{{ row.banReason || "-" }}</div>
            <div>封禁时间：{{ formatDateTime(row.bannedAt) }}</div>
            <div>到期时间：{{ row.bannedUntil ? formatDateTime(row.bannedUntil) : "永久" }}</div>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="260">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="danger" @click="openBanDialog(row)">封禁</el-button>
          <el-button v-else size="small" type="success" @click="doUnban(row.id)">解封</el-button>
          <el-button size="small" @click="showLogs(row.id)">日志</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="banDialogVisible" title="封禁用户" width="520px">
      <el-form label-width="92px">
        <el-form-item label="封禁原因">
          <el-input
            v-model="banForm.reason"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入封禁原因（选填）"
          />
        </el-form-item>
        <el-form-item label="封禁时长">
          <el-input-number v-model="banForm.durationHours" :min="1" :max="8760" />
          <span class="duration-tip">小时（留空表示永久封禁）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBan">确认封禁</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logDialogVisible" title="用户处罚日志" width="780px">
      <div class="log-toolbar">
        <el-select v-model="logActionFilter" placeholder="动作筛选" style="width: 180px">
          <el-option label="全部" value="" />
          <el-option label="封禁 BAN" value="BAN" />
          <el-option label="解封 UNBAN" value="UNBAN" />
        </el-select>
      </div>
      <el-table v-loading="logLoading" :data="filteredLogRows" border>
        <el-table-column prop="id" label="日志ID" width="90" />
        <el-table-column prop="operatorUserId" label="操作人" width="100" />
        <el-table-column prop="action" label="动作" width="100" />
        <el-table-column prop="reason" label="原因" min-width="220" />
        <el-table-column prop="durationHours" label="时长(小时)" width="110" />
        <el-table-column label="到期时间" min-width="180">
          <template #default="{ row }">
            <span :class="expiryClass(row.expiresAt)">
              {{ row.expiresAt ? formatDateTime(row.expiresAt) : "-" }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </app-section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { banUser, fetchUserPunishLogs, fetchUsers, unbanUser, UserItem, UserPunishLogItem } from "../../api/user";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const users = ref<UserItem[]>([]);
const banDialogVisible = ref(false);
const banTargetUserId = ref<number | null>(null);
const banForm = ref<{ reason?: string; durationHours?: number }>({});
const logDialogVisible = ref(false);
const logLoading = ref(false);
const logRows = ref<UserPunishLogItem[]>([]);
const logActionFilter = ref<"" | "BAN" | "UNBAN">("");
const filteredLogRows = computed(() => {
  if (!logActionFilter.value) {
    return logRows.value;
  }
  return logRows.value.filter((row) => row.action === logActionFilter.value);
});

async function loadUsers() {
  loading.value = true;
  try {
    users.value = await fetchUsers();
  } catch (error) {
    ElMessage.error((error as Error).message || "加载失败");
  } finally {
    loading.value = false;
  }
}

function openBanDialog(user: UserItem) {
  banTargetUserId.value = user.id;
  banForm.value = {
    reason: "",
    durationHours: undefined
  };
  banDialogVisible.value = true;
}

async function confirmBan() {
  if (!banTargetUserId.value) {
    return;
  }
  try {
    await banUser(banTargetUserId.value, {
      reason: banForm.value.reason?.trim() || undefined,
      durationHours: banForm.value.durationHours
    });
    ElMessage.success("封禁成功");
    banDialogVisible.value = false;
    await loadUsers();
  } catch (error) {
    ElMessage.error((error as Error).message || "封禁失败");
  }
}

async function doUnban(userId: number) {
  try {
    await unbanUser(userId, { reason: "管理员手动解封" });
    ElMessage.success("解封成功");
    await loadUsers();
  } catch (error) {
    ElMessage.error((error as Error).message || "解封失败");
  }
}

async function showLogs(userId: number) {
  logDialogVisible.value = true;
  logLoading.value = true;
  logActionFilter.value = "";
  try {
    logRows.value = await fetchUserPunishLogs(userId);
  } catch (error) {
    ElMessage.error((error as Error).message || "加载日志失败");
  } finally {
    logLoading.value = false;
  }
}

function expiryClass(expiresAt?: string) {
  if (!expiresAt) {
    return "";
  }
  const time = new Date(expiresAt).getTime();
  if (Number.isNaN(time)) {
    return "";
  }
  const now = Date.now();
  if (time <= now) {
    return "is-expired";
  }
  const leftMs = time - now;
  if (leftMs <= 24 * 3600 * 1000) {
    return "is-soon";
  }
  return "";
}

onMounted(() => {
  loadUsers();
});
</script>

<style scoped>
.section-title {
  margin: 0 0 12px;
}

.toolbar {
  margin-bottom: 12px;
}

.page-table {
  width: 100%;
}

.duration-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #64748b;
}

.log-toolbar {
  margin-bottom: 10px;
}

.is-expired {
  color: #dc2626;
  font-weight: 700;
}

.is-soon {
  color: #d97706;
  font-weight: 700;
}
</style>
