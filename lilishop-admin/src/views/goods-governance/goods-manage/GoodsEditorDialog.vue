<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import {
  createWholesaleGoods,
  getCategoryPage,
  getCategoryParameterOptions,
  getGoodsUnitPage,
  getStoreFreightTemplates,
  getStoreOptions,
  updateWholesaleGoods
} from "@/api/goods-governance";
import ImageUploadField from "@/components/ImageUploadField/index.vue";
import {
  extractApiPayload,
  extractApiRecords,
  getAgentLevelLabel,
  getStoreBizTypeLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "GoodsEditorDialog"
});

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    mode: "create" | "edit";
    goodsData?: Record<string, any> | null;
  }>(),
  {
    goodsData: null
  }
);

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  saved: [];
}>();

type TreeOption = {
  label: string;
  value: string;
  children?: TreeOption[];
};

type ParameterTemplate = {
  paramId: string;
  paramName: string;
  required: number;
  isIndex: number;
  optionList: string[];
  paramValue: string;
};

type SkuEntry = {
  id: string;
  sn: string;
  barcode: string;
  price: number;
  cost: number;
  weight: number;
  quantity: number;
  alertQuantity: number;
  specEntries: Array<{
    specName: string;
    specValue: string;
  }>;
};

type SpecDraftEntry = {
  id: string;
  name: string;
  valuesText: string;
};

type WholesaleRuleEntry = {
  id: string;
  skuId: string;
  num: number;
  price: number;
};

const visible = computed({
  get: () => props.modelValue,
  set: value => emit("update:modelValue", value)
});

const loading = ref(false);
const saving = ref(false);
const optionsLoaded = ref(false);
const freightLoading = ref(false);
const paramLoading = ref(false);
const storeOptions = ref<Record<string, any>[]>([]);
const categoryOptions = ref<TreeOption[]>([]);
const goodsUnitOptions = ref<Record<string, any>[]>([]);
const freightOptions = ref<Record<string, any>[]>([]);
const goodsParams = ref<ParameterTemplate[]>([]);
const specDrafts = ref<SpecDraftEntry[]>([]);

const form = reactive({
  storeId: "",
  goodsName: "",
  sellingPoint: "",
  categoryPath: [] as string[],
  goodsUnit: "",
  templateId: "",
  salesModel: "RETAIL",
  goodsType: "PHYSICAL_GOODS",
  release: false,
  recommend: false,
  barcode: "",
  intro: "",
  mobileIntro: "",
  goodsGalleryList: [] as string[],
  skuList: [] as SkuEntry[],
  wholesaleList: [] as WholesaleRuleEntry[]
});

const dialogTitle = computed(() =>
  props.mode === "create" ? "新增商品" : "编辑商品"
);

const isEdit = computed(() => props.mode === "edit");

const currentGoodsId = computed(() =>
  String(props.goodsData?.id || props.goodsData?.goodsId || "").trim()
);

const categoryLeafId = computed(() => {
  if (!form.categoryPath.length) {
    return "";
  }
  return String(form.categoryPath[form.categoryPath.length - 1] || "").trim();
});

const totalQuantity = computed(() =>
  form.skuList.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
);

const lowestSkuPrice = computed(() => {
  const prices = form.skuList
    .map(item => Number(item.price || 0))
    .filter(price => price > 0);
  if (!prices.length) {
    return 0;
  }
  return Math.min(...prices);
});

const lowestWholesalePrice = computed(() => {
  const prices = form.wholesaleList
    .map(item => Number(item.price || 0))
    .filter(price => price > 0);
  if (!prices.length) {
    return 0;
  }
  return Math.min(...prices);
});

const isPhysicalGoods = computed(() => form.goodsType === "PHYSICAL_GOODS");
const isWholesaleGoods = computed(() => form.salesModel === "WHOLESALE");

function createEmptySku(): SkuEntry {
  return {
    id: "",
    sn: "",
    barcode: "",
    price: 0,
    cost: 0,
    weight: 0,
    quantity: 0,
    alertQuantity: 0,
    specEntries: [{ specName: "", specValue: "" }]
  };
}

function createEmptySpecDraft(): SpecDraftEntry {
  return {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    name: "",
    valuesText: ""
  };
}

function createEmptyWholesaleRule(): WholesaleRuleEntry {
  return {
    id: "",
    skuId: "",
    num: 1,
    price: 0
  };
}

function splitCommaValues(source: unknown) {
  if (!source) {
    return [];
  }
  return Array.from(
    new Set(
      String(source)
        .split(/[,，]/)
        .map(item => item.trim())
        .filter(Boolean)
    )
  );
}

