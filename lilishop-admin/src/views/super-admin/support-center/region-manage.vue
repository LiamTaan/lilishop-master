<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createRegion,
  deleteRegion,
  getRegionAllCityTree,
  getRegionChildren,
  getRegionDetail,
  getRegionRootList,
  syncRegion,
  updateRegion
} from "@/api/super-admin";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { isSuccessResult, unwrapResult } from "@/utils/result";

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const syncVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const currentChildren = ref<Record<string, any>[]>([]);
const editingRow = ref<Record<string, any> | null>(null);
const regionMap = ref(new Map<string, Record<string, any>>());
const rootRegion = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "" });
const form = reactive({
  id: "",
  parentId: "",
  adCode: "",
  cityCode: "",
  name: "",
  path: "",
  center: "",
  level: "",
  orderNum: 0
});
const syncForm = reactive({
  url: ""
});

const columns: TableColumnList = [
  { label: "区域名称", prop: "displayName", minWidth: 200 },
  { label: "层级", prop: "displayLevel", minWidth: 100 },
  { label: "城市编码", prop: "displayCode", minWidth: 140 },
  { label: "上级区域", prop: "displayParent", minWidth: 180 },
  { label: "完整路径", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 300, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "区域总数", value: rows.value.length, accent: "orange" as const, hint: "当前加载的区域节点" },
  { label: "省级区域", value: rows.value.filter(item => item.level === "province").length, accent: "green" as const, hint: "当前列表中的省级节点" },
  { label: "可下钻节点", value: rows.value.filter(item => item.hasChildren).length, accent: "blue" as const, hint: "支持继续查看子区域" },
  { label: "治理动作", value: "详情/子级/新增/编辑/删除", accent: "purple" as const, hint: "已对齐真实区域接口" }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

const LEVEL_LABEL_MAP: Record<string, string> = {
  country: "国家",
  province: "省",
  city: "市",
  district: "区县",
  street: "街道"
};

function rememberRegions(list: Record<string, any>[]) {
  list.forEach(item => {
    if (item?.id) {
      regionMap.value.set(String(item.id), item);
    }
  });
}

function getLevelLabel(level: unknown) {
  const value = String(level || "").trim();
  return LEVEL_LABEL_MAP[value] || value || "-";
}

function buildPathNames(item: Record<string, any>, currentName?: string, fallbackParentName?: string) {
  const ids = String(item?.path || "")
    .split(",")
    .map(value => value.trim())
    .filter(value => value && value !== "0");
  const names = ids
    .map(id => regionMap.value.get(id)?.name)
    .filter(Boolean) as string[];
  if (!names.length && fallbackParentName && fallbackParentName !== "-") {
    names.push(fallbackParentName);
  }
  const finalName = currentName || item?.name || item?.regionName || "";
  if (finalName && names[names.length - 1] !== finalName) {
    names.push(finalName);
  }
  return names.join(" / ") || finalName || "-";
}

function normalizeRecord(item: Record<string, any>, fallbackParentName = "-") {
  const currentName = item.name || item.regionName || "-";
  const parentId = String(item.parentId ?? "");
  const parentName =
    item.parentName ||
    (parentId && parentId !== "0"
      ? regionMap.value.get(parentId)?.name
      : "") ||
    fallbackParentName;
  const level = String(item.level || "");
  return {
    ...item,
    id: item.id || item.regionId || item.name,
    level,
    hasChildren:
      Boolean(item.childrenNum || item.hasChildren || item.subCount) ||
      ["country", "province", "city", "district"].includes(level),
    displayName: currentName,
    displayLevel: getLevelLabel(level),
    displayCode: item.cityCode || item.adCode || item.code || "-",
    displayParent: parentId === "0" ? "顶级区域" : parentName || "-",
    displayRemark: buildPathNames(item, currentName, parentName || "-")
  };
}

function flattenAllCityTree(tree: Record<string, any>[]) {
  const flatRows: Record<string, any>[] = [];
  tree.forEach(province => {
    flatRows.push(
      normalizeRecord(
        {
          ...province,
          level: province.level || "province",
          parentId: province.parentId || rootRegion.value?.id || "0",
          path: province.path || `${rootRegion.value?.id ? `,0,${rootRegion.value.id}` : ""}`
        },
        rootRegion.value?.name || "顶级区域"
      )
    );
    (province.children || []).forEach(city => {
      flatRows.push(
        normalizeRecord(
          {
            ...city,
            level: city.level || "city",
            parentId: city.parentId || province.id,
            path:
              city.path ||
              [
                "",
                "0",
                rootRegion.value?.id,
                province.id
              ]
                .filter(Boolean)
                .join(",")
          },
          province.name || "-"
        )
      );
    });
  });
  return flatRows;
}

async function loadData() {
  try {
    const rootRes = await getRegionRootList();
    const rootList = extractApiRecords(rootRes);
    rememberRegions(rootList);
    const countryNode =
      rootList.find(item => String(item.level || "").trim() === "country") || null;
    rootRegion.value = countryNode;
    let list: Record<string, any>[] = [];
    if (query.keyword) {
      const treeRes = await getRegionAllCityTree();
      const treePayload = isSuccessResult(treeRes)
        ? (unwrapResult(treeRes) ?? [])
        : [];
      const tree = Array.isArray(treePayload) ? treePayload : [];
      rememberRegions(
        tree.flatMap(item => [item, ...((item.children as Record<string, any>[]) || [])])
      );
      list = flattenAllCityTree(tree);
    } else if (countryNode?.id) {
      const provinceRes = await getRegionChildren(String(countryNode.id));
      const provinces = extractApiRecords(provinceRes);
      rememberRegions(provinces);
      list = provinces.map(item => normalizeRecord(item, countryNode.name || "顶级区域"));
    } else {
      list = rootList.map(item => normalizeRecord(item));
    }
    if (query.keyword) {
      list = list.filter(
        item =>
          String(item.displayName || "").includes(query.keyword) ||
          String(item.displayRemark || "").includes(query.keyword) ||
          String(item.displayCode || "").includes(query.keyword)
      );
    }
    rows.value = list;
    selectedRows.value = [];
  } catch (_error) {
    rows.value = [];
    message("区域管理加载失败，请稍后重试", { type: "error" });
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

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

function resetForm() {
  form.id = "";
  form.parentId = "";
  form.adCode = "";
  form.cityCode = "";
  form.name = "";
  form.path = "";
  form.center = "";
  form.level = "";
  form.orderNum = 0;
}

function openCreate(parent?: Record<string, any>) {
  editingRow.value = null;
  resetForm();
  if (parent) {
    form.parentId = String(parent.id || "");
    form.path = `${String(parent.path || "").trim()},${String(parent.id || "")}`;
    form.level = "";
  } else {
    form.parentId = "0";
    form.path = ",0";
  }
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  resetForm();
  try {
    const res = await getRegionDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(res) || row;
    rememberRegions([detail]);
    form.id = detail.id || row.id;
    form.parentId = detail.parentId || "";
    form.adCode = detail.adCode || "";
    form.cityCode = detail.cityCode || "";
    form.name = detail.name || row.displayName;
    form.path = detail.path || "";
    form.center = detail.center || "";
    form.level = String(detail.level ?? "");
    form.orderNum = Number(detail.orderNum || 0);
  } catch (_error) {
    form.id = row.id;
    form.parentId = row.parentId || "";
    form.adCode = row.adCode || "";
    form.cityCode = row.cityCode || "";
    form.name = row.displayName;
    form.path = row.path || "";
    form.center = row.center || "";
    form.level = String(row.level ?? "");
  }
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  try {
    const [detailRes, childRes] = await Promise.all([
      getRegionDetail(String(row.id)),
      getRegionChildren(String(row.id))
    ]);
    const detail = extractApiPayload<Record<string, any>>(detailRes) || row;
    const children = extractApiRecords(childRes);
    rememberRegions([detail, ...children]);
    currentRow.value = normalizeRecord(detail, row.displayParent || "-");
    currentChildren.value = children.map(item =>
      normalizeRecord(item, detail.name || row.displayName || "-")
    );
  } catch (_error) {
    currentChildren.value = [];
  }
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.name.trim()) {
    message("请输入区域名称", { type: "warning" });
    return;
  }
  if (!form.parentId.trim()) {
    message("请输入上级区域ID", { type: "warning" });
    return;
  }
  if (!form.adCode.trim()) {
    message("请输入区域编码", { type: "warning" });
    return;
  }
  if (!form.path.trim()) {
    message("请输入路径ID串", { type: "warning" });
    return;
  }
  if (!form.center.trim()) {
    message("请输入中心点经纬度", { type: "warning" });
    return;
  }
  if (!form.level.trim()) {
    message("请输入层级值", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      id: form.id || undefined,
      parentId: form.parentId || undefined,
      adCode: form.adCode,
      cityCode: form.cityCode,
      name: form.name,
      path: form.path,
      center: form.center,
      level: form.level,
      orderNum: form.orderNum
    };
    if (editingRow.value) {
      await updateRegion(String(editingRow.value.id), payload);
      message("区域信息修改成功", { type: "success" });
    } else {
      await createRegion(payload);
      message("区域信息新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("区域信息保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除区域「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteRegion([String(row.id)]);
    message("区域删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("区域删除失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的区域", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 个区域吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await deleteRegion(selectedIds.value);
    selectedRows.value = [];
    message("区域批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("区域批量删除失败", { type: "error" });
  }
}

async function handleSync() {
  if (!syncForm.url.trim()) {
    message("请输入高德行政地区同步地址", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    await syncRegion(syncForm.url.trim());
    message("区域同步任务已触发", { type: "success" });
    syncVisible.value = false;
    await loadData();
  } catch (_error) {
    message("区域同步失败，请确认同步地址或后端权限", { type: "error" });
  } finally {
    saving.value = false;
  }
}

function exportRegions() {
  if (!rows.value.length) {
    message("暂无可导出的区域数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    区域名称: item.displayName,
    层级: item.displayLevel,
    城市编码: item.displayCode,
    上级区域: item.displayParent,
    完整路径: item.displayRemark
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "区域管理");
  writeFile(workbook, "区域管理.xlsx");
  message("区域数据导出成功", { type: "success" });
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="区域管理"
    description="承接行政区域基础资料治理，支持区域详情、子级查看、新增、编辑、删除与同步入口，作为基础支撑的真实配置页。"
    api-path="/manager/setting/region"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '区域新增', value: '已接入', type: 'primary' },
      { label: '子级下钻', value: '已接入', type: 'success' },
      { label: '同步入口', value: '已接入', type: 'warning' }
    ]"
    keyword-label="区域名称"
    keyword-placeholder="请输入区域名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportRegions">导出</el-button>
      <el-button type="primary" @click="openCreate()">新增区域</el-button>
      <el-button type="warning" plain @click="syncVisible = true">同步行政区域</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openCreate(row)">新增子级</el-button>
      <el-button link type="warning" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑区域' : '新增区域'" width="700px">
    <el-form label-width="110px">
      <el-form-item label="上级区域ID"><el-input v-model="form.parentId" placeholder="顶级区域请填 0" /></el-form-item>
      <el-form-item label="区域名称" required><el-input v-model="form.name" placeholder="请输入区域名称" /></el-form-item>
      <el-form-item label="区域编码" required><el-input v-model="form.adCode" placeholder="请输入区域编码（adCode）" /></el-form-item>
      <el-form-item label="城市编码"><el-input v-model="form.cityCode" placeholder="请输入城市编码" /></el-form-item>
      <el-form-item label="路径ID串" required><el-input v-model="form.path" placeholder="请输入路径ID串，例如 ,0,父ID" /></el-form-item>
      <el-form-item label="中心点" required><el-input v-model="form.center" placeholder="请输入中心点经纬度，例如 113.753094,34.767052" /></el-form-item>
      <el-form-item label="层级" required><el-input v-model="form.level" placeholder="请输入层级值，例如 province/city/district/street" /></el-form-item>
      <el-form-item label="排序值"><el-input-number v-model="form.orderNum" :min="0" style="width: 100%" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="syncVisible" title="同步行政区域" width="620px">
    <el-form label-width="128px">
      <el-form-item label="高德接口地址" required>
        <el-input v-model="syncForm.url" placeholder="请输入高德行政区域同步地址" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="syncVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSync">开始同步</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="区域详情" size="720px">
    <el-descriptions v-if="currentRow" :column="1" border class="region-desc">
      <el-descriptions-item label="区域名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="层级">{{ currentRow.displayLevel }}</el-descriptions-item>
      <el-descriptions-item label="城市编码">{{ currentRow.displayCode }}</el-descriptions-item>
      <el-descriptions-item label="上级区域">{{ currentRow.displayParent }}</el-descriptions-item>
      <el-descriptions-item label="完整路径">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="原始数据"><pre class="detail-json">{{ JSON.stringify(currentRow, null, 2) }}</pre></el-descriptions-item>
    </el-descriptions>
    <el-table :data="currentChildren" border>
      <el-table-column label="子级名称" prop="displayName" min-width="180" />
      <el-table-column label="层级" prop="displayLevel" width="100" />
      <el-table-column label="城市编码" prop="displayCode" min-width="140" />
      <el-table-column label="完整路径" prop="displayRemark" min-width="220" show-overflow-tooltip />
    </el-table>
  </el-drawer>
</template>

<style scoped>
.region-desc {
  margin-bottom: 16px;
}

.detail-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #5d6168;
}
</style>
