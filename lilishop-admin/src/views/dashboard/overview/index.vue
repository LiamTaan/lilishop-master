<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useDark, useECharts } from "@pureadmin/utils";
import { getOrderOverview, getWholesaleDashboard } from "@/api/dashboard";

import Wallet from "~icons/ri/wallet-3-line";
import Group from "~icons/ri/team-line";
import Profit from "~icons/ri/exchange-funds-line";
import Order from "~icons/ri/file-list-3-line";
import Member from "~icons/ri/user-add-line";

defineOptions({
  name: "PlatformOverview"
});

const loading = ref(false);
const rawPayload = ref<Record<string, any>>({});
const orderOverview = ref<Record<string, any>>({});

const { isDark } = useDark();
const chartTheme = computed(() => (isDark.value ? "dark" : "light"));

const coreMetricsRef = ref();
const resourceRef = ref();
const orderMetricsRef = ref();
const evaluationRef = ref();
const { setOptions: setCoreMetricsOptions } = useECharts(coreMetricsRef, {
  theme: chartTheme
});
const { setOptions: setResourceOptions } = useECharts(resourceRef, {
  theme: chartTheme
});
const { setOptions: setOrderMetricsOptions } = useECharts(orderMetricsRef, {
  theme: chartTheme
});
const { setOptions: setEvaluationOptions } = useECharts(evaluationRef, {
  theme: chartTheme
});

const topMetrics = computed(() => {
  const statistics = rawPayload.value.indexStatistics ?? {};
  const businessTrend = rawPayload.value.businessTrend ?? {};
  return [
    {
      label: "今日销售额（元）",
      value: formatMoney(statistics.todayOrderPrice),
      icon: Wallet
    },
    {
      label: "今日订单量",
      value: formatCount(statistics.todayOrderNum),
      icon: Order
    },
    {
      label: "待分账金额",
      value: formatMoney(businessTrend.pendingShareAmount),
      icon: Profit
    },
    {
      label: "当前在线人数",
      value: formatCount(statistics.currentNumberPeopleOnline),
      icon: Group
    },
    {
      label: "今日新增客户",
      value: formatCount(statistics.todayMemberNum),
      icon: Member
    }
  ];
});

const storeRows = computed(() => {
  return (rawPayload.value.storeRankList ?? []).slice(0, 6).map((item, index) => ({
    rank: index + 1,
    name: item.storeName || "-",
    quantity: formatCount(item.num),
    amount: formatMoney(item.price)
  }));
});

const categoryRows = computed(() => {
  return (rawPayload.value.categoryRankList ?? []).slice(0, 6).map((item, index) => ({
    rank: index + 1,
    name: item.categoryName || "-",
    quantity: formatCount(item.num),
    amount: formatMoney(item.price)
  }));
});

const kpiBlocks = computed(() => [
  {
    label: "付款金额",
    value: formatMoney(orderOverview.value.paymentAmount),
    hint: `付款订单 ${formatCount(orderOverview.value.paymentOrderNum)}`
  },
  {
    label: "退款金额",
    value: formatMoney(orderOverview.value.refundOrderPrice),
    hint: `退款订单 ${formatCount(orderOverview.value.refundOrderNum)}`
  },
  {
    label: "下单转化率",
    value: orderOverview.value.orderConversionRate || "0.0%",
    hint: `支付转化率 ${orderOverview.value.paymentsConversionRate || "0.0%"}`
  }
]);

const indicatorRows = computed(() => {
  const statistics = rawPayload.value.indexStatistics ?? {};
  const summary = rawPayload.value.governanceSummary ?? {};
  return [
    {
      label: "今日数据",
      amount: formatMoney(statistics.todayOrderPrice),
      orders: formatCount(statistics.todayOrderNum),
      customers: formatCount(statistics.todayMemberNum),
      stores: formatCount(statistics.todayStoreNum),
      traffic: formatCount(statistics.todayUV)
    },
    {
      label: "累计数据",
      amount: formatMoney(orderOverview.value.orderAmount),
      orders: formatCount(statistics.orderNum),
      customers: formatCount(statistics.memberNum),
      stores: formatCount(statistics.storeNum),
      traffic: formatCount(statistics.lastThirtyUV)
    },
    {
      label: "治理数据",
      amount: formatMoney(summary.orderSummary?.totalFlowPrice),
      orders: formatCount(summary.orderSummary?.totalCount),
      customers: formatCount(summary.afterSaleSummary?.totalCount),
      stores: formatCount(summary.profitSharingSummary?.pendingCount),
      traffic: formatCount(rawPayload.value.verificationSummary?.totalCount)
    }
  ];
});