function normalizeCategoryTree(source: Record<string, any>[]): TreeOption[] {
  return source.map(item => {
    const childrenSource =
      Array.isArray(item.children) && item.children.length
        ? item.children
        : Array.isArray(item.child)
          ? item.child
          : [];
    return {
      label: String(item.name || item.categoryName || "-"),
      value: String(item.id || item.categoryId || ""),
      children: normalizeCategoryTree(childrenSource)
    };
  });
}

function getErrorMessage(error: any, fallback: string) {
  return error?.response?.data?.message || error?.message || fallback;
}

function resetForm() {
  form.storeId = "";
  form.goodsName = "";
  form.sellingPoint = "";
  form.categoryPath = [];
  form.goodsUnit = "";
  form.templateId = "";
  form.salesModel = "RETAIL";
  form.goodsType = "PHYSICAL_GOODS";
  form.release = false;
  form.recommend = false;
  form.barcode = "";
  form.intro = "";
  form.mobileIntro = "";
  form.goodsGalleryList = [""];
  form.skuList = [createEmptySku()];
  form.wholesaleList = [createEmptyWholesaleRule()];
  goodsParams.value = [];
  freightOptions.value = [];
  specDrafts.value = [createEmptySpecDraft()];
}

async function ensureOptionsLoaded() {
  if (optionsLoaded.value) {
    return;
  }
  const [storesResponse, categoriesResponse, goodsUnitsResponse] =
    await Promise.all([
      getStoreOptions(),
      getCategoryPage(),
      getGoodsUnitPage({ pageNumber: 1, pageSize: 200 })
    ]);

  storeOptions.value = (extractApiPayload(storesResponse) || []).map(
    (item: Record<string, any>) => ({
      label: [
        item.storeName || item.name || item.id,
        item.storeType || item.bizType
          ? getStoreBizTypeLabel(item.storeType || item.bizType)
          : "",
        item.agentLevel ? getAgentLevelLabel(item.agentLevel) : ""
      ]
        .filter(Boolean)
        .join(" / "),
      value: String(item.id || item.storeId || "")
    })
  );

  categoryOptions.value = normalizeCategoryTree(
    extractApiRecords(categoriesResponse)
  );

  goodsUnitOptions.value = extractApiRecords(goodsUnitsResponse).map(
    (item: Record<string, any>) => ({
      label: item.name || "-",
      value: item.name || "-"
    })
  );

  optionsLoaded.value = true;
}

async function loadFreightOptions(storeId: string) {
  if (!storeId) {
    freightOptions.value = [];
    form.templateId = "";
    return;
  }
  freightLoading.value = true;
  try {
    const response = await getStoreFreightTemplates(storeId);
    freightOptions.value = (extractApiPayload(response) || []).map(
      (item: Record<string, any>) => ({
        label: item.name || item.templateName || item.id,
        value: String(item.id || item.templateId || "")
      })
    );
    const currentTemplate = String(form.templateId || "").trim();
    if (
      currentTemplate &&
      !freightOptions.value.some(item => item.value === currentTemplate)
    ) {
      form.templateId = "";
    }
  } catch (error: any) {
    freightOptions.value = [];
    message(
      getErrorMessage(error, "运费模板加载失败，请确认该店铺已配置模板"),
      {
        type: "warning"
      }
    );
  } finally {
    freightLoading.value = false;
  }
}

async function loadCategoryParams(
  categoryId: string,
  source?: Record<string, any>
) {
  if (!categoryId) {
    goodsParams.value = [];
    return;
  }
  paramLoading.value = true;
  try {
    const response = await getCategoryParameterOptions(categoryId);
    const incomingParams = Array.isArray(source?.goodsParamsDTOList)
      ? source.goodsParamsDTOList
      : [];
    const valueMap = new Map(
      incomingParams.map((item: Record<string, any>) => [
        String(item?.paramId || item?.paramName || "").trim(),
        String(item?.paramValue || "").trim()
      ])
    );
    goodsParams.value = (extractApiPayload(response) || []).map(
      (item: Record<string, any>) => {
        const paramId = String(item.id || item.paramId || "").trim();
        const paramName = String(item.paramName || "").trim();
        return {
          paramId,
          paramName,
          required: Number(item.required ?? 0),
          isIndex: Number(item.isIndex ?? 0),
          optionList: splitCommaValues(item.options),
          paramValue:
            valueMap.get(paramId) ||
            valueMap.get(paramName) ||
            String(item.paramValue || "").trim()
        };
      }
    );
  } catch (error: any) {
    goodsParams.value = [];
    message(getErrorMessage(error, "分类参数加载失败"), {
      type: "warning"
    });
  } finally {
    paramLoading.value = false;
  }
}

