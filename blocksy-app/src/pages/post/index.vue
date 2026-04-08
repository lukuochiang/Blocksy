<template>
  <view class="page desktop-layout enter">
    <view class="main-col">
      <view v-if="pullStatus !== 'idle'" class="pull-indicator" :style="{ height: `${pullOffset}px` }">
        <view class="pull-text">{{ pullText }}</view>
      </view>
      <view class="hero-card">
        <view class="hero-title">发布中心</view>
        <view class="hero-subtitle">用真实、有温度的内容连接邻里</view>
        <view class="hero-badges">
          <view class="badge">社区可见</view>
          <view class="badge">图文发布</view>
          <view class="badge">即时互动</view>
        </view>
      </view>

      <view class="section composer">
        <view class="title">发布帖子</view>
        <textarea v-model="content" class="textarea" placeholder="分享一下社区新鲜事..." />
        <view class="actions">
          <button class="action-btn" @click="chooseImages">选择图片</button>
          <button class="action-btn" :class="{ 'is-loading': uploading }" :disabled="uploading" @click="uploadImages">
            {{ uploading ? "上传中..." : "上传图片" }}
          </button>
          <button
            class="action-btn"
            type="primary"
            :class="{ 'is-loading': publishing }"
            :disabled="publishing"
            @click="publishPost"
          >
            {{ publishing ? "发布中..." : "发布" }}
          </button>
        </view>
        <view class="image-list">
          <view v-for="item in selectedFiles" :key="item.localPath" class="image-item">
            <image :src="item.localPath" mode="aspectFill" class="image" />
            <view class="status">{{ item.uploaded ? "已上传" : "待上传" }}</view>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="title">帖子列表</view>
        <skeleton-list v-if="loadingPosts" :count="2" />
        <view v-for="item in pagedPosts" :key="item.id" class="card">
          <view class="content">{{ item.content }}</view>
          <scroll-view v-if="item.media?.length" class="media-scroll" scroll-x>
            <image v-for="media in item.media" :key="media.objectKey" :src="media.url" mode="aspectFill" class="media" />
          </scroll-view>
          <view class="meta">#{{ item.id }} · 评论 {{ item.commentCount }} · {{ formatDateTime(item.createdAt) }}</view>
          <view class="comment-box">
            <input
              v-model="commentDraft[item.id]"
              class="comment-input"
              placeholder="写评论..."
            />
            <button class="comment-btn" @click="submitComment(item.id)">评论</button>
          </view>
        </view>
        <empty-state
          v-if="!loadingPosts && !posts.length"
          title="暂无帖子"
          description="发布第一条帖子，开始和邻里互动。"
          cta-text="刷新列表"
          cta-variant="secondary"
          @cta="reloadPosts"
        />
        <list-pager
          v-if="!loadingPosts && posts.length > pageSize"
          :page="page"
          :page-size="pageSize"
          :total="posts.length"
          @prev="prevPage"
          @next="nextPage"
        />
      </view>
    </view>
    <view class="side-col">
      <view class="side-card">
        <view class="side-title">发布建议</view>
        <view class="side-line">1. 优先发布社区真实信息</view>
        <view class="side-line">2. 图片先上传后发布更稳定</view>
        <view class="side-line">3. 避免包含联系方式导流</view>
      </view>
      <view class="side-card">
        <view class="side-title">当前状态</view>
        <view class="side-line">待上传图片：{{ selectedFiles.filter((i) => !i.uploaded).length }}</view>
        <view class="side-line">已加载帖子：{{ posts.length }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { createComment, createPost, getPostList, PostItem, PostMedia } from "../../api/post";
import { uploadFile } from "../../api/file";
import { useUserStore } from "../../store/user";
import { formatDateTime } from "../../utils/datetime";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import ListPager from "../../components/ListPager.vue";
import { useH5PullRefresh } from "../../utils/pull-refresh";

interface SelectedFile {
  localPath: string;
  uploaded: boolean;
  media?: PostMedia;
}

const userStore = useUserStore();
userStore.hydrate();

const content = ref("");
const publishing = ref(false);
const uploading = ref(false);
const selectedFiles = ref<SelectedFile[]>([]);
const posts = ref<PostItem[]>([]);
const loadingPosts = ref(false);
const commentDraft = reactive<Record<number, string>>({});
const page = ref(1);
const pageSize = 4;
const pagedPosts = computed(() => {
  const start = (page.value - 1) * pageSize;
  return posts.value.slice(start, start + pageSize);
});

function ensureLogin(): boolean {
  if (!userStore.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    uni.navigateTo({ url: "/pages/login/index" });
    return false;
  }
  return true;
}

function chooseImages() {
  if (!ensureLogin()) {
    return;
  }
  uni.chooseImage({
    count: 9,
    success: (res) => {
      const added = res.tempFilePaths.map((path) => ({
        localPath: path,
        uploaded: false
      }));
      selectedFiles.value = [...selectedFiles.value, ...added];
    }
  });
}

async function uploadImages() {
  if (!ensureLogin()) {
    return;
  }
  const targets = selectedFiles.value.filter((item) => !item.uploaded);
  if (!targets.length) {
    uni.showToast({ title: "没有待上传图片", icon: "none" });
    return;
  }
  uploading.value = true;
  try {
    for (const item of targets) {
      const uploaded = await uploadFile(item.localPath, "post");
      item.uploaded = true;
      item.media = {
        objectKey: uploaded.objectKey,
        url: uploaded.url,
        size: uploaded.size
      };
    }
    uni.showToast({ title: "上传完成", icon: "success" });
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "上传失败", icon: "none" });
  } finally {
    uploading.value = false;
  }
}

