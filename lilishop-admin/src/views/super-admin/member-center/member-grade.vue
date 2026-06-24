<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createMemberGrade,
  deleteMemberGrade,
  getMemberGradeBenefits,
  getMemberGradeDetail,
  getMemberGradePage,
  updateMemberGrade,
  updateMemberGradeState
} from "@/api/super-admin";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const currentBenefits = ref<Record<string, any>[]>([]);
const query = reactive({ keyword: "", status: "" });
const form = reactive({
  gradeName: "",
  gradeImage: "",
  gradeBackground: "",
  gradeFontColor: "",
  requiredExperience: 1,
  gradeSort: 1,
  gradeState: "OPEN",
  isDefault: false,
  benefitIds: ""
});

const columns: TableColumnList = [
  { label: "等级名称", prop: "displayName", minWidth: 180 },
  { label: "等级状态", prop: "displayStatus", minWidth: 120 },
  { label: "所需经验", prop: "displayExperience", minWidth: 120 },
  { label: "排序值", prop: "displaySort", minWidth: 100 },
  { label: "权益数量", prop: "displayBenefitCount", minWidth: 120 },
  { label: "操作", prop: "operation", width: 280, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "等级总数", value: rows.value.length, accent: "orange" as const, hint: "当前会员等级配置" },
  { label: "启用等级", value: rows.value.filter(item => item.isOpen).length, accent: "green" as const, hint: "可参与升级体系的等级" },
  { label: "默认等级", value: rows.value.filter(item => item.isDefault).length, accent: "blue" as const, hint: "新会员默认命中的等级" },
  { label: "治理动作", value: "新增/编辑/启停/删除", accent: "purple" as const, hint: "会员等级真实配置页" }
]);

function normalizeRecord(item: Record<string, any>) {
  const isOpen = String(item.gradeState || item.state || item.status || "OPEN").toUpperCase() === "OPEN";
  const benefitIds = String(item.benefitIds || "").split(",").filter(Boolean);
  return {
    ...item,
    id: item.id || item.gradeId || item.gradeName,
    isOpen,
    isDefault: Boolean(item.isDefault),
    benefitIds,
    displayName: item.gradeName || "-",
    displayStatus: isOpen ? "启用" : "停用",
    displayExperience: item.requiredExperience ?? 0,
    displaySort: item.gradeSort ?? 0,
    displayBenefitCount: benefitIds.length
  };
}

