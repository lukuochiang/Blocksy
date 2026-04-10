<template>
  <view class="page desktop-layout enter">
    <view class="main-col">
      <view v-if="pullStatus !== 'idle'" class="pull-indicator" :style="{ height: `${pullOffset}px` }">
        <view class="pull-text">{{ pullText }}</view>
      </view>
      <view class="hero-card">
        <view class="title">分类信息</view>
        <view class="subtitle">邻里交易、求助与生活服务</view>
        <view class="hero-pills">
          <view class="pill">热门分类 {{ categories.length - 1 }}</view>
          <view class="pill">当前结果 {{ filteredListings.length }}</view>
        </view>
      </view>

      <view class="tab-row">
        <button
          v-for="item in categories"
          :key="item.code"
          class="tab-btn"
          :class="{ active: activeCategory === item.code }"
          @click="activeCategory = item.code"
        >
          {{ item.name }}
        </button>
      </view>
      <view class="filter-row">
        <input v-model="keyword" class="filter-input" placeholder="搜索标题或内容" />
        <input v-model="minPriceInput" class="filter-input mini" type="number" placeholder="最低价" />
        <input v-model="maxPriceInput" class="filter-input mini" type="number" placeholder="最高价" />
        <button class="ghost-btn" @click="reloadListings">筛选</button>
      </view>
      <view class="sort-row">
        <button class="sort-btn" :class="{ active: sortBy === 'CREATED_AT' && sortOrder === 'DESC' }" @click="setSort('CREATED_AT', 'DESC')">
          最新发布
        </button>
        <button class="sort-btn" :class="{ active: sortBy === 'PRICE' && sortOrder === 'ASC' }" @click="setSort('PRICE', 'ASC')">
          价格升序
        </button>
        <button class="sort-btn" :class="{ active: sortBy === 'PRICE' && sortOrder === 'DESC' }" @click="setSort('PRICE', 'DESC')">
          价格降序
        </button>
      </view>

      <skeleton-list v-if="loading" :count="3" />
      <view class="cards">
        <view v-for="item in pagedListings" :key="item.id" class="card">
          <view class="card-head">
            <view class="card-title">{{ item.title }}</view>
            <view class="price">{{ item.priceText }}</view>
          </view>
          <view class="card-content">{{ item.content }}</view>
          <view class="card-meta">社区 #{{ item.communityId }} · {{ item.createdAt ? formatDateTime(item.createdAt) : "-" }}</view>
          <view class="card-actions">
            <button class="ghost-btn">联系TA</button>
            <button class="ghost-btn" @click="goDetail(item.id)">详情</button>
          </view>
        </view>
      </view>
      <empty-state
        v-if="!loading && !filteredListings.length"
        title="暂无分类信息"
        description="先发布一条分类信息，方便邻里看到你的需求。"
        cta-text="去发布"
        cta-variant="primary"
        @cta="goPost"
      />
      <list-pager
        v-if="!loading && filteredListings.length > pageSize"
        :page="page"
        :page-size="pageSize"
        :total="filteredListings.length"
        @prev="prevPage"
        @next="nextPage"
      />
    </view>
    <view class="side-col">
      <view class="side-card">
        <view class="side-title">分类导航</view>
        <view class="side-line">当前分类：{{ categories.find((c) => c.code === activeCategory)?.name }}</view>
        <view class="side-line">信息数量：{{ filteredListings.length }}</view>
      </view>
      <view class="side-card">
        <view class="side-title">发布提示</view>
        <view class="side-line">完善标题和图片，回复率更高。</view>
        <view class="side-line">涉及交易请线下当面核验。</view>
        <button class="side-btn" @click="uni.reLaunch({ url: '/pages/post/index' })">去发布</button>
      </view>
      <view class="side-card">
        <view class="side-title">热门分类</view>
        <button class="mini-btn" @click="activeCategory = 'SECOND_HAND'">二手交易</button>
        <button class="mini-btn" @click="activeCategory = 'HELP_WANTED'">邻里求助</button>
        <button class="mini-btn" @click="activeCategory = 'LOST_FOUND'">失物招领</button>
      </view>
    </view>
  </view>
  
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import ListPager from "../../components/ListPager.vue";
import { useH5PullRefresh } from "../../utils/pull-refresh";
import SkeletonList from "../../components/SkeletonList.vue";
import EmptyState from "../../components/EmptyState.vue";
import { withMinDuration } from "../../utils/async";
import { getListingList, type ListingCategory, type ListingItem } from "../../api/listing";
import { useUserStore } from "../../store/user";
import { formatDateTime } from "../../utils/datetime";

const categories = [
  { code: "ALL", name: "全部" },
  { code: "SECOND_HAND", name: "二手交易" },
  { code: "LOST_FOUND", name: "失物招领" },
  { code: "HELP_WANTED", name: "邻里求助" }
];

interface ListingCard extends ListingItem {
  priceText: string;
}