async function publishPost() {
  if (!ensureLogin()) {
    return;
  }
  if (!content.value.trim()) {
    uni.showToast({ title: "请输入帖子内容", icon: "none" });
    return;
  }
  if (selectedFiles.value.some((item) => !item.uploaded)) {
    await uploadImages();
    if (selectedFiles.value.some((item) => !item.uploaded)) {
      return;
    }
  }
  publishing.value = true;
  try {
    await createPost({
      communityId: userStore.currentCommunityId || 1,
      content: content.value.trim(),
      media: selectedFiles.value
        .map((item) => item.media)
        .filter((item): item is PostMedia => Boolean(item))
    });
    uni.showToast({ title: "发布成功", icon: "success" });
    content.value = "";
    selectedFiles.value = [];
    await reloadPosts();
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "发布失败", icon: "none" });
  } finally {
    publishing.value = false;
  }
}

async function submitComment(postId: number) {
  if (!ensureLogin()) {
    return;
  }
  const text = commentDraft[postId]?.trim();
  if (!text) {
    uni.showToast({ title: "请输入评论内容", icon: "none" });
    return;
  }
  try {
    await createComment({ postId, content: text });
    commentDraft[postId] = "";
    uni.showToast({ title: "评论成功", icon: "success" });
    await reloadPosts();
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "评论失败", icon: "none" });
  }
}

async function reloadPosts() {
  loadingPosts.value = true;
  try {
    await withMinDuration(async () => {
      posts.value = await getPostList(userStore.currentCommunityId || undefined);
    });
    page.value = 1;
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loadingPosts.value = false;
  }
}

function prevPage() {
  if (page.value <= 1) {
    return;
  }
  page.value -= 1;
}

function nextPage() {
  const totalPages = Math.ceil(posts.value.length / pageSize);
  if (page.value >= totalPages) {
    return;
  }
  page.value += 1;
}

onMounted(() => {
  reloadPosts();
});

const { pullStatus, pullOffset, pullText } = useH5PullRefresh(async () => {
  await reloadPosts();
});
</script>

<style scoped>
.page {
  padding: 16px;
  box-sizing: border-box;
}

.section {
  margin-bottom: 14px;
}

.hero-card {
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #0a68ea 0%, #1f9fff 55%, #3ec2da 100%);
  box-shadow: var(--shadow);
}

.hero-title {
  font-size: 22px;
  font-weight: 800;
}

.hero-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.92);
}

.hero-badges {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.badge {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.34);
}

.composer {
  background: var(--surface);
  backdrop-filter: blur(8px);
  border: 1px solid #dce7f7;
  border-radius: 20px;
  box-shadow: var(--shadow);
  padding: 14px;
}

.title {
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 10px;
}

.textarea {
  width: 100%;
  min-height: 100px;
  border: 1px solid #d6e3f6;
  border-radius: 14px;
  padding: 10px;
  box-sizing: border-box;
  background: #fbfdff;
}

.actions {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
  margin: 10px 0;
}

.action-btn {
  position: relative;
  border: 1px solid #d5e2f5;
  padding: 9px 10px;
  border-radius: 12px;
  //background: var(--surface-solid);
}

.action-btn.is-loading {
  padding-right: 30px;
  opacity: 0.92;
}

.action-btn.is-loading::after {
  content: "";
  position: absolute;
  right: 10px;
  top: 50%;
  width: 12px;
  height: 12px;
  margin-top: -6px;
  border-radius: 999px;
  border: 2px solid currentColor;
  border-right-color: transparent;
  animation: spin 0.7s linear infinite;
}

.image-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.image-item {
  width: 88px;
}

.image {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  border: 1px solid var(--line);
}

.status {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-soft);
}

.card {
  border: 1px solid #dce7f7;
  border-radius: 18px;
  padding: 12px;
  margin-bottom: 10px;
  background: var(--surface);
  backdrop-filter: blur(8px);
  box-shadow: var(--shadow-soft);
}

.content {
  margin-bottom: 8px;
  color: #111827;
}

.meta {
  color: var(--text-soft);
  font-size: 12px;
  margin-bottom: 8px;
}

.media-scroll {
  white-space: nowrap;
  width: 100%;
  margin-bottom: 8px;
}

.media {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  border: 1px solid var(--line);
  margin-right: 8px;
}

.comment-box {
  display: flex;
  gap: 6px;
}

.comment-input {
  flex: 1;
  height: 34px;
  border: 1px solid #d6e3f6;
  border-radius: 12px;
  padding: 0 8px;
  background: #fbfdff;
}

.comment-btn {
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 6px 10px;
  background: #fff;
}

.side-col {
  display: none;
}

.pull-indicator {
  overflow: hidden;
  transition: height 0.18s ease;
}

.pull-text {
  height: 32px;
  display: grid;
  place-items: center;
  font-size: 12px;
  color: var(--primary-dark);
}

@media (min-width: 960px) {
  .desktop-layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 260px;
    gap: 14px;
  }

  .side-col {
    display: grid;
    align-content: start;
    gap: 10px;
  }

  .side-card {
    border: 1px solid #dce7f7;
    border-radius: 18px;
    background: var(--surface);
    backdrop-filter: blur(8px);
    padding: 12px;
    box-shadow: var(--shadow-soft);
  }

  .side-title {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 8px;
  }

  .side-line {
    color: var(--text-soft);
    font-size: 13px;
    margin-bottom: 6px;
  }
}
</style>
