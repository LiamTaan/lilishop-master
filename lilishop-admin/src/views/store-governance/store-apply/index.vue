<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import { auditStore, getStoreApplyPage } from "@/api/store-governance";
import { getRegionChildren, getRegionRootList } from "@/api/super-admin";
import { message } from "@/utils/message";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiRecords,
  getAgentLevelLabel,
  getStoreAuditStatusLabel,
  getStoreBizTypeLabel
} from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "StoreApplyAudit"
});

const data = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const auditVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const regionOptions = ref<Record<string, any>[]>([]);
const regionNameMap = ref(new Map<string, string>());
const rootRegionParentId = ref("0");
const selectedAgentRegionIds = ref<string[]>([]);
const query = reactive({
  keyword: "",
  status: ""
});
const extraFilters = reactive({
  bizType: "",
  agentLevel: "",
  contactKeyword: "",
  regionKeyword: ""
});
const auditForm = reactive({
  auditStatus: "APPROVED" as "APPROVED" | "REJECTED" | "FROZEN",
  auditRemark: "",
  agentLevel: "",
  agentRegionId: "",
  agentRegionName: ""
});

const AGENT_LEVEL_OPTIONS = [
  { label: "市级代理", value: "CITY" },
  { label: "区县代理", value: "COUNTY" },
  { label: "乡镇代理", value: "TOWNSHIP" },
  { label: "批发商代理", value: "WHOLESALER" }
];

const AGENT_REGION_LEVEL_MAP: Record<string, string> = {
  CITY: "city",
  COUNTY: "district",
  TOWNSHIP: "street",
  WHOLESALER: "district"
};

const AGENT_REGION_LEVEL_HINT_MAP: Record<string, string> = {
  CITY: "市级代理只能选择市级区域",
  COUNTY: "区县代理只能选择区县级区域",
  TOWNSHIP: "乡镇代理只能选择街道/乡镇级区域",
  WHOLESALER: "批发商代理只能选择区县级区域"
};

function auditActionLabel(status: "APPROVED" | "REJECTED" | "FROZEN") {
  if (status === "APPROVED") return "通过";
  if (status === "REJECTED") return "驳回";
  return "冻结";
}

function normalizeStoreRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.storeId,
    bizType: item.bizType || item.storeType || "-",
    applyType: item.applyType || item.companyType || "-",
    auditStatus: item.auditStatus || item.storeStatus || "-",
    agentLevel: item.agentLevel || "-",
    regionName: item.regionName || item.agentRegionName || "-",
    contactName: item.contactName || item.linkName || "-",
    contactMobile: item.contactMobile || item.linkPhone || "-",
    submitTime: item.submitTime || item.createTime || "-"
  };
}

const filteredData = computed(() =>
  data.value.filter(item =>
    [
      !extraFilters.bizType || item.bizType === extraFilters.bizType,
      !extraFilters.agentLevel || item.agentLevel === extraFilters.agentLevel,
      !extraFilters.contactKeyword ||
        [item.contactName, item.contactMobile].some(value =>
          String(value || "").includes(extraFilters.contactKeyword)
        ),
      !extraFilters.regionKeyword ||
        String(item.regionName || "").includes(extraFilters.regionKeyword)
    ].every(Boolean)
  )
);

