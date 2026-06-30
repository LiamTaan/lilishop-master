<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import AdminModuleShell from "@/components/AdminModuleShell";
import { getSettingConfig, saveSettingConfig } from "@/api/super-admin";
import { message } from "@/utils/message";
import { isSuccessResult, unwrapResult } from "@/utils/result";

defineOptions({
  name: "PlatformPaymentSetting"
});

type PaymentSupportItem = {
  client?: string;
  supports?: string[];
};

const clientOptions = [
  { value: "H5", label: "移动端 H5", description: "商城 H5、移动浏览器支付场景" },
  { value: "PC", label: "PC 端", description: "PC 收银台与网页支付场景" },
  {
    value: "WECHAT_MP",
    label: "微信小程序",
    description: "微信小程序内下单支付场景"
  },
  { value: "APP", label: "APP 端", description: "原生 App 支付场景" }
];

const paymentMethodOptions = [
  { value: "WALLET", label: "余额支付" },
  { value: "ALIPAY", label: "支付宝" },
  { value: "WECHAT", label: "微信支付" },
  { value: "UNIONPAY", label: "银联云闪付" },
  { value: "BANK_TRANSFER", label: "线下转账" }
];

const clientLabelMap = new Map(clientOptions.map(item => [item.value, item.label]));
const paymentLabelMap = new Map(
  paymentMethodOptions.map(item => [item.value, item.label])
);
const knownClientSet = new Set(clientOptions.map(item => item.value));
const knownPaymentSet = new Set(paymentMethodOptions.map(item => item.value));

const loading = ref(false);
const previewVisible = ref(false);
const rawConfig = ref<Record<string, any>>({});
const lastLoadedPayload = ref<Record<string, any>>({});
const unmanagedClientItems = ref<PaymentSupportItem[]>([]);
const form = reactive<Record<string, string[]>>({});

const extraEntries = computed(() =>
  Object.entries(rawConfig.value).filter(([key]) => key !== "paymentSupportItems")
);

const enabledChannelCount = computed(() =>
  Object.values(form).reduce((sum, current) => sum + (current?.length || 0), 0)
);

const paymentPreview = computed(() =>
  clientOptions.map(client => ({
    client: client.label,
    description: client.description,
    supports: (form[client.value] || []).map(
      payment => paymentLabelMap.get(payment) || payment
    )
  }))
);

const summaryCards = computed(() => [
  {
    label: "配置键",
    value: "PAYMENT_SUPPORT",
    accent: "orange" as const,
    hint: "平台支付方式总入口"
  },
  {
    label: "终端数量",
    value: clientOptions.length,
    accent: "blue" as const,
    hint: "当前已表单化的终端"
  },
  {
    label: "已启用通道",
    value: enabledChannelCount.value,
    accent: "green" as const,
    hint: "所有终端下勾选的支付方式总数"
  },
  {
    label: "未托管配置",
    value: unmanagedClientItems.value.length + extraEntries.value.length,
    accent: "purple" as const,
    hint: "保留但未表单化的原始配置"
  }
]);

function resetForm() {
  clientOptions.forEach(client => {
    form[client.value] = [];
  });
}

function normalizeSupports(value: unknown) {
  if (!Array.isArray(value)) return [];
  return Array.from(
    new Set(
      value
        .map(item => String(item || "").trim())
        .filter(item => item && knownPaymentSet.has(item))
    )
  );
}

function applyPayload(payload: Record<string, any>) {
  resetForm();
  unmanagedClientItems.value = [];

  const items = Array.isArray(payload?.paymentSupportItems)
    ? payload.paymentSupportItems
    : [];

  items.forEach(item => {
    const client = String(item?.client || "").trim();
    const supports = normalizeSupports(item?.supports);
    if (knownClientSet.has(client)) {
      form[client] = supports;
      return;
    }
    unmanagedClientItems.value.push({
      client,
      supports: Array.isArray(item?.supports)
        ? item.supports.map(entry => String(entry || ""))
        : []
    });
  });
}

function buildPayload() {
  return {
    ...rawConfig.value,
    paymentSupportItems: [
      ...clientOptions.map(client => ({
        client: client.value,
        supports: [...(form[client.value] || [])]
      })),
      ...unmanagedClientItems.value
    ]
  };
}

async function loadData() {
  loading.value = true;
  try {
    const response = await getSettingConfig("PAYMENT_SUPPORT");
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || "支付配置读取失败");
    }
    const payload = unwrapResult<Record<string, any>>(response) || {};
    rawConfig.value = payload;
    lastLoadedPayload.value = payload;
    applyPayload(payload);
    message("支付配置读取成功", { type: "success" });
  } catch (error: any) {
    message(error?.message || "支付配置读取失败，请稍后重试", {
      type: "error"
    });
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  loading.value = true;
  try {
    const payload = buildPayload();
    const response = await saveSettingConfig("PAYMENT_SUPPORT", payload);
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || "支付配置保存失败");
    }
    rawConfig.value = payload;
    lastLoadedPayload.value = payload;
    message("支付配置保存成功", { type: "success" });
  } catch (error: any) {
    message(error?.message || "支付配置保存失败", { type: "error" });
  } finally {
    loading.value = false;
  }
}

function handleRestore() {
  applyPayload(lastLoadedPayload.value || {});
  message("支付配置已恢复到最近一次读取结果", { type: "success" });
}

onMounted(() => {
  resetForm();
  loadData();
});
</script>