function normalizeSkuRows(source: unknown): SkuEntry[] {
  if (!Array.isArray(source) || !source.length) {
    return [createEmptySku()];
  }
  return source.map((item: Record<string, any>) => {
    const visibleSpecs = Array.isArray(item.specList)
      ? item.specList
          .filter(
            (spec: Record<string, any>) =>
              spec?.specName && spec.specName !== "images"
          )
          .map((spec: Record<string, any>) => ({
            specName: String(spec.specName || "").trim(),
            specValue: String(spec.specValue || "").trim()
          }))
      : [];
    return {
      id: String(item.id || "").trim(),
      sn: String(item.sn || item.displayCode || "").trim(),
      barcode: String(item.barcode || item.displayBarcode || "").trim(),
      price: Number(item.price || 0),
      cost: Number(item.cost || item.price || 0),
      weight: Number(item.weight || 0),
      quantity: Number(item.quantity || 0),
      alertQuantity: Number(item.alertQuantity || 0),
      specEntries: visibleSpecs.length
        ? visibleSpecs
        : [{ specName: "", specValue: "" }]
    };
  });
}

function normalizeWholesaleRows(source: unknown): WholesaleRuleEntry[] {
  if (!Array.isArray(source) || !source.length) {
    return [createEmptyWholesaleRule()];
  }
  return source.map((item: Record<string, any>) => ({
    id: String(item.id || "").trim(),
    skuId: String(item.skuId || "").trim(),
    num: Number(item.num || 0),
    price: Number(item.price || 0)
  }));
}

async function initializeForm() {
  resetForm();
  loading.value = true;
  try {
    await ensureOptionsLoaded();
    const source = props.goodsData || {};
    if (props.mode === "create") {
      return;
    }
    form.storeId = String(source.storeId || "").trim();
    form.goodsName = String(source.goodsName || "").trim();
    form.sellingPoint = String(source.sellingPoint || "").trim();
    form.categoryPath = splitCommaValues(source.categoryPath);
    form.goodsUnit = String(source.goodsUnit || "").trim();
    form.templateId = String(source.templateId || "").trim();
    form.salesModel = String(source.salesModel || "RETAIL")
      .trim()
      .toUpperCase();
    form.goodsType = String(source.goodsType || "PHYSICAL_GOODS").trim();
    form.release = String(source.marketEnable || "").toUpperCase() === "UPPER";
    form.recommend = Boolean(source.recommend);
    form.barcode = String(source.barcode || "").trim();
    form.intro = String(source.intro || "").trim();
    form.mobileIntro = String(source.mobileIntro || "").trim();
    form.goodsGalleryList = Array.isArray(source.goodsGalleryList)
      ? source.goodsGalleryList.map((item: unknown) =>
          String(item || "").trim()
        )
      : [""];
    form.skuList = normalizeSkuRows(source.skuList);
    form.wholesaleList = isWholesaleGoods.value
      ? normalizeWholesaleRows(source.wholesaleList)
      : [];
    await loadFreightOptions(form.storeId);
    if (categoryLeafId.value) {
      await loadCategoryParams(categoryLeafId.value, source);
    }
  } catch (error: any) {
    message(getErrorMessage(error, "商品编辑器初始化失败"), {
      type: "error"
    });
  } finally {
    loading.value = false;
  }
}

watch(
  () => visible.value,
  async value => {
    if (value) {
      await initializeForm();
    } else {
      resetForm();
    }
  }
);

watch(
  () => form.storeId,
  async (value, oldValue) => {
    if (!visible.value || value === oldValue) {
      return;
    }
    await loadFreightOptions(value);
  }
);

watch(
  () => categoryLeafId.value,
  async (value, oldValue) => {
    if (!visible.value || !value || value === oldValue) {
      return;
    }
    await loadCategoryParams(value, props.goodsData || {});
  }
);

watch(
  () => form.salesModel,
  value => {
    if (value === "WHOLESALE" && !form.wholesaleList.length) {
      form.wholesaleList = [createEmptyWholesaleRule()];
    }
    if (value !== "WHOLESALE") {
      form.wholesaleList = [];
    }
  }
);

function addGalleryUrl(value = "") {
  form.goodsGalleryList.push(value);
}

function removeGalleryUrl(index: number) {
  if (form.goodsGalleryList.length === 1) {
    form.goodsGalleryList.splice(index, 1, "");
    return;
  }
  form.goodsGalleryList.splice(index, 1);
}

function addSku() {
  form.skuList.push(createEmptySku());
}

function removeSku(index: number) {
  if (form.skuList.length === 1) {
    form.skuList.splice(index, 1, createEmptySku());
    return;
  }
  form.skuList.splice(index, 1);
}

