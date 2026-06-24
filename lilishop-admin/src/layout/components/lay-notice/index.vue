<script setup lang="ts">
import { useRouter } from "vue-router";
import { ref, computed, onMounted } from "vue";
import { getIndexNotice } from "@/api/dashboard";
import NoticeList from "./components/NoticeList.vue";
import type { ListItem, TabItem } from "./data";

import BellIcon from "~icons/lucide/bell";
import ArrowRightIcon from "~icons/ri/arrow-right-s-line";
import RefreshLine from "~icons/ri/refresh-line";

const router = useRouter();
const dropdownRef = ref();
const loading = ref(false);
const notices = ref<TabItem[]>([
  {
    key: "business",
    name: "业务提醒",
    list: [],
    emptyText: "暂无待处理提醒"
  }
]);
const activeKey = ref("business");

const getLabel = computed(
  () => (item: TabItem) =>
    item.name + (item.list.length > 0 ? `（${item.list.length}）` : "")
);

const currentNoticeHasData = computed(() => {
  const currentNotice = notices.value.find(
    item => item.key === activeKey.value
  );
  return Boolean(currentNotice && currentNotice.list.length > 0);
});

const hasAnyNoticeData = computed(() => {
  return notices.value.some(
    item => Array.isArray(item.list) && item.list.length > 0
  );
});

const totalNoticeCount = computed(() => {
  return notices.value.reduce((sum, item) => sum + item.list.length, 0);
});

function buildNoticeList(payload: Record<string, any>) {
  const rawItems = [
    {
      count: Number(payload.goods || 0),
      title: "待处理商品",
      description: "进入商品治理页处理待审核或待处理商品",
      path: "/goods-governance/goods-manage",
      extra: "待处理",
      status: "warning" as const
    },
    {
      count: Number(payload.store || 0),
      title: "待处理店铺",
      description: "进入店铺审核页处理待审核店铺",
      path: "/store-governance/store-apply",
      extra: "待审核",
      status: "warning" as const
    },
    {
      count: Number(payload.refund || 0),
      title: "待退款售后",
      description: "进入售后治理页处理退款和售后申请",
      path: "/order-governance/after-sale",
      extra: "待处理",
      status: "danger" as const
    },
    {
      count: Number(payload.waitPayBill || 0),
      title: "待支付账单",
      description: "进入资金对账页查看待支付账单",
      path: "/fund-governance/fund-reconciliation",
      extra: "待支付",
      status: "primary" as const
    }
  ];
  return rawItems
    .filter(item => item.count > 0)
    .map<ListItem>(item => ({
      avatar: "",
      title: `${item.title} ${item.count} 条`,
      description: item.description,
      datetime: "实时统计",
      type: "business",
      path: item.path,
      extra: item.extra,
      status: item.status
    }));
}

async function loadNotices() {
  loading.value = true;
  try {
    const response = await getIndexNotice();
    const payload = response?.result ?? response?.data ?? {};
    notices.value = [
      {
        key: "business",
        name: "业务提醒",
        list: buildNoticeList(payload),
        emptyText: "暂无待处理提醒"
      }
    ];
  } finally {
    loading.value = false;
  }
}

const onWatchMore = () => {
  router.push("/dashboard/wholesale");
  dropdownRef.value.handleClose();
};

const onRefresh = async () => {
  await loadNotices();
};

const onNoticeClick = (item: ListItem) => {
  if (!item.path) return;
  router.push(item.path);
  dropdownRef.value.handleClose();
};

onMounted(() => {
  loadNotices();
});
</script>

<template>
  <el-dropdown ref="dropdownRef" trigger="click" placement="bottom-end">
    <span
      :class="['dropdown-badge', 'navbar-bg-hover', 'select-none', 'mr-1.75']"
    >
      <el-badge :value="totalNoticeCount" :hidden="!hasAnyNoticeData" :max="99">
        <span class="header-notice-icon">
          <IconifyIconOffline :icon="BellIcon" />
        </span>
      </el-badge>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-tabs
          v-model="activeKey"
          :stretch="true"
          v-loading="loading"
          class="dropdown-tabs"
          :style="{ width: '330px' }"
        >
          <template v-for="item in notices" :key="item.key">
            <el-tab-pane :label="getLabel(item)" :name="`${item.key}`">
              <el-scrollbar max-height="345px">
                <div class="noticeList-container">
                  <NoticeList
                    :list="item.list"
                    :emptyText="item.emptyText"
                    @item-click="onNoticeClick"
                  />
                </div>
              </el-scrollbar>
            </el-tab-pane>
          </template>
        </el-tabs>
        <div
          class="border-t border-t-(--el-border-color-light) text-sm"
        >
          <div class="flex-bc m-1">
            <el-button type="primary" size="small" text @click="onWatchMore">
              进入工作台
              <IconifyIconOffline :icon="ArrowRightIcon" />
            </el-button>
            <el-button type="primary" size="small" text @click="onRefresh">
              刷新
              <IconifyIconOffline :icon="RefreshLine" />
            </el-button>
          </div>
        </div>
        <div
          v-if="currentNoticeHasData"
          class="border-t border-t-(--el-border-color-light) text-sm"
        >
          <div class="px-4 py-2 text-[12px] text-[#8a8f97]">
            点击提醒项可直接跳转到对应处理页面
          </div>
        </div>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="scss" scoped>
/* ”铃铛“摇晃衰减动画 */
@keyframes pure-bell-ring {
  0%,
  100% {
    transform-origin: top;
  }

  15% {
    transform: rotateZ(10deg);
  }

  30% {
    transform: rotateZ(-10deg);
  }

  45% {
    transform: rotateZ(5deg);
  }

  60% {
    transform: rotateZ(-5deg);
  }

  75% {
    transform: rotateZ(2deg);
  }
}

.dropdown-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 48px;
  cursor: pointer;

  .header-notice-icon {
    font-size: 16px;
  }

  &:hover {
    .header-notice-icon svg {
      animation: pure-bell-ring 1s both;
    }
  }
}

.dropdown-tabs {
  .noticeList-container {
    padding: 15px 24px 0;
  }

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap)::after {
    height: 1px;
  }

  :deep(.el-tabs__nav-wrap) {
    padding: 0 36px;
  }
}
</style>
