<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  createGoodsGroup,
  deleteGoodsGroup,
  getGoodsGroupDetail,
  getGoodsGroupGoodsPage,
  getGoodsGroupPage,
  updateGoodsGroup,
  updateGoodsGroupGoods
} from "@/api/goods-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import { extractApiPayload, extractApiRecords, formatAdminDateTime } from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "GoodsGroupManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const groupGoods = ref<Record<string, any>[]>([]);
const detailRow = ref<Record<string, any> | null>(null);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const goodsDialogVisible = ref(false);
const saving = ref(false);
const goodsSaving = ref(false);
const editingId = ref("");
const goodsGroupId = ref("");
const query = reactive({
  keyword: "",
  status: ""
});
const form = reactive({
  groupName: "",
  description: ""
});
const goodsForm = reactive({
  goodsIdsText: ""
});

const columns: TableColumnList = [
  { label: "分组名称", prop: "groupName", minWidth: 180 },
  { label: "分组描述", prop: "description", minWidth: 260, showOverflowTooltip: true },
  { label: "更新时间", prop: "updateTime", minWidth: 180 },
  { label: "操作", prop: "operation", fixed: "right", width: 280, slot: "operation" }
];

const groupGoodsColumns: TableColumnList = [
  { label: "商品ID", prop: "id", minWidth: 160 },
  { label: "商品名称", prop: "goodsName", minWidth: 220 },
  { label: "店铺名称", prop: "storeName", minWidth: 180 },
  { label: "库存", prop: "quantity", minWidth: 100 },
  { label: "状态", prop: "marketEnable", minWidth: 120 }
];

const filteredRows = computed(() =>
  rows.value.filter(item =>
    query.keyword ? String(item.groupName).includes(query.keyword) : true
  )
);
const selectedIds = computed(() =>
  [...new Set(selectedRows.value.map(item => String(item.id || "")).filter(Boolean))]
);

const summaryCards = computed(() => [
  { label: "分组总数", value: filteredRows.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "已维护商品",
    value: groupGoods.value.length,
    accent: "green" as const,
    hint: "当前抽屉分组商品数"
  },
  {
    label: "可维护动作",
    value: "分组/商品",
    accent: "blue" as const,
    hint: "支持分组商品维护"
  },
  { label: "管理能力", value: "增改删", accent: "purple" as const, hint: "已接入真实接口" }
]);

function normalizeRow(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.groupId,
    groupName: item.groupName || "-",
    description: item.description || "-",
    updateTime: formatAdminDateTime(item.updateTime || item.createTime)
  };
}

