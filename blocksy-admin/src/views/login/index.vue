<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="title">Norvo Admin 登录</div>
      </template>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="demo" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="blocksy123" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="onLogin">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { login } from "../../api/auth";
import { useAdminStore } from "../../store";

const router = useRouter();
const adminStore = useAdminStore();

const loading = ref(false);
const form = reactive({
  username: "demo",
  password: "blocksy123"
});

async function onLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning("请输入用户名和密码");
    return;
  }
  loading.value = true;
  try {
    const result = await login({
      username: form.username.trim(),
      password: form.password
    });
    adminStore.setAuth(result.token, result.username);
    ElMessage.success("登录成功");
    await router.replace("/dashboard");
  } catch (error) {
    ElMessage.error((error as Error).message || "登录失败");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 15% 20%, #d7f3f1 0%, transparent 28%),
    radial-gradient(circle at 80% 10%, #fde7bf 0%, transparent 26%),
    linear-gradient(180deg, #eef2f7 0%, #f7fafc 100%);
}

.login-card {
  width: 390px;
  border-radius: 16px;
  border: 1px solid #d9e1ef;
  box-shadow: 0 14px 34px #0f172a14;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}
</style>
