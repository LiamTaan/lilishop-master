<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  auditWholesaleGoods,
  createGoodsUnit,
  deleteGoodsUnits,
  getGoodsUnitDetail,
  getGoodsUnitPage,
  getWholesaleGoodsDetail,
  getWholesaleGoodsPage,
  underWholesaleGoods,
  updateGoodsUnit,
  upperWholesaleGoods
} from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime,
  getGoodsAuditStatusLabel,
  getGoodsMarketStatusLabel,
  getGoodsTypeLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "GoodsManage"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailData = ref<Record<string, any> | null>(null);
const currentRow = ref<Record<string, any> | null>(null);
const goodsUnitVisible = ref(false);
const goodsUnitLoading = ref(false);
const goodsUnitSaving = ref(false);
const goodsUnitFormVisible = ref(false);
const goodsUnitRows = ref<Record<string, any>[]>([]);
const goodsUnitEditingId = ref("");
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  goodsCode: "",
  goodsType: "",
  specText: "",
  dateRange: [] as string[]
});
const goodsUnitForm = reactive({
  name: ""
});

const summaryCards = computed<Array<{
  label: string;
  value: string | number;
  accent: "orange" | "blue" | "green" | "purple";
  hint: string;
}>>(() => {
  const total = filteredData.value.length;
  const approved = filteredData.value.filter(item =>
    String(item.authFlag).toUpperCase() === "PASS"
  ).length;
  const online = filteredData.value.filter(item =>
    String(item.marketEnable).toUpperCase() === "UPPER"
  ).length;
  const totalStock = filteredData.value.reduce(
    (sum, item) => sum + Number(item.stock || 0),
    0
  );
  return [
    { label: "商品总数", value: total, accent: "orange", hint: "当前筛选结果" },
    { label: "已通过商品", value: approved, accent: "green", hint: "审核通过的商品" },
    { label: "已上架商品", value: online, accent: "purple", hint: "当前正在销售" },
    { label: "库存件数", value: totalStock, accent: "blue", hint: "用于运营盘货" },
    {
      label: "治理动作",
      value: "已接入",
      accent: "orange",
      hint: "详情/审核/上架/下架"
    }
  ];
});

function normalizeGoodsRecord(item: Record<string, any>) {
  const salePrice = item.salePrice || item.price || item.originalPrice || 0;
  const costPrice = item.costPrice || item.cost || item.buyingPrice || salePrice;
  const authFlag = item.authFlag || item.auditStatus || "TOBEAUDITED";
  const marketEnable = item.marketEnable || item.goodsStatus || "DOWN";
  const deleteFlag = Boolean(item.deleteFlag);
  return {
    ...item,
    id: item.id || item.goodsId,
    goodsName: item.goodsName || item.name || "-",
    thumb:
      item.thumbnail ||
      item.goodsImage ||
      item.small ||
      "",
    storeName: item.storeName || item.storeNickName || "-",
    goodsCode: item.sn || item.goodsSn || item.goodsNo || item.id || "-",
    goodsType: item.goodsType || item.goodsUnit || item.goodsFormat || "",
    categoryName:
      item.categoryName ||
      item.categoryPathName ||
      item.goodsCategoryName ||
      "-",
    specText: item.specs || item.spec || item.unit || "1kg",
    costPrice,
    salePrice,
    profitAmount: Number(salePrice || 0) - Number(costPrice || 0),
    stock: item.quantity || item.stock || item.goodsStock || 0,
    marketEnable,
    authFlag,
    authMessage: item.authMessage || item.auditRemark || "",
    underMessage: item.underMessage || item.underReason || "",
    goodsGalleryList: item.goodsGalleryList || [],
    skuList: item.skuList || [],
    deleteFlag,
    createTime: item.createTime || item.updateTime || "-",
    canApprove: String(authFlag).toUpperCase() === "TOBEAUDITED",
    canReject: String(authFlag).toUpperCase() === "TOBEAUDITED",
    canUpper:
      String(authFlag).toUpperCase() === "PASS" &&
      String(marketEnable).toUpperCase() === "DOWN" &&
      !deleteFlag,
    canDown:
      String(authFlag).toUpperCase() === "PASS" &&
      String(marketEnable).toUpperCase() === "UPPER" &&
      !deleteFlag
  };
}

function normalizeGoodsUnitRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.goodsUnitId,
    name: item.name || "-",
    updateTime: formatAdminDateTime(item.updateTime || item.createTime)
  };
}

const filteredData = computed(() => {
  return data.value.filter(item => {
    const codeMatched = extraFilters.goodsCode
      ? String(item.goodsCode).includes(extraFilters.goodsCode)
      : true;
    const typeMatched = extraFilters.goodsType
      ? String(item.goodsType).includes(extraFilters.goodsType)
      : true;
    const specMatched = extraFilters.specText
      ? String(item.specText).includes(extraFilters.specText)
      : true;
    return codeMatched && typeMatched && specMatched;
  });
});

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 10
  };
  if (query.keyword) params.goodsName = query.keyword;
  if (query.status) params.goodsStatus = query.status;
  const res = await getWholesaleGoodsPage(params);
  const page = extractApiPayload(res);
  data.value = extractApiRecords(page).map(normalizeGoodsRecord);
}

async function loadGoodsUnits() {
  goodsUnitLoading.value = true;
  try {
    const res = await getGoodsUnitPage({
      pageNumber: 1,
      pageSize: 200
    });
    goodsUnitRows.value = extractApiRecords(res).map(normalizeGoodsUnitRecord);
  } catch (_error) {
    goodsUnitRows.value = [];
    message("商品单位加载失败，请稍后重试", { type: "error" });
  } finally {
    goodsUnitLoading.value = false;
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.goodsCode = "";
  extraFilters.goodsType = "";
  extraFilters.specText = "";
  extraFilters.dateRange = [];
  loadData();
}

async function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getWholesaleGoodsDetail(row.id);
    detailData.value = normalizeGoodsRecord(extractApiPayload(res) || row);
  } finally {
    detailLoading.value = false;
  }
}

async function handleAudit(row: Record<string, any>, authFlag: "PASS" | "REFUSE") {
  await ElMessageBox.confirm(
    `确认${authFlag === "PASS" ? "通过" : "驳回"}商品「${row.goodsName || row.id}」吗？`,
    `${authFlag === "PASS" ? "通过" : "驳回"}确认`,
    { type: authFlag === "PASS" ? "success" : "warning" }
  );
  await auditWholesaleGoods([row.id], authFlag);
  message(`商品已${authFlag === "PASS" ? "通过" : "驳回"}`, {
    type: "success"
  });
  await loadData();
}

