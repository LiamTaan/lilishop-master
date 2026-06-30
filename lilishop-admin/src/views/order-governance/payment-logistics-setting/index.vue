<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import AdminModuleShell from "@/components/AdminModuleShell";
import ImageUploadField from "@/components/ImageUploadField/index.vue";
import { getSettingConfig, saveSettingConfig } from "@/api/super-admin";
import { isSuccessResult, unwrapResult } from "@/utils/result";
import { message } from "@/utils/message";

type SettingField = {
  key: string;
  label: string;
  type?: "text" | "textarea" | "number" | "switch" | "password" | "json" | "image";
  placeholder?: string;
  rows?: number;
};

type SettingPanel = {
  key: string;
  title: string;
  shortTitle: string;
  description: string;
  usageTips: string[];
  fields: SettingField[];
  form: Record<string, any>;
  rawConfig: Record<string, any>;
  lastLoadedPayload: Record<string, any>;
  loading: boolean;
};

function createPanel(config: Omit<SettingPanel, "form" | "rawConfig" | "lastLoadedPayload" | "loading">) {
  return reactive<SettingPanel>({
    ...config,
    form: {},
    rawConfig: {},
    lastLoadedPayload: {},
    loading: false
  });
}

const paymentPanel = createPanel({
  key: "PAYMENT_SUPPORT",
  title: "支付基础设置",
  shortTitle: "支付基础",
  description: "配置平台支持哪些支付方式、适用终端和整体开关，作为实际收款能力总入口。",
  usageTips: [
    "这里决定平台允许展示和调用哪些支付方式。",
    "建议先维护支付基础，再分别补全微信、支付宝、银联参数。",
    "如果返回的是数组结构，可直接在右侧预览当前支付方式状态。"
  ],
  fields: [
    {
      key: "paymentSupportItems",
      label: "支付支持项",
      type: "json",
      rows: 12,
      placeholder: "请输入支付支持配置 JSON 数组或对象"
    }
  ]
});

const wechatPanel = createPanel({
  key: "WECHAT_PAYMENT",
  title: "微信支付配置",
  shortTitle: "微信支付",
  description: "维护微信商户号、应用标识、证书与回调地址，供小程序、H5、APP 等场景统一使用。",
  usageTips: [
    "JSAPI、Native、小程序、H5、APP 可按实际终端分别维护 AppId。",
    "私钥、公钥、APIv3 密钥通常由支付服务商或商户平台提供。",
    "回调地址需和后端支付通知地址保持一致。"
  ],
  fields: [
    { key: "jsapiAppId", label: "JSAPI AppId" },
    { key: "nativeAppId", label: "Native AppId" },
    { key: "mpAppId", label: "小程序 AppId" },
    { key: "h5AppId", label: "H5 AppId" },
    { key: "appAppId", label: "APP AppId" },
    { key: "mchId", label: "商户号" },
    { key: "serialNumber", label: "商户证书序列号" },
    { key: "apiclientKey", label: "私钥", type: "password" },
    { key: "publicId", label: "公钥 ID" },
    { key: "publicKey", label: "公钥", type: "password" },
    { key: "publicType", label: "验证方式" },
    { key: "apiKey3", label: "APIv3 密钥", type: "password" },
    { key: "callbackUrl", label: "回调地址" }
  ]
});

const alipayPanel = createPanel({
  key: "ALIPAY_PAYMENT",
  title: "支付宝配置",
  shortTitle: "支付宝",
  description: "维护支付宝应用标识、密钥与证书地址，供网页支付、APP 支付等场景使用。",
  usageTips: [
    "应用 AppId 对应支付宝开放平台应用。",
    "证书路径与私钥需和服务端读取路径保持一致。",
    "回调地址通常用于支付结果异步通知。"
  ],
  fields: [
    { key: "appId", label: "应用 AppId" },
    { key: "privateKey", label: "应用私钥", type: "password" },
    { key: "certPath", label: "应用证书路径", type: "password" },
    {
      key: "alipayPublicCertPath",
      label: "支付宝公钥证书路径",
      type: "password"
    },
    { key: "rootCertPath", label: "支付宝根证书路径", type: "password" },
    { key: "callbackUrl", label: "回调地址" }
  ]
});