function formatMoney(value: unknown) {
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
  const statistics = rawPayload.value.indexStatistics ?? {};
  const profitSharingBalance = rawPayload.value.profitSharingBalance ?? {};

  setCoreMetricsOptions({
    color: ["#ff7a1a"],
    grid: {
      top: 24,
      left: 70,
      right: 24,
      bottom: 24
    },
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" }
    },
    xAxis: {
      type: "value",
      axisLabel: { color: "#98a1ae" },
      splitLine: { lineStyle: { color: "#eef2f7" } }
    },
    yAxis: {
      type: "category",
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: { color: "#56606e" },
      data: ["下单金额", "付款金额", "退款金额", "待分账金额"]
    },
    series: [
      {
        type: "bar",
        barWidth: 18,
        itemStyle: {
          color: "#ff7a1a",
          borderRadius: [0, 8, 8, 0]
        },
        data: [
          Number(orderOverview.value.orderAmount ?? 0),
          Number(orderOverview.value.paymentAmount ?? 0),
          Number(orderOverview.value.refundOrderPrice ?? 0),
          Number(profitSharingBalance.pendingShareAmount ?? 0)
        ]
      }
    ]
  });

  setResourceOptions({
    color: ["#ff7a1a", "#2bc7a3", "#3a7dff", "#8f6bff"],
    tooltip: {
      trigger: "item"
    },
    legend: {
      bottom: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: "#98a1ae" },
      data: ["商品", "会员", "店铺", "订单"]
    },
    graphic: [
      {
        type: "text",
        left: "center",
        top: "39%",
        style: {
          text: formatCount(statistics.todayUV),
          fill: "#2f3135",
          fontSize: 30,
          fontWeight: 700
        }
      },
      {
        type: "text",
        left: "center",
        top: "56%",
        style: {
          text: "今日UV",
          fill: "#98a1ae",
          fontSize: 14
        }
      }
    ],
    series: [
      {
        type: "pie",
        radius: ["58%", "76%"],
        center: ["50%", "40%"],
        label: { show: false },
        data: [
          { value: Number(statistics.goodsNum ?? 0), name: "商品" },
          { value: Number(statistics.memberNum ?? 0), name: "会员" },
          { value: Number(statistics.storeNum ?? 0), name: "店铺" },
          { value: Number(statistics.orderNum ?? 0), name: "订单" }
        ]
      }
    ]
  });

  setOrderMetricsOptions({
    color: ["#3a86ff", "#8ec5ff", "#ff8042"],
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" }
    },
    grid: {
      top: 24,
      left: 40,
      right: 24,
      bottom: 28
    },
    xAxis: {
      type: "category",
      axisTick: { show: false },
      axisLine: { lineStyle: { color: "#e7ebf2" } },
      axisLabel: { color: "#98a1ae" },
      data: ["下单数", "付款单", "退款单", "下单人数", "付款人数"]
    },
    yAxis: {
      type: "value",
      splitLine: { lineStyle: { color: "#eef2f7" } },
      axisLabel: { color: "#98a1ae" }
    },
    series: [
      {
        type: "bar",
        barWidth: 24,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: (params: Record<string, any>) =>
            ["#3a86ff", "#8ec5ff", "#ff8042", "#2bc7a3", "#8f6bff"][params.dataIndex]
        },
        data: [
          Number(orderOverview.value.orderNum ?? 0),
          Number(orderOverview.value.paymentOrderNum ?? 0),
          Number(orderOverview.value.refundOrderNum ?? 0),
          Number(orderOverview.value.orderMemberNum ?? 0),
          Number(orderOverview.value.paymentsNum ?? 0)
        ]
      }
    ]
  });

  setEvaluationOptions({
    color: ["#4ea4ff", "#ff7a1a"],
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" }
    },
    legend: {
      top: 0,
      right: 0,
      textStyle: { color: "#98a1ae" },
      data: ["累计数据", "今日数据"]
    },
    grid: {
      top: 42,
      left: 44,
      right: 24,
      bottom: 32
    },
    xAxis: {
      type: "category",
      axisTick: { show: false },
      axisLine: { lineStyle: { color: "#e7ebf2" } },
      axisLabel: { color: "#98a1ae" },
      data: ["订单", "商品", "会员", "店铺", "UV", "在线人数"]
    },
    yAxis: {
      type: "value",
      splitLine: { lineStyle: { color: "#eef2f7" } },
      axisLabel: { color: "#98a1ae" }
    },
    series: [
      {
        name: "累计数据",
        type: "bar",
        barWidth: 18,
        data: [
          Number(statistics.orderNum ?? 0),
          Number(statistics.goodsNum ?? 0),
          Number(statistics.memberNum ?? 0),
          Number(statistics.storeNum ?? 0),
          Number(statistics.lastThirtyUV ?? 0),
          Number(statistics.currentNumberPeopleOnline ?? 0)
        ]
      },
      {
        name: "今日数据",
        type: "bar",
        barWidth: 18,
        data: [
          Number(statistics.todayOrderNum ?? 0),
          Number(statistics.todayGoodsNum ?? 0),
          Number(statistics.todayMemberNum ?? 0),
          Number(statistics.todayStoreNum ?? 0),
          Number(statistics.todayUV ?? 0),
          Number(statistics.currentNumberPeopleOnline ?? 0)
        ]
      }
    ]
  });
}