async function loadData() {
  try {
    const response = await getGoodsGroupPage({
      pageNumber: 1,
      pageSize: 200
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("商品分组加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword || "";
}

function handleReset() {
  query.keyword = "";
}

function handleSelectionChange(selection: Record<string, any>[]) {
  selectedRows.value = selection;
}

function resetForm() {
  editingId.value = "";
  form.groupName = "";
  form.description = "";
}

function openCreate() {
  resetForm();
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  try {
    const response = await getGoodsGroupDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(response) || row;
    editingId.value = String(detail.id || row.id);
    form.groupName = detail.groupName || "";
    form.description = detail.description || "";
    dialogVisible.value = true;
  } catch (_error) {
    message("分组详情加载失败", { type: "error" });
  }
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getGoodsGroupDetail(String(row.id));
    detailRow.value = normalizeRow(extractApiPayload(response) || row);
    detailVisible.value = true;
  } catch (_error) {
    message("分组详情加载失败", { type: "error" });
  }
}

async function submitForm() {
  if (!form.groupName.trim()) {
    message("请输入分组名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      groupName: form.groupName.trim(),
      description: form.description.trim() || undefined
    };
    if (editingId.value) {
      await updateGoodsGroup(editingId.value, payload);
      message("商品分组更新成功", { type: "success" });
    } else {
      await createGoodsGroup(payload);
      message("商品分组创建成功", { type: "success" });
    }
    dialogVisible.value = false;
    resetForm();
    await loadData();
  } catch (_error) {
    message("商品分组保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除分组「${row.groupName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteGoodsGroup(String(row.id));
    message("商品分组删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("商品分组删除失败，请先移除分组商品", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedRows.value.length) {
    message("请先选择要删除的商品分组", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认批量删除选中的 ${selectedRows.value.length} 个商品分组吗？`,
    "批量删除确认",
    {
      type: "warning"
    }
  );
  const results = await Promise.allSettled(
    selectedRows.value.map(row => deleteGoodsGroup(String(row.id)))
  );
  const successCount = results.filter(item => item.status === "fulfilled").length;
  const failCount = results.length - successCount;
  if (failCount > 0) {
    message(
      `${successCount} 个商品分组删除成功，${failCount} 个删除失败，请先移除分组商品`,
      {
        type: successCount > 0 ? "warning" : "error"
      }
    );
  } else {
    message("商品分组批量删除成功", { type: "success" });
  }
  await loadData();
}

async function openGoodsDialog(row: Record<string, any>) {
  goodsGroupId.value = String(row.id);
  goodsForm.goodsIdsText = "";
  try {
    const response = await getGoodsGroupGoodsPage(goodsGroupId.value, {
      pageNumber: 1,
      pageSize: 100
    });
    groupGoods.value = extractApiRecords(response).map(item => ({
      ...item,
      id: item.id || item.goodsId,
      goodsName: item.goodsName || item.name || "-",
      storeName: item.storeName || "-",
      quantity: item.quantity ?? item.stock ?? 0,
      marketEnable: item.marketEnable || item.goodsStatus || "-"
    }));
    goodsDialogVisible.value = true;
  } catch (_error) {
    groupGoods.value = [];
    message("分组商品加载失败", { type: "error" });
  }
}

async function submitGoodsBinding() {
  const goodsIds = goodsForm.goodsIdsText
    .split(",")
    .map(item => item.trim())
    .filter(Boolean);
  goodsSaving.value = true;
  try {
    await updateGoodsGroupGoods(goodsGroupId.value, goodsIds);
    message("分组商品维护成功", { type: "success" });
    goodsDialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("分组商品维护失败，请检查商品 ID 列表", { type: "error" });
  } finally {
    goodsSaving.value = false;
  }
}

function exportGroups() {
  if (!filteredRows.value.length) {
    message("暂无可导出的商品分组", { type: "warning" });
    return;
  }
  const worksheet = utils.json_to_sheet(
    filteredRows.value.map(item => ({
      分组名称: item.groupName,
      分组描述: item.description,
      更新时间: item.updateTime
    }))
  );
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "商品分组");
  writeFile(workbook, "商品分组.xlsx");
  message("商品分组导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="商品分组"
    description="承接商品分组 CRUD 和分组商品维护，作为轻量分组治理入口。"
    api-path="/manager/goods/goodsGroup/getByPage"
    :columns="columns"
    :data="filteredRows"
    :summary-cards="summaryCards"
    :selectable="true"
    :show-status-filter="false"
    keyword-label="分组名称"
    keyword-placeholder="请输入分组名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增分组</el-button>
      <el-button :disabled="!filteredRows.length" @click="exportGroups">导出</el-button>
      <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
        批量删除
      </el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="warning" @click="openGoodsDialog(row)">
        维护商品
      </el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑分组' : '新增分组'"
    width="520px"
  >
    <el-form label-width="88px">
      <el-form-item label="分组名称" required>
        <el-input v-model="form.groupName" />
      </el-form-item>
      <el-form-item label="分组描述">
        <el-input v-model="form.description" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitForm">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="分组详情" size="38%">
    <el-descriptions v-if="detailRow" :column="1" border>
      <el-descriptions-item label="分组名称">
        {{ detailRow.groupName }}
      </el-descriptions-item>
      <el-descriptions-item label="分组描述">
        {{ detailRow.description }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ detailRow.updateTime }}
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>

  <el-dialog v-model="goodsDialogVisible" title="维护分组商品" width="860px">
    <el-alert
      title="当前接口会按传入商品 ID 列表整体更新分组商品关系。"
      type="info"
      :closable="false"
      class="mb-4"
    />
    <pure-table
      row-key="id"
      :data="groupGoods"
      :columns="groupGoodsColumns"
      max-height="280"
    />
    <el-form label-width="124px" class="mt-4">
      <el-form-item label="商品 ID 列表">
        <el-input
          v-model="goodsForm.goodsIdsText"
          type="textarea"
          :rows="4"
          placeholder="请输入逗号分隔的商品 ID；留空表示清空该分组商品"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="goodsDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="goodsSaving" @click="submitGoodsBinding">
        保存商品关系
      </el-button>
    </template>
  </el-dialog>
</template>
