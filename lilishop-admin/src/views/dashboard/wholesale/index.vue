<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useDark, useECharts } from "@pureadmin/utils";
import { getIndexNotice, getWholesaleDashboard } from "@/api/dashboard";

import Wallet from "~icons/ri/wallet-3-line";
import Store from "~icons/ri/store-2-line";
import Order from "~icons/ri/file-list-3-line";
import Verify from "~icons/ri/task-line";
import Goods from "~icons/ri/shopping-bag-3-line";
import Profit from "~icons/ri/exchange-funds-line";
import Group from "~icons/ri/team-line";
import DataIcon from "~icons/ri/bar-chart-box-line";
import Member from "~icons/ri/user-star-line";

defineOptions({
  name: "WholesaleDashboard"
});

const router = useRouter();
const loading = ref(false);
const rawPayload = ref<Record<string, any>>({});
const noticePayload = ref<Record<string, any>>({});

const { isDark } = useDark();
const chartTheme = computed(() => (isDark.value ? "dark" : "light"));

const coreMetricsRef = ref();
const governanceRef = ref();
const { setOptions: setCoreMetricsOptions } = useECharts(coreMetricsRef, {
  theme: chartTheme
});
const { setOptions: setGovernanceOptions } = useECharts(governanceRef, {
  theme: chartTheme
});

const metricCards = computed(() => {
  const statistics = rawPayload.value.indexStatistics ?? {};
  const businessTrend = rawPayload.value.businessTrend ?? {};
  return [
    {
      label: "今日销售额",
      value: `¥ ${formatAmount(statistics.todayOrderPrice)}`,
      icon: Wallet,
      accent: "blue"
    },
    {
      label: "在营店铺",
      value: formatCount(statistics.storeNum),
      icon: Store,
      accent: "green"
    },
    {
      label: "待分账金额",
      value: `¥ ${formatAmount(businessTrend.pendingShareAmount)}`,
      icon: Profit,
      accent: "orange"
    },
    {
      label: "今日订单总数",
      value: formatCount(statistics.todayOrderNum),
      icon: Order,
      accent: "purple"
    },
    {
      label: "今日新增客户",
      value: formatCount(statistics.todayMemberNum),
      icon: Member,
      accent: "violet"
    }
  ];
});

const quickActions = [
  { label: "钱包流水", icon: Wallet, accent: "blue", path: "/fund-governance/wallet-log" },
  { label: "商品管理", icon: Goods, accent: "orange", path: "/goods-governance/goods-manage" },
  { label: "分账记录", icon: Profit, accent: "purple", path: "/fund-governance/profitsharing-record" },
  { label: "店铺管理", icon: Store, accent: "green", path: "/store-governance/store-manage" },
  { label: "店铺审核", icon: Member, accent: "violet", path: "/store-governance/store-apply" },
  { label: "经营概览", icon: DataIcon, accent: "lime", path: "/dashboard/overview" },
  { label: "拼团治理", icon: Group, accent: "cyan", path: "/marketing-governance/pintuan-manage" },
  { label: "核销记录", icon: Verify, accent: "amber", path: "/order-governance/verification-record" }
];

const noticeList = computed(() => {
  const notice = noticePayload.value ?? {};
  const items = [
    { label: "待处理商品", value: notice.goods ?? 0 },
    { label: "待处理店铺", value: notice.store ?? 0 },
    { label: "待退款售后", value: notice.refund ?? 0 },
    { label: "待支付账单", value: notice.waitPayBill ?? 0 }
  ];
  const hasPending = items.some(item => Number(item.value) > 0);
  return hasPending
    ? items.filter(item => Number(item.value) > 0)
    : [{ label: "当前无待处理提醒", value: 0 }];
});

const storeRankList = computed(() => {
  return (rawPayload.value.storeRankList ?? []).slice(0, 6).map((item, index) => ({
    rank: index + 1,
    name: item.storeName || "-",
    quantity: formatCount(item.num),
    amount: formatAmount(item.price)
  }));
});