const summaryCards = computed(() => [
  { label: "申请总数", value: filteredData.value.length, accent: "orange" as const, hint: "统一入驻主链" },
  {
    label: "待审核申请",
    value: filteredData.value.filter(item =>
      ["SUBMITTED", "PENDING", "WAIT", "DRAFT"].includes(
        String(item.auditStatus).toUpperCase()
      )
    ).length,
    accent: "blue" as const,
    hint: "待平台审核"
  },
  {
    label: "代理申请",
    value: filteredData.value.filter(item => item.bizType === "AGENT").length,
    accent: "warning" as const,
    hint: "代理商入驻申请"
  },
  {
    label: "已通过申请",
    value: filteredData.value.filter(item => String(item.auditStatus).toUpperCase().includes("APPROVED")).length,
    accent: "green" as const,
    hint: "已完成入驻审核"
  }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

const isAgentAuditApproval = computed(
  () =>
    currentRow.value?.bizType === "AGENT" &&
    auditForm.auditStatus === "APPROVED"
);

function normalizeId(value: unknown) {
  if (value === undefined || value === null) return "";
  return String(value).trim();
}

function getRegionLeafLabel(regionId: string) {
  return regionNameMap.value.get(regionId)?.split(",").at(-1) || "";
}

function getRegionLevelLabel(level: string) {
  const normalized = String(level || "").trim().toLowerCase();
  if (normalized === "province") return "省级";
  if (normalized === "city") return "市级";
  if (normalized === "district") return "区县级";
  if (normalized === "street") return "街道/乡镇级";
  if (normalized === "country") return "国家级";
  return level || "未知层级";
}

function normalizeRegionNode(item: Record<string, any>, parentPath = "") {
  const value = normalizeId(item.id || item.regionId);
  const label = String(item.name || item.regionName || "-");
  const currentPath = parentPath ? `${parentPath},${label}` : label;
  if (value) {
    regionNameMap.value.set(value, currentPath);
  }
  const level = String(item.level || "").trim();
  return {
    label,
    value,
    level,
    leaf: level === "street",
    children: [] as Record<string, any>[]
  };
}

function findRegionNode(nodes: Record<string, any>[], value: string): Record<string, any> | null {
  for (const node of nodes) {
    if (node.value === value) return node;
    if (Array.isArray(node.children) && node.children.length) {
      const childMatch = findRegionNode(node.children, value);
      if (childMatch) return childMatch;
    }
  }
  return null;
}

function matchesAgentRegionLevel(agentLevel: string, regionLevel: string) {
  if (!agentLevel || !regionLevel) return false;
  return AGENT_REGION_LEVEL_MAP[String(agentLevel).toUpperCase()] ===
    String(regionLevel).trim().toLowerCase();
}

async function fetchRegionChildren(parentId: string, parentPath = "") {
  try {
    const res = await getRegionChildren(parentId);
    const list = extractApiRecords<Record<string, any>>(res) || [];
    return list.map(item => normalizeRegionNode(item, parentPath));
  } catch (_error) {
    return [];
  }
}

async function loadRegionNode(
  node: Record<string, any>,
  resolve: (data: Record<string, any>[]) => void
) {
  if (!rootRegionParentId.value) {
    resolve([]);
    return;
  }
  const isRoot = node.level === 0;
  const parentId = isRoot ? rootRegionParentId.value : normalizeId(node.data?.value);
  const fallbackPath = Array.isArray(node.pathLabels)
    ? node.pathLabels.join(",")
    : String(node.pathLabels || "");
  const parentPath = isRoot
    ? ""
    : regionNameMap.value.get(parentId) || fallbackPath;
  const children = await fetchRegionChildren(parentId, parentPath);
  if (!isRoot && node.data) {
    node.data.children = children;
  }
  resolve(children);
}

const regionCascaderProps = {
  value: "value",
  label: "label",
  children: "children",
  emitPath: true,
  checkStrictly: true,
  lazy: true,
  lazyLoad: loadRegionNode
};

async function loadRegionOptions() {
  try {
    const rootRes = await getRegionRootList();
    const rootList = extractApiRecords<Record<string, any>>(rootRes) || [];
    const rootRegion =
      rootList.find(item => String(item.level || "").trim() === "country") || rootList[0];
    const hasCountryRoot = String(rootRegion?.level || "").trim() === "country";
    rootRegionParentId.value = hasCountryRoot ? normalizeId(rootRegion?.id) : "0";
    regionNameMap.value = new Map();
    regionOptions.value = hasCountryRoot
      ? await fetchRegionChildren(rootRegionParentId.value)
      : rootList.map(item => normalizeRegionNode(item));
  } catch (_error) {
    regionOptions.value = [];
    regionNameMap.value = new Map();
    rootRegionParentId.value = "0";
    message("地区选项加载失败，当前无法分配代理区域", {
      type: "warning"
    });
  }
}

async function ensureRegionPathLoaded(ids: string[]) {
  if (!ids.length || !rootRegionParentId.value) return;
  let parentId = rootRegionParentId.value;
  let parentPath = "";
  for (const currentId of ids) {
    const siblings = await fetchRegionChildren(parentId, parentPath);
    if (parentId === rootRegionParentId.value) {
      regionOptions.value = siblings;
    } else {
      const parentNode = findRegionNode(regionOptions.value, parentId);
      if (parentNode) {
        parentNode.children = siblings;
      }
    }
    parentId = currentId;
    parentPath = regionNameMap.value.get(currentId) || parentPath;
  }
}

function syncAgentRegionForm(ids: string[]) {
  const values = ids.map(item => item.trim()).filter(Boolean);
  if (!values.length) {
    auditForm.agentRegionId = "";
    auditForm.agentRegionName = "";
    selectedAgentRegionIds.value = [];
    return;
  }
  if (!auditForm.agentLevel) {
    message("请先选择代理等级，再选择代理区域", { type: "warning" });
    auditForm.agentRegionId = "";
    auditForm.agentRegionName = "";
    selectedAgentRegionIds.value = [];
    return;
  }
  const selectedNode = findRegionNode(regionOptions.value, values.at(-1) || "");
  const selectedLevel = String(selectedNode?.level || "");
  if (!matchesAgentRegionLevel(auditForm.agentLevel, selectedLevel)) {
    message(
      `${AGENT_REGION_LEVEL_HINT_MAP[auditForm.agentLevel]}，当前选择了${getRegionLevelLabel(selectedLevel)}`,
      { type: "warning" }
    );
    auditForm.agentRegionId = "";
    auditForm.agentRegionName = "";
    selectedAgentRegionIds.value = [];
    return;
  }
  selectedAgentRegionIds.value = values;
  const path = values
    .map(getRegionLeafLabel)
    .filter(Boolean)
    .join(",");
  auditForm.agentRegionId = values.at(-1) || "";
  auditForm.agentRegionName = path;
}

function resetAuditAssignment() {
  auditForm.agentLevel = "";
  auditForm.agentRegionId = "";
  auditForm.agentRegionName = "";
  selectedAgentRegionIds.value = [];
}

function handleAuditAgentLevelChange() {
  auditForm.agentRegionId = "";
  auditForm.agentRegionName = "";
  selectedAgentRegionIds.value = [];
}

function fillAuditAssignmentFromRow(row: Record<string, any>) {
  auditForm.agentLevel =
    row.bizType === "AGENT" && row.agentLevel !== "-"
      ? String(row.agentLevel || "")
      : "";
  auditForm.agentRegionId =
    row.bizType === "AGENT" ? String(row.agentRegionId || "") : "";
  auditForm.agentRegionName =
    row.bizType === "AGENT"
      ? String(row.agentRegionName || row.regionName || "")
      : "";
  selectedAgentRegionIds.value = [];
}

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 200,
    includeAll: true
  };
  if (query.keyword) params.storeName = query.keyword;
  if (query.status) params.auditStatus = query.status;
  if (extraFilters.bizType) params.bizType = extraFilters.bizType;
  if (extraFilters.agentLevel) params.agentLevel = extraFilters.agentLevel;
  const res = await getStoreApplyPage(params);
  data.value = extractApiRecords(res).map(normalizeStoreRecord);
  selectedRows.value = [];
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.bizType = "";
  extraFilters.agentLevel = "";
  extraFilters.contactKeyword = "";
  extraFilters.regionKeyword = "";
  loadData();
}

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

