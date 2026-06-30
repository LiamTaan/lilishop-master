<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createFreightTemplate,
  deleteFreightTemplate,
  getAllStores,
  getFreightTemplateDetail,
  getFreightTemplateList,
  getRegionAllCityTree,
  updateFreightTemplate
} from "@/api/super-admin";
import { extractApiPayload } from "@/utils/admin-governance";
import { getErrorMessage } from "@/utils/error";
import { message } from "@/utils/message";

type StoreOption = {
  label: string;
  value: string;
};

type RuleForm = {
  id?: string;
  area: string;
  areaId: string;
  firstCompany: number;
  firstPrice: number;
  continuedCompany: number;
  continuedPrice: number;
  regionSelection: string[];
};

type TemplateRow = Record<string, any> & {
  id: string;
  storeId: string;
  storeName: string;
  displayName: string;
  displayPricingMethod: string;
  ruleCount: number;
  updateTime: string;
  freightTemplateChildList: RuleForm[];
};

const rows = ref<TemplateRow[]>([]);
const selectedStoreId = ref("");
const storeOptions = ref<StoreOption[]>([]);
const storeLoading = ref(false);
const regionOptions = ref<Record<string, any>[]>([]);
const regionLoading = ref(false);
const regionsLoaded = ref(false);
const regionNameMap = ref(new Map<string, string>());
const regionPathNameMap = ref(new Map<string, string>());
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<TemplateRow | null>(null);
const editingRow = ref<TemplateRow | null>(null);
const query = reactive({ keyword: "" });
const form = reactive({
  id: "",
  storeId: "",
  name: "",
  pricingMethod: "WEIGHT",
  freightTemplateChildList: [] as RuleForm[]
});

const regionCascaderProps = {
  value: "value",
  label: "label",
  children: "children",
  emitPath: true,
  checkStrictly: false
};

const columns: TableColumnList = [
  { label: "模板名称", prop: "displayName", minWidth: 200, showOverflowTooltip: true },
  { label: "归属店铺", prop: "storeName", minWidth: 220, showOverflowTooltip: true },
  { label: "计价方式", prop: "displayPricingMethod", minWidth: 120 },
  { label: "规则条数", prop: "ruleCount", minWidth: 100 },
  { label: "更新时间", prop: "updateTime", minWidth: 180 },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "模板总数", value: rows.value.length, accent: "orange" as const, hint: "当前店铺下的运费模板数量" },
  { label: "包邮模板", value: rows.value.filter(item => item.pricingMethod === "FREE").length, accent: "green" as const, hint: "计价方式为包邮的模板" },
  { label: "规则明细", value: rows.value.reduce((sum, item) => sum + item.ruleCount, 0), accent: "blue" as const, hint: "已配置的区域规则总条数" },
  { label: "治理动作", value: "查询/新增/编辑/删除", accent: "purple" as const, hint: "平台统一维护店铺模板" }
]);

const dialogTitle = computed(() =>
  editingRow.value ? "编辑运费模板" : "新增运费模板"
);

function normalizeStoreOption(item: Record<string, any>): StoreOption {
  const value = String(item.id || item.storeId || "").trim();
  const name =
    item.storeName ||
    item.name ||
    item.memberName ||
    item.storeId ||
    "未命名店铺";
  return { label: name, value };
}

function getStoreName(storeId?: string) {
  const targetId = String(storeId || "").trim();
  if (!targetId) return "-";
  return storeOptions.value.find(item => item.value === targetId)?.label || targetId;
}

function pricingMethodLabel(value?: string) {
  if (value === "NUM") return "按件";
  if (value === "WEIGHT") return "按重量";
  if (value === "FREE") return "包邮";
  return value || "-";
}

