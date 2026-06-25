<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { getPlatformHomeConfig, savePlatformHomeConfig } from "@/api/home-config";
import {
  getCategoryPage,
  getWholesaleGoodsDetail,
  getWholesaleGoodsPage
} from "@/api/goods-governance";
import {
  createSpecial,
  deleteSpecial,
  getSpecialPage,
  updateSpecial
} from "@/api/super-admin";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { isSuccessResult } from "@/utils/result";

defineOptions({
  name: "ContentHomeConfig"
});

type BannerForm = {
  image: string;
  url: string;
};

type ShortcutForm = {
  id?: string;
  title: string;
  subtitle: string;
  image: string;
  linkType: string;
  linkValue: string;
  linkLabel?: string;
  sortOrder: number;
  displayStatus: string;
  remark: string;
};

type FloorForm = {
  title: string;
  subtitle: string;
  image: string;
  moduleType: string;
  sourceType: string;
  linkType: string;
  linkValue: string;
  linkLabel?: string;
  goodsLimit: number;
  sortOrder: number;
  displayStatus: string;
  specialId: string;
  specialName: string;
  remark: string;
};

type RuleBlock = {
  code: string;
  title: string;
  description: string;
  sourceMenu: string;
  sourceApi: string;
};

type SpecialRow = {
  id: string;
  displayName: string;
  displaySort: number;
  displayRemark: string;
  displayTime: string;
  name?: string;
  description?: string;
  sort?: number;
};

type SelectorKind = "category" | "goods" | "special";
type SelectorScope = "shortcut" | "floor-link" | "floor-source";

const clientTypeOptions = ["APP", "H5", "PC"];
const shortcutLinkTypeOptions = [
  { label: "分类页", value: "CATEGORY" },
  { label: "商品详情", value: "GOODS_DETAIL" },
  { label: "专题页", value: "SPECIAL" },
  { label: "活动页", value: "ACTIVITY" },
  { label: "自定义链接", value: "CUSTOM_URL" }
];
const floorModuleTypeOptions = [
  { label: "活动位", value: "ACTIVITY_SLOT" },
  { label: "推荐楼层", value: "RECOMMEND_FLOOR" }
];
const floorSourceTypeOptions = [
  { label: "专题页", value: "SPECIAL" },
  { label: "秒杀规则块", value: "SECKILL" },
  { label: "月度热卖", value: "HOT_GOODS" },
  { label: "上新推荐", value: "NEW_GOODS" },
  { label: "自定义跳转", value: "CUSTOM" }
];

function createBanner(): BannerForm {
  return {
    image: "",
    url: ""
  };
}

function createShortcut(): ShortcutForm {
  return {
    title: "",
    subtitle: "",
    image: "",
    linkType: "CATEGORY",
    linkValue: "",
    linkLabel: "",
    sortOrder: 0,
    displayStatus: "OPEN",
    remark: ""
  };
}

function createFloor(): FloorForm {
  return {
    title: "",
    subtitle: "",
    image: "",
    moduleType: "ACTIVITY_SLOT",
    sourceType: "CUSTOM",
    linkType: "CATEGORY",
    linkValue: "",
    linkLabel: "",
    goodsLimit: 6,
    sortOrder: 0,
    displayStatus: "OPEN",
    specialId: "",
    specialName: "",
    remark: ""
  };
}

const loading = ref(false);
const saving = ref(false);
const specialSaving = ref(false);
const specialDialogVisible = ref(false);
const specialEditingId = ref("");
const selectorKeyword = ref("");
const selectorVisible = ref(false);
const selectorLoading = ref(false);
const selectorKind = ref<SelectorKind>("category");
const selectorRows = ref<Record<string, any>[]>([]);
const selectorTarget = ref<{ scope: SelectorScope; index: number } | null>(null);

const categoryOptions = ref<Record<string, any>[]>([]);
const specialRows = ref<SpecialRow[]>([]);
const categoryNameMap = ref<Record<string, string>>({});
const specialNameMap = ref<Record<string, string>>({});
const goodsNameMap = ref<Record<string, string>>({});

const specialForm = reactive({
  name: "",
  description: "",
  sort: 0
});

const form = reactive({
  clientType: "APP",
  pageId: "",
  pageName: "",
  publishStatus: "CLOSE",
  legacySectionCount: 0,
  banners: [] as BannerForm[],
  topAdvert: createBanner(),
  shortcutNavList: [] as ShortcutForm[],
  floorModules: [] as FloorForm[],
  ruleBlocks: [] as RuleBlock[]
});

const summaryCards = computed(() => [
  {
    label: "轮播图",
    value: form.banners.length,
    hint: "APP 首页顶部轮播"
  },
  {
    label: "快捷入口",
    value: form.shortcutNavList.length,
    hint: "首页宫格入口"
  },
  {
    label: "楼层模块",
    value: form.floorModules.length,
    hint: "活动位与推荐楼层"
  },
  {
    label: "遗留段落",
    value: form.legacySectionCount,
    hint: "保留但不在此页编辑"
  }
]);

const selectorTitle = computed(() => {
  if (selectorKind.value === "category") return "选择分类";
  if (selectorKind.value === "goods") return "选择商品";
  return "选择专题";
});

