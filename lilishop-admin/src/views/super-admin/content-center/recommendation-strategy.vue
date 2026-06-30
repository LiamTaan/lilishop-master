<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import {
  getHomeRecommendationStrategy,
  saveHomeRecommendationStrategy
} from "@/api/content-operation";
import { getCategoryPage } from "@/api/goods-governance";
import {
  extractApiPayload,
  extractApiRecords
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { isSuccessResult } from "@/utils/result";

defineOptions({
  name: "ContentRecommendationStrategy"
});

type RecommendationConfig = {
  enabled: boolean;
  title: string;
  sortOrder: number;
  limit: number;
  categoryRange: string[];
  preferRecommendFlag: boolean;
  coldStartStrategy: string;
  priceUpperBound: number;
  promotionTypes: string[];
};

const clientTypeOptions = ["APP", "H5", "PC"];
const coldStartStrategyOptions = [
  { label: "热销 + 上新", value: "HOT_AND_NEW" },
  { label: "仅热销补位", value: "HOT_ONLY" }
];
const promotionTypeOptions = [
  { label: "秒杀", value: "SECKILL" },
  { label: "拼团", value: "PINTUAN" },
  { label: "砍价", value: "KANJIA" }
];

const loading = ref(false);
const saving = ref(false);
const categoryOptions = ref<Record<string, any>[]>([]);
const form = reactive({
  clientType: "APP",
  frequentStoresConfig: createConfig("FREQUENT_STORES"),
  guessYouLikeConfig: createConfig("GUESS_YOU_LIKE"),
  lowPriceZoneConfig: createConfig("LOW_PRICE"),
  todaySpecialConfig: createConfig("TODAY_SPECIAL")
});

const enabledCount = computed(() =>
  [
    form.frequentStoresConfig,
    form.guessYouLikeConfig,
    form.lowPriceZoneConfig,
    form.todaySpecialConfig
  ].filter(item => item.enabled).length
);

function createConfig(code: string): RecommendationConfig {
  const base = {
    enabled: true,
    sortOrder: 0,
    categoryRange: [],
    preferRecommendFlag: true,
    coldStartStrategy: "HOT_AND_NEW",
    priceUpperBound: 99,
    promotionTypes: ["SECKILL", "PINTUAN", "KANJIA"]
  };
  if (code === "FREQUENT_STORES") return { ...base, title: "常买店铺", limit: 4 };
  if (code === "GUESS_YOU_LIKE") return { ...base, title: "猜你喜欢", limit: 12 };
  if (code === "LOW_PRICE") return { ...base, title: "低价专区", limit: 12 };
  return { ...base, title: "今日特惠", limit: 12 };
}

function mergeConfig(code: string, source?: Record<string, any>): RecommendationConfig {
  const defaults = createConfig(code);
  return {
    ...defaults,
    ...(source || {}),
    enabled: source?.enabled ?? defaults.enabled,
    categoryRange: Array.isArray(source?.categoryRange)
      ? source.categoryRange.map((item: any) => String(item))
      : [],
    promotionTypes: Array.isArray(source?.promotionTypes)
      ? source.promotionTypes.map((item: any) => String(item))
      : defaults.promotionTypes
  };
}

function normalizeCategory(item: Record<string, any>, level = 1): Record<string, any>[] {
  const current = {
    ...item,
    id: String(item.id || item.categoryId || ""),
    categoryName: item.categoryName || item.name || "-",
    levelLabel: `${level}级`
  };
  const children = item.children || item.child || [];
  return [
    current,
    ...(Array.isArray(children)
      ? children.flatMap((child: Record<string, any>) => normalizeCategory(child, level + 1))
      : [])
  ];
}

async function loadCategories() {
  try {
    categoryOptions.value = extractApiRecords(await getCategoryPage({ pageNumber: 1, pageSize: 500 }))
      .flatMap(item => normalizeCategory(item));
  } catch (_error) {
    categoryOptions.value = [];
  }
}

async function loadData() {
  loading.value = true;
  try {
    const payload = extractApiPayload(await getHomeRecommendationStrategy(form.clientType)) || {};
    form.frequentStoresConfig = mergeConfig("FREQUENT_STORES", payload.frequentStoresConfig);
    form.guessYouLikeConfig = mergeConfig("GUESS_YOU_LIKE", payload.guessYouLikeConfig);
    form.lowPriceZoneConfig = mergeConfig("LOW_PRICE", payload.lowPriceZoneConfig);
    form.todaySpecialConfig = mergeConfig("TODAY_SPECIAL", payload.todaySpecialConfig);
  } catch (_error) {
    message("推荐策略加载失败，请确认管理端接口已启动", { type: "error" });
  } finally {
    loading.value = false;
  }
}

function configPayload(config: RecommendationConfig) {
  return {
    enabled: !!config.enabled,
    title: config.title.trim(),
    sortOrder: Number(config.sortOrder || 0),
    limit: Number(config.limit || 1),
    categoryRange: config.categoryRange,
    preferRecommendFlag: !!config.preferRecommendFlag,
    coldStartStrategy: config.coldStartStrategy,
    priceUpperBound: Number(config.priceUpperBound || 99),
    promotionTypes: config.promotionTypes
  };
}

async function submit() {
  const configs = [
    form.frequentStoresConfig,
    form.guessYouLikeConfig,
    form.lowPriceZoneConfig,
    form.todaySpecialConfig
  ];
  if (configs.some(item => !item.title.trim() || Number(item.limit) <= 0)) {
    message("请填写标题并保证展示数量大于0", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const response = await saveHomeRecommendationStrategy({
      clientType: form.clientType,
      publishNow: true,
      frequentStoresConfig: configPayload(form.frequentStoresConfig),
      guessYouLikeConfig: configPayload(form.guessYouLikeConfig),
      lowPriceZoneConfig: configPayload(form.lowPriceZoneConfig),
      todaySpecialConfig: configPayload(form.todaySpecialConfig)
    });
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || "推荐策略保存失败");
    }
    message("推荐策略保存成功", { type: "success" });
    await loadData();
  } catch (error: any) {
    message(error?.message || "推荐策略保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  await loadCategories();
  await loadData();
});
</script>

<template>
  <div v-loading="loading" class="strategy-page">
    <section class="strategy-toolbar">
      <div>
        <h1>推荐策略</h1>
        <p>推荐区只维护规则参数，商品和店铺结果由订单、足迹、收藏、价格和促销自动计算。</p>
      </div>
      <div class="strategy-actions">
        <el-select v-model="form.clientType" style="width: 120px" @change="loadData">
          <el-option v-for="item in clientTypeOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-tag type="success">已启用 {{ enabledCount }} / 4</el-tag>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </div>
    </section>

    <div class="strategy-grid">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>常买店铺</span>
            <el-switch v-model="form.frequentStoresConfig.enabled" active-text="启用" inactive-text="停用" />
          </div>
        </template>
        <div class="form-grid">
          <el-form-item label="标题"><el-input v-model="form.frequentStoresConfig.title" /></el-form-item>
          <el-form-item label="展示数量"><el-input-number v-model="form.frequentStoresConfig.limit" :min="1" :max="20" style="width: 100%" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.frequentStoresConfig.sortOrder" :min="0" style="width: 100%" /></el-form-item>
        </div>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>猜你喜欢</span>
            <el-switch v-model="form.guessYouLikeConfig.enabled" active-text="启用" inactive-text="停用" />
          </div>
        </template>
        <div class="form-grid">
          <el-form-item label="标题"><el-input v-model="form.guessYouLikeConfig.title" /></el-form-item>
          <el-form-item label="展示数量"><el-input-number v-model="form.guessYouLikeConfig.limit" :min="1" :max="30" style="width: 100%" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.guessYouLikeConfig.sortOrder" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="冷启动">
            <el-select v-model="form.guessYouLikeConfig.coldStartStrategy" style="width: 100%">
              <el-option v-for="item in coldStartStrategyOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类范围">
            <el-select v-model="form.guessYouLikeConfig.categoryRange" multiple clearable collapse-tags style="width: 100%">
              <el-option v-for="item in categoryOptions" :key="item.id" :label="`${item.categoryName}（${item.levelLabel}）`" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="推荐标记"><el-switch v-model="form.guessYouLikeConfig.preferRecommendFlag" /></el-form-item>
        </div>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>低价专区</span>
            <el-switch v-model="form.lowPriceZoneConfig.enabled" active-text="启用" inactive-text="停用" />
          </div>
        </template>
        <div class="form-grid">
          <el-form-item label="标题"><el-input v-model="form.lowPriceZoneConfig.title" /></el-form-item>
          <el-form-item label="展示数量"><el-input-number v-model="form.lowPriceZoneConfig.limit" :min="1" :max="30" style="width: 100%" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.lowPriceZoneConfig.sortOrder" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="价格上限"><el-input-number v-model="form.lowPriceZoneConfig.priceUpperBound" :min="1" :max="999999" :precision="2" style="width: 100%" /></el-form-item>
          <el-form-item label="分类范围">
            <el-select v-model="form.lowPriceZoneConfig.categoryRange" multiple clearable collapse-tags style="width: 100%">
              <el-option v-for="item in categoryOptions" :key="item.id" :label="`${item.categoryName}（${item.levelLabel}）`" :value="item.id" />
            </el-select>
          </el-form-item>
        </div>
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>今日特惠</span>
            <el-switch v-model="form.todaySpecialConfig.enabled" active-text="启用" inactive-text="停用" />
          </div>
        </template>
        <div class="form-grid">
          <el-form-item label="标题"><el-input v-model="form.todaySpecialConfig.title" /></el-form-item>
          <el-form-item label="展示数量"><el-input-number v-model="form.todaySpecialConfig.limit" :min="1" :max="30" style="width: 100%" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.todaySpecialConfig.sortOrder" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="活动类型">
            <el-select v-model="form.todaySpecialConfig.promotionTypes" multiple clearable collapse-tags style="width: 100%">
              <el-option v-for="item in promotionTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类范围">
            <el-select v-model="form.todaySpecialConfig.categoryRange" multiple clearable collapse-tags style="width: 100%">
              <el-option v-for="item in categoryOptions" :key="item.id" :label="`${item.categoryName}（${item.levelLabel}）`" :value="item.id" />
            </el-select>
          </el-form-item>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.strategy-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
}

.strategy-toolbar,
.strategy-actions,
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.strategy-toolbar h1 {
  margin: 0 0 6px;
  font-size: 22px;
}

.strategy-toolbar p {
  margin: 0;
  color: #6b7280;
}

.strategy-grid {
  display: grid;
  gap: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

@media (max-width: 960px) {
  .strategy-toolbar,
  .strategy-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
