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

      <view class="cards">
        <view v-for="item in pagedListings" :key="item.id" class="card">
          <view class="card-head">
            <view class="card-title">{{ item.title }}</view>
            <view class="price">{{ item.priceText }}</view>
          </view>
          <view class="card-content">{{ item.content }}</view>
          <view class="card-meta">{{ item.community }} · {{ item.updatedAt }}</view>
          <view class="card-actions">
            <button class="ghost-btn">联系TA</button>
            <button class="ghost-btn">收藏</button>
          </view>
        </view>
      </view>
      <list-pager
        v-if="filteredListings.length > pageSize"
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
        <button class="mini-btn" @click="activeCategory = 'second_hand'">二手交易</button>
        <button class="mini-btn" @click="activeCategory = 'help'">邻里求助</button>
        <button class="mini-btn" @click="activeCategory = 'services'">本地服务</button>
      </view>
    </view>
  </view>
  
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import ListPager from "../../components/ListPager.vue";
import { useH5PullRefresh } from "../../utils/pull-refresh";

const categories = [
  { code: "all", name: "全部" },
  { code: "second_hand", name: "二手交易" },
  { code: "lost_found", name: "失物招领" },
  { code: "pet_missing", name: "宠物走失" },
  { code: "help", name: "邻里求助" },
  { code: "ride_share", name: "拼车搭子" },
  { code: "housing", name: "房屋租售" },
  { code: "services", name: "本地服务" }
];

const listings = [
  { id: 1, category: "second_hand", title: "九成新儿童书桌", content: "可升降，带收纳，周末自提。", community: "滨江花园", priceText: "¥280", updatedAt: "今天 18:20" },
  { id: 2, category: "lost_found", title: "招领：门禁卡一张", content: "小区南门附近拾到，描述核对后归还。", community: "Norvo 默认社区", priceText: "免费", updatedAt: "今天 16:05" },
  { id: 3, category: "help", title: "求推荐靠谱搬家师傅", content: "本周六上午搬家，距离约 4km。", community: "锦绣家园", priceText: "求推荐", updatedAt: "今天 14:40" },
  { id: 4, category: "services", title: "本地家电清洗服务", content: "空调、洗衣机上门清洗，可开收据。", community: "滨江花园", priceText: "¥99 起", updatedAt: "昨天 21:15" }
];

const activeCategory = ref("all");
const page = ref(1);
const pageSize = 4;

const filteredListings = computed(() => {
  if (activeCategory.value === "all") {
    return listings;
  }
  return listings.filter((item) => item.category === activeCategory.value);
});

const pagedListings = computed(() => {
  const start = (page.value - 1) * pageSize;
  return filteredListings.value.slice(start, start + pageSize);
});

watch(activeCategory, () => {
  page.value = 1;
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

const { pullStatus, pullOffset, pullText } = useH5PullRefresh(async () => {
  page.value = 1;
});
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