const selectorEmptyText = computed(() => {
  if (selectorKind.value === "goods") return "请输入商品名称后搜索";
  return "暂无可选数据";
});

function normalizeSpecialRecord(item: Record<string, any>): SpecialRow {
  return {
    ...item,
    id: String(item.id || item.specialId || item.name || ""),
    displayName: item.name || item.specialName || "-",
    displaySort: Number(item.sort ?? 0),
    displayRemark: item.remark || item.description || "-",
    displayTime: formatAdminDateTime(item.updateTime || item.createTime),
    name: item.name || item.specialName || "",
    description: item.description || item.remark || "",
    sort: Number(item.sort ?? 0)
  };
}

function flattenCategories(
  list: Record<string, any>[],
  level = 0,
  bucket: Record<string, any>[] = []
) {
  list.forEach(item => {
    const record = {
      ...item,
      id: String(item.id || item.categoryId || ""),
      categoryName: item.name || item.categoryName || "-",
      levelValue: Number(item.level ?? item.grade ?? level),
      levelLabel: `${Number(item.level ?? item.grade ?? level) + 1}级`
    };
    bucket.push(record);
    const children = item.children || item.child || [];
    if (Array.isArray(children) && children.length) {
      flattenCategories(children, level + 1, bucket);
    }
  });
  return bucket;
}

function resetSpecialForm() {
  specialEditingId.value = "";
  specialForm.name = "";
  specialForm.description = "";
  specialForm.sort = 0;
}

function getLinkTypeLabel(linkType: string) {
  return (
    shortcutLinkTypeOptions.find(item => item.value === linkType)?.label ||
    linkType ||
    "-"
  );
}

function getSourceTypeLabel(sourceType: string) {
  return (
    floorSourceTypeOptions.find(item => item.value === sourceType)?.label ||
    sourceType ||
    "-"
  );
}

function getModuleTypeLabel(moduleType: string) {
  return (
    floorModuleTypeOptions.find(item => item.value === moduleType)?.label ||
    moduleType ||
    "-"
  );
}

function isRuleDrivenSource(sourceType: string) {
  return ["SECKILL", "HOT_GOODS", "NEW_GOODS"].includes(sourceType);
}

function updateCategoryNameMap() {
  const next: Record<string, string> = {};
  categoryOptions.value.forEach(item => {
    next[String(item.id)] = item.categoryName || "-";
  });
  categoryNameMap.value = next;
}

function updateSpecialNameMap() {
  const next: Record<string, string> = {};
  specialRows.value.forEach(item => {
    next[String(item.id)] = item.displayName || "-";
  });
  specialNameMap.value = next;
}

function resetFormState() {
  form.pageId = "";
  form.pageName = "";
  form.publishStatus = "CLOSE";
  form.legacySectionCount = 0;
  form.banners = [];
  form.topAdvert = createBanner();
  form.shortcutNavList = [];
  form.floorModules = [];
  form.ruleBlocks = [];
}

async function ensureCategoryOptions() {
  if (categoryOptions.value.length) return;
  const response = await getCategoryPage();
  const records = extractApiRecords(response);
  categoryOptions.value = flattenCategories(records);
  updateCategoryNameMap();
}

async function loadSpecialRows() {
  const response = await getSpecialPage();
  specialRows.value = extractApiRecords(response).map(normalizeSpecialRecord);
  updateSpecialNameMap();
}

async function ensureGoodsLabels(goodsIds: string[]) {
  const unresolved = Array.from(
    new Set(
      goodsIds
        .map(id => String(id || "").trim())
        .filter(id => id && !goodsNameMap.value[id])
    )
  );

  if (!unresolved.length) return;

  const entries = await Promise.all(
    unresolved.map(async id => {
      try {
        const response = await getWholesaleGoodsDetail(id);
        const payload = extractApiPayload<Record<string, any>>(response);
        return [
          id,
          payload?.goodsName || payload?.name || payload?.goodsId || id
        ] as const;
      } catch (_error) {
        return [id, id] as const;
      }
    })
  );

  goodsNameMap.value = {
    ...goodsNameMap.value,
    ...Object.fromEntries(entries)
  };
}

