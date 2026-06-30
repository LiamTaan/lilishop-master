<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import { getCategoryPage } from "@/api/goods-governance";
import {
  auditStore,
  createStore,
  disableStore,
  enableStore,
  getStoreApplyPage,
  getStoreAuditLog,
  getStoreDetail,
  getStoreSummary,
  updateStore
} from "@/api/store-governance";
import {
  getMemberDetail,
  getMemberPage,
  getRegionChildren,
  getRegionRootList
} from "@/api/super-admin";
import ImageUploadField from "@/components/ImageUploadField/index.vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime,
  getStoreBizTypeLabel,
  getStoreAuditStatusLabel,
  getStoreStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { isSuccessResult } from "@/utils/result";
import { columns } from "./columns";

defineOptions({
  name: "StoreManage"
});

const APPLY_TYPE_OPTIONS = [
  { label: "个人主体", value: "PERSONAL" },
  { label: "个体户", value: "INDIVIDUAL" },
  { label: "企业法人", value: "COMPANY_LEGAL" },
  { label: "企业非法人", value: "COMPANY_NON_LEGAL" }
];

const BIZ_TYPE_OPTIONS = [
  { label: "供货商", value: "SUPPLIER" },
  { label: "代理商", value: "AGENT" }
];

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

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<Record<string, any>>({});
const dialogVisible = ref(false);
const saving = ref(false);
const editingStoreId = ref("");
const selectedRows = ref<Record<string, any>[]>([]);
const auditDialogVisible = ref(false);
const auditSaving = ref(false);
const auditRow = ref<Record<string, any> | null>(null);
const auditLogVisible = ref(false);
const auditLogs = ref<Record<string, any>[]>([]);
const userOptions = ref<
  Array<{
    label: string;
    value: string;
    nickName: string;
    mobile: string;
  }>
>([]);
const userLoading = ref(false);
const storeCategoryLoading = ref(false);
const storeCategoryOptions = ref<Array<{ label: string; value: string }>>([]);
const storeCategoryNameMap = ref(new Map<string, string>());
const regionOptions = ref<Record<string, any>[]>([]);
const regionNameMap = ref(new Map<string, string>());
const rootRegionParentId = ref("0");
const selectedCompanyRegionIds = ref<string[]>([]);
const selectedAgentRegionIds = ref<string[]>([]);
const selectedStoreCategoryIds = ref<string[]>([]);
const summary = reactive({
  totalCount: 0,
  submittedCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  frozenCount: 0,
  openCount: 0,
  closedCount: 0
});

const query = reactive({
  keyword: "",
  status: ""
});

const extraFilters = reactive({
  categoryKeyword: "",
  auditStatus: ""
});

const auditForm = reactive({
  auditStatus: "APPROVED",
  auditRemark: ""
});

const form = reactive({
  memberId: "",
  applyType: "COMPANY_LEGAL",
  storeType: "SUPPLIER",
  agentLevel: "",
  agentRegionId: "",
  agentRegionName: "",
  storeName: "",
  storeLogo: "",
  storeDesc: "",
  goodsManagementCategory: "",
  selfOperated: false,
  companyName: "",
  companyAddress: "",
  companyAddressIdPath: "",
  companyAddressPath: "",
  linkName: "",
  linkPhone: "",
  companyPhone: "",
  companyEmail: "",
  registeredCapital: 1,
  businessLicenseUrl: "",
  facadeImageUrl: "",
  creditCode: "",
  legalName: "",
  legalId: "",
  legalMobile: "",
  authorizationUrl: "",
  realName: "",
  idCardNo: "",
  idCardFrontUrl: "",
  idCardBackUrl: ""
});

const isPersonalApply = computed(() => form.applyType === "PERSONAL");
const isIndividualApply = computed(() => form.applyType === "INDIVIDUAL");
const isCompanyLegalApply = computed(() => form.applyType === "COMPANY_LEGAL");
const isCompanyAuthorizedApply = computed(
  () => form.applyType === "COMPANY_NON_LEGAL"
);
const isCompanyApply = computed(
  () => isCompanyLegalApply.value || isCompanyAuthorizedApply.value
);
const needsAuthorization = computed(() => isCompanyAuthorizedApply.value);
const isAgentStore = computed(() => form.storeType === "AGENT");

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

const detailItems = computed(() => [
  { label: "店铺名称", value: detail.value.storeName || "-" },
  { label: "店铺ID", value: detail.value.storeId || detail.value.id || "-" },
  { label: "申请主体", value: getApplyTypeLabel(detail.value.applyType) },
  {
    label: "经营类目",
    value: resolveStoreCategoryText(
      detail.value.storeManagementCategory || detail.value.storeCategoryName
    )
  },
  { label: "联系人", value: detail.value.linkName || detail.value.contactName || "-" },
  { label: "联系电话", value: detail.value.linkPhone || detail.value.contactMobile || "-" },
  {
    label: "业务类型",
    value: getStoreBizTypeLabel(detail.value.bizType || detail.value.storeType)
  },
  {
    label: "店铺状态",
    value: getStoreStatusLabel(detail.value.storeDisable ?? detail.value.storeStatus)
  },
  {
    label: "审核状态",
    value: getStoreAuditStatusLabel(
      detail.value.auditStatus ?? detail.value.storeStatus
    )
  },
  { label: "审核备注", value: detail.value.auditRemark || "-" }
]);

const filteredData = computed(() =>
  data.value.filter(item => {
    const categoryMatched = extraFilters.categoryKeyword
      ? String(item.storeCategoryName || "").includes(extraFilters.categoryKeyword)
      : true;
    const auditMatched = extraFilters.auditStatus
      ? String(item.auditStatus || "").toUpperCase() === extraFilters.auditStatus
      : true;
    return categoryMatched && auditMatched;
  })
);

const filteredCount = computed(() => filteredData.value.length);