function addSkuSpecEntry(skuIndex: number) {
  form.skuList[skuIndex]?.specEntries.push({
    specName: "",
    specValue: ""
  });
}

function removeSkuSpecEntry(skuIndex: number, specIndex: number) {
  const specEntries = form.skuList[skuIndex]?.specEntries;
  if (!specEntries) {
    return;
  }
  if (specEntries.length === 1) {
    specEntries.splice(specIndex, 1, { specName: "", specValue: "" });
    return;
  }
  specEntries.splice(specIndex, 1);
}

function getCompletedSpecEntries(sku: SkuEntry) {
  return sku.specEntries
    .map(spec => ({
      specName: spec.specName.trim(),
      specValue: spec.specValue.trim()
    }))
    .filter(spec => spec.specName && spec.specValue);
}

function buildSpecSignature(
  specEntries: Array<{ specName: string; specValue: string }>
) {
  return specEntries
    .map(spec => `${spec.specName.trim()}:${spec.specValue.trim()}`)
    .sort()
    .join("|");
}

function addSpecDraft() {
  specDrafts.value.push(createEmptySpecDraft());
}

function removeSpecDraft(index: number) {
  if (specDrafts.value.length === 1) {
    specDrafts.value.splice(index, 1, createEmptySpecDraft());
    return;
  }
  specDrafts.value.splice(index, 1);
}

function generateSkuFromSpecDrafts() {
  const drafts = specDrafts.value.map(item => ({
    name: item.name.trim(),
    values: splitCommaValues(item.valuesText)
  }));
  if (!drafts.some(item => item.name || item.values.length)) {
    message("请先填写至少一组规格名和规格值", { type: "warning" });
    return;
  }
  const invalidDraft = drafts.find(
    item => Boolean(item.name) !== Boolean(item.values.length)
  );
  if (invalidDraft) {
    message("规格模板里的规格名和规格值需要成对填写", {
      type: "warning"
    });
    return;
  }
  const validDrafts = drafts.filter(item => item.name && item.values.length);
  const duplicatedNames = validDrafts.map(item => item.name);
  if (new Set(duplicatedNames).size !== duplicatedNames.length) {
    message("规格模板里的规格名不能重复", { type: "warning" });
    return;
  }

  const combinations = validDrafts.reduce<
    Array<Array<{ specName: string; specValue: string }>>
  >(
    (current, draft) => {
      const next: Array<Array<{ specName: string; specValue: string }>> = [];
      current.forEach(combination => {
        draft.values.forEach(value => {
          next.push([
            ...combination,
            { specName: draft.name, specValue: value }
          ]);
        });
      });
      return next;
    },
    [[]]
  );

  const existingMap = new Map(
    form.skuList.map(item => [
      buildSpecSignature(getCompletedSpecEntries(item)),
      item
    ])
  );

  form.skuList = combinations.map((specEntries, index) => {
    const signature = buildSpecSignature(specEntries);
    const existing = existingMap.get(signature);
    return {
      id: existing?.id || "",
      sn: existing?.sn || `SKU-${String(index + 1).padStart(3, "0")}`,
      barcode: existing?.barcode || "",
      price: Number(existing?.price || 0),
      cost: Number(existing?.cost || 0),
      weight: Number(existing?.weight || 0),
      quantity: Number(existing?.quantity || 0),
      alertQuantity: Number(existing?.alertQuantity || 0),
      specEntries
    };
  });

  message(`已按规格模板生成 ${form.skuList.length} 条 SKU`, {
    type: "success"
  });
}

function addWholesaleRule() {
  form.wholesaleList.push(createEmptyWholesaleRule());
}

function removeWholesaleRule(index: number) {
  if (form.wholesaleList.length === 1) {
    form.wholesaleList.splice(index, 1, createEmptyWholesaleRule());
    return;
  }
  form.wholesaleList.splice(index, 1);
}