async function hydrateReferenceLabels() {
  const shortcutCategoryIds = form.shortcutNavList
    .filter(item => item.linkType === "CATEGORY" && item.linkValue)
    .map(item => item.linkValue);
  const floorCategoryIds = form.floorModules
    .filter(item => item.linkType === "CATEGORY" && item.linkValue)
    .map(item => item.linkValue);
  const shortcutSpecialIds = form.shortcutNavList
    .filter(item => item.linkType === "SPECIAL" && item.linkValue)
    .map(item => item.linkValue);
  const floorSpecialIds = form.floorModules
    .filter(item => item.linkType === "SPECIAL" && item.linkValue)
    .map(item => item.linkValue);
  const floorSourceSpecialIds = form.floorModules
    .filter(item => item.sourceType === "SPECIAL" && item.specialId)
    .map(item => item.specialId);
  const shortcutGoodsIds = form.shortcutNavList
    .filter(item => item.linkType === "GOODS_DETAIL" && item.linkValue)
    .map(item => item.linkValue);
  const floorGoodsIds = form.floorModules
    .filter(item => item.linkType === "GOODS_DETAIL" && item.linkValue)
    .map(item => item.linkValue);

  if (shortcutCategoryIds.length || floorCategoryIds.length) {
    await ensureCategoryOptions();
  }
  if (shortcutSpecialIds.length || floorSpecialIds.length || floorSourceSpecialIds.length) {
    await loadSpecialRows();
  }
  await ensureGoodsLabels([...shortcutGoodsIds, ...floorGoodsIds]);

  form.shortcutNavList.forEach(item => {
    if (item.linkType === "CATEGORY") {
      item.linkLabel = categoryNameMap.value[item.linkValue] || item.linkValue;
    } else if (item.linkType === "SPECIAL") {
      item.linkLabel = specialNameMap.value[item.linkValue] || item.linkValue;
    } else if (item.linkType === "GOODS_DETAIL") {
      item.linkLabel = goodsNameMap.value[item.linkValue] || item.linkValue;
    } else {
      item.linkLabel = item.linkValue;
    }
  });

  form.floorModules.forEach(item => {
    if (item.sourceType === "SPECIAL" && item.specialId) {
      item.specialName =
        item.specialName || specialNameMap.value[item.specialId] || item.specialId;
    }

    if (item.linkType === "CATEGORY") {
      item.linkLabel = categoryNameMap.value[item.linkValue] || item.linkValue;
    } else if (item.linkType === "SPECIAL") {
      item.linkLabel = specialNameMap.value[item.linkValue] || item.linkValue;
    } else if (item.linkType === "GOODS_DETAIL") {
      item.linkLabel = goodsNameMap.value[item.linkValue] || item.linkValue;
    } else {
      item.linkLabel = item.linkValue;
    }
  });
}

function applyConfig(payload: Record<string, any>) {
  resetFormState();
  form.pageId = payload.pageId || "";
  form.pageName = payload.pageName || "";
  form.publishStatus = payload.publishStatus || "CLOSE";
  form.legacySectionCount = Number(payload.legacySectionCount ?? 0);
  form.banners = Array.isArray(payload.banners)
    ? payload.banners.map((item: Record<string, any>) => ({
        image: item.image || "",
        url: item.url || ""
      }))
    : [];
  form.topAdvert = payload.topAdvert
    ? {
        image: payload.topAdvert.image || "",
        url: payload.topAdvert.url || ""
      }
    : createBanner();
  form.shortcutNavList = Array.isArray(payload.shortcutNavList)
    ? payload.shortcutNavList.map((item: Record<string, any>) => ({
        id: item.id || "",
        title: item.title || "",
        subtitle: item.subtitle || "",
        image: item.image || "",
        linkType: item.linkType || "CATEGORY",
        linkValue: item.linkValue || "",
        linkLabel: item.linkValue || "",
        sortOrder: Number(item.sortOrder ?? 0),
        displayStatus: item.displayStatus || "OPEN",
        remark: item.remark || ""
      }))
    : [];
  form.floorModules = Array.isArray(payload.floorModules)
    ? payload.floorModules.map((item: Record<string, any>) => ({
        title: item.title || "",
        subtitle: item.subtitle || "",
        image: item.image || "",
        moduleType: item.moduleType || "ACTIVITY_SLOT",
        sourceType: item.sourceType || "CUSTOM",
        linkType: item.linkType || "CATEGORY",
        linkValue: item.linkValue || "",
        linkLabel: item.linkValue || "",
        goodsLimit: Number(item.goodsLimit ?? 6),
        sortOrder: Number(item.sortOrder ?? 0),
        displayStatus: item.displayStatus || "OPEN",
        specialId: item.specialId || "",
        specialName: item.specialName || "",
        remark: item.remark || ""
      }))
    : [];
  form.ruleBlocks = Array.isArray(payload.ruleBlocks)
    ? payload.ruleBlocks.map((item: Record<string, any>) => ({
        code: item.code || "",
        title: item.title || "",
        description: item.description || "",
        sourceMenu: item.sourceMenu || "",
        sourceApi: item.sourceApi || ""
      }))
    : [];
}

async function loadConfig() {
  loading.value = true;
  try {
    const response = await getPlatformHomeConfig(form.clientType);
    const payload = extractApiPayload<Record<string, any>>(response) || {};
    applyConfig(payload);
    if (!form.pageName) {
      form.pageName = `${form.clientType}首页配置`;
    }
    await hydrateReferenceLabels();
  } catch (_error) {
    resetFormState();
    form.pageName = `${form.clientType}首页配置`;
    message("首页配置加载失败，请确认管理端接口已启动", { type: "error" });
  } finally {
    loading.value = false;
  }
}

function addBanner() {
  form.banners.push(createBanner());
}

function removeBanner(index: number) {
  form.banners.splice(index, 1);
}

function addShortcut() {
  form.shortcutNavList.push(createShortcut());
}

function removeShortcut(index: number) {
  form.shortcutNavList.splice(index, 1);
}

function addFloor() {
  form.floorModules.push(createFloor());
}

function removeFloor(index: number) {
  form.floorModules.splice(index, 1);
}

function handleShortcutLinkTypeChange(item: ShortcutForm) {
  item.linkValue = "";
  item.linkLabel = "";
}

function handleFloorLinkTypeChange(item: FloorForm) {
  item.linkValue = "";
  item.linkLabel = "";
}

