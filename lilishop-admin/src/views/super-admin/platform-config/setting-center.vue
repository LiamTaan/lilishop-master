<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { message } from "@/utils/message";
import AdminModuleShell from "@/components/AdminModuleShell";
import ImageUploadField from "@/components/ImageUploadField/index.vue";
import { getSettingConfig, saveSettingConfig } from "@/api/super-admin";
import { isSuccessResult, unwrapResult } from "@/utils/result";

const props = defineProps<{
  title: string;
  description: string;
  settingKey: string;
  apiPath: string;
  fields: Array<{
    key: string;
    label: string;
    type?: "text" | "textarea" | "number" | "switch" | "password" | "json" | "image";
    placeholder?: string;
    rows?: number;
  }>;
}>();

const loading = ref(false);
const previewVisible = ref(false);
const rawConfig = ref<Record<string, any>>({});
const lastLoadedPayload = ref<Record<string, any>>({});
const form = reactive<Record<string, any>>({});

const declaredKeys = computed(() => props.fields.map(field => field.key));
const missingFieldCount = computed(
  () => props.fields.filter(field => form[field.key] === "" || form[field.key] === undefined || form[field.key] === null).length
);
const configuredFieldCount = computed(
  () => props.fields.length - missingFieldCount.value
);
const extraEntries = computed(() =>
  Object.entries(rawConfig.value).filter(([key]) => !declaredKeys.value.includes(key))
);
const missingFieldLabels = computed(() =>
  props.fields
    .filter(
      field =>
        form[field.key] === "" ||
        form[field.key] === undefined ||
        form[field.key] === null
    )
    .map(field => field.label)
);

const summaryCards = computed(() => [
  {
    label: "配置键",
    value: props.settingKey,
    accent: "orange" as const,
    hint: "直接复用后端 SettingEnum"
  },
  {
    label: "字段数",
    value: props.fields.length,
    accent: "blue" as const,
    hint: "当前页面已承接配置项"
  },
  {
    label: "已配置字段",
    value: configuredFieldCount.value,
    accent: "green" as const,
    hint: "当前已有值的配置项"
  },
  {
    label: "待补字段",
    value: missingFieldCount.value,
    accent: "purple" as const,
    hint: "当前配置页仍为空的字段"
  },
  {
    label: "未映射字段",
    value: extraEntries.value.length,
    accent: "purple" as const,
    hint: "后端返回但当前页面未展示的键"
  }
]);

function getDefaultValue(field: (typeof props.fields)[number]) {
  if (field.type === "switch") return false;
  if (field.type === "number") return 0;
  if (field.type === "json") return "{}";
  return "";
}

function normalizeFieldValue(
  field: (typeof props.fields)[number],
  value: any
) {
  if (value === undefined || value === null || value === "") {
    return getDefaultValue(field);
  }
  if (field.type === "json") {
    return typeof value === "string" ? value : JSON.stringify(value, null, 2);
  }
  if (field.type === "number") {
    return Number(value || 0);
  }
  if (field.type === "switch") {
    return Boolean(value);
  }
  return value;
}

function buildPayload() {
  const payload: Record<string, any> = {};
  props.fields.forEach(field => {
    if (field.type === "json") {
      try {
        payload[field.key] = form[field.key] ? JSON.parse(form[field.key]) : {};
      } catch (_error) {
        throw new Error(`${field.label} 不是合法 JSON`);
      }
      return;
    }
    payload[field.key] = form[field.key];
  });
  return payload;
}

async function loadData() {
  loading.value = true;
  try {
    const response = await getSettingConfig(props.settingKey);
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || `${props.title}配置读取失败`);
    }
    const payload = unwrapResult(response) || {};
    rawConfig.value = payload || {};
    lastLoadedPayload.value = payload || {};
    props.fields.forEach(field => {
      form[field.key] = normalizeFieldValue(field, payload?.[field.key]);
    });
    message(`${props.title}配置读取成功`, { type: "success" });
  } catch (error: any) {
    message(
      error?.message || `${props.title}配置读取失败，请确认后端配置键是否可用`,
      {
      type: "error"
      }
    );
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  loading.value = true;
  try {
    const payload = buildPayload();
    const response = await saveSettingConfig(props.settingKey, payload);
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || `${props.title}保存失败`);
    }
    rawConfig.value = payload;
    lastLoadedPayload.value = payload;
    message(`${props.title}保存成功`, { type: "success" });
  } catch (error: any) {
    message(error?.message || `${props.title}保存失败`, { type: "error" });
  } finally {
    loading.value = false;
  }
}

