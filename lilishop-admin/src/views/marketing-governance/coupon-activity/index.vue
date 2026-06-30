<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  createCouponActivity,
  deleteCouponActivity,
  getCouponPage,
  getCouponActivityDetail,
  getCouponActivityPage
} from "@/api/marketing-governance";
import { getMemberPage } from "@/api/super-admin";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime,
  getPromotionStatusLabel
} from "@/utils/admin-governance";
import { message } from "@/utils/message";
import {
  createCouponActivityItem,
  createMemberItem,
  extractMemberListFromScopeInfo,
  formatDiscount,
  formatMoney,
  normalizePromotionDetail,
  toNumber
} from "../shared/promotion-helpers";

defineOptions({
  name: "CouponActivityManage"
});

const rows = ref<Record<string, any>[]>([]);
const selectedRows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const dialogVisible = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const saving = ref(false);
const couponLoading = ref(false);
const memberLoading = ref(false);
const query = reactive({
  keyword: "",
  status: ""
});
const couponOptions = ref<Array<{ label: string; value: string; couponName: string }>>([]);
const memberOptions = ref<
  Array<{ label: string; value: string; nickName: string; mobile: string }>
>([]);
const activityForm = reactive(createActivityForm());

const columns: TableColumnList = [
  { label: "活动名称", prop: "promotionName", minWidth: 220 },
  { label: "活动类型", prop: "couponActivityType", minWidth: 140 },
  { label: "活动范围", prop: "activityScope", minWidth: 140 },
  { label: "领取周期", prop: "couponFrequencyEnum", minWidth: 120 },
  { label: "状态", prop: "promotionStatusLabel", minWidth: 120 },
  { label: "操作", prop: "operation", fixed: "right", width: 220, slot: "operation" }
];

const filteredRows = computed(() =>
  rows.value.filter(item => {
    const keywordMatched = query.keyword
      ? String(item.promotionName).includes(query.keyword)
      : true;
    const statusMatched = query.status
      ? String(item.promotionStatus).toUpperCase() === query.status
      : true;
    return keywordMatched && statusMatched;
  })
);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

const detailFields = computed(() => {
  if (!currentRow.value) {
    return [];
  }
  const detail = currentRow.value;
  return [
    { label: "活动名称", value: detail.promotionName || "-" },
    { label: "活动类型", value: detail.couponActivityType || "-" },
    { label: "活动范围", value: detail.activityScope || "-" },
    { label: "领取周期", value: detail.couponFrequencyEnum || "-" },
    { label: "范围说明", value: detail.activityScopeInfo || "-" },
    { label: "开始时间", value: detail.startTimeText || "-" },
    { label: "结束时间", value: detail.endTimeText || "-" },
    { label: "状态", value: detail.promotionStatusLabel || "-" },
    { label: "创建时间", value: detail.createTimeText || "-" }
  ];
});

function normalizeCouponOption(item: Record<string, any>) {
  const value = String(item.id || item.couponId || "").trim();
  const couponName = item.couponName || item.title || item.name || "未命名优惠券";
  return {
    label: couponName,
    value,
    couponName
  };
}

function mergeCouponOptions(list: Record<string, any>[]) {
  const optionMap = new Map(couponOptions.value.map(item => [item.value, item] as const));
  list
    .map(normalizeCouponOption)
    .filter(item => item.value)
    .forEach(item => optionMap.set(item.value, item));
  couponOptions.value = Array.from(optionMap.values());
}