const summaryCards = computed(() => [
  {
    label: "当前列表",
    value: filteredCount.value,
    accent: "orange" as const,
    hint: "当前筛选结果"
  },
  {
    label: "平台店铺",
    value: summary.totalCount,
    accent: "green" as const,
    hint: "后端全量汇总"
  },
  {
    label: "营业店铺",
    value: summary.openCount,
    accent: "blue" as const,
    hint: "全量营业中店铺"
  },
  {
    label: "待审核店铺",
    value: summary.submittedCount,
    accent: "purple" as const,
    hint: "全量待处理审核"
  }
]);

function getApplyTypeLabel(value: unknown) {
  const target = String(value || "").trim().toUpperCase();
  return APPLY_TYPE_OPTIONS.find(item => item.value === target)?.label || "-";
}

function resolveApplyType(
  subjectType: unknown,
  companyIdentityType: unknown,
  fallback?: unknown
) {
  const normalizedSubjectType = String(subjectType || "")
    .trim()
    .toUpperCase();
  const normalizedCompanyIdentityType = String(companyIdentityType || "")
    .trim()
    .toUpperCase();
  if (normalizedSubjectType === "PERSONAL") return "PERSONAL";
  if (normalizedSubjectType === "INDIVIDUAL") return "INDIVIDUAL";
  if (normalizedSubjectType === "COMPANY") {
    return normalizedCompanyIdentityType === "AUTHORIZED"
      ? "COMPANY_NON_LEGAL"
      : "COMPANY_LEGAL";
  }
  const fallbackValue = String(fallback || "")
    .trim()
    .toUpperCase();
  return APPLY_TYPE_OPTIONS.some(item => item.value === fallbackValue)
    ? fallbackValue
    : "COMPANY_LEGAL";
}

function getSubjectPayloadByApplyType(applyType: unknown) {
  const normalized = String(applyType || "")
    .trim()
    .toUpperCase();
  if (normalized === "PERSONAL") {
    return {
      applyType: "PERSONAL",
      subjectType: "PERSONAL",
      companyIdentityType: undefined
    };
  }
  if (normalized === "INDIVIDUAL") {
    return {
      applyType: "INDIVIDUAL",
      subjectType: "INDIVIDUAL",
      companyIdentityType: undefined
    };
  }
  if (normalized === "COMPANY_NON_LEGAL") {
    return {
      applyType: "COMPANY_NON_LEGAL",
      subjectType: "COMPANY",
      companyIdentityType: "AUTHORIZED"
    };
  }
  return {
    applyType: "COMPANY_LEGAL",
    subjectType: "COMPANY",
    companyIdentityType: "LEGAL"
  };
}

function normalizeId(value: unknown) {
  if (value === undefined || value === null) return "";
  return String(value).trim();
}