function normalizeRegionNode(
  item: Record<string, any>,
  parentIds: string[] = [],
  parentLabels: string[] = []
) {
  const value = String(item.id || item.regionId || "").trim();
  const label = String(item.name || item.regionName || "-");
  const currentIds = [...parentIds, value].filter(Boolean);
  const currentLabels = [...parentLabels, label].filter(Boolean);
  if (value) {
    regionNameMap.value.set(value, label);
  }
  if (currentIds.length) {
    regionPathNameMap.value.set(currentIds.join(","), currentLabels.join("/"));
  }
  return {
    value,
    label,
    children: Array.isArray(item.children)
      ? item.children.map((child: Record<string, any>) =>
          normalizeRegionNode(child, currentIds, currentLabels)
        )
      : []
  };
}

async function ensureRegionsLoaded() {
  if (regionsLoaded.value || regionLoading.value) return;
  regionLoading.value = true;
  try {
    const response = await getRegionAllCityTree();
    const payload = extractApiPayload(response);
    regionNameMap.value = new Map<string, string>();
    regionPathNameMap.value = new Map<string, string>();
    regionOptions.value = Array.isArray(payload)
      ? payload.map((item: Record<string, any>) => normalizeRegionNode(item))
      : [];
    regionsLoaded.value = true;
  } catch (error: any) {
    message(getErrorMessage(error, "地区数据加载失败，请稍后重试"), {
      type: "error"
    });
  } finally {
    regionLoading.value = false;
  }
}

function resolveRegionPathLabel(ids: string[]) {
  const key = ids.filter(Boolean).join(",");
  if (!key) return "";
  if (regionPathNameMap.value.has(key)) {
    return regionPathNameMap.value.get(key) || "";
  }
  return ids
    .map(id => regionNameMap.value.get(id) || id)
    .filter(Boolean)
    .join("/");
}

function createEmptyRule(): RuleForm {
  return {
    area: "",
    areaId: "",
    firstCompany: 1,
    firstPrice: 0,
    continuedCompany: 0,
    continuedPrice: 0,
    regionSelection: []
  };
}

function normalizeRule(item: Record<string, any>): RuleForm {
  const areaId = String(item.areaId || "").trim();
  const selection = regionPathNameMap.value.has(areaId)
    ? areaId.split(",").filter(Boolean)
    : [];
  return {
    id: item.id ? String(item.id) : undefined,
    area: String(item.area || "").trim(),
    areaId,
    firstCompany: Number(item.firstCompany ?? 0),
    firstPrice: Number(item.firstPrice ?? 0),
    continuedCompany: Number(item.continuedCompany ?? 0),
    continuedPrice: Number(item.continuedPrice ?? 0),
    regionSelection: selection
  };
}

function normalizeTemplateRow(item: Record<string, any>): TemplateRow {
  const children = Array.isArray(item.freightTemplateChildList)
    ? item.freightTemplateChildList.map((child: Record<string, any>) =>
        normalizeRule(child)
      )
    : [];
  return {
    ...item,
    id: String(item.id || item.templateId || ""),
    storeId: String(item.storeId || ""),
    storeName: getStoreName(item.storeId),
    displayName: item.name || item.templateName || item.id || "-",
    displayPricingMethod: pricingMethodLabel(item.pricingMethod),
    ruleCount: children.length,
    updateTime: item.updateTime || item.createTime || "-",
    freightTemplateChildList: children
  };
}

async function loadStores() {
  storeLoading.value = true;
  try {
    const response = await getAllStores();
    const payload = extractApiPayload(response);
    storeOptions.value = Array.isArray(payload)
      ? payload.map((item: Record<string, any>) => normalizeStoreOption(item))
      : [];
    if (!selectedStoreId.value && storeOptions.value.length === 1) {
      selectedStoreId.value = storeOptions.value[0].value;
    }
  } catch (error: any) {
    message(getErrorMessage(error, "店铺列表加载失败，请稍后重试"), {
      type: "error"
    });
  } finally {
    storeLoading.value = false;
  }
}