async function openAudit(row: Record<string, any>, status: "APPROVED" | "REJECTED" | "FROZEN") {
  currentRow.value = row;
  auditForm.auditStatus = status;
  auditForm.auditRemark = "";
  fillAuditAssignmentFromRow(row);
  if (row.bizType === "AGENT" && auditForm.agentRegionId) {
    const pathIds = String(row.agentRegionIdPath || row.agentRegionPathIds || "")
      .split(",")
      .map(item => item.trim())
      .filter(Boolean);
    if (pathIds.length) {
      await ensureRegionPathLoaded(pathIds);
      selectedAgentRegionIds.value = pathIds;
    }
  }
  auditVisible.value = true;
}

async function submitAudit() {
  if (!currentRow.value?.id) return;
  if (isAgentAuditApproval.value) {
    if (!auditForm.agentLevel) {
      message("审核通过代理申请时必须选择代理等级", { type: "warning" });
      return;
    }
    if (!auditForm.agentRegionId || !auditForm.agentRegionName) {
      message("审核通过代理申请时必须选择代理区域", { type: "warning" });
      return;
    }
    const selectedNode = findRegionNode(regionOptions.value, auditForm.agentRegionId);
    const selectedLevel = String(selectedNode?.level || "");
    if (!matchesAgentRegionLevel(auditForm.agentLevel, selectedLevel)) {
      message(
        `${AGENT_REGION_LEVEL_HINT_MAP[auditForm.agentLevel]}，当前选择了${getRegionLevelLabel(selectedLevel)}`,
        { type: "warning" }
      );
      return;
    }
  }
  await auditStore(currentRow.value.id, {
    auditStatus: auditForm.auditStatus,
    auditRemark: auditForm.auditRemark || undefined,
    agentLevel: isAgentAuditApproval.value ? auditForm.agentLevel : undefined,
    agentRegionId: isAgentAuditApproval.value ? auditForm.agentRegionId : undefined,
    agentRegionName: isAgentAuditApproval.value ? auditForm.agentRegionName : undefined
  });
  message("操作成功", { type: "success" });
  auditVisible.value = false;
  resetAuditAssignment();
  await loadData();
}

