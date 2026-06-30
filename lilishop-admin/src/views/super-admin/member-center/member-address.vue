<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createMemberAddress,
  deleteMemberAddress,
  getMemberDetail,
  getMemberAddressListPage,
  getMemberAddressPage,
  getMemberPage,
  getRegionChildren,
  getRegionRootList,
  updateMemberAddress
} from "@/api/super-admin";
import {
  extractApiPayload,
  extractApiRecords
} from "@/utils/admin-governance";
import { getErrorMessage } from "@/utils/error";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const userOptions = ref<
  Array<{
    label: string;
    value: string;
    nickName: string;
    mobile: string;
  }>
>([]);
const userLoading = ref(false);
const regionOptions = ref<Record<string, any>[]>([]);
const regionNameMap = ref(new Map<string, string>());
const rootRegionId = ref("");
const rootRegionParentId = ref("0");
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const selectedRegionIds = ref<string[]>([]);
const query = reactive({ keyword: "", memberKeyword: "", isDefault: "" });
const form = reactive({
  id: "",
  memberId: "",
  name: "",
  mobile: "",
  consigneeAddressPath: "",
  consigneeAddressIdPath: "",
  detail: "",
  alias: "",
  isDefault: false
});

const columns: TableColumnList = [
  { label: "所属用户", prop: "displayUser", minWidth: 240, showOverflowTooltip: true },
  { label: "收货人", prop: "displayName", minWidth: 140 },
  { label: "手机号", prop: "displayMobile", minWidth: 150 },
  { label: "地址别名", prop: "displayAlias", minWidth: 120 },
  { label: "地址状态", prop: "displayDefault", minWidth: 120 },
  { label: "详细地址", prop: "displayRemark", minWidth: 280, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 220, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "地址总数", value: rows.value.length, accent: "orange" as const, hint: "当前筛选结果地址记录" },
  { label: "默认地址", value: rows.value.filter(item => item.isDefault).length, accent: "green" as const, hint: "标记为默认地址的记录" },
  { label: "别名地址", value: rows.value.filter(item => item.displayAlias !== "-").length, accent: "blue" as const, hint: "带地址别名的记录" },
  { label: "治理动作", value: "查询/新增/编辑/删除", accent: "purple" as const, hint: "地址台账真实承接页" }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

function normalizeId(value: unknown) {
  if (value === undefined || value === null) return "";
  return String(value).trim();
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

function getUserDisplay(memberId?: string) {
  const targetId = String(memberId || "").trim();
  if (!targetId) return "-";
  const option = userOptions.value.find(item => item.value === targetId);
  return option?.label || targetId;
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

async function fetchRegionChildren(parentId: string, parentPath = "") {
  try {
    const res = await getRegionChildren(parentId);
    const list = extractApiRecords<Record<string, any>>(res) || [];
    return list.map(item => normalizeRegionNode(item, parentPath));
  } catch (_error) {
    return [];
  }
}

async function loadRegionNode(node: Record<string, any>, resolve: (data: Record<string, any>[]) => void) {
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
    rootRegionId.value = hasCountryRoot ? normalizeId(rootRegion?.id) : "";
    rootRegionParentId.value = hasCountryRoot ? normalizeId(rootRegion?.id) : "0";
    regionNameMap.value = new Map();
    regionOptions.value = hasCountryRoot
      ? await fetchRegionChildren(rootRegionId.value)
      : rootList.map(item => normalizeRegionNode(item));
  } catch (_error) {
    regionOptions.value = [];
    regionNameMap.value = new Map();
    rootRegionParentId.value = "0";
    message("地区选项加载失败，地址表单暂无法自动选择地区", {
      type: "warning"
    });
  }
}

function syncRegionForm(ids: string[]) {
  const values = ids.map(item => item.trim()).filter(Boolean);
  selectedRegionIds.value = values;
  form.consigneeAddressIdPath = values.join(",");
  form.consigneeAddressPath = values
    .map(id => regionNameMap.value.get(id)?.split(",").at(-1) || "")
    .filter(Boolean)
    .join(",");
}

function restoreRegionSelection(addressIdPath?: string) {
  const ids = String(addressIdPath || "")
    .split(",")
    .map(item => item.trim())
    .filter(Boolean);
  selectedRegionIds.value = ids;
}

async function ensureRegionPathLoaded(ids: string[]) {
  if (!ids.length || !rootRegionParentId.value) return;
  let parentId = rootRegionParentId.value;
  let parentPath = "";
  for (const currentId of ids) {
    const siblings =
      parentId === rootRegionParentId.value && !regionOptions.value.length
        ? await fetchRegionChildren(parentId, parentPath)
        : await fetchRegionChildren(parentId, parentPath);
    if (parentId === rootRegionParentId.value) {
      regionOptions.value = siblings;
    } else {
      const parentNode = findRegionNode(regionOptions.value, parentId);
      if (parentNode) {
        parentNode.children = siblings;
      }
    }
    const currentNode = findRegionNode(regionOptions.value, currentId);
    if (!currentNode) break;
    parentId = currentId;
    parentPath = regionNameMap.value.get(currentId) || parentPath;
  }
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
    const detail = extractApiPayload<Record<string, any>>(res);
    if (detail) {
      mergeUserOptions([detail]);
    }
  } catch (_error) {
    // 用户详情仅用于补齐已选项，失败时保留现有交互即可
  }
}

async function fetchMemberDetail(memberId: string) {
  const targetId = memberId.trim();
  if (!targetId) return null;
  try {
    const res = await getMemberDetail(targetId);
    const detail = extractApiPayload<Record<string, any>>(res);
    if (detail) {
      mergeUserOptions([detail]);
      return detail;
    }
  } catch (_error) {
    // 详情补齐失败时由调用方决定是否降级
  }
  return null;
}

function normalizeRecord(item: Record<string, any>) {
  const memberId = item.memberId || form.memberId;
  const memberNickName =
    item.memberNickName || item.nickName || item.memberName || "";
  const memberUsername = item.memberUsername || item.username || "";
  const memberMobile = item.memberMobile || "";
  return {
    ...item,
    id: item.id || item.addressId,
    memberId,
    isDefault: Boolean(item.isDefault),
    displayUser:
      memberNickName || memberUsername || getUserDisplay(memberId),
    displayMemberId: item.memberId || "-",
    displayName: item.name || item.consignee || "-",
    displayMobile: item.mobile || "-",
    displayAlias: item.alias || "-",
    displayDefault: Boolean(item.isDefault) ? "默认地址" : "普通地址",
    displayRemark:
      [item.consigneeAddressPath, item.detail].filter(Boolean).join(" / ") || "-",
    displayTime: item.createTime || item.updateTime || "-"
  };
}

async function loadData() {
  try {
    const res = await getMemberAddressListPage({
      pageNumber: 1,
      pageSize: 200,
      keyword: query.keyword.trim() || undefined,
      memberKeyword: query.memberKeyword.trim() || undefined,
      isDefault:
        query.isDefault === ""
          ? undefined
          : query.isDefault === "true"
    });
    rows.value = extractApiRecords(res).map(normalizeRecord);
    selectedRows.value = [];
  } catch (_error) {
    message("用户地址加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string; status?: string }) {
  query.keyword = payload.keyword || "";
  query.isDefault = payload.status || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.memberKeyword = "";
  query.isDefault = "";
  rows.value = [];
  selectedRows.value = [];
  loadData();
}

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

async function openCreate() {
  editingRow.value = null;
  form.id = "";
  form.memberId = "";
  form.name = "";
  form.mobile = "";
  form.consigneeAddressPath = "";
  form.consigneeAddressIdPath = "";
  selectedRegionIds.value = [];
  form.detail = "";
  form.alias = "";
  form.isDefault = false;
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  form.id = row.id;
  form.memberId = row.memberId || "";
  await ensureUserOption(form.memberId);
  await fetchMemberDetail(form.memberId);
  form.name = row.name || row.displayName;
  form.mobile = row.mobile || "";
  form.consigneeAddressPath = row.consigneeAddressPath || "";
  form.consigneeAddressIdPath = row.consigneeAddressIdPath || "";
  restoreRegionSelection(form.consigneeAddressIdPath);
  await ensureRegionPathLoaded(selectedRegionIds.value);
  form.detail = row.detail || "";
  form.alias = row.alias || "";
  form.isDefault = Boolean(row.isDefault);
  dialogVisible.value = true;
}

function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.memberId.trim() || !form.name.trim() || !form.detail.trim()) {
    message("请完整填写所属用户、收货人和详细地址", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      id: form.id || undefined,
      memberId: form.memberId,
      name: form.name,
      mobile: form.mobile,
      consigneeAddressPath: form.consigneeAddressPath,
      consigneeAddressIdPath: form.consigneeAddressIdPath,
      detail: form.detail,
      alias: form.alias,
      isDefault: form.isDefault
    };
    if (editingRow.value) {
      await updateMemberAddress(payload);
      message("用户地址修改成功", { type: "success" });
    } else {
      await createMemberAddress(payload);
      message("用户地址新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (error) {
    message(getErrorMessage(error, "用户地址保存失败，请确认地址字段契约"), {
      type: "error"
    });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除地址「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteMemberAddress(String(row.id));
    message("用户地址删除成功", { type: "success" });
    await loadData();
  } catch (error) {
    message(getErrorMessage(error, "用户地址删除失败"), { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的地址", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 条地址吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedIds.value.map(id => deleteMemberAddress(id)));
    selectedRows.value = [];
    message("用户地址批量删除成功", { type: "success" });
    await loadData();
  } catch (error) {
    message(getErrorMessage(error, "用户地址批量删除失败"), { type: "error" });
  }
}

function exportMemberAddresses() {
  if (!rows.value.length) {
    message("暂无可导出的用户地址数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    所属用户: item.displayUser,
    收货人: item.displayName,
    手机号: item.displayMobile,
    地址别名: item.displayAlias,
    地址状态: item.displayDefault,
    详细地址: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "用户地址");
  writeFile(workbook, "用户地址.xlsx");
  message("用户地址导出成功", { type: "success" });
}

onMounted(() => {
  loadRegionOptions();
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="用户地址"
    description="承接前台用户收货地址台账、地址编辑和删除治理。列表按地址本身与所属用户筛选，选择用户仅用于新增/编辑维护。"
    api-path="/manager/member/address"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    status-label="默认地址"
    :status-options="[
      { label: '默认地址', value: 'true' },
      { label: '普通地址', value: 'false' }
    ]"
    :quick-actions="[
      { label: '地址筛选', value: '按地址与用户查询', type: 'primary' },
      { label: '地址维护', value: '新增/编辑/删除', type: 'success' },
      { label: '批删导出', value: '已接入', type: 'warning' }
    ]"
    keyword-label="地址关键词"
    keyword-placeholder="请输入收货人、手机号、别名或详细地址"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #filters-extra>
      <el-form-item label="所属用户">
        <el-input
          v-model="query.memberKeyword"
          clearable
          placeholder="请输入用户昵称、账号或手机号"
          style="width: 100%"
        />
      </el-form-item>
    </template>
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportMemberAddresses">导出</el-button>
      <el-button type="success" plain @click="openCreate">新增地址</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑用户地址' : '新增用户地址'" width="680px">
    <el-form label-width="110px">
      <el-form-item label="所属用户" required>
        <el-select
          v-model="form.memberId"
          filterable
          remote
          clearable
          reserve-keyword
          :remote-method="searchUsers"
          :loading="userLoading"
          placeholder="请输入昵称或手机号搜索用户"
          style="width: 100%"
          @change="value => ensureUserOption(String(value || ''))"
        >
          <el-option
            v-for="item in userOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="收货人" required><el-input v-model="form.name" placeholder="请输入收货人姓名" /></el-form-item>
      <el-form-item label="手机号">
        <el-input
          v-model="form.mobile"
          placeholder="已脱敏可直接保存；如需修改请输入完整新手机号"
        />
      </el-form-item>
      <el-form-item label="所在地区">
        <el-cascader
          v-model="selectedRegionIds"
          :options="regionOptions"
          :props="regionCascaderProps"
          clearable
          filterable
          :show-all-levels="true"
          placeholder="请选择省/市/区/街道，系统将自动生成地区路径"
          style="width: 100%"
          @change="value => syncRegionForm((value as string[]) || [])"
        />
      </el-form-item>
      <div class="form-tip">
        当前地区路径：{{ form.consigneeAddressPath || "未选择" }}
        <br />
        当前地区ID路径：{{ form.consigneeAddressIdPath || "未生成" }}
      </div>
      <el-form-item label="详细地址" required><el-input v-model="form.detail" type="textarea" :rows="4" placeholder="请输入详细地址" /></el-form-item>
      <el-form-item label="地址别名"><el-input v-model="form.alias" placeholder="如：家、公司" /></el-form-item>
      <el-form-item label="默认地址"><el-switch v-model="form.isDefault" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="用户地址详情" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="所属用户">{{ currentRow.displayUser }}</el-descriptions-item>
      <el-descriptions-item label="用户ID">{{ currentRow.displayMemberId }}</el-descriptions-item>
      <el-descriptions-item label="收货人">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ currentRow.displayMobile }}</el-descriptions-item>
      <el-descriptions-item label="地址别名">{{ currentRow.displayAlias }}</el-descriptions-item>
      <el-descriptions-item label="地址状态">{{ currentRow.displayDefault }}</el-descriptions-item>
      <el-descriptions-item label="详细地址">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<style scoped>
.detail-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #5d6168;
}

.form-tip {
  margin: -4px 0 14px 110px;
  font-size: 12px;
  color: #8a5b20;
  line-height: 1.6;
}
</style>