const unionpayPanel = createPanel({
  key: "UNIONPAY_PAYMENT",
  title: "银联支付配置",
  shortTitle: "银联支付",
  description: "维护银联商户接入密钥、交易请求地址和应用标识，用于银联支付链路。",
  usageTips: [
    "商户号和密钥通常由银联渠道方提供。",
    "请求地址与交易域名需按正式环境或测试环境区分。",
    "应用 ID 为空时，先确认该业务线是否启用了银联渠道。"
  ],
  fields: [
    { key: "unionPayMachId", label: "商户号" },
    { key: "unionPayKey", label: "密钥", type: "password" },
    { key: "unionPayServerUrl", label: "请求地址" },
    { key: "unionPayDomain", label: "交易请求地址" },
    { key: "unionPayAppId", label: "应用 ID" }
  ]
});

const logisticsPanel = createPanel({
  key: "LOGISTICS_SETTING",
  title: "物流设置",
  shortTitle: "物流设置",
  description: "维护物流查询渠道、电子面单及快递服务商参数，支撑订单发货和轨迹查询。",
  usageTips: [
    "物流查询类型决定优先走哪家查询服务。",
    "快递鸟、快递100、顺丰可按实际合作方维护对应凭证。",
    "面单模板、月结号等字段通常用于打印电子面单。"
  ],
  fields: [
    { key: "type", label: "物流查询类型" },
    { key: "kdniaoEbusinessID", label: "快递鸟商户号" },
    { key: "kdniaoAppKey", label: "快递鸟 AppKey", type: "password" },
    { key: "requestType", label: "调用端口" },
    { key: "kuaidi100Customer", label: "快递100 授权码", type: "password" },
    { key: "kuaidi100Key", label: "快递100 Key", type: "password" },
    { key: "clientCode", label: "顺丰顾客编码" },
    { key: "checkWord", label: "顺丰校验码", type: "password" },
    { key: "callUrl", label: "顺丰请求地址" },
    { key: "templateCode", label: "顺丰打印模板" },
    { key: "monthlyCardNo", label: "顺丰月结号" }
  ]
});

const panels = [
  paymentPanel,
  wechatPanel,
  alipayPanel,
  unionpayPanel,
  logisticsPanel
];

const activeTab = ref(paymentPanel.key);
const previewVisible = ref(false);
const previewPanelKey = ref(paymentPanel.key);

const currentPanel = computed(
  () => panels.find(panel => panel.key === activeTab.value) || paymentPanel
);

const previewPanel = computed(
  () => panels.find(panel => panel.key === previewPanelKey.value) || paymentPanel
);

const summaryCards = computed(() => {
  const mappedFieldCount = panels.reduce(
    (sum, panel) => sum + panel.fields.length,
    0
  );
  const configuredFieldCount = panels.reduce(
    (sum, panel) => sum + getConfiguredFieldCount(panel),
    0
  );
  const configuredGroupCount = panels.filter(panel =>
    panel.fields.some(field => {
      const value = panel.form[field.key];
      return value !== "" && value !== undefined && value !== null;
    })
  ).length;
  const extraFieldCount = panels.reduce(
    (sum, panel) => sum + getExtraEntries(panel).length,
    0
  );

  return [
    {
      label: "配置分组",
      value: panels.length,
      hint: "已拆成支付与物流 5 个可维护分组"
    },
    {
      label: "已映射字段",
      value: mappedFieldCount,
      hint: "当前页面可直接维护的配置项"
    },
    {
      label: "已配置字段",
      value: configuredFieldCount,
      hint: "当前已有值的配置项"
    },
    {
      label: "已配置分组",
      value: configuredGroupCount,
      hint: "当前已读取到内容的分组数量"
    },
    {
      label: "未映射字段",
      value: extraFieldCount,
      hint: "后端返回但当前页面未单独展开的键"
    }
  ];
});

const paymentSupportPreview = computed(() => {
  const parsed = safeParseJson(paymentPanel.form.paymentSupportItems);
  if (!parsed) return [];

  if (typeof parsed !== "object") return [];

  const source = Array.isArray(parsed)
    ? parsed
    : Array.isArray(parsed.list)
      ? parsed.list
      : Object.entries(parsed).map(([key, value]) => {
          if (value && typeof value === "object") {
            return { key, ...(value as Record<string, any>) };
          }
          return { key, value };
        });

  return source.map((item: Record<string, any>, index: number) => {
    const channel =
      item.paymentName ||
      item.name ||
      item.way ||
      item.paymentType ||
      item.code ||
      item.key ||
      `支付方式 ${index + 1}`;
    const scene =
      [
        item.clientType,
        item.terminal,
        item.support,
        item.scene,
        item.remark
      ]
        .filter(Boolean)
        .join(" / ") || "未标注适用终端";
    const statusValue =
      item.enable ?? item.enabled ?? item.open ?? item.status ?? item.isOpen;
    const statusText =
      statusValue === false ||
      ["0", "CLOSE", "DISABLED", "OFF"].includes(
        String(statusValue || "").toUpperCase()
      )
        ? "关闭"
        : "启用";

    return {
      channel,
      scene,
      statusText
    };
  });
});