const categoryRankList = computed(() => {
  return (rawPayload.value.categoryRankList ?? []).slice(0, 6).map((item, index) => ({
    rank: index + 1,
    name: item.categoryName || "-",
    quantity: formatCount(item.num),
    amount: formatAmount(item.price)
  }));
});

function formatAmount(value: unknown) {
  const amount = Number(value ?? 0);
  if (!Number.isFinite(amount)) return "0.00";
  return amount.toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

function formatCount(value: unknown) {
  const count = Number(value ?? 0);
  if (!Number.isFinite(count)) return "0";
  return count.toLocaleString("zh-CN");
}

function renderCharts() {
  const profitSharingBalance = rawPayload.value.profitSharingBalance ?? {};
  const businessTrend = rawPayload.value.businessTrend ?? {};
  const governanceSummary = rawPayload.value.governanceSummary ?? {};
  const orderSummary = governanceSummary.orderSummary ?? {};
  const afterSaleSummary = governanceSummary.afterSaleSummary ?? {};
  const profitSharingSummary = governanceSummary.profitSharingSummary ?? {};
  const verificationExceptionSummary =
    governanceSummary.verificationExceptionSummary ?? {};

  setCoreMetricsOptions({
    color: ["#ff7a1a"],
    grid: {
      top: 30,
      left: 70,
      right: 30,
      bottom: 30
    },
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" }
    },
    xAxis: {
      type: "value",
      axisLabel: { color: "#8e97a5" },
      splitLine: { lineStyle: { color: "#eef2f7" } }
    },
    yAxis: {
      type: "category",
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: { color: "#56606e" },
      data: ["今日下单金额", "待分账金额", "已付款总额", "退款总额"]
    },
    series: [
      {
        type: "bar",
        barWidth: 16,
        itemStyle: {
          color: "#ff7a1a",
          borderRadius: [0, 8, 8, 0]
        },
        data: [
          Number(businessTrend.todayOrderPrice ?? 0),
          Number(businessTrend.pendingShareAmount ?? 0),
          Number(profitSharingBalance.paidAmount ?? 0),
          Number(profitSharingBalance.refundAmount ?? 0)
        ]
      }
    ]
  });

  setGovernanceOptions({
    color: ["#2f8fff", "#33c8a0", "#ff8042", "#7d6bff", "#ff5f5f"],
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" }
    },
    grid: {
      top: 30,
      left: 34,
      right: 20,
      bottom: 46
    },
    xAxis: {
      type: "category",
      axisTick: { show: false },
      axisLine: { lineStyle: { color: "#e7ebf2" } },
      axisLabel: { color: "#8e97a5" },
      data: ["订单", "售后", "待分账", "异常核销", "待审核店铺"]
    },
    yAxis: {
      type: "value",
      axisLabel: { color: "#8e97a5" },
      splitLine: { lineStyle: { color: "#eef2f7" } }
    },
    series: [
      {
        type: "bar",
        barWidth: 24,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: (params: Record<string, any>) =>
            ["#2f8fff", "#33c8a0", "#ff8042", "#7d6bff", "#ff5f5f"][params.dataIndex]
        },
        data: [
          Number(orderSummary.totalCount ?? 0),
          Number(afterSaleSummary.totalCount ?? 0),
          Number(profitSharingSummary.pendingCount ?? 0),
          Number(verificationExceptionSummary.exceptionCount ?? 0),
          Number(businessTrend.submittedStoreCount ?? 0)
        ]
      }
    ]
  });
}

async function loadDashboard() {
  loading.value = true;
  try {
    const [dashboardRes, noticeRes] = await Promise.all([
      getWholesaleDashboard(),
      getIndexNotice()
    ]);
    rawPayload.value = dashboardRes.result ?? dashboardRes.data ?? {};
    noticePayload.value = noticeRes.result ?? noticeRes.data ?? {};
  } finally {
    loading.value = false;
    await nextTick();
    renderCharts();
  }
}

watch(chartTheme, async () => {
  await nextTick();
  renderCharts();
});

onMounted(() => {
  loadDashboard();
});
</script>