function handleFloorSourceTypeChange(item: FloorForm) {
  if (item.sourceType !== "SPECIAL") {
    item.specialId = "";
    item.specialName = "";
  }
  if (isRuleDrivenSource(item.sourceType)) {
    item.linkType = "ACTIVITY";
    item.linkValue = "";
    item.linkLabel = "";
  } else if (!item.linkType) {
    item.linkType = "CATEGORY";
  }
}

function getLinkValuePlaceholder(linkType: string) {
  if (linkType === "CATEGORY") return "请选择分类";
  if (linkType === "GOODS_DETAIL") return "请选择商品";
  if (linkType === "SPECIAL") return "请选择专题";
  if (linkType === "ACTIVITY") return "请输入活动标识或活动链接";
  return "请输入完整跳转链接";
}

async function loadSelectorRows() {
  selectorLoading.value = true;
  try {
    if (selectorKind.value === "category") {
      await ensureCategoryOptions();
      const keyword = selectorKeyword.value.trim();
      selectorRows.value = keyword
        ? categoryOptions.value.filter(item =>
            String(item.categoryName).includes(keyword)
          )
        : categoryOptions.value;
      return;
    }

    if (selectorKind.value === "special") {
      if (!specialRows.value.length) {
        await loadSpecialRows();
      }
      const keyword = selectorKeyword.value.trim();
      selectorRows.value = keyword
        ? specialRows.value.filter(item =>
            String(item.displayName).includes(keyword)
          )
        : specialRows.value;
      return;
    }

    const response = await getWholesaleGoodsPage({
      pageNumber: 1,
      pageSize: 50,
      goodsName: selectorKeyword.value.trim() || undefined
    });
    selectorRows.value = extractApiRecords(response).map(
      (item: Record<string, any>) => ({
        ...item,
        id: String(item.id || item.goodsId || ""),
        goodsName: item.goodsName || item.name || "-",
        storeName: item.storeName || "-",
        marketEnable: item.marketEnable || item.goodsStatus || "-"
      })
    );
  } catch (_error) {
    selectorRows.value = [];
    message("关联数据加载失败，请稍后重试", { type: "error" });
  } finally {
    selectorLoading.value = false;
  }
}

async function openSelector(kind: SelectorKind, scope: SelectorScope, index: number) {
  selectorKind.value = kind;
  selectorTarget.value = { scope, index };
  selectorKeyword.value = "";
  selectorVisible.value = true;
  await loadSelectorRows();
}

function closeSelector() {
  selectorVisible.value = false;
  selectorTarget.value = null;
  selectorRows.value = [];
  selectorKeyword.value = "";
}

function applySelectedRow(row: Record<string, any>) {
  if (!selectorTarget.value) return;

  const { scope, index } = selectorTarget.value;
  const rowId = String(row.id || "");

  if (scope === "shortcut") {
    const target = form.shortcutNavList[index];
    if (!target) return;
    target.linkValue = rowId;
    target.linkLabel =
      row.categoryName || row.goodsName || row.displayName || rowId;
    closeSelector();
    return;
  }

  const target = form.floorModules[index];
  if (!target) return;

  if (scope === "floor-source") {
    target.specialId = rowId;
    target.specialName = row.displayName || row.name || rowId;
    target.sourceType = "SPECIAL";
    target.linkType = "SPECIAL";
    target.linkValue = rowId;
    target.linkLabel = target.specialName;
    closeSelector();
    return;
  }

  target.linkValue = rowId;
  target.linkLabel = row.categoryName || row.goodsName || row.displayName || rowId;
  if (target.linkType === "SPECIAL" && !target.specialId) {
    target.specialId = rowId;
    target.specialName = row.displayName || row.name || rowId;
  }
  closeSelector();
}

function buildSavePayload(publishNow: boolean) {
  return {
    clientType: form.clientType,
    pageName: form.pageName?.trim() || `${form.clientType}首页配置`,
    publishNow,
    banners: form.banners
      .filter(item => item.image.trim())
      .map(item => ({
        image: item.image.trim(),
        url: item.url.trim()
      })),
    topAdvert: {
      image: form.topAdvert.image.trim(),
      url: form.topAdvert.url.trim()
    },
    shortcutNavList: form.shortcutNavList.map(item => ({
      id: item.id || undefined,
      title: item.title.trim(),
      subtitle: item.subtitle.trim(),
      image: item.image.trim(),
      linkType: item.linkType,
      linkValue: item.linkValue.trim(),
      sortOrder: Number(item.sortOrder ?? 0),
      displayStatus: item.displayStatus || "OPEN",
      remark: item.remark.trim()
    })),
    floorModules: form.floorModules.map(item => ({
      title: item.title.trim(),
      subtitle: item.subtitle.trim(),
      image: item.image.trim(),
      moduleType: item.moduleType,
      sourceType: item.sourceType,
      linkType: item.linkType,
      linkValue: item.linkValue.trim(),
      goodsLimit: Number(item.goodsLimit ?? 6),
      sortOrder: Number(item.sortOrder ?? 0),
      displayStatus: item.displayStatus || "OPEN",
      specialId: item.specialId.trim(),
      specialName: item.specialName.trim(),
      remark: item.remark.trim()
    }))
  };
}