function buildPayload() {
  const normalizedGalleryList = Array.from(
    new Set(form.goodsGalleryList.map(item => item.trim()).filter(Boolean))
  );

  const normalizedParams = goodsParams.value
    .map(item => ({
      paramId: item.paramId,
      paramName: item.paramName,
      paramValue: String(item.paramValue || "").trim(),
      required: item.required,
      isIndex: item.isIndex
    }))
    .filter(item => item.paramValue);

  const normalizedWholesaleList = isWholesaleGoods.value
    ? form.wholesaleList
        .map(item => ({
          id: item.id || undefined,
          skuId: item.skuId || undefined,
          goodsId: currentGoodsId.value || undefined,
          num: Number(item.num || 0),
          price: Number(item.price || 0)
        }))
        .filter(item => item.num > 0 && item.price > 0)
    : [];

  const skuPrice = isWholesaleGoods.value
    ? lowestWholesalePrice.value
    : lowestSkuPrice.value;
  const normalizedSkuList = form.skuList.map(item => {
    const payload: Record<string, any> = {
      id: item.id || undefined,
      sn: item.sn.trim(),
      barcode: item.barcode.trim() || undefined,
      price: isWholesaleGoods.value ? skuPrice : Number(item.price || 0),
      cost: isWholesaleGoods.value ? skuPrice : Number(item.cost || 0),
      quantity: Number(item.quantity || 0),
      weight: Number(item.weight || 0),
      alertQuantity: Number(item.alertQuantity || 0)
    };
    item.specEntries.forEach(spec => {
      const specName = spec.specName.trim();
      const specValue = spec.specValue.trim();
      if (specName && specValue) {
        payload[specName] = specValue;
      }
    });
    return payload;
  });

  return {
    storeId: form.storeId,
    goodsName: form.goodsName.trim(),
    barcode: form.barcode.trim(),
    intro: form.intro.trim(),
    mobileIntro: (form.mobileIntro || form.intro).trim(),
    quantity: totalQuantity.value,
    release: form.release,
    recommend: form.recommend,
    regeneratorSkuFlag: !isEdit.value,
    goodsParamsDTOList: normalizedParams,
    goodsGalleryList: normalizedGalleryList,
    templateId: isPhysicalGoods.value ? form.templateId : "0",
    sellingPoint: form.sellingPoint.trim(),
    salesModel: form.salesModel,
    haveSpec: normalizedSkuList.some(item =>
      Object.keys(item).some(
        key =>
          ![
            "id",
            "sn",
            "barcode",
            "price",
            "cost",
            "quantity",
            "weight",
            "alertQuantity"
          ].includes(key)
      )
    )
      ? "1"
      : "0",
    goodsUnit: form.goodsUnit.trim(),
    goodsType: form.goodsType,
    price: skuPrice,
    categoryPath: form.categoryPath.join(","),
    storeCategoryPath: categoryLeafId.value,
    skuList: normalizedSkuList,
    wholesaleList: normalizedWholesaleList
  };
}

function validateForm() {
  if (!form.storeId) {
    message("请选择归属店铺", { type: "warning" });
    return false;
  }
  if (!form.goodsName.trim()) {
    message("请输入商品名称", { type: "warning" });
    return false;
  }
  if (!form.categoryPath.length) {
    message("请选择商品分类", { type: "warning" });
    return false;
  }
  if (!form.goodsUnit.trim()) {
    message("请选择商品单位", { type: "warning" });
    return false;
  }
  if (isPhysicalGoods.value && !form.templateId) {
    message("实物商品必须选择运费模板", { type: "warning" });
    return false;
  }
  if (!form.goodsGalleryList.some(item => item.trim())) {
    message("请至少维护一张商品图片", { type: "warning" });
    return false;
  }
  if (!form.skuList.length) {
    message("至少需要一条 SKU", { type: "warning" });
    return false;
  }
  const invalidSku = form.skuList.find(
    item =>
      !item.sn.trim() ||
      (!isWholesaleGoods.value &&
        (Number(item.price) <= 0 || Number(item.cost) <= 0)) ||
      Number(item.quantity) < 0 ||
      (isPhysicalGoods.value && Number(item.weight) < 0)
  );
  if (invalidSku) {
    message(
      isWholesaleGoods.value
        ? "请补全 SKU 编码，并确认库存、重量合法"
        : "请补全 SKU 编码、零售价、成本价，并确认库存、重量合法",
      {
        type: "warning"
      }
    );
    return false;
  }
  if (!isWholesaleGoods.value && !form.skuList.some(item => item.price > 0)) {
    message("零售商品至少需要一条有效售价", {
      type: "warning"
    });
    return false;
  }
  const hasInvalidSpecEntry = form.skuList.some(item =>
    item.specEntries.some(spec => {
      const specName = spec.specName.trim();
      const specValue = spec.specValue.trim();
      return Boolean(specName) !== Boolean(specValue);
    })
  );
  if (hasInvalidSpecEntry) {
    message("规格名和规格值需要成对填写，不能只填一边", {
      type: "warning"
    });
    return false;
  }
  const hasDuplicateSpecNames = form.skuList.some(item => {
    const specNames = getCompletedSpecEntries(item).map(spec => spec.specName);
    return new Set(specNames).size !== specNames.length;
  });
  if (hasDuplicateSpecNames) {
    message("同一个 SKU 内的规格名不能重复", {
      type: "warning"
    });
    return false;
  }
  if (form.skuList.length > 1) {
    const hasSkuWithoutSpecs = form.skuList.some(
      item => !getCompletedSpecEntries(item).length
    );
    if (hasSkuWithoutSpecs) {
      message("多 SKU 商品请为每条 SKU 维护至少一组完整规格", {
        type: "warning"
      });
      return false;
    }
    const skuSpecSignatures = form.skuList.map(item =>
      getCompletedSpecEntries(item)
        .map(spec => `${spec.specName}:${spec.specValue}`)
        .sort()
        .join("|")
    );
    if (new Set(skuSpecSignatures).size !== skuSpecSignatures.length) {
      message("SKU 规格组合不能重复", { type: "warning" });
      return false;
    }
  }
  const invalidRequiredParam = goodsParams.value.find(
    item => Number(item.required) === 1 && !String(item.paramValue || "").trim()
  );
  if (invalidRequiredParam) {
    message(`请填写必填参数「${invalidRequiredParam.paramName}」`, {
      type: "warning"
    });
    return false;
  }
  if (isWholesaleGoods.value) {
    if (!form.wholesaleList.length) {
      message("批发商品请至少维护一条阶梯批发价", { type: "warning" });
      return false;
    }
    const invalidWholesale = form.wholesaleList.find(
      item => Number(item.num || 0) <= 0 || Number(item.price || 0) <= 0
    );
    if (invalidWholesale) {
      message("阶梯批发价里的起批量和单价都必须大于 0", {
        type: "warning"
      });
      return false;
    }
  }
  return true;
}