<template>
  <div v-loading="loading" class="workbench-page">
    <section class="metric-strip">
      <article
        v-for="item in metricCards"
        :key="item.label"
        class="metric-strip__item"
      >
        <div class="metric-strip__icon" :class="`metric-strip__icon--${item.accent}`">
          <component :is="item.icon" />
        </div>
        <div class="metric-strip__copy">
          <span class="metric-strip__label">{{ item.label }}</span>
          <strong class="metric-strip__value">{{ item.value }}</strong>
        </div>
      </article>
    </section>

    <section class="dashboard-grid">
      <div class="dashboard-grid__main">
        <article class="panel">
          <div class="panel__header">
            <h3>经营核心指标</h3>
          </div>
          <div ref="coreMetricsRef" class="chart-host chart-host--large" />
        </article>

        <article class="panel">
          <div class="panel__header">
            <h3>治理概览</h3>
          </div>
          <div ref="governanceRef" class="chart-host chart-host--large" />
        </article>
      </div>

      <div class="dashboard-grid__side">
        <article class="panel side-panel">
          <div class="panel__header">
            <h3>常用功能</h3>
          </div>
          <div class="quick-grid">
            <button
              v-for="item in quickActions"
              :key="item.label"
              type="button"
              class="quick-grid__item"
              @click="router.push(item.path)"
            >
              <div class="quick-grid__icon" :class="`quick-grid__icon--${item.accent}`">
                <component :is="item.icon" />
              </div>
              <span>{{ item.label }}</span>
            </button>
          </div>
        </article>

        <article class="panel panel--notice">
          <div class="panel__header">
            <h3>业务提醒</h3>
          </div>
          <div
            v-for="item in noticeList"
            :key="item.label"
            class="notice-row"
          >
            <span>{{ item.label }}</span>
            <strong>{{ formatCount(item.value) }}</strong>
          </div>
        </article>

        <article class="panel panel--rank">
          <div class="panel__header">
            <h3>热卖店铺</h3>
          </div>
          <div v-if="storeRankList.length" class="rank-list">
            <div
              v-for="item in storeRankList"
              :key="`${item.rank}-${item.name}`"
              class="rank-list__row"
            >
              <span class="rank-pill" :class="{ 'rank-pill--plain': item.rank > 3 }">
                {{ item.rank }}
              </span>
              <span class="rank-list__name">{{ item.name }}</span>
              <span class="rank-list__meta">销量 {{ item.quantity }}</span>
              <span class="rank-list__amount">¥ {{ item.amount }}</span>
            </div>
          </div>
          <div v-else class="empty-copy">暂无店铺排行数据</div>
        </article>

        <article class="panel panel--rank">
          <div class="panel__header">
            <h3>热卖品类</h3>
          </div>
          <div v-if="categoryRankList.length" class="rank-list">
            <div
              v-for="item in categoryRankList"
              :key="`${item.rank}-${item.name}`"
              class="rank-list__row"
            >
              <span class="rank-pill" :class="{ 'rank-pill--plain': item.rank > 3 }">
                {{ item.rank }}
              </span>
              <span class="rank-list__name">{{ item.name }}</span>
              <span class="rank-list__meta">销量 {{ item.quantity }}</span>
              <span class="rank-list__amount">¥ {{ item.amount }}</span>
            </div>
          </div>
          <div v-else class="empty-copy">暂无品类排行数据</div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.workbench-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  color: #fff;
  background:
    linear-gradient(135deg, rgb(255 122 26 / 98%), rgb(255 153 42 / 92%)),
    linear-gradient(120deg, #ff7a1a, #ff9729);
  border-radius: 18px;
  box-shadow: 0 18px 36px rgb(255 122 26 / 18%);
}

.metric-strip__item {
  display: flex;
  gap: 18px;
  align-items: center;
  padding: 26px 22px;
}

.metric-strip__item + .metric-strip__item {
  border-left: 1px solid rgb(255 255 255 / 18%);
}

.metric-strip__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  color: #ff7a1a;
  font-size: 26px;
  border-radius: 50%;
  background: rgb(255 255 255 / 92%);
}