function parseRegionPathNames(path: unknown) {
  return String(path || "")
    .split(",")
    .map(item => item.trim())
    .filter(Boolean);
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

function normalizeUserOption(item: Record<string, any>) {
  const value = String(item.id || item.memberId || "").trim();
  const nickName =
    item.nickName || item.nickname || item.memberName || item.username || "未命名用户";
  const mobile = item.mobile || "-";
  return {
    label: `${nickName} (${mobile})`,
    value,
    nickName,
    mobile
  };
}

function normalizeStoreCategoryValue(value: unknown) {
  return String(value || "")
    .split(",")
    .map(item => item.trim())
    .filter(Boolean);
}

function flattenCategoryTree(
  list: Record<string, any>[],
  bucket: Record<string, any>[] = []
) {
  list.forEach(item => {
    bucket.push(item);
    const childrenSource =
      Array.isArray(item.children) && item.children.length
        ? item.children
        : Array.isArray(item.child) && item.child.length
          ? item.child
          : [];
    if (childrenSource.length) {
      flattenCategoryTree(childrenSource, bucket);
    }
  });
  return bucket;
}

function resolveStoreCategoryText(value: unknown) {
  const entries = normalizeStoreCategoryValue(value);
  if (!entries.length) {
    return String(value || "-") || "-";
  }
  return entries
    .map(item => storeCategoryNameMap.value.get(item) || item)
    .join("、");
}

function resolveStoreCategoryIds(value: unknown) {
  const entries = normalizeStoreCategoryValue(value);
  if (!entries.length) return [];
  const labelToIdMap = new Map(
    storeCategoryOptions.value.map(item => [item.label, item.value] as const)
  );
  return entries
    .map(item => {
      if (storeCategoryNameMap.value.has(item)) return item;
      return labelToIdMap.get(item) || "";
    })
    .filter(Boolean);
}

function syncStoreCategoryForm(ids: string[]) {
  const values = ids.map(item => item.trim()).filter(Boolean);
  selectedStoreCategoryIds.value = values;
  form.goodsManagementCategory = values.join(",");
}

async function ensureStoreCategoryOptions() {
  if (storeCategoryOptions.value.length) return;
  storeCategoryLoading.value = true;
  try {
    const response = await getCategoryPage();
    const source = extractApiRecords<Record<string, any>>(response) || [];
    const flat = flattenCategoryTree(source);
    storeCategoryNameMap.value = new Map(
      flat
        .map(item => [
          String(item.id || item.categoryId || "").trim(),
          String(item.name || item.categoryName || "-")
        ] as const)
        .filter(item => item[0])
    );
    storeCategoryOptions.value = flat
      .filter(item => Number(item.level ?? 0) === 0)
      .map(item => ({
        label: String(item.name || item.categoryName || "-"),
        value: String(item.id || item.categoryId || "").trim()
      }))
      .filter(item => item.value);
    if (data.value.length) {
      data.value = data.value.map(normalizeStoreRecord);
    }
    if (detail.value && Object.keys(detail.value).length) {
      detail.value = normalizeStoreRecord(detail.value);
    }
  } catch (_error) {
    storeCategoryOptions.value = [];
    storeCategoryNameMap.value = new Map();
    message("店铺经营类目加载失败，暂无法选择平台类目", {
      type: "warning"
    });
  } finally {
    storeCategoryLoading.value = false;
  }
}

function mergeUserOptions(list: Record<string, any>[]) {
  const optionMap = new Map(
    userOptions.value.map(item => [item.value, item] as const)
  );
  list
    .map(normalizeUserOption)
    .filter(item => item.value)
    .forEach(item => optionMap.set(item.value, item));
  userOptions.value = Array.from(optionMap.values());
}

function replaceUserOptions(list: Record<string, any>[], preserveValues: string[] = []) {
  const preserveSet = new Set(
    preserveValues.map(item => item.trim()).filter(Boolean)
  );
  const optionMap = new Map<string, (typeof userOptions.value)[number]>();
  userOptions.value
    .filter(item => preserveSet.has(item.value))
    .forEach(item => optionMap.set(item.value, item));
  list
    .map(normalizeUserOption)
    .filter(item => item.value)
    .forEach(item => optionMap.set(item.value, item));
  userOptions.value = Array.from(optionMap.values());
}

async function searchUsers(keyword: string) {
  const searchText = keyword.trim();
  if (!searchText) {
    userOptions.value = [];
    return;
  }
  userLoading.value = true;
  try {
    const isFullMobile = /^1\d{10}$/.test(searchText);
    const res = await getMemberPage({
      nickName: isFullMobile ? undefined : searchText,
      mobile: isFullMobile ? searchText : undefined
    });
    replaceUserOptions(extractApiRecords(res), [form.memberId]);
  } catch (_error) {
    message("用户搜索失败，请稍后重试", { type: "error" });
  } finally {
    userLoading.value = false;
  }
}

async function ensureUserOption(memberId: string) {
  const targetId = memberId.trim();
  if (!targetId) return;
  if (userOptions.value.some(item => item.value === targetId)) return;
  try {
    const res = await getMemberDetail(targetId);
    const detailPayload = extractApiPayload<Record<string, any>>(res);
    if (detailPayload) {
      mergeUserOptions([detailPayload]);
    }
  } catch (_error) {
    // 仅用于回填已选会员，不阻断主流程
  }
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
    rootRegionParentId.value = hasCountryRoot
      ? normalizeId(rootRegion?.id)
      : "0";
    regionNameMap.value = new Map();
    regionOptions.value = hasCountryRoot
      ? await fetchRegionChildren(rootRegionParentId.value)
      : rootList.map(item => normalizeRegionNode(item));
  } catch (_error) {
    regionOptions.value = [];
    regionNameMap.value = new Map();
    rootRegionParentId.value = "0";
    message("地区选项加载失败，店铺表单暂无法自动选择地区", {
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

async function resolveRegionPathIdsByNames(names: string[]) {
  if (!names.length || !rootRegionParentId.value) return [];
  let parentId = rootRegionParentId.value;
  let parentPath = "";
  const resolvedIds: string[] = [];
  for (const currentName of names) {
    const siblings = await fetchRegionChildren(parentId, parentPath);
    if (parentId === rootRegionParentId.value) {
      regionOptions.value = siblings;
    } else {
      const parentNode = findRegionNode(regionOptions.value, parentId);
      if (parentNode) {
        parentNode.children = siblings;
      }
    }
    const matched = siblings.find(item => item.label === currentName);
    if (!matched) {
      break;
    }
    resolvedIds.push(matched.value);
    parentId = matched.value;
    parentPath = regionNameMap.value.get(matched.value) || currentName;
  }
  return resolvedIds;
}

function syncCompanyRegionForm(ids: string[]) {
  const values = ids.map(item => item.trim()).filter(Boolean);
  selectedCompanyRegionIds.value = values;
  form.companyAddressIdPath = values.join(",");
  form.companyAddressPath = values
    .map(getRegionLeafLabel)
    .filter(Boolean)
    .join(",");
}

function syncAgentRegionForm(ids: string[]) {
  const values = ids.map(item => item.trim()).filter(Boolean);
  if (!values.length) {
    form.agentRegionId = "";
    form.agentRegionName = "";
    selectedAgentRegionIds.value = [];
    return;
  }
  if (!form.agentLevel) {
    message("请先选择代理等级，再选择代理区域", { type: "warning" });
    form.agentRegionId = "";
    form.agentRegionName = "";
    selectedAgentRegionIds.value = [];
    return;
  }
  const selectedNode = findRegionNode(regionOptions.value, values.at(-1) || "");
  const selectedLevel = String(selectedNode?.level || "");
  if (!matchesAgentRegionLevel(form.agentLevel, selectedLevel)) {
    message(
      `${AGENT_REGION_LEVEL_HINT_MAP[form.agentLevel]}，当前选择了${getRegionLevelLabel(selectedLevel)}`,
      { type: "warning" }
    );
    form.agentRegionId = "";
    form.agentRegionName = "";
    selectedAgentRegionIds.value = [];
    return;
  }
  selectedAgentRegionIds.value = values;
  const path = values
    .map(getRegionLeafLabel)
    .filter(Boolean)
    .join(",");
  form.agentRegionId = values.at(-1) || "";
  form.agentRegionName = path;
}

function handleStoreTypeChange(value: string) {
  if (value !== "AGENT") {
    form.agentLevel = "";
    form.agentRegionId = "";
    form.agentRegionName = "";
    selectedAgentRegionIds.value = [];
  }
}

function handleAgentLevelChange() {
  form.agentRegionId = "";
  form.agentRegionName = "";
  selectedAgentRegionIds.value = [];
}

function normalizeStoreRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.storeId,
    contactName:
      item.contactName || item.linkName || item.memberName || item.legalName || "-",
    contactMobile:
      item.contactMobile || item.linkPhone || item.legalMobile || item.companyPhone || "-",
    storeCategoryName:
      resolveStoreCategoryText(
        item.storeCategoryName || item.storeManagementCategory
      ) || "-",
    storeStatus:
      item.storeDisable ??
      item.storeStatus ??
      item.status ??
      item.storeState ??
      "-",
    auditStatus:
      item.auditStatus ??
      item.applyStatus ??
      item.audit_state ??
      item.storeStatus ??
      "-",
    bizType: item.bizType || item.storeType || "-",
    auditRemark: item.auditRemark || "-"
  };
}

function resetForm() {
  editingStoreId.value = "";
  form.memberId = "";
  form.applyType = "COMPANY_LEGAL";
  form.storeType = "SUPPLIER";
  form.agentLevel = "";
  form.agentRegionId = "";
  form.agentRegionName = "";
  form.storeName = "";
  form.storeLogo = "";
  form.storeDesc = "";
  form.goodsManagementCategory = "";
  selectedStoreCategoryIds.value = [];
  form.selfOperated = false;
  form.companyName = "";
  form.companyAddress = "";
  form.companyAddressIdPath = "";
  form.companyAddressPath = "";
  form.linkName = "";
  form.linkPhone = "";
  form.companyPhone = "";
  form.companyEmail = "";
  form.registeredCapital = 1;
  form.businessLicenseUrl = "";
  form.facadeImageUrl = "";
  form.creditCode = "";
  form.legalName = "";
  form.legalId = "";
  form.legalMobile = "";
  form.authorizationUrl = "";
  form.realName = "";
  form.idCardNo = "";
  form.idCardFrontUrl = "";
  form.idCardBackUrl = "";
  selectedCompanyRegionIds.value = [];
  selectedAgentRegionIds.value = [];
}

function applyDetailToForm(payload: Record<string, any>) {
  editingStoreId.value = String(payload.storeId || payload.id || "");
  form.memberId = String(payload.memberId || "").trim();
  form.applyType = resolveApplyType(
    payload.subjectType,
    payload.companyIdentityType,
    payload.applyType
  );
  form.storeType = String(payload.storeType || payload.bizType || "SUPPLIER").toUpperCase();
  form.agentLevel = payload.agentLevel || "";
  form.agentRegionId = payload.agentRegionId || "";
  form.agentRegionName = payload.agentRegionName || "";
  form.storeName = payload.storeName || "";
  form.storeLogo = payload.storeLogo || "";
  form.storeDesc = payload.storeDesc || "";
  syncStoreCategoryForm(
    resolveStoreCategoryIds(
      payload.goodsManagementCategory || payload.storeManagementCategory || ""
    )
  );
  form.selfOperated = Boolean(payload.selfOperated);
  form.companyName = payload.companyName || "";
  form.companyAddress =
    payload.companyAddress || payload.storeAddressDetail || "";
  form.companyAddressIdPath =
    payload.companyAddressIdPath || payload.storeAddressIdPath || "";
  form.companyAddressPath =
    payload.companyAddressPath || payload.storeAddressPath || "";
  form.linkName = payload.linkName || "";
  form.linkPhone = payload.linkPhone || "";
  form.companyPhone = payload.companyPhone || "";
  form.companyEmail = payload.companyEmail || "";
  form.registeredCapital = Number(payload.registeredCapital || 1);
  form.businessLicenseUrl = payload.businessLicenseUrl || "";
  form.facadeImageUrl = payload.facadeImageUrl || "";
  form.creditCode = payload.creditCode || "";
  form.legalName = payload.legalName || "";
  form.legalId = payload.legalId || "";
  form.legalMobile = payload.legalMobile || "";
  form.authorizationUrl = payload.authorizationUrl || "";
  form.realName = payload.realName || "";
  form.idCardNo = payload.idCardNo || "";
  form.idCardFrontUrl = payload.idCardFrontUrl || "";
  form.idCardBackUrl = payload.idCardBackUrl || "";
  selectedCompanyRegionIds.value = String(
    payload.companyAddressIdPath || payload.storeAddressIdPath || ""
  )
    .split(",")
    .map(item => item.trim())
    .filter(Boolean);
  selectedAgentRegionIds.value = [];
}

async function loadData() {
  try {
    const params: Record<string, any> = {
      pageNumber: 1,
      pageSize: 200
    };
    if (query.keyword) params.storeName = query.keyword;
    if (query.status) {
      params.storeDisable = query.status;
      params.storeStatus = query.status;
    }
    const res = await getStoreApplyPage(params);
    data.value = extractApiRecords(res).map(normalizeStoreRecord);
  } catch (_error) {
    data.value = [];
    message("店铺列表加载失败，请稍后重试", { type: "error" });
  }
}

async function loadSummary() {
  try {
    const res = await getStoreSummary();
    Object.assign(summary, extractApiPayload<Record<string, any>>(res) || {});
  } catch (_error) {
    Object.assign(summary, {
      totalCount: 0,
      submittedCount: 0,
      approvedCount: 0,
      rejectedCount: 0,
      frozenCount: 0,
      openCount: 0,
      closedCount: 0
    });
  }
}

async function refreshStorePage() {
  await Promise.all([loadData(), loadSummary()]);
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  extraFilters.categoryKeyword = "";
  extraFilters.auditStatus = "";
  loadData();
}

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

async function openDetail(row: Record<string, any>) {
  const storeId = row.storeId || row.id;
  if (!storeId) return;
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getStoreDetail(storeId);
    detail.value = normalizeStoreRecord(extractApiPayload(res) ?? row);
  } finally {
    detailLoading.value = false;
  }
}

async function openCreate() {
  resetForm();
  await ensureStoreCategoryOptions();
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  const storeId = row.storeId || row.id;
  if (!storeId) return;
  saving.value = false;
  resetForm();
  try {
    await ensureStoreCategoryOptions();
    const res = await getStoreDetail(String(storeId));
    const payload = extractApiPayload<Record<string, any>>(res) || row;
    applyDetailToForm(payload);
    if (form.memberId) {
      await ensureUserOption(form.memberId);
    }
    if (selectedCompanyRegionIds.value.length) {
      await ensureRegionPathLoaded(selectedCompanyRegionIds.value);
    } else if (form.companyAddressPath) {
      const resolvedCompanyIds = await resolveRegionPathIdsByNames(
        parseRegionPathNames(form.companyAddressPath)
      );
      if (resolvedCompanyIds.length) {
        syncCompanyRegionForm(resolvedCompanyIds);
      }
    }
    if (form.agentRegionName) {
      const resolvedAgentIds = await resolveRegionPathIdsByNames(
        parseRegionPathNames(form.agentRegionName)
      );
      if (resolvedAgentIds.length) {
        syncAgentRegionForm(resolvedAgentIds);
      } else if (form.agentRegionId) {
        selectedAgentRegionIds.value = [form.agentRegionId];
      }
    }
    dialogVisible.value = true;
  } catch (_error) {
    message("店铺详情加载失败，暂无法进入编辑", { type: "error" });
  }
}

function validateStoreForm() {
  const subjectMobile = form.legalMobile.trim() || form.linkPhone.trim();
  if (!editingStoreId.value && !form.memberId.trim()) {
    message("请先选择所属会员", { type: "warning" });
    return false;
  }
  if (!form.storeName.trim()) {
    message("请输入店铺名称", { type: "warning" });
    return false;
  }
  if (form.storeDesc.trim().length < 6) {
    message("店铺简介至少需要 6 个字符", { type: "warning" });
    return false;
  }
  if (!selectedStoreCategoryIds.value.length) {
    message("请选择店铺经营类目", { type: "warning" });
    return false;
  }
  if (!form.companyAddress.trim()) {
    message("请输入公司详细地址", { type: "warning" });
    return false;
  }
  if (isAgentStore.value) {
    if (!form.agentLevel) {
      message("代理商店铺请选择代理等级", { type: "warning" });
      return false;
    }
    if (!form.agentRegionId || !form.agentRegionName) {
      message("代理商店铺请选择代理区域", { type: "warning" });
      return false;
    }
    const selectedNode = findRegionNode(regionOptions.value, form.agentRegionId);
    const selectedLevel = String(selectedNode?.level || "");
    if (!matchesAgentRegionLevel(form.agentLevel, selectedLevel)) {
      message(
        `${AGENT_REGION_LEVEL_HINT_MAP[form.agentLevel]}，当前选择了${getRegionLevelLabel(selectedLevel)}`,
        { type: "warning" }
      );
      return false;
    }
  }
  if (isPersonalApply.value) {
    if (!form.realName.trim() || !form.idCardNo.trim() || !subjectMobile) {
      message("个人主体请完整填写姓名、身份证号和本人手机号", {
        type: "warning"
      });
      return false;
    }
    return true;
  }
  if (isIndividualApply.value) {
    if (
      !form.businessLicenseUrl.trim() ||
      !form.creditCode.trim() ||
      !form.realName.trim() ||
      !form.idCardNo.trim() ||
      !subjectMobile
    ) {
      message("个体户请完整填写营业执照、信用代码、经营者姓名、身份证号和手机号", {
        type: "warning"
      });
      return false;
    }
    return true;
  }
  if (isCompanyLegalApply.value) {
    if (
      !form.businessLicenseUrl.trim() ||
      !form.creditCode.trim() ||
      !form.legalName.trim() ||
      !form.legalId.trim() ||
      !form.legalMobile.trim()
    ) {
      message("企业法人请完整填写营业执照、信用代码和法人信息", {
        type: "warning"
      });
      return false;
    }
    return true;
  }
  if (
    !form.businessLicenseUrl.trim() ||
    !form.creditCode.trim() ||
    !form.realName.trim() ||
    !form.idCardNo.trim() ||
    !form.legalMobile.trim()
  ) {
    message("企业非法人请完整填写营业执照、信用代码、被授权人信息和手机号", {
      type: "warning"
    });
    return false;
  }
  if (needsAuthorization.value && !form.authorizationUrl.trim()) {
    message("企业非法人主体请上传授权书", { type: "warning" });
    return false;
  }
  return true;
}

function buildStorePayload() {
  const subjectPayload = getSubjectPayloadByApplyType(form.applyType);
  const subjectMobile =
    isPersonalApply.value || isIndividualApply.value
      ? form.legalMobile.trim() || form.linkPhone.trim()
      : form.legalMobile.trim();
  const payload: Record<string, any> = {
    applyType: subjectPayload.applyType,
    subjectType: subjectPayload.subjectType,
    companyIdentityType: subjectPayload.companyIdentityType,
    storeType: form.storeType,
    storeName: form.storeName.trim(),
    storeLogo: form.storeLogo.trim() || undefined,
    storeDesc: form.storeDesc.trim(),
    goodsManagementCategory: selectedStoreCategoryIds.value.join(","),
    selfOperated: form.selfOperated,
    companyName: form.companyName.trim() || undefined,
    companyAddress: form.companyAddress.trim(),
    companyAddressIdPath: form.companyAddressIdPath.trim() || undefined,
    companyAddressPath: form.companyAddressPath.trim() || undefined,
    storeAddressDetail: form.companyAddress.trim(),
    storeAddressIdPath: form.companyAddressIdPath.trim() || undefined,
    storeAddressPath: form.companyAddressPath.trim() || undefined,
    linkName: form.linkName.trim() || undefined,
    linkPhone: form.linkPhone.trim() || undefined,
    companyPhone: form.companyPhone.trim() || undefined,
    companyEmail: form.companyEmail.trim() || undefined,
    registeredCapital:
      Number(form.registeredCapital || 0) > 0
        ? Number(form.registeredCapital || 0)
        : undefined,
    businessLicenseUrl: form.businessLicenseUrl.trim() || undefined,
    facadeImageUrl: form.facadeImageUrl.trim() || undefined,
    creditCode: form.creditCode.trim() || undefined,
    legalName: form.legalName.trim() || undefined,
    legalId: form.legalId.trim() || undefined,
    legalMobile: subjectMobile || undefined,
    authorizationUrl: form.authorizationUrl.trim() || undefined,
    realName: form.realName.trim() || undefined,
    idCardNo: form.idCardNo.trim() || undefined,
    idCardFrontUrl: form.idCardFrontUrl.trim() || undefined,
    idCardBackUrl: form.idCardBackUrl.trim() || undefined
  };
  if (!editingStoreId.value) {
    payload.memberId = form.memberId.trim();
  }
  if (isAgentStore.value) {
    payload.agentLevel = form.agentLevel;
    payload.agentRegionId = form.agentRegionId;
    payload.agentRegionName = form.agentRegionName;
  }
  return payload;
}

async function submitStoreForm() {
  if (!validateStoreForm()) return;
  saving.value = true;
  try {
    const payload = buildStorePayload();
    const response = editingStoreId.value
      ? await updateStore(editingStoreId.value, payload)
      : await createStore(payload);
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || "店铺保存失败");
    }
    if (editingStoreId.value) {
      message("店铺更新成功", { type: "success" });
    } else {
      message("店铺创建成功", { type: "success" });
    }
    dialogVisible.value = false;
    await refreshStorePage();
  } catch (error) {
    message(
      error instanceof Error && error.message
        ? error.message
        : "店铺保存失败，请检查表单必填项与字段契约",
      {
        type: "error"
      }
    );
  } finally {
    saving.value = false;
  }
}