function validateBeforeSave() {
  if (!form.pageName.trim()) {
    message("请输入页面名称", { type: "warning" });
    return false;
  }

  const invalidShortcut = form.shortcutNavList.find(
    item => !item.title.trim() || !item.linkType || !item.linkValue.trim()
  );
  if (invalidShortcut) {
    message("快捷入口请至少填写标题、跳转类型和跳转值", {
      type: "warning"
    });
    return false;
  }

  const invalidFloor = form.floorModules.find(item => {
    if (!item.title.trim()) return true;
    if (item.sourceType === "SPECIAL" && !item.specialId.trim()) return true;
    if (!isRuleDrivenSource(item.sourceType) && !item.linkValue.trim()) return true;
    return false;
  });
  if (invalidFloor) {
    message("楼层模块请至少填写标题，并完成对应的专题或跳转绑定", {
      type: "warning"
    });
    return false;
  }

  return true;
}

async function submitConfig(publishNow: boolean) {
  if (!validateBeforeSave()) return;

  saving.value = true;
  try {
    const response = await savePlatformHomeConfig(buildSavePayload(publishNow));
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || "首页配置保存失败");
    }
    message(publishNow ? "首页配置已发布" : "首页配置草稿已保存", {
      type: "success"
    });
    await loadConfig();
  } catch (error: any) {
    message(error?.message || "首页配置保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

function openSpecialCreate() {
  resetSpecialForm();
  specialDialogVisible.value = true;
}

function openSpecialEdit(row: Record<string, any>) {
  const current = normalizeSpecialRecord(row);
  specialEditingId.value = current.id;
  specialForm.name = current.name || current.displayName || "";
  specialForm.description = current.description || current.displayRemark || "";
  specialForm.sort = Number(current.sort ?? current.displaySort ?? 0);
  specialDialogVisible.value = true;
}

async function submitSpecial() {
  if (!specialForm.name.trim()) {
    message("请输入专题名称", { type: "warning" });
    return;
  }

  specialSaving.value = true;
  try {
    const payload = {
      name: specialForm.name.trim(),
      description: specialForm.description.trim(),
      sort: Number(specialForm.sort ?? 0)
    };
    if (specialEditingId.value) {
      await updateSpecial(specialEditingId.value, payload);
      message("专题已更新", { type: "success" });
    } else {
      await createSpecial(payload);
      message("专题已新增", { type: "success" });
    }
    specialDialogVisible.value = false;
    await loadSpecialRows();
    await hydrateReferenceLabels();
  } catch (_error) {
    message("专题保存失败，请确认专题接口字段契约", { type: "error" });
  } finally {
    specialSaving.value = false;
  }
}

async function removeSpecial(row: Record<string, any>) {
  const current = normalizeSpecialRecord(row);
  await ElMessageBox.confirm(
    `确认删除专题「${current.displayName}」吗？`,
    "删除确认",
    { type: "warning" }
  );
  try {
    await deleteSpecial(current.id);
    message("专题已删除", { type: "success" });
    await loadSpecialRows();
    await hydrateReferenceLabels();
  } catch (_error) {
    message("专题删除失败", { type: "error" });
  }
}

onMounted(async () => {
  await loadSpecialRows();
  await loadConfig();
});
</script>

<template>
  <div v-loading="loading" class="home-config-page">
    <section class="home-config-page__hero">
      <div>
        <p class="home-config-page__eyebrow">批发 APP 首页维护</p>
        <h1>首页配置</h1>
        <p class="home-config-page__desc">
          这页直接维护 APP 首页轮播、顶部广告、快捷入口、楼层模块，并把专题作为可选活数据接入。
          秒杀、月度热卖、上新、常逛店铺仍由规则自动生成，不再让运营手改 JSON。
        </p>
      </div>
      <div class="home-config-page__actions">
        <el-button @click="loadConfig">重新加载</el-button>
        <el-button type="primary" :loading="saving" @click="submitConfig(false)">
          保存草稿
        </el-button>
        <el-button type="success" :loading="saving" @click="submitConfig(true)">
          保存并发布
        </el-button>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="card in summaryCards" :key="card.label" class="summary-card">
        <span class="summary-card__label">{{ card.label }}</span>
        <strong class="summary-card__value">{{ card.value }}</strong>
        <span class="summary-card__hint">{{ card.hint }}</span>
      </article>
    </section>

    <el-alert
      title="APP 映射：首页配置对应 APP 首页的轮播图、顶部广告、首页宫格入口和楼层活动位；帮助与公告对应 APP 帮助中心与公告页。"
      type="info"
      :closable="false"
      class="home-config-page__alert"
    />

    <el-alert
      v-if="form.legacySectionCount > 0"
      :title="`当前配置里仍有 ${form.legacySectionCount} 个遗留 section。保存时会保留这些数据，但本页不会直接编辑它们。`"
      type="warning"
      :closable="false"
      class="home-config-page__alert"
    />

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-card__header">
          <div>
            <h2>基础信息</h2>
            <p>管理当前客户端首页草稿与发布状态</p>
          </div>
          <el-tag :type="form.publishStatus === 'OPEN' ? 'success' : 'info'" round>
            {{ form.publishStatus === "OPEN" ? "当前已发布" : "当前为草稿" }}
          </el-tag>
        </div>
      </template>
      <div class="form-grid form-grid--basic">
        <el-form-item label="客户端">
          <el-select v-model="form.clientType" style="width: 100%" @change="loadConfig">
            <el-option
              v-for="item in clientTypeOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="页面名称">
          <el-input v-model="form.pageName" placeholder="请输入页面名称" />
        </el-form-item>
        <el-form-item label="页面ID">
          <el-input :model-value="form.pageId || '首次保存后生成'" disabled />
        </el-form-item>
        <el-form-item label="维护页面">
          <el-input model-value="APP 首页 / 轮播图 / 快捷入口 / 活动位" disabled />
        </el-form-item>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-card__header">
          <div>
            <h2>轮播图</h2>
            <p>对应 APP 首页顶部轮播，支持外链或内部路由标识</p>
          </div>
          <el-button type="primary" plain @click="addBanner">新增轮播图</el-button>
        </div>
      </template>
      <el-empty v-if="!form.banners.length" description="暂无轮播图，点击右上角新增" />
      <div v-else class="stack-list">
        <article v-for="(item, index) in form.banners" :key="index" class="editor-card">
          <div class="editor-card__toolbar">
            <strong>轮播图 {{ index + 1 }}</strong>
            <el-button link type="danger" @click="removeBanner(index)">删除</el-button>
          </div>
          <div class="form-grid">
            <el-form-item label="图片地址">
              <el-input v-model="item.image" placeholder="请输入轮播图地址" />
            </el-form-item>
            <el-form-item label="跳转值">
              <el-input v-model="item.url" placeholder="可填链接、路由标识或活动标识" />
            </el-form-item>
          </div>
          <div v-if="item.image" class="image-preview">
            <img :src="item.image" alt="banner" class="image-preview__img" />
          </div>
        </article>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-card__header">
          <div>
            <h2>顶部广告</h2>
            <p>对应轮播下方单张活动图，可用于平台招商或重点活动位</p>
          </div>
        </div>
      </template>
      <div class="form-grid">
        <el-form-item label="图片地址">
          <el-input v-model="form.topAdvert.image" placeholder="请输入顶部广告图片地址" />
        </el-form-item>
        <el-form-item label="跳转值">
          <el-input v-model="form.topAdvert.url" placeholder="请输入活动链接或页面标识" />
        </el-form-item>
      </div>
      <div v-if="form.topAdvert.image" class="image-preview">
        <img :src="form.topAdvert.image" alt="top advert" class="image-preview__img" />
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-card__header">
          <div>
            <h2>快捷入口</h2>
            <p>对应 APP 首页宫格入口，可直接关联分类、商品、专题等活数据</p>
          </div>
          <el-button type="primary" plain @click="addShortcut">新增入口</el-button>
        </div>
      </template>
      <el-empty v-if="!form.shortcutNavList.length" description="暂无快捷入口，点击右上角新增" />
      <div v-else class="stack-list">
        <article
          v-for="(item, index) in form.shortcutNavList"
          :key="item.id || index"
          class="editor-card"
        >
          <div class="editor-card__toolbar">
            <strong>快捷入口 {{ index + 1 }}</strong>
            <el-button link type="danger" @click="removeShortcut(index)">删除</el-button>
          </div>
          <div class="form-grid">
            <el-form-item label="标题">
              <el-input v-model="item.title" placeholder="例如：爆款专区" />
            </el-form-item>
            <el-form-item label="副标题">
              <el-input v-model="item.subtitle" placeholder="例如：本月热卖" />
            </el-form-item>
            <el-form-item label="图标地址">
              <el-input v-model="item.image" placeholder="请输入图标地址" />
            </el-form-item>
            <el-form-item label="跳转类型">
              <el-select
                v-model="item.linkType"
                style="width: 100%"
                @change="handleShortcutLinkTypeChange(item)"
              >
                <el-option
                  v-for="option in shortcutLinkTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="跳转值">
              <div class="compound-field">
                <el-input
                  v-model="item.linkValue"
                  :placeholder="getLinkValuePlaceholder(item.linkType)"
                />
                <el-button
                  v-if="item.linkType === 'CATEGORY'"
                  @click="openSelector('category', 'shortcut', index)"
                >
                  选择分类
                </el-button>
                <el-button
                  v-if="item.linkType === 'GOODS_DETAIL'"
                  @click="openSelector('goods', 'shortcut', index)"
                >
                  选择商品
                </el-button>
                <el-button
                  v-if="item.linkType === 'SPECIAL'"
                  @click="openSelector('special', 'shortcut', index)"
                >
                  选择专题
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="当前映射">
              <el-input
                :model-value="item.linkLabel || item.linkValue || '未选择'"
                disabled
              />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="item.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="展示状态">
              <el-switch
                v-model="item.displayStatus"
                active-value="OPEN"
                inactive-value="CLOSE"
                active-text="启用"
                inactive-text="停用"
              />
            </el-form-item>
            <el-form-item label="备注">
              <el-input
                v-model="item.remark"
                placeholder="记录该入口维护的 APP 页面或活动说明"
              />
            </el-form-item>
          </div>
        </article>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-card__header">
          <div>
            <h2>楼层模块 / 活动位</h2>
            <p>对应 APP 首页中部活动卡、专题楼层和推荐位</p>
          </div>
          <el-button type="primary" plain @click="addFloor">新增楼层</el-button>
        </div>
      </template>
      <el-empty v-if="!form.floorModules.length" description="暂无楼层模块，点击右上角新增" />
      <div v-else class="stack-list">
        <article v-for="(item, index) in form.floorModules" :key="index" class="editor-card">
          <div class="editor-card__toolbar">
            <div>
              <strong>楼层模块 {{ index + 1 }}</strong>
              <span class="editor-card__meta">
                {{ getModuleTypeLabel(item.moduleType) }} / {{ getSourceTypeLabel(item.sourceType) }}
              </span>
            </div>
            <el-button link type="danger" @click="removeFloor(index)">删除</el-button>
          </div>
          <div class="form-grid">
            <el-form-item label="标题">
              <el-input v-model="item.title" placeholder="例如：整箱团批" />
            </el-form-item>
            <el-form-item label="副标题">
              <el-input v-model="item.subtitle" placeholder="例如：满减直降专区" />
            </el-form-item>
            <el-form-item label="图片地址">
              <el-input v-model="item.image" placeholder="请输入楼层图片地址" />
            </el-form-item>
            <el-form-item label="模块类型">
              <el-select v-model="item.moduleType" style="width: 100%">
                <el-option
                  v-for="option in floorModuleTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="数据来源">
              <el-select
                v-model="item.sourceType"
                style="width: 100%"
                @change="handleFloorSourceTypeChange(item)"
              >
                <el-option
                  v-for="option in floorSourceTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="专题绑定">
              <div class="compound-field">
                <el-input
                  :model-value="item.specialName || '未绑定专题'"
                  :disabled="item.sourceType !== 'SPECIAL'"
                />
                <el-button
                  :disabled="item.sourceType !== 'SPECIAL'"
                  @click="openSelector('special', 'floor-source', index)"
                >
                  选择专题
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="跳转类型">
              <el-select
                v-model="item.linkType"
                style="width: 100%"
                :disabled="isRuleDrivenSource(item.sourceType)"
                @change="handleFloorLinkTypeChange(item)"
              >
                <el-option
                  v-for="option in shortcutLinkTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="跳转值">
              <div class="compound-field">
                <el-input
                  v-model="item.linkValue"
                  :disabled="isRuleDrivenSource(item.sourceType)"
                  :placeholder="getLinkValuePlaceholder(item.linkType)"
                />
                <el-button
                  v-if="item.linkType === 'CATEGORY' && !isRuleDrivenSource(item.sourceType)"
                  @click="openSelector('category', 'floor-link', index)"
                >
                  选择分类
                </el-button>
                <el-button
                  v-if="item.linkType === 'GOODS_DETAIL' && !isRuleDrivenSource(item.sourceType)"
                  @click="openSelector('goods', 'floor-link', index)"
                >
                  选择商品
                </el-button>
                <el-button
                  v-if="item.linkType === 'SPECIAL' && !isRuleDrivenSource(item.sourceType)"
                  @click="openSelector('special', 'floor-link', index)"
                >
                  选择专题
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="当前映射">
              <el-input
                :model-value="
                  isRuleDrivenSource(item.sourceType)
                    ? '规则自动生成，不手工指定列表'
                    : item.linkLabel || item.linkValue || '未选择'
                "
                disabled
              />
            </el-form-item>
            <el-form-item label="展示数量">
              <el-input-number v-model="item.goodsLimit" :min="1" :max="20" style="width: 100%" />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="item.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="展示状态">
              <el-switch
                v-model="item.displayStatus"
                active-value="OPEN"
                inactive-value="CLOSE"
                active-text="启用"
                inactive-text="停用"
              />
            </el-form-item>
            <el-form-item label="备注">
              <el-input
                v-model="item.remark"
                placeholder="可写该模块维护的 APP 页面、业务场景或联调说明"
              />
            </el-form-item>
          </div>
        </article>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-card__header">
          <div>
            <h2>规则型模块</h2>
            <p>这部分不是死数据，不通过 JSON 手工维护</p>
          </div>
        </div>
      </template>
      <div class="rule-grid">
        <article v-for="item in form.ruleBlocks" :key="item.code" class="rule-card">
          <strong>{{ item.title }}</strong>
          <p>{{ item.description }}</p>
          <span>来源菜单：{{ item.sourceMenu }}</span>
          <span>来源接口：{{ item.sourceApi }}</span>
        </article>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-card__header">
          <div>
            <h2>专题页台账</h2>
            <p>楼层和快捷入口引用的专题在这里维护，避免“只能填 ID、运营不知道是什么”的问题</p>
          </div>
          <el-button type="primary" plain @click="openSpecialCreate">新增专题</el-button>
        </div>
      </template>
      <el-table :data="specialRows" border>
        <el-table-column prop="displayName" label="专题名称" min-width="200" />
        <el-table-column prop="displaySort" label="排序" width="100" />
        <el-table-column prop="displayRemark" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="displayTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSpecialEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeSpecial(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>

  <el-dialog v-model="selectorVisible" :title="selectorTitle" width="860px">
    <div class="selector-toolbar">
      <el-input
        v-model="selectorKeyword"
        placeholder="请输入关键字"
        clearable
        @keyup.enter="loadSelectorRows"
      />
      <el-button type="primary" @click="loadSelectorRows">搜索</el-button>
    </div>
    <el-table v-loading="selectorLoading" :data="selectorRows" border height="420">
      <el-table-column
        v-if="selectorKind === 'category'"
        prop="categoryName"
        label="分类名称"
        min-width="220"
      />
      <el-table-column
        v-if="selectorKind === 'category'"
        prop="levelLabel"
        label="层级"
        width="100"
      />

      <el-table-column
        v-if="selectorKind === 'goods'"
        prop="goodsName"
        label="商品名称"
        min-width="260"
      />
      <el-table-column
        v-if="selectorKind === 'goods'"
        prop="storeName"
        label="店铺"
        min-width="180"
      />
      <el-table-column
        v-if="selectorKind === 'goods'"
        prop="marketEnable"
        label="状态"
        width="120"
      />

      <el-table-column
        v-if="selectorKind === 'special'"
        prop="displayName"
        label="专题名称"
        min-width="220"
      />
      <el-table-column
        v-if="selectorKind === 'special'"
        prop="displaySort"
        label="排序"
        width="100"
      />
      <el-table-column
        v-if="selectorKind === 'special'"
        prop="displayRemark"
        label="说明"
        min-width="220"
      />

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="applySelectedRow(row)">
            选择
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!selectorLoading && !selectorRows.length" :description="selectorEmptyText" />
    <template #footer>
      <el-button @click="closeSelector">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="specialDialogVisible"
    :title="specialEditingId ? '编辑专题' : '新增专题'"
    width="560px"
  >
    <el-form label-width="88px">
      <el-form-item label="专题名称" required>
        <el-input v-model="specialForm.name" placeholder="请输入专题名称" />
      </el-form-item>
      <el-form-item label="排序值">
        <el-input-number v-model="specialForm.sort" :min="0" style="width: 100%" />
      </el-form-item>
      <el-form-item label="专题说明">
        <el-input
          v-model="specialForm.description"
          type="textarea"
          :rows="4"
          placeholder="请输入专题说明"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="specialDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="specialSaving" @click="submitSpecial">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.home-config-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 20px;
  background:
    radial-gradient(circle at top right, rgba(245, 174, 76, 0.16), transparent 28%),
    linear-gradient(180deg, #fffaf2 0%, #ffffff 24%, #fffdf8 100%);
}

.home-config-page__hero {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24px 28px;
  border: 1px solid #f1e5d4;
  border-radius: 22px;
  background: linear-gradient(135deg, #fff6e7 0%, #fffdfa 54%, #fff3dc 100%);
  box-shadow: 0 12px 30px rgba(194, 126, 24, 0.08);
}

.home-config-page__eyebrow {
  margin: 0 0 8px;
  color: #b56a17;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.home-config-page__hero h1 {
  margin: 0;
  color: #2f3135;
  font-size: 30px;
  font-weight: 700;
}

.home-config-page__desc {
  max-width: 760px;
  margin: 12px 0 0;
  color: #6c7077;
  line-height: 1.8;
}

.home-config-page__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.home-config-page__alert {
  border-radius: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px 20px;
  border: 1px solid #f0e5d8;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
}

.summary-card__label {
  color: #8c7258;
  font-size: 13px;
}

.summary-card__value {
  color: #2f3135;
  font-size: 28px;
  line-height: 1;
}

.summary-card__hint {
  color: #8a8f97;
  font-size: 12px;
}

.panel-card {
  border-radius: 20px;
}

.panel-card__header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.panel-card__header h2 {
  margin: 0;
  color: #2f3135;
  font-size: 18px;
}

.panel-card__header p {
  margin: 6px 0 0;
  color: #8a8f97;
  line-height: 1.6;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px 20px;
}

.form-grid--basic {
  align-items: end;
}

.stack-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.editor-card {
  padding: 16px;
  border: 1px solid #f2e6d8;
  border-radius: 18px;
  background: #fffdf9;
}

.editor-card__toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.editor-card__meta {
  margin-left: 10px;
  color: #8a8f97;
  font-size: 12px;
}

.compound-field {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  width: 100%;
}

.image-preview {
  margin-top: 12px;
  padding: 12px;
  border: 1px dashed #f0d3ad;
  border-radius: 16px;
  background: #fff9f1;
}

.image-preview__img {
  max-width: 220px;
  max-height: 140px;
  object-fit: cover;
  border-radius: 12px;
}

.rule-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.rule-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border: 1px solid #ebeef4;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfd 100%);
}

.rule-card strong {
  color: #2f3135;
}

.rule-card p {
  margin: 0;
  color: #6c7077;
  line-height: 1.7;
}

.rule-card span {
  color: #8a8f97;
  font-size: 12px;
  word-break: break-all;
}

.selector-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  margin-bottom: 14px;
}

@media (max-width: 1100px) {
  .summary-grid,
  .rule-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .home-config-page__hero,
  .panel-card__header {
    flex-direction: column;
  }

  .home-config-page__actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .home-config-page {
    padding: 12px;
  }

  .compound-field {
    grid-template-columns: 1fr;
  }
}
</style>