async function handleSubmit() {
  if (!validateForm()) {
    return;
  }
  saving.value = true;
  try {
    const payload = buildPayload();
    if (isEdit.value && currentGoodsId.value) {
      await updateWholesaleGoods(currentGoodsId.value, payload);
      message("商品更新成功", { type: "success" });
    } else {
      await createWholesaleGoods(payload);
      message("商品创建成功", { type: "success" });
    }
    emit("saved");
    visible.value = false;
  } catch (error: any) {
    message(getErrorMessage(error, "商品保存失败，请检查必填项和接口契约"), {
      type: "error"
    });
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="980px"
    destroy-on-close
  >
    <div v-loading="loading" class="goods-editor">
      <el-alert
        v-if="mode === 'create'"
        type="info"
        :closable="false"
        title="零售商品不再强制维护批发规则；批发商品可维护阶梯批发价。新增时支持先配规格模板，再自动编排 SKU 组合。"
      />

      <el-form label-width="100px">
        <div class="goods-editor__grid">
          <el-form-item label="归属店铺" required>
            <el-select
              v-model="form.storeId"
              filterable
              placeholder="请选择店铺"
            >
              <el-option
                v-for="item in storeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="商品分类" required>
            <el-cascader
              v-model="form.categoryPath"
              :options="categoryOptions"
              :props="{ checkStrictly: false, emitPath: true }"
              filterable
              clearable
              placeholder="请选择分类"
            />
          </el-form-item>
          <el-form-item label="商品名称" required>
            <el-input
              v-model="form.goodsName"
              maxlength="50"
              show-word-limit
              placeholder="请输入商品名称"
            />
          </el-form-item>
          <el-form-item label="商品单位" required>
            <el-select v-model="form.goodsUnit" filterable allow-create>
              <el-option
                v-for="item in goodsUnitOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="商品类型">
            <el-radio-group v-model="form.goodsType">
              <el-radio value="PHYSICAL_GOODS">实物商品</el-radio>
              <el-radio value="VIRTUAL_GOODS">虚拟商品</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="销售模式">
            <el-radio-group v-model="form.salesModel">
              <el-radio value="RETAIL">零售商品</el-radio>
              <el-radio value="WHOLESALE">批发商品</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="运费模板" required>
            <el-select
              v-model="form.templateId"
              :disabled="!isPhysicalGoods"
              :loading="freightLoading"
              placeholder="请选择运费模板"
            >
              <el-option
                v-for="item in freightOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="商品条码">
            <div class="goods-editor__field-stack">
              <el-input
                v-model="form.barcode"
                maxlength="64"
                placeholder="用于扫码枪、仓储识别，可不填"
              />
              <span class="goods-editor__hint">
                商品条码是整件商品的主识别码，不是必填；如果每个 SKU
                都有独立条码，可只填 SKU 条码。
              </span>
            </div>
          </el-form-item>
          <el-form-item label="卖点">
            <el-input
              v-model="form.sellingPoint"
              maxlength="60"
              show-word-limit
              placeholder="请输入卖点"
            />
          </el-form-item>
          <el-form-item label="发布状态">
            <el-switch
              v-model="form.release"
              active-text="立即上架"
              inactive-text="先存为下架"
            />
          </el-form-item>
          <el-form-item label="推荐商品">
            <el-switch v-model="form.recommend" />
          </el-form-item>
        </div>

        <el-form-item label="商品图片" required>
          <div class="goods-editor__block">
            <div
              v-for="(image, index) in form.goodsGalleryList"
              :key="`gallery-${index}`"
              class="goods-editor__gallery-row"
            >
              <ImageUploadField
                v-model="form.goodsGalleryList[index]"
                directory-path="goods-gallery"
                tip="请直接上传商品图片，上传成功后会自动回填地址并显示预览。"
              />
              <el-button type="danger" plain @click="removeGalleryUrl(index)">
                删除
              </el-button>
            </div>
            <el-button plain @click="addGalleryUrl()">新增图片位</el-button>
          </div>
        </el-form-item>

        <el-form-item label="商品参数">
          <div v-loading="paramLoading" class="goods-editor__block">
            <el-alert
              type="info"
              :closable="false"
              title="商品参数是分类绑定的模板字段，例如材质、产地、保质期。只有分类先绑定了参数模板，这里才会自动出现。"
            />
            <div v-if="goodsParams.length" class="goods-editor__params-grid">
              <el-form-item
                v-for="item in goodsParams"
                :key="item.paramId || item.paramName"
                :label="item.paramName"
                class="goods-editor__inner-item"
                :required="Number(item.required) === 1"
              >
                <el-select
                  v-model="item.paramValue"
                  filterable
                  allow-create
                  clearable
                  default-first-option
                  :reserve-keyword="false"
                  placeholder="请选择或输入参数值"
                >
                  <el-option
                    v-for="option in item.optionList"
                    :key="option"
                    :label="option"
                    :value="option"
                  />
                </el-select>
              </el-form-item>
            </div>
            <el-empty
              v-else
              description="当前分类还没有绑定参数模板，所以这里为空；请先到商品列表右上角的“参数绑定”里新增参数，并在参数表单中勾选关联分类。"
            />
          </div>
        </el-form-item>

        <el-form-item v-if="!isEdit" label="规格模板">
          <div class="goods-editor__block">
            <el-alert
              type="info"
              :closable="false"
              title="先填规格名和规格值列表，例如 颜色 = 红色,蓝色；尺寸 = S,M。点击自动生成后，系统会帮你编排出全部 SKU 组合。"
            />
            <div
              v-for="(draft, index) in specDrafts"
              :key="draft.id"
              class="goods-editor__spec-template-row"
            >
              <el-input v-model="draft.name" placeholder="规格名，例如 颜色" />
              <el-input
                v-model="draft.valuesText"
                placeholder="规格值，多个用逗号分隔，例如 红色,蓝色"
              />
              <el-button type="danger" plain @click="removeSpecDraft(index)">
                删除
              </el-button>
            </div>
            <div class="goods-editor__toolbar">
              <el-button plain @click="addSpecDraft">新增规格组</el-button>
              <el-button
                type="primary"
                plain
                @click="generateSkuFromSpecDrafts"
              >
                自动生成 SKU
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="SKU 列表" required>
          <div class="goods-editor__block">
            <el-alert
              v-if="isEdit && form.skuList.length > 1"
              type="warning"
              :closable="false"
              title="当前商品存在多个 SKU，编辑时将保留现有规格组合，只更新当前维护的数值字段。"
            />
            <div
              v-for="(sku, index) in form.skuList"
              :key="sku.id || `sku-${index}`"
              class="goods-editor__sku-card"
            >
              <div class="goods-editor__sku-head">
                <h4>SKU {{ index + 1 }}</h4>
                <div class="goods-editor__sku-actions">
                  <span v-if="sku.id">ID: {{ sku.id }}</span>
                  <el-button
                    v-if="!isEdit"
                    link
                    type="danger"
                    @click="removeSku(index)"
                  >
                    删除 SKU
                  </el-button>
                </div>
              </div>
              <div class="goods-editor__grid">
                <el-form-item label="SKU 编码" required>
                  <el-input
                    v-model="sku.sn"
                    maxlength="30"
                    placeholder="请输入 SKU 编码"
                  />
                </el-form-item>
                <el-form-item label="SKU 条码">
                  <div class="goods-editor__field-stack">
                    <el-input
                      v-model="sku.barcode"
                      maxlength="64"
                      placeholder="SKU 的扫码条码，可不填"
                    />
                    <span class="goods-editor__hint">
                      如果不同规格有不同包装条码，请填在这里；没有独立条码可以留空。
                    </span>
                  </div>
                </el-form-item>
                <el-form-item v-if="!isWholesaleGoods" label="零售价">
                  <el-input-number
                    v-model="sku.price"
                    :min="0.01"
                    :max="99999999"
                    :precision="2"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item v-if="!isWholesaleGoods" label="成本价">
                  <el-input-number
                    v-model="sku.cost"
                    :min="0.01"
                    :max="99999999"
                    :precision="2"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="库存">
                  <el-input-number
                    v-model="sku.quantity"
                    :min="0"
                    :max="99999999"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="预警库存">
                  <el-input-number
                    v-model="sku.alertQuantity"
                    :min="0"
                    :max="99999999"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="重量">
                  <el-input-number
                    v-model="sku.weight"
                    :disabled="!isPhysicalGoods"
                    :min="0"
                    :max="99999999"
                    :precision="2"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
              <div class="goods-editor__spec-list">
                <div
                  v-for="(spec, specIndex) in sku.specEntries"
                  :key="`${sku.id || index}-${specIndex}`"
                  class="goods-editor__spec-row"
                >
                  <el-input
                    v-model="spec.specName"
                    :disabled="isEdit"
                    placeholder="规格名，例如 颜色"
                  />
                  <el-input
                    v-model="spec.specValue"
                    :disabled="isEdit"
                    placeholder="规格值，例如 红色"
                  />
                  <el-button
                    v-if="!isEdit"
                    link
                    type="danger"
                    @click="removeSkuSpecEntry(index, specIndex)"
                  >
                    删除
                  </el-button>
                </div>
                <el-button
                  v-if="!isEdit"
                  plain
                  size="small"
                  @click="addSkuSpecEntry(index)"
                >
                  新增规格项
                </el-button>
              </div>
            </div>
            <el-button v-if="!isEdit" plain @click="addSku">新增 SKU</el-button>
          </div>
        </el-form-item>

        <el-form-item v-if="isWholesaleGoods" label="阶梯批发价" required>
          <div class="goods-editor__block">
            <el-alert
              type="info"
              :closable="false"
              title="这里不是简单的两个数字，而是“买满多少件，单价是多少”。例如：满 1 件 5.00 元，满 10 件 4.50 元。"
            />
            <div
              v-for="(rule, index) in form.wholesaleList"
              :key="rule.id || `rule-${index}`"
              class="goods-editor__rule-row"
            >
              <span class="goods-editor__rule-label">满</span>
              <el-input-number
                v-model="rule.num"
                :min="1"
                :max="99999999"
                placeholder="起批量"
              />
              <span class="goods-editor__rule-label">件，单价</span>
              <el-input-number
                v-model="rule.price"
                :min="0.01"
                :max="99999999"
                :precision="2"
                placeholder="批发价"
              />
              <span class="goods-editor__rule-label">元</span>
              <el-button
                type="danger"
                plain
                @click="removeWholesaleRule(index)"
              >
                删除
              </el-button>
            </div>
            <el-button plain @click="addWholesaleRule">新增阶梯</el-button>
          </div>
        </el-form-item>

        <el-form-item label="PC 详情">
          <el-input
            v-model="form.intro"
            type="textarea"
            :rows="4"
            placeholder="支持直接输入 HTML 或纯文本"
          />
        </el-form-item>
        <el-form-item label="移动端详情">
          <el-input
            v-model="form.mobileIntro"
            type="textarea"
            :rows="4"
            placeholder="为空时默认复用 PC 详情"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.goods-editor {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.goods-editor__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.goods-editor__block {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.goods-editor__gallery-row,
.goods-editor__spec-row,
.goods-editor__spec-template-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: center;
}

.goods-editor__gallery-row {
  grid-template-columns: minmax(0, 1fr) auto;
}

.goods-editor__rule-row {
  display: grid;
  grid-template-columns: auto 140px auto 160px auto auto;
  gap: 12px;
  align-items: center;
}

.goods-editor__spec-row {
  grid-template-columns: 180px minmax(0, 1fr) auto;
}

.goods-editor__spec-template-row {
  grid-template-columns: 180px minmax(0, 1fr) auto;
}

.goods-editor__sku-card {
  padding: 16px;
  background: #fffaf5;
  border: 1px solid #f2e4d6;
  border-radius: 14px;
}

.goods-editor__sku-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.goods-editor__sku-head h4 {
  margin: 0;
  font-size: 15px;
}

.goods-editor__sku-head span {
  color: #8a8f97;
  font-size: 12px;
}

.goods-editor__sku-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.goods-editor__field-stack {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.goods-editor__hint {
  color: #8a8f97;
  font-size: 12px;
  line-height: 1.5;
}

.goods-editor__params-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.goods-editor__inner-item {
  margin-bottom: 0;
}

.goods-editor__spec-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.goods-editor__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.goods-editor__rule-label {
  color: #7a5f48;
  font-size: 13px;
  white-space: nowrap;
}
</style>