<template>
  <AdminModuleShell
    title="支付配置"
    description="按终端维护平台可用支付方式，不再直接暴露 JSON 结构给运营侧。"
    api-path="/manager/setting/setting"
    :tips="[
      '先勾选各终端允许使用的支付方式，再分别维护微信、支付宝、银联参数。',
      '保存时会按后端要求回写 paymentSupportItems。',
      '如果历史配置里存在当前页面未托管的数据，会原样保留在原始预览中。'
    ]"
    :summary-cards="summaryCards"
  >
    <el-alert
      v-if="unmanagedClientItems.length || extraEntries.length"
      type="warning"
      :closable="false"
      class="mb-4"
      title="检测到历史配置中存在未表单化字段，保存时会原样保留，请在原始配置预览中复核。"
    />

    <section class="payment-layout">
      <div class="payment-card">
        <div class="payment-card__header">
          <div>
            <h3>终端支付矩阵</h3>
            <p>按终端勾选允许展示的支付方式，未勾选即表示该终端不开放该通道。</p>
          </div>
          <el-tag type="warning" effect="plain">PAYMENT_SUPPORT</el-tag>
        </div>

        <div class="payment-grid">
          <div
            v-for="client in clientOptions"
            :key="client.value"
            class="payment-client-card"
          >
            <div class="payment-client-card__title">
              <div>
                <strong>{{ client.label }}</strong>
                <p>{{ client.description }}</p>
              </div>
              <el-tag
                :type="(form[client.value] || []).length ? 'success' : 'info'"
                effect="plain"
              >
                {{ (form[client.value] || []).length }} 项
              </el-tag>
            </div>

            <el-checkbox-group
              v-model="form[client.value]"
              class="payment-method-group"
            >
              <el-checkbox
                v-for="method in paymentMethodOptions"
                :key="method.value"
                :label="method.value"
                border
                class="payment-method-item"
              >
                {{ method.label }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </div>

        <div class="setting-actions">
          <el-button @click="handleRestore">恢复读取值</el-button>
          <el-button type="warning" plain @click="previewVisible = true">
            原始配置预览
          </el-button>
          <el-button @click="loadData">重新读取</el-button>
          <el-button type="primary" :loading="loading" @click="handleSave">
            保存配置
          </el-button>
        </div>
      </div>

      <div class="payment-side-column">
        <div class="payment-card payment-card--side">
          <h3>配置说明</h3>
          <ul class="payment-note-list">
            <li>这里控制的是“终端允许使用哪些支付方式”，不是具体密钥。</li>
            <li>微信、支付宝、银联的商户参数仍应分别在独立页面配置。</li>
            <li>如果某终端下一个支付方式都不勾选，前台该终端将没有可用在线支付通道。</li>
          </ul>
        </div>

        <div class="payment-card payment-card--side">
          <h3>当前预览</h3>
          <div class="payment-preview-list">
            <div
              v-for="item in paymentPreview"
              :key="item.client"
              class="payment-preview-item"
            >
              <div>
                <strong>{{ item.client }}</strong>
                <p>{{ item.description }}</p>
              </div>
              <span class="payment-preview-item__supports">
                {{ item.supports.length ? item.supports.join("、") : "未开放" }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </AdminModuleShell>

  <el-drawer v-model="previewVisible" title="原始配置预览" size="680px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="配置键">
        PAYMENT_SUPPORT
      </el-descriptions-item>
      <el-descriptions-item label="未托管客户端">
        {{
          unmanagedClientItems.length
            ? unmanagedClientItems
                .map(item => clientLabelMap.get(String(item.client || "")) || item.client || "-")
                .join("、")
            : "无"
        }}
      </el-descriptions-item>
      <el-descriptions-item label="额外顶层字段">
        {{
          extraEntries.length
            ? extraEntries.map(([key]) => key).join("、")
            : "无"
        }}
      </el-descriptions-item>
      <el-descriptions-item label="原始 JSON">
        <pre class="raw-json">{{ JSON.stringify(rawConfig, null, 2) }}</pre>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.payment-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(320px, 1fr);
  gap: 16px;
}

.payment-card {
  background: #fff;
  border: 1px solid var(--wholesale-card-border);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgb(21 26 38 / 4%);
  padding: 20px 22px;
}

.payment-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.payment-card__header h3,
.payment-card--side h3 {
  margin: 0;
  color: #2f3135;
  font-size: 18px;
  font-weight: 700;
}

.payment-card__header p {
  margin: 8px 0 0;
  color: #667085;
  line-height: 1.7;
}

.payment-grid,
.payment-side-column,
.payment-preview-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-client-card {
  border: 1px solid #eef1f5;
  border-radius: 16px;
  padding: 18px;
  background: linear-gradient(180deg, #fff 0%, #fbfcff 100%);
}

.payment-client-card__title,
.payment-preview-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.payment-client-card__title strong,
.payment-preview-item strong {
  color: #22262d;
  font-size: 16px;
}

.payment-client-card__title p,
.payment-preview-item p {
  margin: 6px 0 0;
  color: #7b8493;
  font-size: 12px;
  line-height: 1.6;
}

.payment-method-group {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

:deep(.payment-method-item) {
  width: 100%;
  height: auto;
  margin-right: 0;
  padding: 12px 14px;
}

.setting-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.payment-card--side {
  min-height: 220px;
}

.payment-note-list {
  margin: 14px 0 0;
  padding-left: 18px;
  color: #4f5560;
  line-height: 1.9;
}

.payment-preview-item {
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 14px;
}

.payment-preview-item__supports {
  color: #dd7a18;
  font-size: 13px;
  text-align: right;
  line-height: 1.6;
}

.raw-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #5d6168;
}

@media (width <= 1200px) {
  .payment-layout {
    grid-template-columns: 1fr;
  }
}

@media (width <= 900px) {
  .payment-method-group {
    grid-template-columns: 1fr;
  }
}
</style>