function getDefaultValue(field: SettingField) {
  if (field.type === "switch") return false;
  if (field.type === "number") return 0;
  if (field.type === "json") return "{}";
  return "";
}

function normalizeFieldValue(field: SettingField, value: any) {
  if (value === undefined || value === null || value === "") {
    return getDefaultValue(field);
  }
  if (field.type === "json") {
    return typeof value === "string" ? value : JSON.stringify(value, null, 2);
  }
  if (field.type === "number") return Number(value || 0);
  if (field.type === "switch") return Boolean(value);
  return value;
}

function safeParseJson(value: unknown) {
  if (!value || typeof value !== "string") return null;
  try {
    return JSON.parse(value);
  } catch (_error) {
    return null;
  }
}

function getExtraEntries(panel: SettingPanel) {
  const declaredKeys = panel.fields.map(field => field.key);
  return Object.entries(panel.rawConfig || {}).filter(
    ([key]) => !declaredKeys.includes(key)
  );
}

function getMissingFields(panel: SettingPanel) {
  return panel.fields.filter(field => {
    const value = panel.form[field.key];
    return value === "" || value === undefined || value === null;
  });
}

function getConfiguredFieldCount(panel: SettingPanel) {
  return panel.fields.length - getMissingFields(panel).length;
}

function buildPayload(panel: SettingPanel) {
  const payload: Record<string, any> = {};
  panel.fields.forEach(field => {
    if (field.type === "json") {
      try {
        payload[field.key] = panel.form[field.key]
          ? JSON.parse(panel.form[field.key])
          : {};
      } catch (_error) {
        throw new Error(`${field.label} 不是合法 JSON`);
      }
      return;
    }
    payload[field.key] = panel.form[field.key];
  });
  return payload;
}

async function loadPanel(panel: SettingPanel) {
  panel.loading = true;
  try {
    const response = await getSettingConfig(panel.key);
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || `${panel.title}读取失败`);
    }
    const payload = unwrapResult(response) || {};
    panel.rawConfig = payload;
    panel.lastLoadedPayload = payload;
    panel.fields.forEach(field => {
      panel.form[field.key] = normalizeFieldValue(field, payload?.[field.key]);
    });
  } catch (error: any) {
    message(error?.message || `${panel.title}读取失败`, { type: "error" });
  } finally {
    panel.loading = false;
  }
}

async function handleSave(panel: SettingPanel) {
  panel.loading = true;
  try {
    const payload = buildPayload(panel);
    const response = await saveSettingConfig(panel.key, payload);
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || `${panel.title}保存失败`);
    }
    panel.rawConfig = payload;
    panel.lastLoadedPayload = payload;
    message(`${panel.title}保存成功`, { type: "success" });
  } catch (error: any) {
    message(error?.message || `${panel.title}保存失败`, { type: "error" });
  } finally {
    panel.loading = false;
  }
}

function handleRestore(panel: SettingPanel) {
  panel.fields.forEach(field => {
    panel.form[field.key] = normalizeFieldValue(
      field,
      panel.lastLoadedPayload?.[field.key]
    );
  });
  message(`${panel.title}已恢复到最近一次读取结果`, { type: "success" });
}

function openPreview(panel: SettingPanel) {
  previewPanelKey.value = panel.key;
  previewVisible.value = true;
}

onMounted(() => {
  panels.forEach(panel => {
    panel.fields.forEach(field => {
      panel.form[field.key] = getDefaultValue(field);
    });
  });
  void Promise.all(panels.map(loadPanel));
});
</script>

