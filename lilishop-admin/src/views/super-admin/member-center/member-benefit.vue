<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createMemberBenefit,
  deleteMemberBenefit,
  getMemberBenefitDetail,
  getMemberBenefitPage,
  getMemberBenefitTypes,
  updateMemberBenefit,
  updateMemberBenefitState
} from "@/api/super-admin";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const benefitTypes = ref<Array<{ label: string; value: string }>>([]);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", status: "" });
const form = reactive({
  benefitName: "",
  benefitLogo: "",
  benefitType: "",
  benefitDesc: "",
  benefitSort: 1,
  benefitState: "OPEN",
  benefitConfig: ""
});

const columns: TableColumnList = [
  { label: "权益名称", prop: "displayName", minWidth: 180 },
  { label: "权益类型", prop: "displayType", minWidth: 140 },
  { label: "启用状态", prop: "displayStatus", minWidth: 120 },
  { label: "排序值", prop: "displaySort", minWidth: 100 },
  { label: "权益说明", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "操作", prop: "operation", width: 280, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "权益总数", value: rows.value.length, accent: "orange" as const, hint: "当前会员权益配置" },
  { label: "启用权益", value: rows.value.filter(item => item.isOpen).length, accent: "green" as const, hint: "当前可分配的会员权益" },
  { label: "权益类型数", value: benefitTypes.value.length, accent: "blue" as const, hint: "后端返回的权益枚举类型" },
  { label: "治理动作", value: "新增/编辑/启停/删除", accent: "purple" as const, hint: "权益配置真实承接页" }
]);

function normalizeRecord(item: Record<string, any>) {
  const isOpen = String(item.benefitState || item.state || "OPEN").toUpperCase() === "OPEN";
  return {
    ...item,
    id: item.id || item.benefitId || item.benefitName,
    isOpen,
    displayName: item.benefitName || item.name || "-",
    displayType: item.benefitType || "-",
    displayStatus: isOpen ? "启用" : "停用",
    displaySort: item.benefitSort ?? 0,
    displayRemark: item.benefitDesc || item.description || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.keyword) params.keyword = query.keyword;
    if (query.status) params.state = query.status === "启用" ? "OPEN" : "CLOSE";
    const res = await getMemberBenefitPage(params);
    rows.value = extractApiRecords(res).map(normalizeRecord);
  } catch (_error) {
    message("会员权益加载失败，请稍后重试", { type: "error" });
  }
}

async function loadTypes() {
  try {
    const res = await getMemberBenefitTypes();
    benefitTypes.value = extractApiRecords(res).map(item => ({
      label: item.description || item.label || item.name || item.value,
      value: item.name || item.value || item.code
    }));
  } catch (_error) {
    benefitTypes.value = [];
  }
}

function handleSearch(payload: { keyword: string; status: string }) {
  query.keyword = payload.keyword || "";
  query.status = payload.status || "";
  loadData();
}

function handleReset() {
  query.keyword = "";
  query.status = "";
  loadData();
}

function resetForm() {
  form.benefitName = "";
  form.benefitLogo = "";
  form.benefitType = "";
  form.benefitDesc = "";
  form.benefitSort = 1;
  form.benefitState = "OPEN";
  form.benefitConfig = "";
}