async function handleUpper(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认上架商品「${row.goodsName || row.id}」吗？`,
    "上架确认",
    { type: "success" }
  );
  await upperWholesaleGoods([row.id]);
  message("商品已上架", { type: "success" });
  await loadData();
}

async function handleUnder(row: Record<string, any>) {
  const { value } = await ElMessageBox.prompt(
    `请输入商品「${row.goodsName || row.id}」的下架原因`,
    "下架确认",
    {
      confirmButtonText: "确认下架",
      cancelButtonText: "取消",
      inputPattern: /\S+/,
      inputErrorMessage: "下架原因不能为空"
    }
  );
  await underWholesaleGoods([row.id], value.trim());
  message("商品已下架", { type: "success" });
  await loadData();
}

function exportGoods() {
  if (!filteredData.value.length) {
    message("暂无可导出的商品数据", { type: "warning" });
    return;
  }
  const table = filteredData.value.map(item => ({
    商品标题: item.goodsName,
    店铺名称: item.storeName,
    商品编码: item.goodsCode,
    商品类型: getGoodsTypeLabel(item.goodsType),
    规格: item.specText,
    成本价: item.costPrice,
    售价: item.salePrice,
    库存: item.stock,
    销售状态: getGoodsMarketStatusLabel(item.marketEnable),
    审核状态: getGoodsAuditStatusLabel(item.authFlag),
    创建时间: item.createTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "商品列表");
  writeFile(workbook, "商品列表.xlsx");
  message("商品数据导出成功", { type: "success" });
}

function resetGoodsUnitForm() {
  goodsUnitEditingId.value = "";
  goodsUnitForm.name = "";
}

async function openGoodsUnitDialog() {
  goodsUnitVisible.value = true;
  await loadGoodsUnits();
}

function openGoodsUnitCreate() {
  resetGoodsUnitForm();
  goodsUnitFormVisible.value = true;
}

async function openGoodsUnitEdit(row: Record<string, any>) {
  try {
    const res = await getGoodsUnitDetail(String(row.id));
    const detail = normalizeGoodsUnitRecord(extractApiPayload(res) || row);
    goodsUnitEditingId.value = String(detail.id);
    goodsUnitForm.name = detail.name || "";
    goodsUnitFormVisible.value = true;
  } catch (_error) {
    message("商品单位详情加载失败", { type: "error" });
  }
}

async function submitGoodsUnit() {
  const name = goodsUnitForm.name.trim();
  if (!name) {
    message("请输入商品单位名称", { type: "warning" });
    return;
  }
  if (name.length > 5) {
    message("商品单位长度不能超过 5 个字符", { type: "warning" });
    return;
  }
  goodsUnitSaving.value = true;
  try {
    if (goodsUnitEditingId.value) {
      await updateGoodsUnit(goodsUnitEditingId.value, { name });
      message("商品单位更新成功", { type: "success" });
    } else {
      await createGoodsUnit({ name });
      message("商品单位新增成功", { type: "success" });
    }
    goodsUnitFormVisible.value = false;
    resetGoodsUnitForm();
    await loadGoodsUnits();
  } catch (_error) {
    message("商品单位保存失败，请确认管理端接口鉴权与字段契约", {
      type: "error"
    });
  } finally {
    goodsUnitSaving.value = false;
  }
}

async function handleGoodsUnitDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认删除商品单位「${row.name || row.id}」吗？`,
    "删除确认",
    { type: "warning" }
  );
  try {
    await deleteGoodsUnits([String(row.id)]);
    message("商品单位删除成功", { type: "success" });
    await loadGoodsUnits();
  } catch (_error) {
    message("商品单位删除失败，请确认是否已有商品正在使用该单位", {
      type: "error"
    });
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="商品列表"
    description="承接平台批发商品列表、审核、上下架和导出动作。"
    api-path="/manager/goods/goods/wholesale/list"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待审核', value: 'TOBEAUDITED' },
      { label: '已通过', value: 'PASS' },
      { label: '已驳回', value: 'REFUSE' },
      { label: '已上架', value: 'UPPER' },
      { label: '已下架', value: 'DOWN' }
    ]"
    status-label="商品状态"
    keyword-label="商品名称"
    keyword-placeholder="请输入商品名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" plain @click="openGoodsUnitDialog">
        单位管理
      </el-button>
      <el-button @click="exportGoods">导出</el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="商品编码">
        <el-input
          v-model="extraFilters.goodsCode"
          placeholder="请输入商品编码"
          clearable
        />
      </el-form-item>
      <el-form-item label="商品类型">
        <el-input
          v-model="extraFilters.goodsType"
          placeholder="请输入商品类型"
          clearable
        />
      </el-form-item>
      <el-form-item label="规格">
        <el-input
          v-model="extraFilters.specText"
          placeholder="请选择规格"
          clearable
        />
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button
        v-if="row.canApprove"
        link
        type="success"
        @click="handleAudit(row, 'PASS')"
      >
        通过
      </el-button>
      <el-button
        v-if="row.canReject"
        link
        type="danger"
        @click="handleAudit(row, 'REFUSE')"
      >
        驳回
      </el-button>
      <el-button
        v-if="row.canUpper"
        link
        type="success"
        @click="handleUpper(row)"
      >
        上架
      </el-button>
      <el-button
        v-if="row.canDown"
        link
        type="warning"
        @click="handleUnder(row)"
      >
        下架
      </el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="detailVisible"
    :title="`商品详情 - ${detailData?.goodsName || currentRow?.goodsName || '-'}`"
    width="820px"
  >
    <div v-loading="detailLoading" class="goods-detail">
      <div class="goods-detail__hero">
        <img
          v-if="detailData?.thumb"
          :src="detailData.thumb"
          :alt="detailData.goodsName || 'goods'"
          class="goods-detail__thumb"
        />
        <div v-else class="goods-detail__thumb goods-detail__thumb--empty">
          暂无图片
        </div>
        <div class="goods-detail__summary">
          <h3>{{ detailData?.goodsName || "-" }}</h3>
          <p>{{ detailData?.storeName || "-" }}</p>
          <div class="goods-detail__tags">
            <el-tag round effect="light">
              {{ getGoodsTypeLabel(detailData?.goodsType) }}
            </el-tag>
            <el-tag type="success" round effect="light">
              {{ getGoodsMarketStatusLabel(detailData?.marketEnable) }}
            </el-tag>
            <el-tag type="warning" round effect="light">
              {{ getGoodsAuditStatusLabel(detailData?.authFlag) }}
            </el-tag>
          </div>
        </div>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="商品编码">
          {{ detailData?.goodsCode || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="商品类型">
          {{ getGoodsTypeLabel(detailData?.goodsType) }}
        </el-descriptions-item>
        <el-descriptions-item label="规格">
          {{ detailData?.specText || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="分类">
          {{ detailData?.categoryName || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="售价">
          ¥ {{ detailData?.salePrice || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="成本价">
          ¥ {{ detailData?.costPrice || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="库存">
          {{ detailData?.stock || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detailData?.createTime || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="审核说明" :span="2">
          {{ detailData?.authMessage || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="下架原因" :span="2">
          {{ detailData?.underMessage || "-" }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="goodsUnitVisible"
    title="商品单位管理"
    width="720px"
  >
    <div class="goods-unit-panel">
      <el-alert
        title="本页单位管理固定调用管理端 /manager/goods/goodsUnit，不依赖 seller-api /store/**。"
        type="info"
        :closable="false"
      />
      <div class="goods-unit-panel__toolbar">
        <el-button type="primary" @click="openGoodsUnitCreate">
          新增单位
        </el-button>
      </div>
      <el-table v-loading="goodsUnitLoading" :data="goodsUnitRows" border>
        <el-table-column prop="name" label="单位名称" min-width="180" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openGoodsUnitEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleGoodsUnitDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <el-button @click="goodsUnitVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="goodsUnitFormVisible"
    :title="goodsUnitEditingId ? '编辑商品单位' : '新增商品单位'"
    width="460px"
    append-to-body
  >
    <el-form label-width="88px">
      <el-form-item label="单位名称" required>
        <el-input
          v-model="goodsUnitForm.name"
          maxlength="5"
          show-word-limit
          placeholder="请输入单位名称，例如 件 / 箱 / 瓶"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="goodsUnitFormVisible = false">取消</el-button>
      <el-button type="primary" :loading="goodsUnitSaving" @click="submitGoodsUnit">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
:deep(.goods-thumb) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.goods-thumb__img) {
  width: 66px;
  height: 66px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid #f1e5d9;
  background: #fff8f0;
}

:deep(.goods-thumb__placeholder) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 66px;
  height: 66px;
  color: #b08b6b;
  font-size: 12px;
  background: #fff8f0;
  border: 1px solid #f1e5d9;
  border-radius: 12px;
}

:deep(.goods-title__name) {
  color: #30343a;
  font-weight: 600;
  line-height: 1.6;
  word-break: break-all;
}

:deep(.goods-title__sub) {
  margin-top: 6px;
  color: #8a8f97;
  font-size: 12px;
}

:deep(.money-text) {
  color: #ff6e18;
  font-weight: 700;
}

:deep(.money-text--profit) {
  color: #ff5b39;
}

.goods-detail {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.goods-detail__hero {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

.goods-detail__thumb {
  width: 108px;
  height: 108px;
  object-fit: cover;
  border: 1px solid #f1e5d9;
  border-radius: 16px;
  background: #fff8f0;
}

.goods-detail__thumb--empty {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #9a7b5b;
  font-size: 13px;
}

.goods-detail__summary {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.goods-detail__summary h3 {
  margin: 0;
  color: #2f3135;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.5;
}

.goods-detail__summary p {
  margin: 0;
  color: #8a8f97;
}

.goods-detail__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.goods-unit-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.goods-unit-panel__toolbar {
  display: flex;
  justify-content: flex-end;
}
</style>