const userStore = useUserStore();
userStore.hydrate();
const listings = ref<ListingCard[]>([]);
const loading = ref(false);
const activeCategory = ref<"ALL" | ListingCategory>("ALL");
const keyword = ref("");
const minPriceInput = ref("");
const maxPriceInput = ref("");
const sortBy = ref<"CREATED_AT" | "PRICE">("CREATED_AT");
const sortOrder = ref<"ASC" | "DESC">("DESC");
const page = ref(1);
const pageSize = 4;

const filteredListings = computed(() => {
  return listings.value;
});

const pagedListings = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredListings.value.slice(start, start + pageSize);
});

watch(activeCategory, () => {
  void reloadListings();
});

function prevPage() {
  if (page.value <= 1) {
    return;
  }
  page.value -= 1;
}

function nextPage() {
  const totalPages = Math.ceil(filteredListings.value.length / pageSize);
  if (page.value >= totalPages) {
    return;
  }
  page.value += 1;
}

function goPost() {
  uni.reLaunch({ url: "/pages/post/index" });
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/listing-detail/index?id=${id}` });
}

async function reloadListings() {
  loading.value = true;
  try {
    await withMinDuration(async () => {
      const minPrice = minPriceInput.value ? Number(minPriceInput.value) : undefined;
      const maxPrice = maxPriceInput.value ? Number(maxPriceInput.value) : undefined;
      const result = await getListingList({
        communityId: userStore.currentCommunityId || undefined,
        category: activeCategory.value,
        keyword: keyword.value || undefined,
        minPrice: Number.isFinite(minPrice as number) ? minPrice : undefined,
        maxPrice: Number.isFinite(maxPrice as number) ? maxPrice : undefined,
        sortBy: sortBy.value,
        sortOrder: sortOrder.value
      });
      listings.value = result.map((item) => ({
        ...item,
        priceText: item.price == null ? "面议" : `¥${item.price}`
      }));
    });
    page.value = 1;
  } catch (error) {
    uni.showToast({ title: (error as Error).message || "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function setSort(nextSortBy: "CREATED_AT" | "PRICE", nextSortOrder: "ASC" | "DESC") {
  sortBy.value = nextSortBy;
  sortOrder.value = nextSortOrder;
  void reloadListings();
}

const { pullStatus, pullOffset, pullText } = useH5PullRefresh(async () => {
  await reloadListings();
});

reloadListings();
</script>

<style scoped>
.page {
  padding: 16px;
  box-sizing: border-box;
}

.hero-card {
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #0963e6 0%, #18a4ff 58%, #32cab9 100%);
  box-shadow: var(--shadow);
}

.title {
  font-size: 22px;
  font-weight: 800;
}

.subtitle {
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.92);
  font-size: 13px;
}

.hero-pills {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pill {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.tab-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.filter-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.filter-input {
  flex: 1;
  min-width: 160px;
  background: #fff;
  border: 1px solid #d5e2f5;
  border-radius: 12px;
  padding: 8px 10px;
  font-size: 13px;
}

.filter-input.mini {
  flex: 0 0 100px;
  min-width: 100px;
}

.sort-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.sort-btn {
  border: 1px solid #d5e2f5;
  border-radius: 999px;
  background: #fff;
  color: #5c6986;
  padding: 6px 12px;
  font-size: 12px;
}

.sort-btn.active {
  background: linear-gradient(135deg, #e8f1ff 0%, #ebfbf7 100%);
  color: var(--primary-dark);
  border-color: #bed3f6;
  font-weight: 700;
}

.tab-btn {
  border: 1px solid #d5e2f5;
  border-radius: 999px;
  background: var(--surface);
  backdrop-filter: blur(8px);
  color: var(--text-soft);
  padding: 7px 12px;
  font-size: 12px;
}

.tab-btn.active {
  background: linear-gradient(135deg, #e8f1ff 0%, #ebfbf7 100%);
  color: var(--primary-dark);
  border-color: #bed3f6;
  font-weight: 700;
}

.cards {
  display: grid;
  gap: 10px;
}

.card {
  background: var(--surface);
  backdrop-filter: blur(8px);
  border: 1px solid #dce7f7;
  border-radius: 18px;
  padding: 12px;
  box-shadow: var(--shadow-soft);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
}

.price {
  color: var(--primary-dark);
  font-weight: 700;
}

.card-content {
  margin-top: 7px;
  color: #334155;
  font-size: 14px;
}

.card-meta {
  margin-top: 8px;
  color: var(--text-soft);
  font-size: 12px;
}

.card-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.ghost-btn {
  border: 1px solid #d5e2f5;
  background: #fff;
  border-radius: 12px;
  padding: 7px 12px;
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

.side-col {
  display: none;
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

  .side-btn,
  .mini-btn {
    width: 100%;
    border: 1px solid var(--line);
    background: #fff;
    margin-top: 8px;
  }
}
</style>
