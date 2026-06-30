<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import { getWholesaleSkuPage } from "@/api/goods-governance";
import {
  createKanjia,
  deleteKanjia,
  getKanjiaDetail,
  getKanjiaPage,
  updateKanjia
} from "@/api/marketing-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import {
  createKanjiaGoodsItem,
  formatDateTimeForField,
  formatMoney,
  normalizePromotionDetail,
  toNumber
} from "../shared/promotion-helpers";

defineOptions({
  name: "KanjiaManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const saving = ref(false);
const skuLoading = ref(false);
const editingId = ref("");
const editingBase = ref<Record<string, any>>({});
const skuOptions = ref<
  Array<{
    label: string;
    value: string;
    skuId: string;
    goodsId: string;
    goodsName: string;
    thumbnail: string;
    originalPrice: number;
  }>
>([]);
const query = reactive({
  keyword: "",
  status: ""
});
const kanjiaForm = reactive(createKanjiaForm());

const columns: TableColumnList = [
  { label: "活动名称", prop: "promotionName", minWidth: 220 },
  { label: "商品名称", prop: "goodsName", minWidth: 220 },
  { label: "活动库存", prop: "stock", minWidth: 100 },
  { label: "结算价", prop: "settlementPriceDisplay", minWidth: 120 },
  { label: "状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "操作", prop: "operation", fixed: "right", width: 260, slot: "operation" }
];

const filteredRows = computed(() =>
  rows.value.filter(item => {
    const keywordMatched = query.keyword
      ? [item.promotionName, item.goodsName].some(value =>
          String(value || "").includes(query.keyword)
        )
      : true;
    const statusMatched = query.status
      ? String(item.promotionStatus).toUpperCase() === query.status
      : true;
    return keywordMatched && statusMatched;
  })
);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

const summaryCards = computed(() => [
  {
    label: "活动总数",
    value: filteredRows.value.length,
    accent: "orange" as const,
    hint: "当前筛选结果"
  },
  {
    label: "进行中",
    value: filteredRows.value.filter(item => String(item.promotionStatus).toUpperCase() === "START").length,
    accent: "green" as const,
    hint: "当前有效砍价"
  },
  {
    label: "商品覆盖",
    value: [...new Set(filteredRows.value.map(item => item.goodsName))].length,
    accent: "blue" as const,
    hint: "参与活动商品数"
  },
  {
    label: "治理动作",
    value: "增改删/导出",
    accent: "purple" as const,
    hint: "已接入批量治理"
  }
]);

const detailFields = computed(() => {
  if (!currentRow.value) {
    return [];
  }
  const detail = currentRow.value;
  return [
    { label: "活动名称", value: detail.promotionName || "-" },
    { label: "商品名称", value: detail.goodsName || "-" },
    { label: "活动库存", value: detail.stock || 0 },
    { label: "原价", value: formatMoney(detail.originalPrice) },
    { label: "结算价", value: formatMoney(detail.settlementPrice) },
    { label: "最低购买价", value: formatMoney(detail.purchasePrice) },
    { label: "单次最低砍价", value: formatMoney(detail.lowestPrice) },
    { label: "单次最高砍价", value: formatMoney(detail.highestPrice) },
    { label: "开始时间", value: detail.startTimeText || "-" },
    { label: "结束时间", value: detail.endTimeText || "-" },
    { label: "状态", value: detail.promotionStatusLabel || "-" }
  ];
});

function normalizeSkuOption(item: Record<string, any>) {
  const skuId = String(item.id || item.skuId || "").trim();
  const goodsName = item.goodsName || item.name || "未命名商品";
  const goodsId = String(item.goodsId || "").trim();
  const simpleSpecs = item.simpleSpecs ? ` / ${item.simpleSpecs}` : "";
  return {
    label: `${goodsName}${simpleSpecs}`,
    value: skuId,
    skuId,
    goodsId,
    goodsName,
    thumbnail: item.thumbnail || "",
    originalPrice: toNumber(item.price || item.originalPrice)
  };
}

function mergeSkuOptions(list: Record<string, any>[]) {
  const optionMap = new Map(skuOptions.value.map(item => [item.value, item] as const));
  list
    .map(normalizeSkuOption)
    .filter(item => item.value)
    .forEach(item => optionMap.set(item.value, item));
  skuOptions.value = Array.from(optionMap.values());
}

async function searchPromotionSkus(keyword: string) {
  skuLoading.value = true;
  try {
    const response = await getWholesaleSkuPage({
      pageNumber: 1,
      pageSize: 50,
      salesModel: "WHOLESALE",
      goodsName: keyword.trim() || undefined
    });
    mergeSkuOptions(extractApiRecords(response));
  } catch (_error) {
    message("商品搜索失败", { type: "error" });
  } finally {
    skuLoading.value = false;
  }
}

function handlePromotionSkuChange(item: Record<string, any>, skuId: string) {
  const target = skuOptions.value.find(option => option.value === skuId);
  item.skuId = skuId;
  item.goodsId = target?.goodsId || "";
  item.goodsName = target?.goodsName || "";
  item.thumbnail = target?.thumbnail || "";
  if (!item.originalPrice) {
    item.originalPrice = target?.originalPrice || 0;
  }
}

function createKanjiaForm() {
  return {
    startTime: "",
    endTime: "",
    promotionGoodsList: [createKanjiaGoodsItem({ promotionName: "" })]
  };
}

function resetKanjiaForm() {
  Object.assign(kanjiaForm, createKanjiaForm());
}

function fillKanjiaForm(source: Record<string, any>) {
  Object.assign(kanjiaForm, {
    startTime: formatDateTimeForField(source.startTime),
    endTime: formatDateTimeForField(source.endTime),
    promotionGoodsList: [createKanjiaGoodsItem(source)]
  });
  mergeSkuOptions(kanjiaForm.promotionGoodsList);
}

function buildCreatePayload() {
  return {
    startTime: kanjiaForm.startTime,
    endTime: kanjiaForm.endTime,
    promotionGoodsList: kanjiaForm.promotionGoodsList.map(item => ({
      ...item,
      settlementPrice: toNumber(item.settlementPrice),
      purchasePrice: toNumber(item.purchasePrice),
      lowestPrice: toNumber(item.lowestPrice),
      highestPrice: toNumber(item.highestPrice),
      stock: toNumber(item.stock)
    }))
  };
}

function buildUpdatePayload() {
  const item = kanjiaForm.promotionGoodsList[0];
  return {
    ...editingBase.value,
    ...item,
    id: editingId.value,
    startTime: kanjiaForm.startTime,
    endTime: kanjiaForm.endTime,
    settlementPrice: toNumber(item.settlementPrice),
    purchasePrice: toNumber(item.purchasePrice),
    lowestPrice: toNumber(item.lowestPrice),
    highestPrice: toNumber(item.highestPrice),
    stock: toNumber(item.stock)
  };
}

function normalizeRow(item: Record<string, any>) {
  return {
    ...normalizePromotionDetail(item),
    promotionName: item.promotionName || "-",
    goodsName: item.goodsName || "-",
    stock: Number(item.stock ?? 0),
    settlementPriceDisplay: `¥ ${Number(item.settlementPrice ?? 0).toFixed(2)}`
  };
}

async function loadData() {
  try {
    const response = await getKanjiaPage({
      pageNumber: 1,
      pageSize: 200,
      promotionName: query.keyword || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("砍价活动列表加载失败", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword || "";
  query.status = payload.status || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  loadData();
}

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

function openCreate() {
  editingId.value = "";
  editingBase.value = {};
  resetKanjiaForm();
  searchPromotionSkus("");
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  try {
    const response = await getKanjiaDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(response) || row;
    editingId.value = String(row.id);
    editingBase.value = detail;
    fillKanjiaForm(detail);
    dialogVisible.value = true;
  } catch (_error) {
    message("砍价活动详情加载失败", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getKanjiaDetail(String(row.id));
    currentRow.value = normalizeRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("砍价活动详情加载失败", { type: "error" });
  }
}

async function submitPayload() {
  saving.value = true;
  try {
    if (editingId.value) {
      await updateKanjia(buildUpdatePayload());
      message("砍价活动更新成功", { type: "success" });
    } else {
      await createKanjia(buildCreatePayload());
      message("砍价活动创建成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("砍价活动保存失败，请检查表单内容", { type: "error" });
  } finally {
    saving.value = false;
  }
}

function addGoodsItem() {
  kanjiaForm.promotionGoodsList.push(createKanjiaGoodsItem());
}

function removeGoodsItem(index: number) {
  if (kanjiaForm.promotionGoodsList.length === 1) {
    kanjiaForm.promotionGoodsList.splice(0, 1, createKanjiaGoodsItem());
    return;
  }
  kanjiaForm.promotionGoodsList.splice(index, 1);
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除砍价活动「${row.promotionName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteKanjia([String(row.id)]);
    message("砍价活动删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("砍价活动删除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的砍价活动", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 个砍价活动吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await deleteKanjia(selectedIds.value);
    selectedRows.value = [];
    message("砍价活动批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("砍价活动批量删除失败", { type: "error" });
  }
}

function exportKanjia() {
  if (!filteredRows.value.length) {
    message("暂无可导出的砍价活动数据", { type: "warning" });
    return;
  }
  const table = filteredRows.value.map(item => ({
    活动名称: item.promotionName,
    商品名称: item.goodsName,
    活动库存: item.stock,
    结算价: item.settlementPriceDisplay,
    状态: item.promotionStatusLabel || getPromotionStatusLabel(item.promotionStatus),
    开始时间: item.startTimeText || "-",
    结束时间: item.endTimeText || "-"
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "砍价活动管理");
  writeFile(workbook, "砍价活动管理.xlsx");
  message("砍价活动数据导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="砍价活动管理"
    description="承接砍价活动列表、详情、创建、编辑和删除。"
    api-path="/manager/promotion/kanJiaGoods"
    :columns="columns"
    :data="filteredRows"
    selectable
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待开始', value: 'NEW' },
      { label: '进行中', value: 'START' },
      { label: '已结束', value: 'END' }
    ]"
    keyword-label="活动/商品"
    keyword-placeholder="请输入活动名称或商品名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportKanjia">导出</el-button>
      <el-button type="primary" @click="openCreate">新增砍价活动</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑砍价活动' : '新增砍价活动'"
    width="980px"
  >
    <el-form label-width="110px">
      <div class="form-grid">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="kanjiaForm.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="kanjiaForm.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </div>
      <section class="goods-section">
        <div class="section-header">
          <span>砍价商品</span>
          <el-button v-if="!editingId" type="primary" link @click="addGoodsItem">
            新增商品
          </el-button>
        </div>
        <div
          v-for="(item, index) in kanjiaForm.promotionGoodsList"
          :key="`kanjia-${index}`"
          class="goods-card"
        >
          <div class="section-header">
            <strong>商品 {{ index + 1 }}</strong>
            <el-button
              v-if="!editingId"
              type="danger"
              link
              @click="removeGoodsItem(index)"
            >
              移除
            </el-button>
          </div>
          <div class="form-grid">
            <el-form-item label="活动名称">
              <el-input v-model="item.promotionName" />
            </el-form-item>
            <el-form-item label="选择商品">
              <el-select
                :model-value="item.skuId"
                filterable
                remote
                clearable
                reserve-keyword
                :remote-method="searchPromotionSkus"
                :loading="skuLoading"
                :disabled="!!editingId"
                placeholder="请输入商品名称搜索后选择 SKU"
                style="width: 100%"
                @update:model-value="value => handlePromotionSkuChange(item, String(value || ''))"
              >
                <el-option
                  v-for="option in skuOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="商品名称">
              <el-input v-model="item.goodsName" disabled />
            </el-form-item>
            <el-form-item label="原价">
              <el-input-number
                v-model="item.originalPrice"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="活动库存">
              <el-input-number v-model="item.stock" :min="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="结算价">
              <el-input-number
                v-model="item.settlementPrice"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="最低购买价">
              <el-input-number
                v-model="item.purchasePrice"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="最低砍价">
              <el-input-number
                v-model="item.lowestPrice"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="最高砍价">
              <el-input-number
                v-model="item.highestPrice"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="缩略图">
              <el-input v-model="item.thumbnail" />
            </el-form-item>
          </div>
        </div>
      </section>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitPayload">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="砍价活动详情" size="56%">
    <el-descriptions v-if="currentRow" :column="2" border>
      <el-descriptions-item
        v-for="field in detailFields"
        :key="field.label"
        :label="field.label"
      >
        <div class="detail-text">{{ field.value }}</div>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

.goods-section {
  margin-top: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  background: #fafbfc;
}

.goods-card {
  margin-top: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #edf0f5;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-text {
  white-space: pre-wrap;
  word-break: break-word;
}

@media (width <= 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