async function loadData() {
  if (!selectedStoreId.value) {
    rows.value = [];
    return;
  }
  try {
    const response = await getFreightTemplateList(selectedStoreId.value);
    const payload = extractApiPayload(response);
    let list = Array.isArray(payload)
      ? payload.map((item: Record<string, any>) => normalizeTemplateRow(item))
      : [];
    if (query.keyword.trim()) {
      const keyword = query.keyword.trim();
      list = list.filter(
        item =>
          item.displayName.includes(keyword) ||
          item.storeName.includes(keyword) ||
          item.displayPricingMethod.includes(keyword)
      );
    }
    rows.value = list;
  } catch (error: any) {
    rows.value = [];
    message(getErrorMessage(error, "运费模板加载失败，请稍后重试"), {
      type: "error"
    });
  }
}

function resetForm() {
  form.id = "";
  form.storeId = selectedStoreId.value;
  form.name = "";
  form.pricingMethod = "WEIGHT";
  form.freightTemplateChildList = [createEmptyRule()];
}

function handlePricingMethodChange(value: string) {
  if (value === "FREE") {
    form.freightTemplateChildList = [];
    return;
  }
  if (!form.freightTemplateChildList.length) {
    form.freightTemplateChildList = [createEmptyRule()];
  }
}

function handleSearch(payload: { keyword: string }) {
  query.keyword = payload.keyword || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  loadData();
}

function handleStoreChange(value: string) {
  selectedStoreId.value = String(value || "").trim();
  loadData();
}

function addRule() {
  form.freightTemplateChildList.push(createEmptyRule());
}

function removeRule(index: number) {
  form.freightTemplateChildList.splice(index, 1);
}

function syncRuleRegion(rule: RuleForm) {
  const ids = (rule.regionSelection || []).map(item => String(item).trim()).filter(Boolean);
  rule.areaId = ids.join(",");
  rule.area = resolveRegionPathLabel(ids);
}

function openCreate() {
  editingRow.value = null;
  resetForm();
  dialogVisible.value = true;
}

async function openEdit(row: TemplateRow) {
  await ensureRegionsLoaded();
  editingRow.value = row;
  try {
    const response = await getFreightTemplateDetail(row.id);
    const payload = extractApiPayload(response) as Record<string, any>;
    const detail = normalizeTemplateRow(payload || row);
    form.id = detail.id;
    form.storeId = detail.storeId;
    form.name = detail.displayName;
    form.pricingMethod = detail.pricingMethod || "WEIGHT";
    form.freightTemplateChildList =
      detail.freightTemplateChildList.length > 0
        ? detail.freightTemplateChildList.map(rule => ({ ...rule }))
        : detail.pricingMethod === "FREE"
          ? []
          : [createEmptyRule()];
  } catch (error: any) {
    message(getErrorMessage(error, "模板详情加载失败，请稍后重试"), {
      type: "error"
    });
    return;
  }
  dialogVisible.value = true;
}

async function openDetail(row: TemplateRow) {
  await ensureRegionsLoaded();
  try {
    const response = await getFreightTemplateDetail(row.id);
    const payload = extractApiPayload(response) as Record<string, any>;
    currentRow.value = normalizeTemplateRow(payload || row);
  } catch (_error) {
    currentRow.value = row;
  }
  detailVisible.value = true;
}

function buildPayload() {
  const freightTemplateChildList =
    form.pricingMethod === "FREE"
      ? []
      : form.freightTemplateChildList.map(rule => ({
          id: rule.id,
          area: rule.area,
          areaId: rule.areaId,
          firstCompany: Number(rule.firstCompany ?? 0),
          firstPrice: Number(rule.firstPrice ?? 0),
          continuedCompany: Number(rule.continuedCompany ?? 0),
          continuedPrice: Number(rule.continuedPrice ?? 0)
        }));
  return {
    id: form.id || undefined,
    storeId: form.storeId,
    name: form.name,
    pricingMethod: form.pricingMethod,
    freightTemplateChildList
  };
}