async function handleFreeze(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认冻结入驻主体「${row.storeName || row.id}」吗？`,
    "冻结确认",
    { type: "warning" }
  );
  currentRow.value = row;
  fillAuditAssignmentFromRow(row);
  auditForm.auditStatus = "FROZEN";
  auditForm.auditRemark = "管理端冻结";
  await submitAudit();
}

async function handleUnfreeze(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认解冻入驻主体「${row.storeName || row.id}」吗？`,
    "解冻确认",
    { type: "warning" }
  );
  currentRow.value = row;
  fillAuditAssignmentFromRow(row);
  auditForm.auditStatus = "APPROVED";
  auditForm.auditRemark = "管理端解冻";
  await submitAudit();
}

async function applyAuditToRows(
  rows: Record<string, any>[],
  auditStatus: "APPROVED" | "REJECTED" | "FROZEN",
  auditRemark?: string
) {
  await Promise.all(
    rows.map(row =>
      auditStore(String(row.id), {
        auditStatus,
        auditRemark
      })
    )
  );
}

async function handleBatchAudit(auditStatus: "APPROVED" | "REJECTED" | "FROZEN") {
  if (!selectedRows.value.length) {
    message(`请先勾选需要${auditActionLabel(auditStatus)}的入驻申请`, {
      type: "warning"
    });
    return;
  }
  if (
    auditStatus === "APPROVED" &&
    selectedRows.value.some(row => String(row.bizType || "").toUpperCase() === "AGENT")
  ) {
    message("代理申请通过时需要逐条配置代理等级和代理区域，不支持批量通过", {
      type: "warning"
    });
    return;
  }
  await ElMessageBox.confirm(
    `确认将已勾选的 ${selectedIds.value.length} 条入驻申请批量${auditActionLabel(auditStatus)}吗？`,
    "批量审核确认",
    { type: "warning" }
  );
  await applyAuditToRows(
    selectedRows.value,
    auditStatus,
    `管理端批量${auditActionLabel(auditStatus)}`
  );
  selectedRows.value = [];
  message(`入驻申请批量${auditActionLabel(auditStatus)}成功`, { type: "success" });
  await loadData();
}