function openAudit(row: Record<string, any>) {
  auditRow.value = row;
  auditForm.auditStatus = "APPROVED";
  auditForm.auditRemark = row.auditRemark || "";
  auditDialogVisible.value = true;
}

async function submitAudit() {
  if (!auditRow.value) return;
  auditSaving.value = true;
  try {
    await auditStore(String(auditRow.value.id), {
      auditStatus: auditForm.auditStatus as "APPROVED" | "REJECTED" | "FROZEN",
      auditRemark: auditForm.auditRemark.trim() || undefined
    });
    auditDialogVisible.value = false;
    message("店铺审核处理成功", { type: "success" });
    await refreshStorePage();
  } catch (_error) {
    message("店铺审核处理失败，请检查审核状态与备注", {
      type: "error"
    });
  } finally {
    auditSaving.value = false;
  }
}

async function toggleStoreStatus(row: Record<string, any>, action: "enable" | "disable") {
  const actionText = action === "enable" ? "启用" : "停业";
  await ElMessageBox.confirm(
    `确认${actionText}店铺「${row.storeName || row.id}」吗？`,
    `${actionText}确认`,
    { type: action === "enable" ? "success" : "warning" }
  );
  try {
    if (action === "enable") {
      await enableStore(String(row.id));
    } else {
      await disableStore(String(row.id));
    }
    message(`店铺已${actionText}`, { type: "success" });
    await refreshStorePage();
  } catch (_error) {
    message(`店铺${actionText}失败，请稍后重试`, { type: "error" });
  }
}