function validateForm() {
  if (!form.storeId.trim()) {
    message("请选择归属店铺", { type: "warning" });
    return false;
  }
  if (!form.name.trim()) {
    message("请输入模板名称", { type: "warning" });
    return false;
  }
  if (form.pricingMethod !== "FREE") {
    if (!form.freightTemplateChildList.length) {
      message("请至少配置一条区域运费规则", { type: "warning" });
      return false;
    }
    for (const [index, rule] of form.freightTemplateChildList.entries()) {
      if (!String(rule.area).trim() || !String(rule.areaId).trim()) {
        message(`第 ${index + 1} 条规则缺少适用地区`, { type: "warning" });
        return false;
      }
      if (Number(rule.firstCompany) <= 0) {
        message(`第 ${index + 1} 条规则首件/首重必须大于 0`, {
          type: "warning"
        });
        return false;
      }
      if (Number(rule.firstPrice) < 0 || Number(rule.continuedPrice) < 0) {
        message(`第 ${index + 1} 条规则运费不能小于 0`, {
          type: "warning"
        });
        return false;
      }
      if (Number(rule.continuedCompany) < 0) {
        message(`第 ${index + 1} 条规则续件/续重不能小于 0`, {
          type: "warning"
        });
        return false;
      }
    }
  }
  return true;
}

