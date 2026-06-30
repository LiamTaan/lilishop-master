<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  addMemberGroupUsers,
  createMemberGroup,
  deleteMemberGroup,
  getMemberDetail,
  getMemberGroupDetail,
  getMemberGroupPage,
  getMemberGroupUsers,
  getMemberPage,
  removeMemberGroupUser,
  updateMemberGroup
} from "@/api/super-admin";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "MemberGroupManage"
});

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
const dialogVisible = ref(false);
const detailVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const currentUsers = ref<Record<string, any>[]>([]);
const query = reactive({ keyword: "" });
const selectedMemberIds = ref<string[]>([]);
const form = reactive({
  groupName: "",
  description: ""
});

const columns: TableColumnList = [
  { label: "分组名称", prop: "displayName", minWidth: 180 },
  { label: "分组描述", prop: "displayRemark", minWidth: 260, showOverflowTooltip: true },
  { label: "创建时间", prop: "displayTime", minWidth: 180 },
  { label: "成员数量", prop: "displayCount", minWidth: 120 },
  { label: "操作", prop: "operation", width: 260, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "分组总数", value: rows.value.length, accent: "orange" as const, hint: "当前会员分组台账" },
  { label: "含成员分组", value: rows.value.filter(item => item.displayCount > 0).length, accent: "green" as const, hint: "已承接会员归组的分组" },
  { label: "空分组", value: rows.value.filter(item => item.displayCount === 0).length, accent: "blue" as const, hint: "待补充运营成员的分组" },
  { label: "治理动作", value: "新增/编辑/成员维护", accent: "purple" as const, hint: "分组与成员关系已承接" }
]);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

function normalizeRecord(item: Record<string, any>) {
  return {
    ...item,
    id: item.id || item.groupId || item.groupName,
    displayName: item.groupName || item.name || "-",
    displayRemark: item.description || item.remark || "-",
    displayTime: item.createTime || item.updateTime || "-",
    displayCount: Number(item.memberCount || item.userCount || 0)
  };
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

function getUserOption(memberId: string) {
  const targetId = memberId.trim();
  return userOptions.value.find(item => item.value === targetId);
}

function normalizeGroupUserRecord(item: Record<string, any>) {
  const memberId = String(item.memberId || item.id || "").trim();
  const userOption = memberId ? getUserOption(memberId) : undefined;
  return {
    ...item,
    memberId,
    memberName:
      item.memberName ||
      item.nickName ||
      item.nickname ||
      item.memberNickname ||
      userOption?.nickName ||
      "-",
    mobile: item.mobile || userOption?.mobile || "-",
    createTime: item.createTime || item.updateTime || "-"
  };
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
    replaceUserOptions(extractApiRecords(res), selectedMemberIds.value);
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
    // 仅用于补齐已选项，失败时保留现有交互
  }
}

async function refreshCurrentUsers(groupId: string) {
  const res = await getMemberGroupUsers(groupId);
  const rawUsers = extractApiRecords(res);
  const memberIds = rawUsers
    .map(item => String(item.memberId || item.id || "").trim())
    .filter(Boolean);
  await Promise.all(memberIds.map(ensureUserOption));
  currentUsers.value = rawUsers.map(normalizeGroupUserRecord);
}