function openCreate() {
  editingRow.value = null;
  resetForm();
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  resetForm();
  try {
    const res = await getMemberBenefitDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(res) || row;
    form.benefitName = detail.benefitName || row.displayName;
    form.benefitLogo = detail.benefitLogo || "";
    form.benefitType = detail.benefitType || "";
    form.benefitDesc = detail.benefitDesc || row.displayRemark;
    form.benefitSort = Number(detail.benefitSort || 1);
    form.benefitState = detail.benefitState || (row.isOpen ? "OPEN" : "CLOSE");
    form.benefitConfig = detail.benefitConfig || "";
  } catch (_error) {
    form.benefitName = row.displayName;
    form.benefitDesc = row.displayRemark;
    form.benefitState = row.isOpen ? "OPEN" : "CLOSE";
  }
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  try {
    const res = await getMemberBenefitDetail(String(row.id));
    currentRow.value = normalizeRecord(extractApiPayload<Record<string, any>>(res) || row);
  } catch (_error) {
    currentRow.value = row;
  }
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.benefitName.trim()) {
    message("请输入权益名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      benefitName: form.benefitName,
      benefitLogo: form.benefitLogo,
      benefitType: form.benefitType,
      benefitDesc: form.benefitDesc,
      benefitSort: form.benefitSort,
      benefitState: form.benefitState,
      benefitConfig: form.benefitConfig
    };
    if (editingRow.value) {
      await updateMemberBenefit(String(editingRow.value.id), payload);
      message("会员权益修改成功", { type: "success" });
    } else {
      await createMemberBenefit(payload);
      message("会员权益新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("会员权益保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleToggleState(row: Record<string, any>) {
  try {
    await updateMemberBenefitState(String(row.id), row.isOpen ? "CLOSE" : "OPEN");
    message(row.isOpen ? "会员权益已停用" : "会员权益已启用", { type: "success" });
    await loadData();
  } catch (_error) {
    message("会员权益状态切换失败", { type: "error" });
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除会员权益「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteMemberBenefit(String(row.id));
    message("会员权益删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("会员权益删除失败", { type: "error" });
  }
}

onMounted(async () => {
  await Promise.all([loadData(), loadTypes()]);
});
</script>

<template>
  <WholesaleAdminPage
    title="会员权益"
    description="承接等级权益配置、权益类型映射、权益启停和说明维护，作为会员体系权益侧的主配置页。"
    api-path="/manager/member/benefit/getByPage"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '启用', value: '启用' },
      { label: '停用', value: '停用' }
    ]"
    :quick-actions="[
      { label: '权益新增', value: '已接入', type: 'primary' },
      { label: '权益类型', value: '已承接', type: 'success' },
      { label: '权益状态', value: '启用/停用', type: 'warning' }
    ]"
    keyword-label="权益名称"
    keyword-placeholder="请输入权益名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增权益</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="warning" @click="handleToggleState(row)">
        {{ row.isOpen ? "停用" : "启用" }}
      </el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑会员权益' : '新增会员权益'" width="680px">
    <el-form label-width="100px">
      <el-form-item label="权益名称" required><el-input v-model="form.benefitName" placeholder="请输入权益名称" /></el-form-item>
      <el-form-item label="权益 Logo"><el-input v-model="form.benefitLogo" placeholder="请输入权益 Logo 地址" /></el-form-item>
      <el-form-item label="权益类型">
        <el-select v-model="form.benefitType" placeholder="请选择权益类型" clearable style="width: 100%">
          <el-option v-for="item in benefitTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序值"><el-input-number v-model="form.benefitSort" :min="1" style="width: 100%" /></el-form-item>
      <el-form-item label="权益状态">
        <el-radio-group v-model="form.benefitState">
          <el-radio value="OPEN">启用</el-radio>
          <el-radio value="CLOSE">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="权益说明"><el-input v-model="form.benefitDesc" type="textarea" :rows="4" placeholder="请输入权益说明" /></el-form-item>
      <el-form-item label="扩展配置"><el-input v-model="form.benefitConfig" type="textarea" :rows="5" placeholder="请输入 JSON 扩展配置" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="会员权益详情" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="权益名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="权益类型">{{ currentRow.displayType }}</el-descriptions-item>
      <el-descriptions-item label="权益状态">{{ currentRow.displayStatus }}</el-descriptions-item>
      <el-descriptions-item label="排序值">{{ currentRow.displaySort }}</el-descriptions-item>
      <el-descriptions-item label="权益说明">{{ currentRow.displayRemark }}</el-descriptions-item>
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
</style>