<template>
  <AdminModuleShell title="支付 / 物流设置" description="" api-path="/manager/setting/setting">
    <section class="setting-overview">
      <div
        v-for="card in summaryCards"
        :key="card.label"
        class="setting-stat-card"
      >
        <span class="setting-stat-card__label">{{ card.label }}</span>
        <strong class="setting-stat-card__value">{{ card.value }}</strong>
        <span class="setting-stat-card__hint">{{ card.hint }}</span>
      </div>
    </section>

    <section class="setting-guide">
      <div>
        <h3 class="setting-guide__title">当前分组</h3>
        <p class="setting-guide__desc">{{ currentPanel.description }}</p>
      </div>
      <ul class="setting-guide__list">
        <li v-for="tip in currentPanel.usageTips" :key="tip">
          {{ tip }}
        </li>
      </ul>
    </section>

    <el-tabs v-model="activeTab" class="setting-tabs">
      <el-tab-pane
        v-for="panel in panels"
        :key="panel.key"
        :label="panel.shortTitle"
        :name="panel.key"
      >
        <section class="setting-panel-grid">
          <div class="setting-card">
            <div class="setting-card__header">
              <div>
                <h3 class="setting-card__title">{{ panel.title }}</h3>
                <p class="setting-card__desc">{{ panel.description }}</p>
              </div>
              <el-tag type="warning" effect="plain">
                {{ panel.key }}
              </el-tag>
            </div>

            <el-form
              label-width="138px"
              class="setting-form"
              :class="{ 'setting-form--single': panel.fields.length <= 2 }"
            >
              <el-form-item
                v-for="field in panel.fields"
                :key="field.key"
                :label="field.label"
                class="mb-0!"
              >
                <el-switch
                  v-if="field.type === 'switch'"
                  v-model="panel.form[field.key]"
                  inline-prompt
                  active-text="开"
                  inactive-text="关"
                />
                <el-input-number
                  v-else-if="field.type === 'number'"
                  v-model="panel.form[field.key]"
                  class="w-full!"
                  :min="0"
                />
                <ImageUploadField
                  v-else-if="field.type === 'image'"
                  v-model="panel.form[field.key]"
                  :tip="`${field.label} 统一走上传组件维护`"
                />
                <el-input
                  v-else-if="field.type === 'password'"
                  v-model="panel.form[field.key]"
                  show-password
                  :placeholder="field.placeholder || `请输入${field.label}`"
                />
                <el-input
                  v-else-if="field.type === 'json'"
                  v-model="panel.form[field.key]"
                  type="textarea"
                  :rows="field.rows || 8"
                  :placeholder="field.placeholder || `请输入${field.label}`"
                />
                <el-input
                  v-else-if="field.type === 'textarea'"
                  v-model="panel.form[field.key]"
                  type="textarea"
                  :rows="field.rows || 4"
                  :placeholder="field.placeholder || `请输入${field.label}`"
                />
                <el-input
                  v-else
                  v-model="panel.form[field.key]"
                  :placeholder="field.placeholder || `请输入${field.label}`"
                />
              </el-form-item>
            </el-form>

            <div class="setting-actions">
              <el-button @click="handleRestore(panel)">恢复读取值</el-button>
              <el-button type="warning" plain @click="openPreview(panel)">
                原始配置预览
              </el-button>
              <el-button @click="loadPanel(panel)">重新读取</el-button>
              <el-button
                type="primary"
                :loading="panel.loading"
                @click="handleSave(panel)"
              >
                保存{{ panel.shortTitle }}
              </el-button>
            </div>

            <div class="setting-panel-status">
              <div class="setting-panel-status__item">
                <span>配置完成度</span>
                <strong>
                  {{ getConfiguredFieldCount(panel) }} / {{ panel.fields.length }}
                </strong>
              </div>
              <div class="setting-panel-status__item">
                <span>待补字段</span>
                <strong>{{ getMissingFields(panel).length }}</strong>
              </div>
              <div
                v-if="getMissingFields(panel).length"
                class="setting-panel-status__missing"
              >
                {{ getMissingFields(panel).map(field => field.label).join("、") }}
              </div>
            </div>
          </div>

          <div class="setting-side-column">
            <div class="setting-card setting-card--side">
              <h3 class="setting-card__title">使用说明</h3>
              <ul class="setting-note-list">
                <li v-for="tip in panel.usageTips" :key="tip">
                  {{ tip }}
                </li>
              </ul>
            </div>

            <div
              v-if="panel.key === paymentPanel.key"
              class="setting-card setting-card--side"
            >
              <h3 class="setting-card__title">支付方式预览</h3>
              <div v-if="paymentSupportPreview.length" class="channel-list">
                <div
                  v-for="item in paymentSupportPreview"
                  :key="`${item.channel}-${item.scene}`"
                  class="channel-item"
                >
                  <div>
                    <strong>{{ item.channel }}</strong>
                    <p>{{ item.scene }}</p>
                  </div>
                  <el-tag
                    :type="item.statusText === '启用' ? 'success' : 'info'"
                    effect="plain"
                  >
                    {{ item.statusText }}
                  </el-tag>
                </div>
              </div>
              <el-empty
                v-else
                description="当前未识别出可预览的支付方式，可先读取或补充支付支持项 JSON。"
              />
            </div>

            <div
              v-else
              class="setting-card setting-card--side setting-card--meta"
            >
              <h3 class="setting-card__title">分组状态</h3>
              <div class="setting-meta-row">
                <span>已映射字段</span>
                <strong>{{ panel.fields.length }}</strong>
              </div>
              <div class="setting-meta-row">
                <span>未映射字段</span>
                <strong>{{ getExtraEntries(panel).length }}</strong>
              </div>
              <div class="setting-meta-row">
                <span>保存方式</span>
                <strong>按分组保存</strong>
              </div>
            </div>
          </div>
        </section>
      </el-tab-pane>
    </el-tabs>
  </AdminModuleShell>

  <el-drawer v-model="previewVisible" title="原始配置预览" size="680px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="配置键">
        {{ previewPanel.key }}
      </el-descriptions-item>
      <el-descriptions-item label="配置分组">
        {{ previewPanel.title }}
      </el-descriptions-item>
      <el-descriptions-item label="未映射字段">
        {{
          getExtraEntries(previewPanel).length
            ? getExtraEntries(previewPanel)
                .map(([key]) => key)
                .join("、")
            : "无"
        }}
      </el-descriptions-item>
      <el-descriptions-item label="原始 JSON">
        <pre class="raw-json">{{ JSON.stringify(previewPanel.rawConfig, null, 2) }}</pre>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.setting-overview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.setting-stat-card,
