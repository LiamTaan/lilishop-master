<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  createMember,
  getMemberDetail,
  getMemberPage,
  updateMember,
  updateMemberPoint,
  updateMemberStatus
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { getAgentLevelLabel, getStoreBizTypeLabel } from "@/utils/admin-governance";

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const editVisible = ref(false);
const pointVisible = ref(false);
const saving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const editingRow = ref<Record<string, any> | null>(null);
const query = reactive({ keyword: "", status: "" });
const form = reactive({
  username: "",
  password: "",
  mobile: "",
  nickName: "",
  region: "",
  regionId: "",
  sex: 1,
  birthday: "",
  face: ""
});
const pointForm = reactive({
  point: 0,
  type: "INCREASE"
});

const columns: TableColumnList = [
  { label: "登录账号", prop: "displayUsername", minWidth: 160 },
  { label: "用户昵称", prop: "displayName", minWidth: 180 },
  { label: "手机号", prop: "displayMobile", minWidth: 150 },
  { label: "账号归属", prop: "displayAccountOwnership", minWidth: 200 },
  { label: "商家侧身份", prop: "displayClerkIdentity", minWidth: 140 },
  { label: "主店铺", prop: "displayMainStore", minWidth: 180, showOverflowTooltip: true },
  { label: "账号状态", prop: "displayStatus", minWidth: 120 },
  { label: "注册时间", prop: "displayTime", minWidth: 180 },
  { label: "操作", prop: "operation", width: 320, fixed: "right", slot: "operation" }
];

const summaryCards = computed(() => [
  { label: "基础账号总数", value: rows.value.length, accent: "orange" as const, hint: "li_member 基础账号台账" },
  { label: "已开通商家侧", value: rows.value.filter(item => item.hasClerkAccount).length, accent: "green" as const, hint: "已关联 clerk + store" },
  { label: "店主账号", value: rows.value.filter(item => item.shopkeeper).length, accent: "warning" as const, hint: "商家侧店主身份" },
  { label: "冻结账号", value: rows.value.filter(item => item.isDisabled).length, accent: "blue" as const, hint: "基础账号已冻结" }
]);

function asBoolean(value: unknown) {
  return value === true || value === "true" || value === 1 || value === "1";
}

function buildAccountOwnershipLabel(item: Record<string, any>) {
  const ownerships: string[] = [];
  if (item.hasClerkAccount) ownerships.push("商家账号");
  if (item.isAgent) ownerships.push("代理");
  if (item.isSupplier) ownerships.push("供货商");
  if (ownerships.length === 0) {
    ownerships.push("会员账号");
  }
  return ownerships.join(" / ");
}

function buildClerkIdentityLabel(item: Record<string, any>) {
  if (!item.hasClerkAccount) return "未开通";
  return item.shopkeeper ? "店主" : "店员";
}

function buildClerkStatusLabel(item: Record<string, any>) {
  if (!item.hasClerkAccount) return "未开通商家侧账号";
  return item.clerkStatus ? "正常" : "已停用";
}

function normalizeRecord(item: Record<string, any>) {
  const disabled =
    item.disabled === false ||
    String(item.memberStatus || "")
      .toUpperCase()
      .includes("DISABLE");
  const hasClerkAccount = asBoolean(item.hasClerkAccount);
  const shopkeeper = asBoolean(item.shopkeeper);
  const clerkStatus = hasClerkAccount && asBoolean(item.clerkStatus);
  return {
    ...item,
    id: item.id || item.memberId || item.username || item.mobile,
    isDisabled: disabled,
    hasClerkAccount,
    clerkId: item.clerkId || "",
    shopkeeper,
    clerkStatus,
    isSupplier: asBoolean(item.supplier),
    isAgent: asBoolean(item.agent),
    displayUsername: item.username || "-",
    displayName: item.nickName || item.nickname || item.memberName || item.username || "-",
    displayMobile: item.mobile || "-",
    displayAccountOwnership: buildAccountOwnershipLabel({
      ...item,
      hasClerkAccount,
      isSupplier: asBoolean(item.supplier),
      isAgent: asBoolean(item.agent)
    }),
    displayClerkIdentity: buildClerkIdentityLabel({ hasClerkAccount, shopkeeper }),
    displayClerkStatus: buildClerkStatusLabel({ hasClerkAccount, clerkStatus }),
    displayAgentLevel: asBoolean(item.agent) ? getAgentLevelLabel(item.agentLevel) : "-",
    displayMainStore: item.mainStoreName || "未关联店铺",
    displayStatus: disabled ? "已冻结" : "正常",
    displayPoints: item.point ?? item.points ?? item.memberPoints ?? 0,
    displayTime: item.createTime || item.regTime || item.lastLoginTime || "-",
    displayRegion: item.region || "-",
    displayRemark: item.email || item.remark || "-"
  };
}