async function loadDashboard() {
  loading.value = true;
  try {
    const [dashboardRes, orderOverviewRes] = await Promise.all([
      getWholesaleDashboard(),
      getOrderOverview()
    ]);
    rawPayload.value = dashboardRes.result ?? dashboardRes.data ?? {};
    orderOverview.value = orderOverviewRes.result ?? orderOverviewRes.data ?? {};
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
  <div v-loading="loading" class="overview-page">
    <section class="overview-topbar">
      <article
        v-for="item in topMetrics"
        :key="item.label"
        class="overview-topbar__item"
      >
        <div class="overview-topbar__icon">
          <component :is="item.icon" />
        </div>
        <div class="overview-topbar__copy">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </article>
    </section>

    <section class="overview-grid">
      <article class="panel panel--wide">
        <div class="panel__heading">
          <div class="section-title">
            <span class="section-title__mark section-title__mark--orange" />
            <h3>经营核心指标</h3>
          </div>
        </div>
        <div ref="coreMetricsRef" class="chart-host chart-host--trend" />
      </article>

      <article class="panel">
        <div class="panel__heading">
          <div class="section-title">
            <span class="section-title__mark section-title__mark--orange" />
            <h3>资源概况</h3>
          </div>
        </div>
        <div ref="resourceRef" class="chart-host chart-host--activity" />
      </article>
    </section>

    <section class="overview-grid">
      <article class="panel panel--rank">
        <div class="panel__heading">
          <div class="section-title">
            <span class="section-title__mark section-title__mark--orange" />
            <h3>热卖店铺排行</h3>
          </div>
        </div>
        <div class="rank-table">
          <div class="rank-table__head">
            <span>排名</span>
            <span>店铺名称</span>
            <span>销量</span>
            <span>销售额</span>
          </div>
          <div
            v-for="item in storeRows"
            :key="`${item.rank}-${item.name}`"
            class="rank-table__row"
          >
            <span class="rank-pill" :class="{ 'rank-pill--plain': item.rank > 3 }">
              {{ item.rank }}
            </span>
            <span>{{ item.name }}</span>
            <span>{{ item.quantity }}</span>
            <span>¥ {{ item.amount }}</span>
          </div>
          <div v-if="!storeRows.length" class="empty-copy">暂无店铺排行数据</div>
        </div>
      </article>

      <article class="panel panel--rank">
        <div class="panel__heading">
          <div class="section-title">
            <span class="section-title__mark section-title__mark--orange" />
            <h3>热卖品类排行</h3>
          </div>
        </div>
        <div class="rank-table">
          <div class="rank-table__head">
            <span>排名</span>
            <span>品类名称</span>
            <span>销量</span>
            <span>销售额</span>
          </div>
          <div
            v-for="item in categoryRows"
            :key="`${item.rank}-${item.name}`"
            class="rank-table__row"
          >
            <span class="rank-pill" :class="{ 'rank-pill--plain': item.rank > 3 }">
              {{ item.rank }}
            </span>
            <span>{{ item.name }}</span>
            <span>{{ item.quantity }}</span>
            <span>¥ {{ item.amount }}</span>
          </div>
          <div v-if="!categoryRows.length" class="empty-copy">暂无品类排行数据</div>
        </div>
      </article>
    </section>

    <section class="overview-grid overview-grid--group">
      <article class="panel panel--wide">
        <div class="panel__heading">
          <div class="section-title">
            <span class="section-title__mark section-title__mark--orange" />
            <h3>订单概览</h3>
          </div>
        </div>
        <div ref="orderMetricsRef" class="chart-host chart-host--group" />
      </article>

      <article class="panel kpi-panel">
        <div
          v-for="item in kpiBlocks"
          :key="item.label"
          class="kpi-block"
        >
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <p class="kpi-change">{{ item.hint }}</p>
        </div>
      </article>
    </section>

    <article class="panel panel--evaluation">
      <div class="panel__heading">
        <div class="section-title">
          <span class="section-title__mark section-title__mark--orange" />
          <h3>整体评估</h3>
        </div>
      </div>

      <div ref="evaluationRef" class="chart-host chart-host--evaluation" />

      <table class="indicator-table">
        <thead>
          <tr>
            <th>指标</th>
            <th>交易额</th>
            <th>订单数</th>
            <th>客户/售后</th>
            <th>店铺/待分账</th>
            <th>流量/核销</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in indicatorRows" :key="item.label">
            <td>{{ item.label }}</td>
            <td>{{ item.amount }}</td>
            <td>{{ item.orders }}</td>
            <td>{{ item.customers }}</td>
            <td>{{ item.stores }}</td>
            <td>{{ item.traffic }}</td>
          </tr>
        </tbody>
      </table>
    </article>
  </div>
</template>

<style scoped>
.overview-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.overview-topbar {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 0;
  padding: 12px 0;
  color: #fff;
  background:
    linear-gradient(135deg, rgb(255 122 26 / 98%), rgb(255 153 42 / 92%)),
    linear-gradient(120deg, #ff7a1a, #ff9729);
  border-radius: 18px;
  box-shadow: 0 18px 36px rgb(255 122 26 / 18%);
}

.overview-topbar__item {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 14px 18px;
}

.overview-topbar__item + .overview-topbar__item {
  border-left: 1px solid rgb(255 255 255 / 20%);
}

.overview-topbar__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  font-size: 24px;
  color: #ff7a1a;
  background: rgb(255 255 255 / 86%);
  border-radius: 50%;
}

.overview-topbar__copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.overview-topbar__copy span {
  color: rgb(255 245 236 / 92%);
  font-size: 13px;
}

.overview-topbar__copy strong {
  font-size: 16px;
  font-weight: 700;
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.95fr) minmax(320px, 1fr);
  gap: 18px;
}

.overview-grid--group {
  grid-template-columns: minmax(0, 1fr) 260px;
}

.panel {
  background: #fff;
  border: 1px solid var(--wholesale-card-border);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgb(21 26 38 / 4%);
}

.panel__heading {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px 0;
}

.section-title {
  display: flex;
  gap: 10px;
  align-items: center;
}

.section-title h3 {
  margin: 0;
  color: #2f3135;
  font-size: 16px;
  font-weight: 700;
}

.section-title__mark {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.section-title__mark--orange {
  background: #ff7a1a;
}

.chart-host {
  width: 100%;
}

.chart-host--trend,
.chart-host--activity,
.chart-host--group {
  height: 300px;
}

.chart-host--evaluation {
  height: 360px;
}

.panel--rank {
  padding-bottom: 18px;
}

.rank-table {
  padding: 12px 22px 0;
}

.rank-table__head,
.rank-table__row {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) 88px 110px;
  gap: 12px;
  align-items: center;
}

.rank-table__head {
  color: #b0b7c3;
  font-size: 13px;
  padding: 8px 0 10px;
}

.rank-table__row {
  min-height: 54px;
  color: #4d5360;
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

.kpi-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 14px;
  padding: 22px;
}

.kpi-block {
  padding: 12px 0;
}

.kpi-block + .kpi-block {
  border-top: 1px solid #f2f4f8;
}

.kpi-block span {
  color: #8b919c;
  font-size: 14px;
}

.kpi-block strong {
  display: block;
  margin: 12px 0 10px;
  color: #2f3135;
  font-size: 30px;
  font-weight: 700;
}

.kpi-change {
  margin: 0;
  color: #8b919c;
  font-size: 13px;
}

.panel--evaluation {
  padding-bottom: 22px;
}

.indicator-table {
  width: calc(100% - 44px);
  margin: 16px 22px 0;
  overflow: hidden;
  border-collapse: collapse;
  border-radius: 12px;
}

.indicator-table th,
.indicator-table td {
  padding: 14px 12px;
  color: #58606c;
  font-size: 13px;
  text-align: center;
  border: 1px solid #f0f2f6;
}

.indicator-table thead th {
  color: #49505b;
  font-weight: 700;
  background: #f7f8fa;
}

.empty-copy {
  padding: 18px 0 0;
  color: #9aa2ae;
  font-size: 13px;
}

@media (width <= 1180px) {
  .overview-topbar {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .overview-grid,
  .overview-grid--group {
    grid-template-columns: 1fr;
  }
}

@media (width <= 900px) {
  .overview-topbar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .overview-topbar__item:nth-child(4),
  .overview-topbar__item:nth-child(5) {
    border-top: 1px solid rgb(255 255 255 / 18%);
  }
}

@media (width <= 640px) {
  .overview-topbar {
    grid-template-columns: 1fr;
  }

  .overview-topbar__item + .overview-topbar__item {
    border-top: 1px solid rgb(255 255 255 / 18%);
    border-left: none;
  }

  .rank-table__head,
  .rank-table__row {
    grid-template-columns: 42px minmax(0, 1fr) 64px 88px;
    font-size: 12px;
  }
}
</style>
