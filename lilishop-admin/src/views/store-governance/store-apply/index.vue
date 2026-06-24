<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { auditStore, getStoreApplyPage } from "@/api/store-governance";
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
const auditVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
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
  auditRemark: ""
});

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

async function loadData() {
  const params: Record<string, any> = {
    pageNumber: 1,
    pageSize: 10,
    includeAll: true
  };
  if (query.keyword) params.storeName = query.keyword;
  if (query.status) params.auditStatus = query.status;
  if (extraFilters.bizType) params.bizType = extraFilters.bizType;
  if (extraFilters.agentLevel) params.agentLevel = extraFilters.agentLevel;
  const res = await getStoreApplyPage(params);
  data.value = extractApiRecords(res).map(normalizeStoreRecord);
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

function openAudit(row: Record<string, any>, status: "APPROVED" | "REJECTED" | "FROZEN") {
  currentRow.value = row;
  auditForm.auditStatus = status;
  auditForm.auditRemark = "";
  auditVisible.value = true;
}

async function submitAudit() {
  if (!currentRow.value?.id) return;
  await auditStore(currentRow.value.id, {
    auditStatus: auditForm.auditStatus,
    auditRemark: auditForm.auditRemark || undefined
  });
  message("操作成功", { type: "success" });
  auditVisible.value = false;
  await loadData();
}

async function handleFreeze(row: Record<string, any>) {
  await ElMessageBox.confirm(
    `确认冻结入驻主体「${row.storeName || row.id}」吗？`,
    "冻结确认",
    { type: "warning" }
  );
  currentRow.value = row;
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
  auditForm.auditStatus = "APPROVED";
  auditForm.auditRemark = "管理端解冻";
  await submitAudit();
}

onMounted(() => {
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
      <el-form-item label="代理等级" v-if="currentRow?.bizType === 'AGENT'">
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
