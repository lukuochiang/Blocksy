<template>
  <app-section>
    <h3 class="section-title">品牌信息配置</h3>
    <el-form inline :model="form" class="toolbar">
      <el-form-item label="配置键">
        <el-input v-model="form.settingKey" placeholder="如 logo_url" />
      </el-form-item>
      <el-form-item label="配置值">
        <el-input v-model="form.settingValue" placeholder="URL / 文案" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="settingKey" label="配置键" width="220" />
      <el-table-column prop="settingValue" label="配置值" min-width="280" />
      <el-table-column label="更新时间" min-width="170">
        <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
      </el-table-column>
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchPlatformSettings, savePlatformSetting, type PlatformSettingItem } from "../../api/admin-settings";
import { formatDateTime } from "../../utils/datetime";

const loading = ref(false);
const saving = ref(false);
const rows = ref<PlatformSettingItem[]>([]);
const form = reactive({ settingKey: "", settingValue: "" });

async function loadRows() {
  loading.value = true;
  try {
    rows.value = await fetchPlatformSettings("BRAND");
  } catch (error) {
    ElMessage.error((error as Error).message || "加载失败");
  } finally {
    loading.value = false;
  }
}

async function save() {
  if (!form.settingKey.trim()) {
    ElMessage.warning("配置键不能为空");
    return;
  }
  saving.value = true;
  try {
    await savePlatformSetting({
      settingGroup: "BRAND",
      settingKey: form.settingKey.trim(),
      settingValue: form.settingValue,
      description: "品牌配置"
    });
    ElMessage.success("保存成功");
    form.settingKey = "";
    form.settingValue = "";
    await loadRows();
  } catch (error) {
    ElMessage.error((error as Error).message || "保存失败");
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadRows();
});
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
