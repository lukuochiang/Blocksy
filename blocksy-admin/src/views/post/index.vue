<template>
  <app-section>
    <h3 class="section-title">帖子管理</h3>
    <div class="toolbar">
      <el-button type="primary" @click="loadPosts">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="posts" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="authorId" label="作者ID" width="100" />
      <el-table-column prop="communityId" label="社区ID" width="100" />
      <el-table-column label="内容" min-width="280">
        <template #default="{ row }">
          <div class="content">{{ row.content }}</div>
          <div v-if="row.media?.length" class="media">
            <el-image
              v-for="item in row.media"
              :key="item.objectKey"
              :src="item.url"
              fit="cover"
              class="thumb"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="commentCount" label="评论数" width="90" />
      <el-table-column prop="likeCount" label="点赞数" width="90" />
      <el-table-column label="创建时间" min-width="220">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="mockOffShelf(row.id)">下架</el-button>
          <el-button size="small" type="danger" @click="mockDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </app-section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import AppSection from "../../components/AppSection.vue";
import { fetchPosts, PostItem } from "../../api/post";
import { formatDateTime } from "../../utils/datetime";

const posts = ref<PostItem[]>([]);
const loading = ref(false);

async function loadPosts() {
  loading.value = true;
  try {
    posts.value = await fetchPosts();
  } catch (error) {
    ElMessage.error((error as Error).message || "加载帖子失败");
  } finally {
    loading.value = false;
  }
}

function mockOffShelf(postId: number) {
  ElMessage.info(`预留能力：下架帖子 #${postId}`);
}

function mockDelete(postId: number) {
  ElMessage.info(`预留能力：删除帖子 #${postId}`);
}

onMounted(() => {
  loadPosts();
});
</script>

<style scoped>
.section-title {
  margin: 0 0 12px;
}

.toolbar {
  margin-bottom: 12px;
}

.content {
  margin-bottom: 8px;
  white-space: pre-wrap;
}

.media {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.thumb {
  width: 56px;
  height: 56px;
  border-radius: 4px;
}
</style>