function exportStoreApplies() {
  if (!filteredData.value.length) {
    message("暂无可导出的入驻申请数据", { type: "warning" });
    return;
  }
  const table = filteredData.value.map(item => ({
    店铺名称: item.storeName || "-",
    业务类型: getStoreBizTypeLabel(item.bizType),
    代理等级: getAgentLevelLabel(item.agentLevel),
    联系人: item.contactName,
    联系电话: item.contactMobile,
    区域: item.regionName,
    审核状态: getStoreAuditStatusLabel(item.auditStatus),
    提交时间: item.submitTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "统一入驻审核");
  writeFile(workbook, "统一入驻审核.xlsx");
  message("入驻申请导出成功", { type: "success" });
}

onMounted(() => {
  loadRegionOptions();
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="统一入驻审核"
    description="承接供货商与代理商统一入驻申请，按业务类型、代理等级、区域进行审核与冻结治理。"
    api-path="/manager/store/store"
    :columns="columns"
    :data="filteredData"
    selectable
    :summary-cards="summaryCards"
    :status-options="[
      { label: '草稿', value: 'DRAFT' },
      { label: '待审核', value: 'SUBMITTED' },
      { label: '审核通过', value: 'APPROVED' },
      { label: '审核驳回', value: 'REJECTED' },
      { label: '已冻结', value: 'FROZEN' }
    ]"
    :quick-actions="[
      { label: '统一主链', value: '已收口', type: 'primary' },
      { label: '代理等级', value: '已接入', type: 'warning' },
      { label: '冻结/解冻', value: '已支持', type: 'success' }
    ]"
    keyword-label="店铺名称"
    keyword-placeholder="请输入店铺名称"
    status-label="审核状态"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #filters-extra>
      <el-form-item label="业务类型">
        <el-select v-model="extraFilters.bizType" placeholder="全部" clearable>
          <el-option label="供货商" value="SUPPLIER" />
          <el-option label="代理商" value="AGENT" />
        </el-select>
      </el-form-item>
      <el-form-item label="代理等级">
        <el-select v-model="extraFilters.agentLevel" placeholder="全部" clearable>
          <el-option label="市级代理" value="CITY" />
          <el-option label="区县代理" value="COUNTY" />
          <el-option label="乡镇代理" value="TOWNSHIP" />
          <el-option label="批发商代理" value="WHOLESALER" />
        </el-select>
      </el-form-item>
      <el-form-item label="联系人/电话">
        <el-input
          v-model="extraFilters.contactKeyword"
          placeholder="请输入联系人或电话"
          clearable
        />
      </el-form-item>
      <el-form-item label="区域">
        <el-input
          v-model="extraFilters.regionKeyword"
          placeholder="请输入区域名称"
          clearable
        />
      </el-form-item>
    </template>
    <template #table-extra>
      <el-button type="success" plain @click="handleBatchAudit('APPROVED')">批量通过</el-button>
      <el-button type="danger" plain @click="handleBatchAudit('REJECTED')">批量驳回</el-button>
      <el-button type="warning" plain @click="handleBatchAudit('FROZEN')">批量冻结</el-button>
      <el-button @click="exportStoreApplies">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button
        v-if="!['APPROVED', 'FROZEN'].includes(row.auditStatus)"
        link
        type="primary"
        @click="openAudit(row, 'APPROVED')"
      >
        通过
      </el-button>
      <el-button
        v-if="!['REJECTED', 'APPROVED', 'FROZEN'].includes(row.auditStatus)"
        link
        type="danger"
        @click="openAudit(row, 'REJECTED')"
      >
        驳回
      </el-button>
      <el-button
        v-if="row.auditStatus === 'APPROVED'"
        link
        type="warning"
        @click="handleFreeze(row)"
      >
        冻结
      </el-button>
      <el-button
        v-if="row.auditStatus === 'FROZEN'"
        link
        type="success"
        @click="handleUnfreeze(row)"
      >
        解冻
      </el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="auditVisible"
    :title="`入驻审核 - ${currentRow?.storeName || '-'}`"
    width="520px"
  >
    <el-form label-width="88px">
      <el-form-item label="业务类型">
        <span>{{ getStoreBizTypeLabel(currentRow?.bizType) }}</span>
      </el-form-item>
      <el-form-item label="当前代理" v-if="currentRow?.bizType === 'AGENT'">
        <span>{{ getAgentLevelLabel(currentRow?.agentLevel) }}</span>
      </el-form-item>
      <el-form-item label="当前状态">
        <span>{{ getStoreAuditStatusLabel(currentRow?.auditStatus) }}</span>
      </el-form-item>
      <el-form-item label="审核动作">
        <el-tag
          :type="
            auditForm.auditStatus === 'APPROVED'
              ? 'success'
              : auditForm.auditStatus === 'REJECTED'
                ? 'danger'
              : 'warning'
          "
        >
          {{ currentRow?.auditStatus === "FROZEN" && auditForm.auditStatus === "APPROVED" ? "解冻" : auditActionLabel(auditForm.auditStatus) }}
        </el-tag>
      </el-form-item>
      <el-form-item
        v-if="isAgentAuditApproval"
        label="代理等级"
        required
      >
        <el-select
          v-model="auditForm.agentLevel"
          placeholder="请选择代理等级"
          clearable
          @change="handleAuditAgentLevelChange"
        >
          <el-option
            v-for="item in AGENT_LEVEL_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="isAgentAuditApproval"
        label="代理区域"
        required
      >
        <el-cascader
          v-model="selectedAgentRegionIds"
          class="w-full"
          :options="regionOptions"
          :props="regionCascaderProps"
          clearable
          filterable
          placeholder="请选择代理区域"
          @change="value => syncAgentRegionForm((value || []) as string[])"
        />
        <div class="mt-2 text-xs text-gray-400">
          {{ AGENT_REGION_LEVEL_HINT_MAP[auditForm.agentLevel] || "请先选择代理等级，再选择匹配层级的代理区域" }}
        </div>
      </el-form-item>
      <el-form-item label="审核备注">
        <el-input
          v-model="auditForm.auditRemark"
          type="textarea"
          :rows="4"
          :placeholder="`请输入${auditActionLabel(auditForm.auditStatus)}说明`"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="auditVisible = false">取消</el-button>
      <el-button type="primary" @click="submitAudit">提交</el-button>
    </template>
  </el-dialog>
</template>