async function handleBatchStatus(action: "enable" | "disable") {
  if (!selectedIds.value.length) {
    message(`请先勾选需要${action === "enable" ? "启用" : "停业"}的店铺`, {
      type: "warning"
    });
    return;
  }
  const actionText = action === "enable" ? "启用" : "停业";
  await ElMessageBox.confirm(
    `确认批量${actionText}已勾选的 ${selectedIds.value.length} 个店铺吗？`,
    `批量${actionText}确认`,
    { type: action === "enable" ? "success" : "warning" }
  );
  try {
    await Promise.all(
      selectedIds.value.map(id =>
        action === "enable" ? enableStore(id) : disableStore(id)
      )
    );
    selectedRows.value = [];
    message(`批量${actionText}成功`, { type: "success" });
    await refreshStorePage();
  } catch (_error) {
    message(`批量${actionText}失败，请稍后重试`, { type: "error" });
  }
}

async function openAuditLog(row: Record<string, any>) {
  try {
    const res = await getStoreAuditLog(String(row.id));
    auditLogs.value = extractApiRecords(res).map((item: Record<string, any>) => ({
      ...item,
      displayAuditStatus: item.toAuditStatus || item.auditStatus || item.status || "-",
      displayOperatorName: item.operatorName || item.createBy || item.operatorId || "-",
      displayCreateTime: formatAdminDateTime(item.createTime)
    }));
    auditLogVisible.value = true;
  } catch (_error) {
    auditLogs.value = [];
    message("审核记录加载失败，请稍后重试", { type: "error" });
  }
}