function normalizeMemberOption(item: Record<string, any>) {
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

function mergeMemberOptions(list: Record<string, any>[]) {
  const optionMap = new Map(memberOptions.value.map(item => [item.value, item] as const));
  list
    .map(normalizeMemberOption)
    .filter(item => item.value)
    .forEach(item => optionMap.set(item.value, item));
  memberOptions.value = Array.from(optionMap.values());
}

function createActivityForm() {
  return {
    promotionName: "",
    couponActivityType: "SPECIFY",
    activityScope: "ALL",
    couponFrequencyEnum: "DAY",
    activityScopeInfo: "",
    startTime: "",
    endTime: "",
    couponActivityItems: [createCouponActivityItem()],
    memberDTOS: [createMemberItem()]
  };
}

function resetActivityForm() {
  Object.assign(activityForm, createActivityForm());
}

async function searchCoupons(keyword: string) {
  couponLoading.value = true;
  try {
    const response = await getCouponPage({
      pageNumber: 1,
      pageSize: 50,
      couponName: keyword.trim() || undefined
    });
    mergeCouponOptions(extractApiRecords(response));
  } catch (_error) {
    message("优惠券搜索失败", { type: "error" });
  } finally {
    couponLoading.value = false;
  }
}

async function searchMembers(keyword: string) {
  const searchText = keyword.trim();
  if (!searchText) {
    memberOptions.value = [];
    return;
  }
  memberLoading.value = true;
  try {
    const isFullMobile = /^1\d{10}$/.test(searchText);
    const response = await getMemberPage({
      nickName: isFullMobile ? undefined : searchText,
      mobile: isFullMobile ? searchText : undefined
    });
    mergeMemberOptions(extractApiRecords(response));
  } catch (_error) {
    message("会员搜索失败", { type: "error" });
  } finally {
    memberLoading.value = false;
  }
}

function handleCouponChange(item: Record<string, any>, couponId: string) {
  const target = couponOptions.value.find(option => option.value === couponId);
  item.couponId = couponId;
  item.couponName = target?.couponName || "";
}

function handleMemberChange(item: Record<string, any>, memberId: string) {
  const target = memberOptions.value.find(option => option.value === memberId);
  item.id = memberId;
  item.nickName = target?.nickName || "";
}

function buildCouponActivityPayload() {
  return {
    promotionName: activityForm.promotionName,
    couponActivityType: activityForm.couponActivityType,
    activityScope: activityForm.activityScope,
    couponFrequencyEnum: activityForm.couponFrequencyEnum,
    activityScopeInfo: activityForm.activityScopeInfo,
    startTime: activityForm.startTime,
    endTime: activityForm.endTime,
    couponActivityItems: activityForm.couponActivityItems.map(item => ({
      couponId: item.couponId,
      num: toNumber(item.num)
    })),
    memberDTOS:
      activityForm.activityScope === "DESIGNATED"
        ? activityForm.memberDTOS
            .filter(item => item.id || item.nickName)
            .map(item => ({ id: item.id, nickName: item.nickName }))
        : []
  };
}

function normalizeRow(item: Record<string, any>) {
  return {
    ...normalizePromotionDetail(item),
    promotionName: item.promotionName || "-",
    couponActivityType: item.couponActivityType || "-",
    activityScope: item.activityScope || "-",
    couponFrequencyEnum: item.couponFrequencyEnum || "-"
  };
}

async function loadData() {
  try {
    const response = await getCouponActivityPage({
      pageNumber: 1,
      pageSize: 200,
      promotionName: query.keyword || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("券活动列表加载失败", { type: "error" });
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

function handleSelectionChange(rows: Record<string, any>[]) {
  selectedRows.value = rows;
}

function openCreate() {
  resetActivityForm();
  searchCoupons("");
  dialogVisible.value = true;
}

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getCouponActivityDetail(String(row.id));
    const detail = extractApiPayload<Record<string, any>>(response) || row;
    const normalized: Record<string, any> = normalizeRow(detail);
    normalized.memberDTOS = extractMemberListFromScopeInfo(detail.activityScopeInfo);
    normalized.couponActivityItems = Array.isArray(detail.couponActivityItems)
      ? detail.couponActivityItems.map((item: Record<string, any>) => ({
          ...createCouponActivityItem(item),
          displayAmount:
            item.couponType === "DISCOUNT"
              ? formatDiscount(item.couponDiscount)
              : formatMoney(item.price)
        }))
      : [];
    currentRow.value = normalized;
    detailVisible.value = true;
  } catch (_error) {
    message("券活动详情加载失败", { type: "error" });
  }
}

async function submitPayload() {
  saving.value = true;
  try {
    await createCouponActivity(buildCouponActivityPayload());
    message("券活动创建成功", { type: "success" });
    dialogVisible.value = false;
    await loadData();
  } catch (_error) {
    message("券活动创建失败，请检查表单内容", { type: "error" });
  } finally {
    saving.value = false;
  }
}

function addCouponItem() {
  activityForm.couponActivityItems.push(createCouponActivityItem());
}

function removeCouponItem(index: number) {
  if (activityForm.couponActivityItems.length === 1) {
    activityForm.couponActivityItems.splice(0, 1, createCouponActivityItem());
    return;
  }
  activityForm.couponActivityItems.splice(index, 1);
}

function addMemberItem() {
  activityForm.memberDTOS.push(createMemberItem());
}

function removeMemberItem(index: number) {
  if (activityForm.memberDTOS.length === 1) {
    activityForm.memberDTOS.splice(0, 1, createMemberItem());
    return;
  }
  activityForm.memberDTOS.splice(index, 1);
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认关闭券活动「${row.promotionName}」吗？`, "关闭确认", {
    type: "warning"
  });
  try {
    await deleteCouponActivity(String(row.id));
    message("券活动已关闭", { type: "success" });
    await loadData();
  } catch (_error) {
    message("券活动关闭失败", { type: "error" });
  }
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要关闭的券活动", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认关闭已勾选的 ${selectedIds.value.length} 个券活动吗？`,
    "批量关闭确认",
    { type: "warning" }
  );
  try {
    await Promise.all(selectedIds.value.map(id => deleteCouponActivity(id)));
    selectedRows.value = [];
    message("券活动批量关闭成功", { type: "success" });
    await loadData();
  } catch (_error) {
    message("券活动批量关闭失败", { type: "error" });
  }
}

function exportActivities() {
  if (!filteredRows.value.length) {
    message("暂无可导出的券活动数据", { type: "warning" });
    return;
  }
  const table = filteredRows.value.map(item => ({
    活动名称: item.promotionName,
    活动类型: item.couponActivityType,
    活动范围: item.activityScope,
    领取周期: item.couponFrequencyEnum,
    状态: item.promotionStatusLabel || getPromotionStatusLabel(item.promotionStatus),
    创建时间: item.createTimeText || formatAdminDateTime(item.createTime)
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "券活动管理");
  writeFile(workbook, "券活动管理.xlsx");
  message("券活动数据导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="券活动管理"
    description="承接优惠券活动的列表、详情、新增和关闭处理。"
    api-path="/manager/promotion/couponActivity"
    :columns="columns"
    :data="filteredRows"
    selectable
    :status-options="[
      { label: '待开始', value: 'NEW' },
      { label: '进行中', value: 'START' },
      { label: '已结束', value: 'END' }
    ]"
    keyword-label="活动名称"
    keyword-placeholder="请输入活动名称"
    @search="handleSearch"
    @reset="handleReset"
    @selection-change="handleSelectionChange"
  >
    <template #table-extra>
      <el-button type="danger" plain @click="handleBatchDelete">批量关闭</el-button>
      <el-button @click="exportActivities">导出</el-button>
      <el-button type="primary" @click="openCreate">新增券活动</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="danger" @click="handleDelete(row)">关闭</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="dialogVisible" title="新增券活动" width="980px">
    <el-form label-width="110px">
      <div class="form-grid">
        <el-form-item label="活动名称">
          <el-input v-model="activityForm.promotionName" />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="activityForm.couponActivityType">
            <el-option label="精准发券" value="SPECIFY" />
            <el-option label="新人赠券" value="REGISTERED" />
            <el-option label="邀新赠券" value="INVITE_NEW" />
            <el-option label="自动赠券" value="AUTO_COUPON" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动范围">
          <el-select v-model="activityForm.activityScope">
            <el-option label="全部客户" value="ALL" />
            <el-option label="指定客户" value="DESIGNATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="领取周期">
          <el-select v-model="activityForm.couponFrequencyEnum">
            <el-option label="每天" value="DAY" />
            <el-option label="每周" value="WEEK" />
            <el-option label="每月" value="MONTH" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="activityForm.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="activityForm.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </div>
      <el-form-item label="范围说明">
        <el-input
          v-model="activityForm.activityScopeInfo"
          type="textarea"
          :rows="2"
          placeholder="补充活动范围说明，可留空"
        />
      </el-form-item>

      <section class="form-section">
        <div class="section-header">
          <span>发券明细</span>
          <el-button type="primary" link @click="addCouponItem">新增券项</el-button>
        </div>
        <div
          v-for="(item, index) in activityForm.couponActivityItems"
          :key="`coupon-item-${index}`"
          class="editor-card"
        >
          <div class="section-header">
            <strong>券项 {{ index + 1 }}</strong>
            <el-button type="danger" link @click="removeCouponItem(index)">移除</el-button>
          </div>
          <div class="form-grid">
            <el-form-item label="选择优惠券">
              <el-select
                :model-value="item.couponId"
                filterable
                remote
                clearable
                reserve-keyword
                :remote-method="searchCoupons"
                :loading="couponLoading"
                placeholder="请输入优惠券名称搜索后选择"
                style="width: 100%"
                @update:model-value="value => handleCouponChange(item, String(value || ''))"
              >
                <el-option
                  v-for="option in couponOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="发放数量">
              <el-input-number v-model="item.num" :min="1" :max="2" style="width: 100%" />
            </el-form-item>
          </div>
        </div>
      </section>

      <section v-if="activityForm.activityScope === 'DESIGNATED'" class="form-section">
        <div class="section-header">
          <span>指定客户</span>
          <el-button type="primary" link @click="addMemberItem">新增客户</el-button>
        </div>
        <div
          v-for="(item, index) in activityForm.memberDTOS"
          :key="`member-${index}`"
          class="editor-card"
        >
          <div class="section-header">
            <strong>客户 {{ index + 1 }}</strong>
            <el-button type="danger" link @click="removeMemberItem(index)">移除</el-button>
          </div>
          <div class="form-grid">
            <el-form-item label="指定会员">
              <el-select
                :model-value="item.id"
                filterable
                remote
                clearable
                reserve-keyword
                :remote-method="searchMembers"
                :loading="memberLoading"
                placeholder="请输入昵称或手机号搜索会员"
                style="width: 100%"
                @update:model-value="value => handleMemberChange(item, String(value || ''))"
              >
                <el-option
                  v-for="option in memberOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="会员昵称">
              <el-input v-model="item.nickName" disabled />
            </el-form-item>
          </div>
        </div>
      </section>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitPayload">
        保存
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="券活动详情" size="56%">
    <el-descriptions v-if="currentRow" :column="2" border class="mb-4">
      <el-descriptions-item
        v-for="field in detailFields"
        :key="field.label"
        :label="field.label"
        :span="field.label === '范围说明' ? 2 : 1"
      >
        <div class="detail-text">{{ field.value }}</div>
      </el-descriptions-item>
    </el-descriptions>

    <section v-if="currentRow?.couponActivityItems?.length" class="form-section">
      <div class="section-header">
        <span>发券明细</span>
      </div>
      <pure-table
        row-key="couponId"
        :data="currentRow.couponActivityItems"
        :columns="[
          { label: '优惠券名称', prop: 'couponName', minWidth: 180 },
          { label: '券类型', prop: 'couponType', minWidth: 120 },
          { label: '面额/折扣', prop: 'displayAmount', minWidth: 120 },
          { label: '发放数量', prop: 'num', minWidth: 100 }
        ]"
      >
        <template #default />
      </pure-table>
    </section>

    <section v-if="currentRow?.memberDTOS?.length" class="form-section">
      <div class="section-header">
        <span>指定客户</span>
      </div>
      <pure-table
        row-key="id"
        :data="currentRow.memberDTOS"
        :columns="[
          { label: '会员昵称', prop: 'nickName', minWidth: 180 }
        ]"
      />
    </section>
  </el-drawer>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

.form-section {
  margin-top: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  background: #fafbfc;
}

.editor-card {
  margin-top: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #edf0f5;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-text {
  white-space: pre-wrap;
  word-break: break-word;
}

@media (width <= 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