.setting-guide,
.setting-card {
  background: #fff;
  border: 1px solid var(--wholesale-card-border);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgb(21 26 38 / 4%);
}

.setting-stat-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px 22px;
}

.setting-stat-card__label {
  color: #7b8493;
  font-size: 13px;
}

.setting-stat-card__value {
  color: #22262d;
  font-size: 30px;
  line-height: 1;
}

.setting-stat-card__hint {
  color: #a0a7b4;
  font-size: 12px;
}

.setting-guide {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  justify-content: space-between;
  padding: 20px 22px;
}

.setting-guide__title,
.setting-card__title {
  margin: 0;
  color: #2f3135;
  font-size: 18px;
  font-weight: 700;
}

.setting-guide__desc,
.setting-card__desc {
  margin: 8px 0 0;
  color: #667085;
  line-height: 1.7;
}

.setting-guide__list,
.setting-note-list {
  margin: 0;
  padding-left: 18px;
  color: #4f5560;
  line-height: 1.9;
}

.setting-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.setting-panel-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(320px, 1fr);
  gap: 16px;
}

.setting-card {
  padding: 20px 22px;
}

.setting-card__header {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 18px;
}

.setting-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 18px;
}

.setting-form--single {
  grid-template-columns: minmax(0, 1fr);
}

:deep(.setting-form .el-form-item) {
  margin: 0 !important;
}

:deep(.setting-form .el-form-item__content) {
  min-width: 0;
}

.setting-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.setting-panel-status {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #f0f2f5;
}

.setting-panel-status__item {
  display: flex;
  gap: 10px;
  align-items: baseline;
  padding: 10px 14px;
  background: #f8fafc;
  border-radius: 12px;
}

.setting-panel-status__item span {
  color: #7b8493;
  font-size: 12px;
}

.setting-panel-status__item strong {
  color: #22262d;
  font-size: 18px;
}

.setting-panel-status__missing {
  width: 100%;
  color: #8a93a1;
  font-size: 12px;
  line-height: 1.8;
}

.setting-side-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.setting-card--side {
  min-height: 220px;
}

.channel-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.channel-item,
.setting-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 14px;
}

.channel-item strong,
.setting-meta-row strong {
  color: #22262d;
}

.channel-item p {
  margin: 6px 0 0;
  color: #667085;
  font-size: 12px;
}

.setting-card--meta {
  gap: 12px;
}

.raw-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #5d6168;
}

@media (width <= 1200px) {
  .setting-overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .setting-panel-grid {
    grid-template-columns: 1fr;
  }
}

@media (width <= 900px) {
  .setting-guide {
    flex-direction: column;
  }

  .setting-form {
    grid-template-columns: 1fr;
  }
}

@media (width <= 640px) {
  .setting-overview {
    grid-template-columns: 1fr;
  }
}
</style>