async function loadData() {
  try {
    const res = await getMemberGroupPage();
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.keyword) list = list.filter(item => item.displayName.includes(query.keyword));
    rows.value = list;
  } catch (_error) {
    message("会员分组加载失败，请稍后重试", { type: "error" });
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

function openCreate() {
  editingRow.value = null;
  form.groupName = "";
  form.description = "";
  dialogVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  try {
    const res = await getMemberGroupDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(res) || row;
    form.groupName = detail.groupName || row.displayName;
    form.description = detail.description || row.displayRemark;
  } catch (_error) {
    form.groupName = row.displayName;
    form.description = row.displayRemark;
  }
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  currentRow.value = row;
  selectedMemberIds.value = [];
  try {
    await refreshCurrentUsers(String(row.id));
    mergeUserOptions(currentUsers.value);
  } catch (_error) {
    currentUsers.value = [];
  }
  detailVisible.value = true;
}

async function handleSave() {
  if (!form.groupName.trim()) {
    message("请输入分组名称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const payload = { groupName: form.groupName, description: form.description };
    if (editingRow.value) {
      await updateMemberGroup(String(editingRow.value.id), {
        id: editingRow.value.id,
        ...payload
      });
      message("会员分组修改成功", { type: "success" });
    } else {
      await createMemberGroup(payload);
      message("会员分组新增成功", { type: "success" });
    }
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("会员分组保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除会员分组「${row.displayName}」吗？`, "删除确认", {
    type: "warning"
  });
  try {
    await deleteMemberGroup(String(row.id));
    message("会员分组删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("会员分组删除失败，可能仍存在分组成员", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的会员分组", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 个会员分组吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedIds.value.map(id => deleteMemberGroup(id)));
    selectedRows.value = [];
    message("会员分组批量删除成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("会员分组批量删除失败，可能存在仍含成员的分组", {
      type: "error"
    });
  }
}

async function handleAppendUsers() {
  if (!currentRow.value) return;
  const memberIds = selectedMemberIds.value.map(item => item.trim()).filter(Boolean);
  if (!memberIds.length) {
    message("请至少选择一个用户", { type: "warning" });
    return;
  }
  await Promise.all(memberIds.map(ensureUserOption));
  saving.value = true;
  try {
    await addMemberGroupUsers(String(currentRow.value.id), memberIds);
    message("分组成员更新成功", { type: "success" });
    selectedMemberIds.value = [];
    await refreshCurrentUsers(String(currentRow.value.id));
    mergeUserOptions(currentUsers.value);
    await loadData();
  } catch (_error) {
    message("分组成员更新失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleRemoveUser(memberId: string) {
  if (!currentRow.value) return;
  try {
    await removeMemberGroupUser(String(currentRow.value.id), memberId);
    message("分组成员移除成功", { type: "success" });
    await refreshCurrentUsers(String(currentRow.value.id));
    await loadData();
  } catch (_error) {
    message("分组成员移除失败", { type: "error" });
  }
}

function exportMemberGroups() {
  if (!rows.value.length) {
    message("暂无可导出的会员分组数据", { type: "warning" });
    return;
  }
  const table = rows.value.map(item => ({
    分组名称: item.displayName,
    分组描述: item.displayRemark,
    创建时间: item.displayTime,
    成员数量: item.displayCount
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "会员分组");
  writeFile(workbook, "会员分组.xlsx");
  message("会员分组导出成功", { type: "success" });
}

onMounted(loadData);
</script>

<template>
  <WholesaleAdminPage
    title="会员分组"
    description="承接会员分组配置、分组描述维护和分组成员关系治理，作为会员分层运营的组织页。"
    api-path="/manager/member/memberGroup/getByPage"
    :columns="columns"
    :data="rows"
    selectable
    :summary-cards="summaryCards"
    :show-status-filter="false"
    :quick-actions="[
      { label: '分组新增', value: '已接入', type: 'primary' },
      { label: '成员维护', value: '已接入', type: 'success' },
      { label: '删除校验', value: '有成员不可删', type: 'warning' }
    ]"
    keyword-label="分组名称"
    keyword-placeholder="请输入分组名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量删除</el-button>
      <el-button @click="exportMemberGroups">导出</el-button>
      <el-button type="primary" @click="openCreate">新增分组</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">成员</el-button>
      <el-button link type="success" @click="openEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" :title="editingRow ? '编辑会员分组' : '新增会员分组'" width="560px">
    <el-form label-width="88px">
      <el-form-item label="分组名称" required><el-input v-model="form.groupName" placeholder="请输入分组名称" /></el-form-item>
      <el-form-item label="分组描述"><el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入分组描述" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="会员分组成员" size="720px">
    <el-descriptions v-if="currentRow" :column="1" border class="group-desc">
      <el-descriptions-item label="分组名称">{{ currentRow.displayName }}</el-descriptions-item>
      <el-descriptions-item label="分组描述">{{ currentRow.displayRemark }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ currentRow.displayTime }}</el-descriptions-item>
    </el-descriptions>
    <div class="group-append">
      <el-select
        v-model="selectedMemberIds"
        multiple
        filterable
        remote
        clearable
        reserve-keyword
        :remote-method="searchUsers"
        :loading="userLoading"
        placeholder="请输入昵称或手机号搜索用户后选择，可多选"
        style="flex: 1"
      >
        <el-option
          v-for="item in userOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button type="primary" :loading="saving" @click="handleAppendUsers">追加成员</el-button>
    </div>
    <el-table :data="currentUsers" border>
      <el-table-column label="会员昵称" prop="memberName" min-width="160" />
      <el-table-column label="手机号" prop="mobile" min-width="150" />
      <el-table-column label="用户ID" prop="memberId" min-width="180" />
      <el-table-column label="加入时间" prop="createTime" min-width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleRemoveUser(String(row.memberId || row.id))">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-drawer>
</template>

<style scoped>
.group-desc {
  margin-bottom: 16px;
}

.group-append {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