async function handleSave() {
  if (!validateForm()) return;
  saving.value = true;
  try {
    const payload = buildPayload();
    if (editingRow.value) {
      await updateFreightTemplate(editingRow.value.id, payload);
      message("运费模板修改成功", { type: "success" });
    } else {
      await createFreightTemplate(payload);
      message("运费模板新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    selectedStoreId.value = form.storeId;
    await loadData();
  } catch (error: any) {
    message(getErrorMessage(error, "运费模板保存失败，请确认规则配置后重试"), {
      type: "error"
    });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: TemplateRow) {
  await ElMessageBox.confirm(`确认删除运费模板「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteFreightTemplate(row.id, row.storeId);
    message("运费模板删除成功", { type: "success" });
    await loadData();
  } catch (error: any) {
    message(getErrorMessage(error, "运费模板删除失败"), { type: "error" });
  }
}

onMounted(async () => {
  await Promise.all([loadStores(), ensureRegionsLoaded()]);
  await loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="运费模板"
    description="平台统一维护店铺运费模板；商品继续按模板绑定，物流公司配置仍独立管理。"
    api-path="/manager/goods/freightTemplate/store/{storeId}"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '模板治理', value: '平台统一', type: 'primary' },
      { label: '物流绑定', value: '独立配置', type: 'success' },
      { label: '店铺写入', value: '已收口', type: 'warning' }
    ]"
    keyword-label="模板/店铺"
    keyword-placeholder="请输入模板名称或店铺名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-select
        v-model="selectedStoreId"
        filterable
        clearable
        :loading="storeLoading"
        placeholder="请选择店铺"
        style="width: 280px"
        @change="value => handleStoreChange(String(value || ''))"
      >
        <el-option
          v-for="item in storeOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button type="primary" @click="openCreate">新增模板</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="920px">
    <el-form label-width="110px">
      <el-form-item label="归属店铺" required>
        <el-select
          v-model="form.storeId"
          filterable
          :disabled="Boolean(editingRow)"
          placeholder="请选择店铺"
          style="width: 100%"
        >
          <el-option
            v-for="item in storeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="模板名称" required>
        <el-input v-model="form.name" placeholder="请输入模板名称" />
      </el-form-item>
      <el-form-item label="计价方式" required>
        <el-radio-group
          v-model="form.pricingMethod"
          @change="value => handlePricingMethodChange(String(value || ''))"
        >
          <el-radio label="WEIGHT">按重量</el-radio>
          <el-radio label="NUM">按件</el-radio>
          <el-radio label="FREE">包邮</el-radio>
        </el-radio-group>
      </el-form-item>

      <template v-if="form.pricingMethod !== 'FREE'">
        <div class="rules-header">
          <div>
            <h4>区域运费规则</h4>
            <p>当前版本按单条地区路径维护规则；同价多地区请新增多条规则。</p>
          </div>
          <el-button type="primary" plain @click="addRule">新增规则</el-button>
        </div>

        <div
          v-for="(rule, index) in form.freightTemplateChildList"
          :key="rule.id || index"
          class="rule-card"
        >
          <div class="rule-card__header">
            <span>规则 {{ index + 1 }}</span>
            <el-button
              link
              type="danger"
              :disabled="form.freightTemplateChildList.length === 1"
              @click="removeRule(index)"
            >
              删除
            </el-button>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="辅助选区">
                <el-cascader
                  v-model="rule.regionSelection"
                  :options="regionOptions"
                  :props="regionCascaderProps"
                  clearable
                  filterable
                  :show-all-levels="true"
                  placeholder="选择地区后自动生成地区路径"
                  style="width: 100%"
                  @change="syncRuleRegion(rule)"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="地区名称" required>
                <el-input
                  v-model="rule.area"
                  placeholder="如：浙江/杭州"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="24">
              <el-form-item label="地区ID" required>
                <el-input
                  v-model="rule.areaId"
                  placeholder="如：11,1101；支持手工维护"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item
                :label="form.pricingMethod === 'WEIGHT' ? '首重' : '首件'"
                required
              >
                <el-input-number
                  v-model="rule.firstCompany"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="首费" required>
                <el-input-number
                  v-model="rule.firstPrice"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item :label="form.pricingMethod === 'WEIGHT' ? '续重' : '续件'">
                <el-input-number
                  v-model="rule.continuedCompany"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="续费">
                <el-input-number
                  v-model="rule.continuedPrice"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </template>

      <el-alert
        v-else
        title="包邮模板不会生成区域规则，结算时直接跳过运费计算。"
        type="success"
        :closable="false"
      />
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="运费模板详情" size="720px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="模板名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="归属店铺">{{ currentRow.storeName }}</el-descriptions-item>
      <el-descriptions-item label="计价方式">{{ currentRow.displayPricingMethod }}</el-descriptions-item>
      <el-descriptions-item label="规则条数">{{ currentRow.ruleCount }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentRow.updateTime }}</el-descriptions-item>
    </el-descriptions>

    <div v-if="currentRow && currentRow.freightTemplateChildList.length" class="detail-rules">
      <h4>规则明细</h4>
      <el-table :data="currentRow.freightTemplateChildList" border>
        <el-table-column label="地区名称" prop="area" min-width="180" />
        <el-table-column label="地区ID" prop="areaId" min-width="180" />
        <el-table-column label="首量" prop="firstCompany" min-width="90" />
        <el-table-column label="首费" prop="firstPrice" min-width="90" />
        <el-table-column label="续量" prop="continuedCompany" min-width="90" />
        <el-table-column label="续费" prop="continuedPrice" min-width="90" />
      </el-table>
    </div>

    <div v-if="currentRow" class="detail-raw">
      <h4>原始数据</h4>
      <pre>{{ JSON.stringify(currentRow, null, 2) }}</pre>
    </div>
  </el-drawer>
</template>

<style scoped>
.rules-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin: 8px 0 16px;
  gap: 16px;
}

.rules-header h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: #2e3138;
}

.rules-header p {
  margin: 0;
  font-size: 12px;
  color: #7a808a;
}

.rule-card {
  padding: 16px 16px 2px;
  margin-bottom: 16px;
  border: 1px solid #e6eaf2;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.rule-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2e3138;
}

.detail-rules,
.detail-raw {
  margin-top: 20px;
}

.detail-rules h4,
.detail-raw h4 {
  margin: 0 0 12px;
  font-size: 15px;
  color: #2e3138;
}

.detail-raw pre {
  padding: 14px;
  margin: 0;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
  color: #4d5561;
  background: #f5f7fb;
  border-radius: 12px;
}
</style>