.metric-strip__icon--blue {
  color: #2f8fff;
}

.metric-strip__icon--green {
  color: #33c8a0;
}

.metric-strip__icon--orange {
  color: #ff8042;
}

.metric-strip__icon--purple {
  color: #7d6bff;
}

.metric-strip__icon--violet {
  color: #ff8b38;
}

.metric-strip__copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.metric-strip__label {
  color: rgb(255 244 232 / 92%);
  font-size: 14px;
}

.metric-strip__value {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 18px;
}

.dashboard-grid__main,
.dashboard-grid__side {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel {
  background: #fff;
  border: 1px solid var(--wholesale-card-border);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgb(21 26 38 / 4%);
}

.panel__header {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px 0;
}

.panel__header h3 {
  margin: 0;
  color: #2f3135;
  font-size: 16px;
  font-weight: 700;
}

.chart-host {
  width: 100%;
}

.chart-host--large {
  height: 320px;
}

.side-panel {
  padding-bottom: 18px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px 12px;
  padding: 12px 22px 6px;
}

.quick-grid__item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  color: #4a505b;
  text-align: center;
  cursor: pointer;
  background: transparent;
  border: none;
}

.quick-grid__item span {
  font-size: 13px;
}

.quick-grid__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  color: #fff;
  font-size: 20px;
  border-radius: 12px;
}

.quick-grid__icon--blue {
  background: linear-gradient(180deg, #5da7ff 0%, #2f8fff 100%);
}

.quick-grid__icon--orange {
  background: linear-gradient(180deg, #ff8e59 0%, #ff6f47 100%);
}

.quick-grid__icon--purple,
.quick-grid__icon--violet {
  background: linear-gradient(180deg, #aa7dff 0%, #8f6bff 100%);
}

.quick-grid__icon--green,
.quick-grid__icon--lime {
  background: linear-gradient(180deg, #7ddb60 0%, #49c458 100%);
}

.quick-grid__icon--cyan {
  background: linear-gradient(180deg, #5cb8ff 0%, #2f8fff 100%);
}

.quick-grid__icon--amber {
  background: linear-gradient(180deg, #ffb060 0%, #ff8b38 100%);
}

.panel--notice {
  padding-bottom: 10px;
}

.notice-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 22px;
  color: #4e5561;
  font-size: 14px;
}

.notice-row strong {
  color: #ff7a1a;
}

.notice-row + .notice-row {
  border-top: 1px solid #f1f3f7;
}

.panel--rank {
  padding-bottom: 16px;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 12px 22px 0;
}

.rank-list__row {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) 72px 88px;
  gap: 12px;
  align-items: center;
  min-height: 48px;
  color: #4d5360;
}

.rank-list__row + .rank-list__row {
  border-top: 1px solid #f2f4f8;
}

.rank-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  background: #ff7a1a;
  border-radius: 50%;
}

.rank-pill--plain {
  color: #6b7280;
  background: #eef1f6;
}

.rank-list__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-list__meta {
  color: #8e97a5;
  font-size: 12px;
}

.rank-list__amount {
  color: #2f3135;
  font-weight: 600;
  text-align: right;
}

.empty-copy {
  padding: 18px 22px;
  color: #9aa2ae;
  font-size: 13px;
}

@media (width <= 1180px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-grid__side {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    align-items: start;
  }

  .metric-strip {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (width <= 900px) {
  .metric-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .metric-strip__item:nth-child(3),
  .metric-strip__item:nth-child(5) {
    border-left: none;
  }

  .metric-strip__item:nth-child(n + 3) {
    border-top: 1px solid rgb(255 255 255 / 18%);
  }

  .dashboard-grid__side {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (width <= 640px) {
  .metric-strip {
    grid-template-columns: 1fr;
  }

  .metric-strip__item + .metric-strip__item {
    border-top: 1px solid rgb(255 255 255 / 18%);
    border-left: none;
  }

  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .rank-list__row {
    grid-template-columns: 28px minmax(0, 1fr) 64px 72px;
  }
}
</style>