async function loadData() {
  try {
    const params: Record<string, any> = {};
    if (query.keyword) {
      const searchText = query.keyword.trim();
      const isFullMobile = /^1\d{10}$/.test(searchText);
      if (isFullMobile) {
        params.mobile = searchText;
      } else {
        params.nickName = searchText;
      }
    }
    const res = await getMemberPage(params);
    let list = extractApiRecords(res).map(normalizeRecord);
    if (query.status) list = list.filter(item => item.displayStatus === query.status);
    rows.value = list;
  } catch (_error) {
    message("用户账号数据加载失败，请稍后重试", { type: "error" });
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
  form.username = "";
  form.password = "";
  form.mobile = "";
  form.nickName = "";
  form.region = "";
  form.regionId = "";
  form.sex = 1;
  form.birthday = "";
  form.face = "";
}

function openCreate() {
  editingRow.value = null;
  resetForm();
  editVisible.value = true;
}

async function openEdit(row: Record<string, any>) {
  editingRow.value = row;
  resetForm();
  try {
    const res = await getMemberDetail(String(row.id));
    const detail = (res as any).result || (res as any).data || row;
    form.nickName = detail.nickName || detail.nickname || row.displayName || "";
    form.mobile = detail.mobile || row.displayMobile || "";
    form.region = detail.region || "";
    form.regionId = detail.regionId || "";
    form.sex = Number(detail.sex ?? 1);
    form.birthday = detail.birthday || "";
    form.face = detail.face || "";
  } catch (_error) {
    form.nickName = row.displayName || "";
    form.mobile = row.displayMobile || "";
  }
  editVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  try {
    const res = await getMemberDetail(String(row.id));
    currentRow.value = normalizeRecord(
      ((res as any).result || (res as any).data || row) as Record<string, any>
    );
  } catch (_error) {
    currentRow.value = normalizeRecord(row);
  }
  detailVisible.value = true;
}

async function handleSave() {
  if (!editingRow.value) {
    if (!form.username.trim() || !form.mobile.trim()) {
      message("新增基础账号时请填写登录账号和手机号", { type: "warning" });
      return;
    }
  } else if (!form.nickName.trim()) {
    message("编辑基础账号时请填写用户昵称", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    if (editingRow.value) {
      await updateMember({
        id: editingRow.value.id,
        password: form.password || undefined,
        nickName: form.nickName,
        region: form.region,
        regionId: form.regionId,
        sex: form.sex,
        birthday: form.birthday || undefined,
        face: form.face
      });
      message("基础账号信息修改成功", { type: "success" });
    } else {
      await createMember({
        username: form.username,
        password: form.password,
        mobile: form.mobile
      });
      message("基础账号新增成功", { type: "success" });
    }
    editVisible.value = false;
    await loadData();
  } catch (_error) {
    message("基础账号保存失败，请确认接口字段契约", { type: "error" });
  } finally {
    saving.value = false;
  }
}

async function handleToggleStatus(row: Record<string, any>) {
  try {
    await updateMemberStatus([String(row.id)], row.isDisabled);
    message(row.isDisabled ? "基础账号已恢复正常" : "基础账号已冻结", { type: "success" });
    await loadData();
  } catch (_error) {
    message("基础账号状态变更失败", { type: "error" });
  }
}

function openPointDialog(row: Record<string, any>) {
  editingRow.value = row;
  pointForm.point = 0;
  pointForm.type = "INCREASE";
  pointVisible.value = true;
}

async function handlePointSave() {
  if (!editingRow.value || pointForm.point <= 0) {
    message("请输入大于 0 的积分值", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    await updateMemberPoint(
      String(editingRow.value.id),
      Number(pointForm.point),
      pointForm.type
    );
    message("基础账号积分调整成功", { type: "success" });
    pointVisible.value = false;
    await loadData();
  } catch (_error) {
    message("基础账号积分调整失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

onMounted(loadData);
</script>

<template>
  <div class="member-account-page">
    <el-alert
      title="本页仅维护 li_member 基础账号"
      type="info"
      :closable="false"
      description="商家登录资格来源于 member + clerk + store 的关联；商家侧角色、菜单权限和店员维护不在本页处理。"
    />

    <WholesaleAdminPage
      title="用户账号管理"
      description="统一查看 li_member 基础账号，并补充商家侧开通情况，避免把基础账号台账误解为完整商家账号管理。"
      api-path="/manager/passport/member"
      :columns="columns"
      :data="rows"
      :summary-cards="summaryCards"
      :status-options="[
        { label: '正常', value: '正常' },
        { label: '已冻结', value: '已冻结' }
      ]"
      :quick-actions="[
        { label: '基础账号', value: '已接入', type: 'primary' },
        { label: '商家侧关联', value: '已聚合', type: 'warning' },
        { label: '积分调整', value: '已接入', type: 'success' }
      ]"
      keyword-label="昵称/手机号"
      keyword-placeholder="请输入昵称，或输入 11 位手机号精确匹配"
      @search="handleSearch"
      @reset="handleReset"
    >
      <template #table-extra>
        <el-button type="primary" @click="openCreate">新增基础账号</el-button>
      </template>
      <template #operation="{ row }">
        <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        <el-button link type="success" @click="openEdit(row)">编辑</el-button>
        <el-button link type="warning" @click="openPointDialog(row)">调积分</el-button>
        <el-button link type="danger" @click="handleToggleStatus(row)">
          {{ row.isDisabled ? "恢复" : "冻结" }}
        </el-button>
      </template>
    </WholesaleAdminPage>
  </div>

  <el-dialog
    v-model="editVisible"
    :title="editingRow ? '编辑基础账号' : '新增基础账号'"
    width="620px"
  >
    <el-alert
      class="dialog-note"
      title="仅维护基础账号信息"
      type="warning"
      :closable="false"
      description="本弹窗不维护商家端角色、菜单权限和店员授权。"
    />
    <el-form label-width="92px">
      <template v-if="!editingRow">
        <el-form-item label="登录账号" required>
          <el-input v-model="form.username" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.mobile" placeholder="请输入手机号" />
        </el-form-item>
      </template>
      <el-form-item v-if="editingRow" label="用户昵称" required>
        <el-input v-model="form.nickName" placeholder="请输入用户昵称" />
      </el-form-item>
      <el-form-item v-else label="登录密码">
        <el-input v-model="form.password" placeholder="请输入登录密码" />
      </el-form-item>
      <template v-if="editingRow">
        <el-form-item label="重置密码">
          <el-input v-model="form.password" placeholder="如需重置密码请填写" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="地区">
          <el-input v-model="form.region" placeholder="请输入地区名称" />
        </el-form-item>
        <el-form-item label="地区ID">
          <el-input v-model="form.regionId" placeholder="请输入地区ID" />
        </el-form-item>
        <el-form-item label="生日">
          <el-input v-model="form.birthday" placeholder="yyyy-MM-dd" />
        </el-form-item>
        <el-form-item label="头像地址">
          <el-input v-model="form.face" placeholder="请输入头像地址" />
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="pointVisible" title="调整基础账号积分" width="480px">
    <el-alert
      class="dialog-note"
      title="仅调整基础账号积分"
      type="info"
      :closable="false"
      description="商家侧角色、菜单和店员关系不在这里维护。"
    />
    <el-form label-width="88px">
      <el-form-item label="调整类型">
        <el-radio-group v-model="pointForm.type">
          <el-radio value="INCREASE">增加</el-radio>
          <el-radio value="DECREASE">减少</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="积分值" required>
        <el-input-number v-model="pointForm.point" :min="1" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pointVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handlePointSave">确认调整</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="用户账号详情" size="620px">
    <div v-if="currentRow" class="detail-pane">
      <el-alert
        title="商家侧信息仅做查看"
        type="info"
        :closable="false"
        description="若未关联 clerk 关系，则表示该基础账号尚未开通商家侧登录资格。"
      />

      <section class="detail-section">
        <h3 class="detail-section__title">基础账号信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="登录账号">
            {{ currentRow.displayUsername }}
          </el-descriptions-item>
          <el-descriptions-item label="用户昵称">{{ currentRow.displayName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentRow.displayMobile }}</el-descriptions-item>
          <el-descriptions-item label="账号归属">
            {{ currentRow.displayAccountOwnership }}
          </el-descriptions-item>
          <el-descriptions-item label="账号状态">{{ currentRow.displayStatus }}</el-descriptions-item>
          <el-descriptions-item label="会员等级">
            {{ currentRow.gradeName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="积分">{{ currentRow.displayPoints }}</el-descriptions-item>
          <el-descriptions-item label="所在地区">{{ currentRow.displayRegion }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentRow.displayTime }}</el-descriptions-item>
          <el-descriptions-item label="备注信息">{{ currentRow.displayRemark }}</el-descriptions-item>
        </el-descriptions>
      </section>

      <section class="detail-section">
        <h3 class="detail-section__title">商家侧信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商家侧账号">
            {{ currentRow.hasClerkAccount ? "已开通" : "未开通商家侧账号" }}
          </el-descriptions-item>
          <el-descriptions-item label="商家侧身份">
            {{ currentRow.displayClerkIdentity }}
          </el-descriptions-item>
          <el-descriptions-item label="商家侧状态">
            {{ currentRow.displayClerkStatus }}
          </el-descriptions-item>
          <el-descriptions-item label="主店铺">
            {{ currentRow.displayMainStore }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺业务类型">
            {{ getStoreBizTypeLabel(currentRow.storeBizType) }}
          </el-descriptions-item>
          <el-descriptions-item label="代理等级">
            {{ currentRow.isAgent ? getAgentLevelLabel(currentRow.agentLevel) : "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="代理区域">
            {{ currentRow.agentRegionName || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="商家侧账号ID">
            {{ currentRow.clerkId || "-" }}
          </el-descriptions-item>
        </el-descriptions>
      </section>
    </div>
  </el-drawer>
</template>

<style scoped>
.member-account-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dialog-note {
  margin-bottom: 16px;
}

.detail-pane {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-section__title {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 700;
}

</style>