function exportStores() {
  if (!filteredData.value.length) {
    message("暂无可导出的店铺数据", { type: "warning" });
    return;
  }
  const table = filteredData.value.map(item => ({
    店铺名称: item.storeName,
    申请主体: getApplyTypeLabel(item.applyType),
    业务类型: getStoreBizTypeLabel(item.bizType),
    联系人: item.contactName,
    联系电话: item.contactMobile,
    经营类目: item.storeCategoryName,
    店铺状态: getStoreStatusLabel(item.storeStatus),
    审核状态: getStoreAuditStatusLabel(item.auditStatus),
    审核备注: item.auditRemark || "-"
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "店铺管理");
  writeFile(workbook, "店铺管理.xlsx");
  message("店铺数据导出成功", { type: "success" });
}

onMounted(() => {
  ensureStoreCategoryOptions();
  refreshStorePage();
  loadRegionOptions();
});
</script>

<template>
  <WholesaleAdminPage
    title="店铺管理"
    description="承接店铺列表、新增、编辑、审核、启停与审核历史治理动作。当前后端未暴露物理删除接口，因此本页不伪造删除按钮。"
    api-path="/manager/store/store/get/detail/{storeId}"
    :columns="columns"
    :data="filteredData"
    selectable
    :summary-cards="summaryCards"
    :status-options="[
      { label: '营业中', value: 'OPEN' },
      { label: '已停业', value: 'CLOSE' },
      { label: '已冻结', value: 'FROZEN' }
    ]"
    :quick-actions="[
      { label: '店铺新增', value: '已接入', type: 'primary' },
      { label: '店铺编辑', value: '已接入', type: 'success' },
      { label: '批量启停', value: '已接入', type: 'warning' }
    ]"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增店铺</el-button>
      <el-button type="success" plain @click="handleBatchStatus('enable')">
        批量启用
      </el-button>
      <el-button type="warning" plain @click="handleBatchStatus('disable')">
        批量停业
      </el-button>
      <el-button @click="exportStores">导出</el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="经营类目">
        <el-input
          v-model="extraFilters.categoryKeyword"
          placeholder="请输入经营类目"
          clearable
        />
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select
          v-model="extraFilters.auditStatus"
          placeholder="请选择审核状态"
          clearable
        >
          <el-option label="草稿" value="DRAFT" />
          <el-option label="待审核" value="SUBMITTED" />
          <el-option label="审核通过" value="APPROVED" />
          <el-option label="审核驳回" value="REJECTED" />
          <el-option label="已冻结" value="FROZEN" />
        </el-select>
      </el-form-item>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button
        v-if="['DRAFT', 'SUBMITTED', 'PENDING', 'WAIT'].includes(String(row.auditStatus).toUpperCase())"
        link
        type="success"
        @click="openAudit(row)"
      >
        审核
      </el-button>
      <el-button
        v-if="['OPEN', 'ENABLE', 'APPROVED', 'TRUE', '1'].includes(String(row.storeStatus).toUpperCase())"
        link
        type="warning"
        @click="toggleStoreStatus(row, 'disable')"
      >
        停业
      </el-button>
      <el-button
        v-else
        link
        type="success"
        @click="toggleStoreStatus(row, 'enable')"
      >
        启用
      </el-button>
      <el-button link @click="openAuditLog(row)">审核记录</el-button>
    </template>
  </WholesaleAdminPage>

  <el-drawer v-model="detailVisible" title="店铺详情" size="42%">
    <div v-loading="detailLoading">
      <el-descriptions :column="1" border>
        <el-descriptions-item
          v-for="item in detailItems"
          :key="item.label"
          :label="item.label"
        >
          {{ item.value }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </el-drawer>

  <el-dialog
    v-model="dialogVisible"
    :title="editingStoreId ? '编辑店铺' : '新增店铺'"
    width="980px"
  >
    <el-alert
      title="当前管理端已支持新增/编辑/启停；物理删除暂无管理端后端接口，因此本页不提供删除提交。"
      type="info"
      :closable="false"
      class="store-form-alert"
    />
    <el-form label-width="108px">
      <div class="store-form-section">
        <h3>基础信息</h3>
        <div class="store-form-grid">
          <el-form-item label="所属会员" required>
            <el-select
              v-model="form.memberId"
              filterable
              remote
              clearable
              reserve-keyword
              :remote-method="searchUsers"
              :loading="userLoading"
              :disabled="Boolean(editingStoreId)"
              placeholder="请输入昵称或手机号搜索会员"
              style="width: 100%"
            >
              <el-option
                v-for="item in userOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="申请主体" required>
            <el-select v-model="form.applyType" style="width: 100%">
              <el-option
                v-for="item in APPLY_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="业务类型" required>
            <el-select
              v-model="form.storeType"
              style="width: 100%"
              @change="handleStoreTypeChange"
            >
              <el-option
                v-for="item in BIZ_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="店铺名称" required>
            <el-input v-model="form.storeName" placeholder="请输入店铺名称" />
          </el-form-item>
          <el-form-item label="经营类目" required>
            <el-select
              v-model="selectedStoreCategoryIds"
              multiple
              collapse-tags
              collapse-tags-tooltip
              filterable
              clearable
              :loading="storeCategoryLoading"
              placeholder="请选择店铺经营类目"
              style="width: 100%"
              @change="value => syncStoreCategoryForm((value as string[]) || [])"
            >
              <el-option
                v-for="item in storeCategoryOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <div class="mt-2 text-xs text-gray-400">
              经营类目从平台一级商品分类中选择，编辑时会按已保存类目自动回显
            </div>
          </el-form-item>
          <el-form-item label="是否自营">
            <el-switch v-model="form.selfOperated" />
          </el-form-item>
          <el-form-item label="店铺简介" required class="store-form-grid__full">
            <el-input
              v-model="form.storeDesc"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="请输入店铺简介，至少 6 个字符"
            />
          </el-form-item>
        </div>
      </div>

      <div class="store-form-section">
        <h3>图片资料</h3>
        <div class="store-form-grid">
          <el-form-item label="店铺 Logo">
            <ImageUploadField
              v-model="form.storeLogo"
              tip="店铺 Logo 统一走上传组件维护"
            />
          </el-form-item>
          <el-form-item label="门头照">
            <ImageUploadField
              v-model="form.facadeImageUrl"
              tip="门头照建议上传实际店铺外观图"
            />
          </el-form-item>
          <el-form-item
            v-if="!isPersonalApply"
            label="营业执照"
            required
          >
            <ImageUploadField
              v-model="form.businessLicenseUrl"
              tip="个体户及企业主体都需要上传营业执照图片"
            />
          </el-form-item>
          <el-form-item
            v-if="isPersonalApply || isIndividualApply || isCompanyAuthorizedApply"
            label="身份证正面"
            required
          >
            <ImageUploadField
              v-model="form.idCardFrontUrl"
              tip="请上传当前主体对应经办人的身份证正面"
            />
          </el-form-item>
          <el-form-item
            v-if="isPersonalApply || isIndividualApply || isCompanyAuthorizedApply"
            label="身份证反面"
            required
          >
            <ImageUploadField
              v-model="form.idCardBackUrl"
              tip="请上传当前主体对应经办人的身份证反面"
            />
          </el-form-item>
          <el-form-item
            v-if="needsAuthorization"
            label="授权书"
            required
          >
            <ImageUploadField
              v-model="form.authorizationUrl"
              tip="企业非法人主体需要上传授权书"
            />
          </el-form-item>
        </div>
      </div>

      <div class="store-form-section">
        <h3>联系人与地址</h3>
        <div class="store-form-grid">
          <el-form-item label="联系人">
            <el-input v-model="form.linkName" placeholder="请输入联系人姓名" />
          </el-form-item>
          <el-form-item label="联系人电话">
            <el-input v-model="form.linkPhone" placeholder="请输入联系人手机号" />
          </el-form-item>
          <el-form-item label="公司电话">
            <el-input v-model="form.companyPhone" placeholder="请输入公司电话" />
          </el-form-item>
          <el-form-item label="公司邮箱">
            <el-input v-model="form.companyEmail" placeholder="请输入公司邮箱" />
          </el-form-item>
          <el-form-item label="公司名称">
            <el-input v-model="form.companyName" placeholder="请输入公司名称，可选" />
          </el-form-item>
          <el-form-item label="注册资金">
            <el-input-number
              v-model="form.registeredCapital"
              :min="1"
              :max="999999999"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="公司地区">
            <el-cascader
              v-model="selectedCompanyRegionIds"
              :options="regionOptions"
              :props="regionCascaderProps"
              clearable
              filterable
              :show-all-levels="true"
              placeholder="请选择公司所在地区"
              style="width: 100%"
              @change="value => syncCompanyRegionForm((value as string[]) || [])"
            />
          </el-form-item>
          <el-form-item label="公司详细地址" required>
            <el-input
              v-model="form.companyAddress"
              placeholder="请输入公司详细地址"
            />
          </el-form-item>
        </div>
        <div class="store-form-tip">
          当前公司地区路径：{{ form.companyAddressPath || "未选择" }}
        </div>
      </div>

      <div v-if="isAgentStore" class="store-form-section">
        <h3>代理信息</h3>
        <div class="store-form-grid">
          <el-form-item label="代理等级" required>
            <el-select v-model="form.agentLevel" style="width: 100%" @change="handleAgentLevelChange">
              <el-option
                v-for="item in AGENT_LEVEL_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="代理区域" required>
            <el-cascader
              v-model="selectedAgentRegionIds"
              :options="regionOptions"
              :props="regionCascaderProps"
              clearable
              filterable
              :show-all-levels="true"
              placeholder="请选择代理区域"
              style="width: 100%"
              @change="value => syncAgentRegionForm((value as string[]) || [])"
            />
            <div class="mt-2 text-xs text-gray-400">
              {{ AGENT_REGION_LEVEL_HINT_MAP[form.agentLevel] || "请先选择代理等级，再选择匹配层级的代理区域" }}
            </div>
          </el-form-item>
          <el-form-item label="区域名称">
            <el-input :model-value="form.agentRegionName || '未选择'" disabled />
          </el-form-item>
          <el-form-item label="区域 ID">
            <el-input :model-value="form.agentRegionId || '未选择'" disabled />
          </el-form-item>
        </div>
      </div>

      <div v-if="isIndividualApply" class="store-form-section">
        <h3>个体户主体信息</h3>
        <div class="store-form-grid">
          <el-form-item label="统一信用代码" required>
            <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
          </el-form-item>
          <el-form-item label="经营者姓名" required>
            <el-input v-model="form.realName" placeholder="请输入经营者姓名" />
          </el-form-item>
          <el-form-item label="经营者身份证" required>
            <el-input v-model="form.idCardNo" placeholder="请输入经营者身份证号" />
          </el-form-item>
          <el-form-item label="经营者手机号" required>
            <el-input v-model="form.legalMobile" placeholder="请输入经营者手机号" />
          </el-form-item>
        </div>
      </div>

      <div v-if="isCompanyApply" class="store-form-section">
        <h3>{{ isCompanyLegalApply ? "企业法人信息" : "企业被授权人信息" }}</h3>
        <div class="store-form-grid">
          <el-form-item label="统一信用代码" required>
            <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
          </el-form-item>
          <el-form-item v-if="isCompanyLegalApply" label="法人姓名" required>
            <el-input v-model="form.legalName" placeholder="请输入法人姓名" />
          </el-form-item>
          <el-form-item v-if="isCompanyLegalApply" label="法人身份证" required>
            <el-input v-model="form.legalId" placeholder="请输入法人身份证号" />
          </el-form-item>
          <el-form-item v-if="isCompanyLegalApply" label="法人手机号" required>
            <el-input v-model="form.legalMobile" placeholder="请输入法人手机号" />
          </el-form-item>
          <el-form-item v-if="isCompanyAuthorizedApply" label="被授权人姓名" required>
            <el-input v-model="form.realName" placeholder="请输入被授权人姓名" />
          </el-form-item>
          <el-form-item v-if="isCompanyAuthorizedApply" label="被授权人身份证" required>
            <el-input v-model="form.idCardNo" placeholder="请输入被授权人身份证号" />
          </el-form-item>
          <el-form-item v-if="isCompanyAuthorizedApply" label="被授权人手机号" required>
            <el-input v-model="form.legalMobile" placeholder="请输入被授权人手机号" />
          </el-form-item>
        </div>
      </div>

      <div v-if="isPersonalApply" class="store-form-section">
        <h3>个人主体信息</h3>
        <div class="store-form-grid">
          <el-form-item label="真实姓名" required>
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="身份证号" required>
            <el-input v-model="form.idCardNo" placeholder="请输入身份证号" />
          </el-form-item>
          <el-form-item label="本人手机号" required>
            <el-input v-model="form.legalMobile" placeholder="请输入本人手机号" />
          </el-form-item>
        </div>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitStoreForm">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="auditDialogVisible" title="店铺审核" width="560px">
    <el-form label-width="96px">
      <el-form-item label="审核状态" required>
        <el-select v-model="auditForm.auditStatus" style="width: 100%">
          <el-option label="审核通过" value="APPROVED" />
          <el-option label="审核驳回" value="REJECTED" />
          <el-option label="冻结" value="FROZEN" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核备注">
        <el-input
          v-model="auditForm.auditRemark"
          type="textarea"
          :rows="4"
          placeholder="请输入审核备注，可留空"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="auditDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="auditSaving" @click="submitAudit">
        提交
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="auditLogVisible" title="审核记录" size="48%">
    <el-table :data="auditLogs" border>
      <el-table-column label="审核状态" min-width="120">
        <template #default="{ row }">
          {{ getStoreAuditStatusLabel(row.displayAuditStatus) }}
        </template>
      </el-table-column>
      <el-table-column label="审核备注" prop="auditRemark" min-width="220" />
      <el-table-column label="操作人" prop="displayOperatorName" min-width="120" />
      <el-table-column label="时间" prop="displayCreateTime" min-width="180" />
    </el-table>
  </el-drawer>
</template>

<style scoped>
.store-form-alert {
  margin-bottom: 18px;
}

.store-form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px 0;
  border-top: 1px solid #f0f1f4;
}

.store-form-section:first-of-type {
  padding-top: 0;
  border-top: none;
}

.store-form-section h3 {
  margin: 0;
  color: #2f3135;
  font-size: 16px;
  font-weight: 700;
}

.store-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 18px;
}

.store-form-grid__full {
  grid-column: 1 / -1;
}

.store-form-tip {
  color: #8a5b20;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 960px) {
  .store-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
