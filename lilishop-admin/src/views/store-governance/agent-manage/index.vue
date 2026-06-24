<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  auditAgentBind,
  getAgentRolePage,
  getAgentStorePage,
  unbindAgentBind
} from "@/api/store-governance";
import { message } from "@/utils/message";
import { extractApiRecords } from "@/utils/admin-governance";
import { columns } from "./columns";

defineOptions({
  name: "AgentManage"
});

const roles = ref<any[]>([]);
const binds = ref<any[]>([]);
const query = reactive({
  keyword: "",
  status: ""
});
const summaryCards = computed(() => [
  { label: "代理身份数", value: roles.value.length, accent: "orange" as const, hint: "已建立代理身份" },
  {
    label: "生效代理",
    value: data.value.filter(item => item.roleStatus === "ENABLE").length,
    accent: "green" as const,
    hint: "已开通代理经营身份"
  },
  {
    label: "额外绑定店铺",
    value: data.value.filter(item => !!item.bindId).length,
    accent: "blue" as const,
    hint: "代理扩展店铺绑定"
  },
  { label: "区域治理", value: "已接入", accent: "purple" as const, hint: "等级/区域/绑定统一治理" }
]);
const auditVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const auditForm = reactive({
  auditStatus: "APPROVED" as "APPROVED" | "REJECTED",
  auditRemark: ""
});

function auditActionLabel(status: "APPROVED" | "REJECTED") {
  return status === "APPROVED" ? "通过" : "驳回";
}

const data = computed(() => {
  const rows: Record<string, any>[] = [];
  roles.value.forEach(role => {
    const roleBinds = binds.value.filter(
      bind => bind.agentMemberId === role.memberId
    );
    if (roleBinds.length === 0) {
      rows.push({
        id: `ROLE-${role.id}`,
        roleId: role.id,
        bindId: "",
        agentName: role.memberName || "-",
        mobile: role.mobile || role.memberId || "-",
        agentLevel: role.agentLevel || "-",
        storeName: "-",
        regionName: role.regionName || "-",
        roleStatus: role.status || "-",
        bindStatus: "-",
        auditStatus: "-",
        auditRemark: "",
        remark: role.remark || ""
      });
      return;
    }
    roleBinds.forEach(bind => {
      rows.push({
        id: bind.id,
        roleId: role.id,
        bindId: bind.id,
        agentName: bind.agentMemberName || role.memberName || "-",
        mobile: role.mobile || role.memberId || bind.agentMemberId || "-",
        agentLevel: role.agentLevel || "-",
        storeName: bind.storeName || "-",
        regionName: bind.regionName || role.regionName || "-",
        roleStatus: role.status || "-",
        bindStatus: bind.bindStatus || "-",
        auditStatus: bind.auditStatus || "-",
        auditRemark: bind.auditRemark || "",
        remark: bind.remark || role.remark || ""
      });
    });
  });
  binds.value
    .filter(bind => !roles.value.some(role => role.memberId === bind.agentMemberId))
    .forEach(bind => {
      rows.push({
        id: bind.id,
        roleId: "",
        bindId: bind.id,
        agentName: bind.agentMemberName || "-",
        mobile: bind.agentMemberId || "-",
        agentLevel: "-",
        storeName: bind.storeName || "-",
        regionName: bind.regionName || "-",
        roleStatus: "-",
        bindStatus: bind.bindStatus || "-",
        auditStatus: bind.auditStatus || "-",
        auditRemark: bind.auditRemark || "",
        remark: bind.remark || ""
      });
    });
  return rows.filter(item => {
    const keywordMatched = query.keyword
      ? [item.agentName, item.storeName, item.regionName, item.mobile]
          .filter(Boolean)
          .some(value => String(value).includes(query.keyword))
      : true;
    const statusMatched = query.status
      ? item.auditStatus === query.status || item.bindStatus === query.status
      : true;
    return keywordMatched && statusMatched;
  });
});

async function loadData() {
  try {
    const [roleRes, bindRes] = await Promise.all([
      getAgentRolePage(),
      getAgentStorePage()
    ]);
    roles.value = extractApiRecords(roleRes);
    binds.value = extractApiRecords(bindRes);
  } catch (_error) {
    roles.value = [];
    binds.value = [];
    message("代理治理数据加载失败，请稍后重试", { type: "error" });
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword;
  query.status = payload.status;
}

function handleReset() {
  query.keyword = "";
  query.status = "";
}

function openAudit(row: Record<string, any>, status: "APPROVED" | "REJECTED") {
  currentRow.value = row;
  auditForm.auditStatus = status;
  auditForm.auditRemark = row.auditRemark || "";
  auditVisible.value = true;
}

async function submitAudit() {
  if (!currentRow.value?.id) return;
  if (!currentRow.value.bindId) return;
  await auditAgentBind(currentRow.value.bindId, {
    auditStatus: auditForm.auditStatus,
    auditRemark: auditForm.auditRemark || undefined
  });
  message("审核成功", { type: "success" });
  auditVisible.value = false;
  await loadData();
}

async function handleUnbind(row: Record<string, any>) {
  if (!row.bindId) return;
  await ElMessageBox.confirm(
    `确认解绑代理商「${row.agentName}」与店铺「${row.storeName}」吗？`,
    "解绑确认",
    { type: "warning" }
  );
  await unbindAgentBind(row.bindId);
  message("解绑成功", { type: "success" });
  await loadData();
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="代理治理"
    description="承接代理身份、代理等级、区域范围与扩展店铺绑定治理，不再承担主入驻审核链路。"
    api-path="/manager/agent/role + /manager/agent/store"
    :columns="columns"
    :data="data"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待审核', value: 'SUBMITTED' },
      { label: '审核通过', value: 'APPROVED' },
      { label: '审核驳回', value: 'REJECTED' },
      { label: '已绑定', value: 'BOUND' },
      { label: '已解绑', value: 'UNBOUND' }
    ]"
    :quick-actions="[
      { label: '身份名录', value: '已接入', type: 'primary' },
      { label: '绑定审核', value: '已保留', type: 'warning' },
      { label: '区域等级', value: '已展示', type: 'success' }
    ]"
    keyword-label="代理商/店铺"
    keyword-placeholder="请输入代理商、店铺或区域"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #operation="{ row }">
      <el-button
        v-if="row.bindId && row.auditStatus !== 'APPROVED'"
        link
        type="primary"
        @click="openAudit(row, 'APPROVED')"
      >
        通过
      </el-button>
      <el-button
        v-if="row.bindId && row.auditStatus !== 'REJECTED'"
        link
        type="danger"
        @click="openAudit(row, 'REJECTED')"
      >
        驳回
      </el-button>
      <el-button
        v-if="row.bindId && row.auditStatus === 'APPROVED'"
        link
        type="warning"
        @click="handleUnbind(row)"
      >
        解绑
      </el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog
    v-model="auditVisible"
    :title="`代理绑定审核 - ${currentRow?.agentName || '-'}`"
    width="520px"
  >
    <el-form label-width="88px">
      <el-form-item label="代理等级">
        <span>{{ currentRow?.agentLevel || "-" }}</span>
      </el-form-item>
      <el-form-item label="区域范围">
        <span>{{ currentRow?.regionName || "-" }}</span>
      </el-form-item>
      <el-form-item label="审核动作">
        <el-tag :type="auditForm.auditStatus === 'APPROVED' ? 'success' : 'danger'">
          {{ auditActionLabel(auditForm.auditStatus) }}
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