async function loadData() {
  try {
    const res = await getMemberGradePage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) list = list.filter(item => item.displayName.includes(query.keyword));
    if (query.status) list = list.filter(item => item.displayStatus === query.status);
    rows.value = list;
  } catch (_error) {
    message("会员等级加载失败，请稍后重试", { type: "error" });
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
  form.gradeName = "";
  form.gradeImage = "";
  form.gradeBackground = "";
  form.gradeFontColor = "";
  form.requiredExperience = 1;
  form.gradeSort = 1;
  form.gradeState = "OPEN";
  form.isDefault = false;
  form.benefitIds = "";
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
    const res = await getMemberGradeDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(res) || row;
    form.gradeName = detail.gradeName || row.displayName;
    form.gradeImage = detail.gradeImage || "";
    form.gradeBackground = detail.gradeBackground || "";
    form.gradeFontColor = detail.gradeFontColor || "";
    form.requiredExperience = Number(detail.requiredExperience || 1);
    form.gradeSort = Number(detail.gradeSort || 1);
    form.gradeState = detail.gradeState || (row.isOpen ? "OPEN" : "CLOSE");
    form.isDefault = Boolean(detail.isDefault);
    form.benefitIds = detail.benefitIds || "";
  } catch (_error) {
    form.gradeName = row.displayName;
    form.requiredExperience = Number(row.requiredExperience || 1);
    form.gradeSort = Number(row.gradeSort || 1);
    form.gradeState = row.isOpen ? "OPEN" : "CLOSE";
  }
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  try {
    const [detailRes, benefitRes] = await Promise.all([
      getMemberGradeDetail(String(row.id)),
      getMemberGradeBenefits(String(row.id))
    ]);
    currentRow.value = normalizeRecord(extractApiPayload<Record<string, any>>(detailRes) || row);
    currentBenefits.value = extractApiRecords(benefitRes);
  } catch (_error) {
    currentBenefits.value = [];
  }
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.gradeName.trim()) {
    message("请输入等级名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = {
      gradeName: form.gradeName,
      gradeImage: form.gradeImage,
      gradeBackground: form.gradeBackground,
      gradeFontColor: form.gradeFontColor,
      requiredExperience: form.requiredExperience,
      gradeSort: form.gradeSort,
      gradeState: form.gradeState,
      isDefault: form.isDefault,
      benefitIds: form.benefitIds
    };
    if (editingRow.value) {
      await updateMemberGrade(String(editingRow.value.id), payload);
      message("会员等级修改成功", { type: "success" });
    } else {
      await createMemberGrade(payload);
      message("会员等级新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("会员等级保存失败，请确认字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleToggleState(row: Record<string, any>) {
  try {
    await updateMemberGradeState(String(row.id), row.isOpen ? "CLOSE" : "OPEN");
    message(row.isOpen ? "会员等级已停用" : "会员等级已启用", { type: "success" });
    await loadData();
  } catch (_error) {
    message("会员等级状态切换失败", { type: "error" });
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除会员等级「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteMemberGrade(String(row.id));
    message("会员等级删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("会员等级删除失败", { type: "error" });
  }
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="会员等级"
    description="承接会员等级配置、经验门槛治理、等级启停和等级权益查看，作为会员成长体系的核心配置页。"
    api-path="/manager/member/memberGrade/getByPage"
    :columns="columns"
    :data="rows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '启用', value: '启用' },
      { label: '停用', value: '停用' }
    ]"
    :quick-actions="[
      { label: '等级新增', value: '已接入', type: 'primary' },
      { label: '状态切换', value: '启用/停用', type: 'warning' },
      { label: '权益查看', value: '已接入', type: 'success' }
    ]"
    keyword-label="等级名称"
    keyword-placeholder="请输入等级名称"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button type="primary" @click="openCreate">新增等级</el-button>
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

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑会员等级' : '新增会员等级'" width="680px">
    <el-form label-width="110px">
      <el-form-item label="等级名称" required><el-input v-model="form.gradeName" placeholder="请输入等级名称" /></el-form-item>
      <el-form-item label="等级图标" required><el-input v-model="form.gradeImage" placeholder="请输入等级图标地址" /></el-form-item>
      <el-form-item label="背景图"><el-input v-model="form.gradeBackground" placeholder="请输入等级背景图地址" /></el-form-item>
      <el-form-item label="字体颜色"><el-input v-model="form.gradeFontColor" placeholder="请输入字体颜色值" /></el-form-item>
      <el-form-item label="所需经验" required><el-input-number v-model="form.requiredExperience" :min="1" style="width: 100%" /></el-form-item>
      <el-form-item label="排序值" required><el-input-number v-model="form.gradeSort" :min="1" style="width: 100%" /></el-form-item>
      <el-form-item label="等级状态">
        <el-radio-group v-model="form.gradeState">
          <el-radio value="OPEN">启用</el-radio>
          <el-radio value="CLOSE">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="默认等级"><el-switch v-model="form.isDefault" /></el-form-item>
      <el-form-item label="权益ID串"><el-input v-model="form.benefitIds" placeholder="多个权益ID请使用英文逗号分隔" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="会员等级详情" size="620px">
    <el-descriptions v-if="currentRow" :column="1" border>
      <el-descriptions-item label="等级名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="等级状态">{{ currentRow.displayStatus }}</el-descriptions-item>
      <el-descriptions-item label="所需经验">{{ currentRow.displayExperience }}</el-descriptions-item>
      <el-descriptions-item label="排序值">{{ currentRow.displaySort }}</el-descriptions-item>
      <el-descriptions-item label="默认等级">{{ currentRow.isDefault ? "是" : "否" }}</el-descriptions-item>
      <el-descriptions-item label="关联权益">
        {{ currentBenefits.length ? currentBenefits.map(item => item.benefitName || item.name || item.id).join("、") : "暂无关联权益" }}
      </el-descriptions-item>
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