function handleRestore() {
  props.fields.forEach(field => {
    form[field.key] = normalizeFieldValue(field, lastLoadedPayload.value?.[field.key]);
  });
  message(`${props.title}已恢复到最近一次读取结果`, { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <AdminModuleShell
    :title="props.title"
    :description="props.description"
    :api-path="props.apiPath"
    :tips="['配置读取', '整包保存', '与后端枚举对齐', '支持原始 JSON 预览']"
    :summary-cards="summaryCards"
  >
    <el-alert
      v-if="extraEntries.length"
      type="warning"
      :closable="false"
      class="mb-4"
      title="后端返回了当前页面未映射的配置字段，联调时请关注原始配置预览。"
    />

    <el-alert
      v-if="missingFieldLabels.length"
      type="info"
      :closable="false"
      class="mb-4"
      :title="`待补字段：${missingFieldLabels.join('、')}`"
    />

    <el-form
      label-width="120px"
      class="setting-form grid grid-cols-1 gap-4 lg:grid-cols-2"
    >
      <el-form-item
        v-for="field in props.fields"
        :key="field.key"
        :label="field.label"
        class="mb-0!"
      >
        <el-switch
          v-if="field.type === 'switch'"
          v-model="form[field.key]"
          inline-prompt
          active-text="开"
          inactive-text="关"
        />
        <el-input-number
          v-else-if="field.type === 'number'"
          v-model="form[field.key]"
          class="w-full!"
          :min="0"
        />
        <ImageUploadField
          v-else-if="field.type === 'image'"
          v-model="form[field.key]"
          :tip="`${field.label} 统一走上传组件维护`"
        />
        <el-input
          v-else-if="field.type === 'password'"
          v-model="form[field.key]"
          show-password
          :placeholder="field.placeholder || `请输入${field.label}`"
        />
        <el-input
          v-else-if="field.type === 'json'"
          v-model="form[field.key]"
          type="textarea"
          :rows="field.rows || 6"
          :placeholder="field.placeholder || `请输入${field.label} JSON`"
        />
        <el-input
          v-else-if="field.type === 'textarea'"
          v-model="form[field.key]"
          type="textarea"
          :rows="field.rows || 4"
          :placeholder="field.placeholder || `请输入${field.label}`"
        />
        <el-input
          v-else
          v-model="form[field.key]"
          :placeholder="field.placeholder || `请输入${field.label}`"
        />
      </el-form-item>
    </el-form>

    <div class="setting-actions mt-6 flex justify-end gap-3">
      <el-button @click="handleRestore">恢复读取值</el-button>
      <el-button type="warning" plain @click="previewVisible = true">
        原始配置预览
      </el-button>
      <el-button @click="loadData">重新读取</el-button>
      <el-button type="primary" :loading="loading" @click="handleSave">
        保存配置
      </el-button>
    </div>

    <div class="setting-status-grid mt-6">
      <div class="setting-status-card">
        <span class="setting-status-card__label">配置完成度</span>
        <strong class="setting-status-card__value">
          {{ configuredFieldCount }} / {{ props.fields.length }}
        </strong>
        <span class="setting-status-card__hint">
          {{ missingFieldLabels.length ? "仍有字段待补齐" : "当前字段已全部配置" }}
        </span>
      </div>
      <div class="setting-status-card">
        <span class="setting-status-card__label">缺失字段</span>
        <strong class="setting-status-card__value">
          {{ missingFieldLabels.length || 0 }}
        </strong>
        <span class="setting-status-card__hint">
          {{
            missingFieldLabels.length
              ? missingFieldLabels.slice(0, 3).join("、")
              : "无缺失项"
          }}
        </span>
      </div>
    </div>
  </AdminModuleShell>

  <el-drawer v-model="previewVisible" title="原始配置预览" size="620px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="配置键">{{ props.settingKey }}</el-descriptions-item>
      <el-descriptions-item label="未映射字段">
        {{ extraEntries.length ? extraEntries.map(([key]) => key).join("、") : "无" }}
      </el-descriptions-item>
      <el-descriptions-item label="原始 JSON">
        <pre class="raw-json">{{ JSON.stringify(rawConfig, null, 2) }}</pre>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.setting-form {
  max-width: 1120px;
}

.setting-status-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  max-width: 720px;
}

.setting-status-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid var(--wholesale-card-border);
  border-radius: 16px;
}

.setting-status-card__label {
  color: #7b8493;
  font-size: 13px;
}

.setting-status-card__value {
  color: #22262d;
  font-size: 28px;
  line-height: 1;
}

.setting-status-card__hint {
  color: #8a93a1;
  font-size: 12px;
  line-height: 1.6;
}

.setting-actions {
  flex-wrap: wrap;
}

.raw-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #5d6168;
}

@media (width <= 900px) {
  .setting-status-grid {
    grid-template-columns: 1fr;
  }
}
</style>
