<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  auditStore,
  disableStore,
  enableStore,
  getStoreApplyPage,
  getStoreAuditLog,
  getStoreDetail
} from "@/api/store-governance";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  getStoreBizTypeLabel,
  getStoreAuditStatusLabel,
  getStoreStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { columns } from "./columns";

defineOptions({
  name: "StoreManage"
});

const data = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<Record<string, any>>({});
const auditDialogVisible = ref(false);
const auditSaving = ref(false);
const auditRow = ref<Record<string, any> | null>(null);
const auditLogVisible = ref(false);
const auditLogs = ref<Record<string, any>[]>([]);
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

const detailItems = computed(() => [
  { label: "店铺名称", value: detail.value.storeName || "-" },
  { label: "店铺ID", value: detail.value.storeId || detail.value.id || "-" },
  {
    label: "店铺分类",
    value:
      detail.value.storeManagementCategory ||
      detail.value.storeCategoryName ||
      "-"
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

function normalizeStoreRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.storeId,
    contactName:
      item.contactName || item.linkName || item.memberName || item.legalName || "-",
    contactMobile:
      item.contactMobile || item.linkPhone || item.legalMobile || item.companyPhone || "-",
    storeCategoryName:
      item.storeCategoryName || item.storeManagementCategory || "-",
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

const summaryCards = computed(() => [
  { label: "店铺总数", value: filteredData.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "营业店铺",
    value: filteredData.value.filter(item => String(item.storeStatus).toUpperCase().includes("OPEN")).length,
    accent: "green" as const,
    hint: "当前经营中店铺"
  },
  {
    label: "待审核店铺",
    value: filteredData.value.filter(item =>
      ["SUBMITTED", "PENDING", "WAIT", "DRAFT"].includes(
        String(item.auditStatus).toUpperCase()
      )
    ).length,
    accent: "blue" as const,
    hint: "待处理审核"
  },
  {
    label: "店铺治理",
    value: "审核/启停",
    accent: "purple" as const,
    hint: "已接入真实操作"
  }
]);

async function loadData() {
  try {
    const params: Record<string, any> = {
      pageNumber: 1,
      pageSize: 10
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
    await loadData();
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
    await loadData();
  } catch (_error) {
    message(`店铺${actionText}失败，请稍后重试`, { type: "error" });
  }
}

async function openAuditLog(row: Record<string, any>) {
  try {
    const res = await getStoreAuditLog(String(row.id));
    auditLogs.value = extractApiRecords(res);
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
    业务类型: getStoreBizTypeLabel(item.bizType),
    联系人: item.contactName,
    联系电话: item.contactMobile,
    店铺分类: item.storeCategoryName,
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
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="店铺管理"
    description="承接店铺列表、详情、审核、启停与审核历史治理动作。"
    api-path="/manager/store/store/get/detail/{storeId}"
    :columns="columns"
    :data="filteredData"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '营业中', value: 'OPEN' },
      { label: '已停业', value: 'CLOSE' },
      { label: '已冻结', value: 'FROZEN' }
    ]"
    :quick-actions="[
      { label: '店铺审核', value: '已接入', type: 'warning' },
      { label: '分类筛选', value: '前端补充', type: 'success' },
      { label: '启停操作', value: '已接入', type: 'primary' }
    ]"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportStores">导出</el-button>
    </template>
    <template #filters-extra>
      <el-form-item label="店铺分类">
        <el-input
          v-model="extraFilters.categoryKeyword"
          placeholder="请输入店铺分类"
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
          {{ getStoreAuditStatusLabel(row.auditStatus || row.status) }}
        </template>
      </el-table-column>
      <el-table-column label="审核备注" prop="auditRemark" min-width="220" />
      <el-table-column label="操作人" prop="createBy" min-width="120" />
      <el-table-column label="时间" prop="createTime" min-width="180" />
    </el-table>
  </el-drawer>
</template>
