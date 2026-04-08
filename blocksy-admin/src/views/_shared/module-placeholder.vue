<template>
  <app-section>
    <div class="head">
      <h3 class="title">{{ title }}</h3>
      <p class="desc">{{ description }}</p>
    </div>

    <el-alert
      type="info"
      show-icon
      :closable="false"
      class="alert"
      title="该页面已纳入管理后台信息架构，当前为第一版可用骨架。"
    />

    <div class="cards">
      <el-card v-for="item in features" :key="item.name" shadow="hover" class="feature-card">
        <h4>{{ item.name }}</h4>
        <p>{{ item.usage }}</p>
        <p class="ops"><strong>典型操作：</strong>{{ item.action }}</p>
        <p class="role"><strong>涉及角色：</strong>{{ item.role }}</p>
      </el-card>
    </div>
  </app-section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import AppSection from "../../components/AppSection.vue";

interface FeatureItem {
  name: string;
  usage: string;
  action: string;
  role: string;
}

const route = useRoute();

const title = computed(() => String(route.meta.title || "模块页面"));
const description = computed(() => String(route.meta.description || ""));
const features = computed<FeatureItem[]>(() => (route.meta.features as FeatureItem[]) || []);
</script>

<style scoped>
.head {
  margin-bottom: 14px;
}

.title {
  margin: 0;
  font-size: 22px;
}

.desc {
  margin: 8px 0 0;
  color: #64748b;
}

.alert {
  margin-bottom: 14px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}

.feature-card h4 {
  margin: 0 0 8px;
  font-size: 16px;
}

.feature-card p {
  margin: 0 0 8px;
  color: #475569;
  line-height: 1.6;
}

.ops,
.role {
  font-size: 13px;
}
</style>
