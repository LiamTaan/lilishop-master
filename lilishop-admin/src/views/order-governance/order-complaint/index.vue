<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import {
  completeOrderComplaint,
  createOrderComplaintCommunication,
  getOrderComplaintDetail,
  getOrderComplaintPage,
  updateOrderComplaintStatus
} from "@/api/order-governance";
import ImageUploadField from "@/components/ImageUploadField/index.vue";
import WholesaleAdminPage from "@/components/WholesaleAdminPage";
import {
  extractApiPayload,
  extractApiRecords,
  formatAdminDateTime
} from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "OrderComplaintManage"
});

const rows = ref<Record<string, any>[]>([]);
const detailVisible = ref(false);
const detailRow = ref<Record<string, any> | null>(null);
const statusDialogVisible = ref(false);
const statusSaving = ref(false);
const currentRow = ref<Record<string, any> | null>(null);
const communicationContent = ref("");
const communicationSaving = ref(false);
const query = reactive({
  keyword: "",
  status: ""
});
const statusForm = reactive({
  complainStatus: "PENDING",
  appealContent: "",
  images: [""] as string[]
});

const columns: TableColumnList = [
  { label: "投诉单号", prop: "complainSn", minWidth: 180 },
  { label: "订单号", prop: "orderSn", minWidth: 180 },
  { label: "会员", prop: "memberName", minWidth: 140 },
  { label: "店铺", prop: "storeName", minWidth: 180 },
  { label: "投诉状态", prop: "complainStatus", minWidth: 120 },
  { label: "创建时间", prop: "createTime", minWidth: 180 },
  { label: "操作", prop: "operation", fixed: "right", width: 280, slot: "operation" }
];

const communicationColumns: TableColumnList = [
  { label: "沟通方", prop: "owner", minWidth: 120 },
  { label: "沟通人", prop: "ownerName", minWidth: 140 },
  { label: "沟通内容", prop: "content", minWidth: 320, showOverflowTooltip: true },
  { label: "沟通时间", prop: "createTime", minWidth: 180 }
];

const filteredRows = computed(() =>
  rows.value.filter(item => {
    const keywordMatched = query.keyword
      ? [item.complainSn, item.orderSn, item.memberName].some(value =>
          String(value || "").includes(query.keyword)
        )
      : true;
    const statusMatched = query.status
      ? String(item.complainStatus).toUpperCase() === query.status
      : true;
    return keywordMatched && statusMatched;
  })
);

const summaryCards = computed(() => [
  { label: "投诉总数", value: filteredRows.value.length, accent: "orange" as const, hint: "当前筛选结果" },
  {
    label: "待处理投诉",
    value: filteredRows.value.filter(item =>
      ["NEW", "PENDING", "APPLY"].includes(String(item.complainStatus).toUpperCase())
    ).length,
    accent: "blue" as const,
    hint: "待平台处理"
  },
  {
    label: "已完结投诉",
    value: filteredRows.value.filter(item =>
      ["COMPLETE", "COMPLETED", "FINISH", "FINISHED"].includes(
        String(item.complainStatus).toUpperCase()
      )
    ).length,
    accent: "green" as const,
    hint: "已仲裁/已结束"
  },
  {
    label: "治理动作",
    value: "详情/沟通/仲裁",
    accent: "purple" as const,
    hint: "已接入真实操作"
  }
]);

function normalizeRow(item: Record<string, any>) {
  const appealImages = Array.isArray(item.images)
    ? item.images
    : Array.isArray(item.appealImages)
      ? item.appealImages
      : [];
  return {
    ...item,
    id: item.id || item.complainId,
    complainSn: item.complainSn || item.id || "-",
    orderSn: item.orderSn || "-",
    memberName: item.memberName || item.nickname || "-",
    storeName: item.storeName || "-",
    complainStatus: item.complainStatus || item.status || "-",
    complainTopic: item.complainTopic || item.content || item.reason || "-",
    appealContent: item.appealContent || "-",
    appealImages,
    arbitrationResult: item.arbitrationResult || "-",
    createTime: formatAdminDateTime(item.createTime),
    orderComplaintCommunications: (item.orderComplaintCommunications || []).map(
      (communication: Record<string, any>) => ({
        ...communication,
        owner: communication.owner || communication.communicationOwner || "-",
        ownerName: communication.ownerName || communication.operator || "-",
        content: communication.content || "-",
        createTime: formatAdminDateTime(communication.createTime)
      })
    )
  };
}

async function loadData() {
  try {
    const response = await getOrderComplaintPage({
      pageNumber: 1,
      pageSize: 200,
      orderSn: query.keyword || undefined,
      complainStatus: query.status || undefined
    });
    rows.value = extractApiRecords(response).map(normalizeRow);
  } catch (_error) {
    rows.value = [];
    message("投诉列表加载失败，请稍后重试", { type: "error" });
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

async function openDetail(row: Record<string, any>) {
  try {
    const response = await getOrderComplaintDetail(String(row.id));
    detailRow.value = normalizeRow(extractApiPayload(response) || row);
    currentRow.value = detailRow.value;
    detailVisible.value = true;
  } catch (_error) {
    message("投诉详情加载失败", { type: "error" });
  }
}

function openStatusDialog(row: Record<string, any>) {
  currentRow.value = row;
  statusForm.complainStatus = String(row.complainStatus || "PENDING");
  statusForm.appealContent = row.appealContent || "";
  statusForm.images = Array.isArray(row.appealImages) && row.appealImages.length
    ? [...row.appealImages]
    : [""];
  statusDialogVisible.value = true;
}

async function submitStatus() {
  if (!currentRow.value) return;
  statusSaving.value = true;
  try {
    await updateOrderComplaintStatus({
      complainId: currentRow.value.id,
      complainStatus: statusForm.complainStatus,
      appealContent: statusForm.appealContent || undefined,
      images: statusForm.images.map(item => item.trim()).filter(Boolean)
    });
    message("投诉状态更新成功", { type: "success" });
    statusDialogVisible.value = false;
    await loadData();
    if (detailVisible.value && currentRow.value.id) {
      await openDetail(currentRow.value);
    }
  } catch (_error) {
    message("投诉状态更新失败", { type: "error" });
  } finally {
    statusSaving.value = false;
  }
}

function addAppealImage() {
  statusForm.images.push("");
}

function removeAppealImage(index: number) {
  if (statusForm.images.length === 1) {
    statusForm.images.splice(0, 1, "");
    return;
  }
  statusForm.images.splice(index, 1);
}

async function handleComplete(row: Record<string, any>) {
  const { value } = await ElMessageBox.prompt("请输入仲裁结果", "仲裁完成", {
    inputPattern: /\S+/,
    inputErrorMessage: "仲裁结果不能为空"
  });
  try {
    await completeOrderComplaint(String(row.id), value.trim());
    message("投诉已完成仲裁", { type: "success" });
    await loadData();
  } catch (_error) {
    message("投诉仲裁失败", { type: "error" });
  }
}

async function submitCommunication() {
  if (!detailRow.value?.id) return;
  if (!communicationContent.value.trim()) {
    message("请输入沟通内容", { type: "warning" });
    return;
  }
  communicationSaving.value = true;
  try {
    await createOrderComplaintCommunication(
      String(detailRow.value.id),
      communicationContent.value.trim()
    );
    communicationContent.value = "";
    message("投诉沟通发送成功", { type: "success" });
    await openDetail(detailRow.value);
  } catch (_error) {
    message("投诉沟通发送失败", { type: "error" });
  } finally {
    communicationSaving.value = false;
  }
}

function exportComplaints() {
  if (!filteredRows.value.length) {
    message("暂无可导出的投诉数据", { type: "warning" });
    return;
  }
  const table = filteredRows.value.map(item => ({
    投诉单号: item.complainSn,
    订单号: item.orderSn,
    会员: item.memberName,
    店铺: item.storeName,
    投诉状态: item.complainStatus,
    投诉主题: item.complainTopic,
    创建时间: item.createTime
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "订单投诉");
  writeFile(workbook, "订单投诉.xlsx");
  message("投诉数据导出成功", { type: "success" });
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <WholesaleAdminPage
    title="订单投诉"
    description="承接用户投诉详情查看、平台沟通、状态流转和仲裁完成处理。"
    api-path="/manager/order/complain"
    :columns="columns"
    :data="filteredRows"
    :summary-cards="summaryCards"
    :status-options="[
      { label: '待处理', value: 'PENDING' },
      { label: '处理中', value: 'PROCESSING' },
      { label: '已完成', value: 'COMPLETE' }
    ]"
    keyword-label="投诉/订单"
    keyword-placeholder="请输入投诉单号、订单号或会员"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #table-extra>
      <el-button @click="exportComplaints">导出</el-button>
    </template>
    <template #operation="{ row }">
      <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      <el-button link type="warning" @click="openStatusDialog(row)">状态处理</el-button>
      <el-button link type="danger" @click="handleComplete(row)">仲裁完成</el-button>
    </template>
  </WholesaleAdminPage>

  <el-dialog v-model="statusDialogVisible" title="投诉状态处理" width="620px">
    <el-form label-width="96px">
      <el-form-item label="投诉状态">
        <el-select v-model="statusForm.complainStatus" style="width: 100%">
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已完成" value="COMPLETE" />
          <el-option label="已关闭" value="CLOSE" />
        </el-select>
      </el-form-item>
      <el-form-item label="申诉内容">
        <el-input v-model="statusForm.appealContent" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="申诉图片">
        <div class="image-upload-list">
          <div
            v-for="(image, index) in statusForm.images"
            :key="`appeal-image-${index}`"
            class="image-upload-item"
          >
            <ImageUploadField
              v-model="statusForm.images[index]"
              tip="申诉图片统一走上传组件维护"
            />
            <el-button
              type="danger"
              plain
              @click="removeAppealImage(index)"
            >
              删除图片
            </el-button>
          </div>
          <el-button type="primary" plain @click="addAppealImage">
            新增图片
          </el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="statusDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="statusSaving" @click="submitStatus">
        提交
      </el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="detailVisible" title="投诉详情" size="70%">
    <template v-if="detailRow">
      <el-descriptions :column="2" border class="mb-4">
        <el-descriptions-item label="投诉单号">
          {{ detailRow.complainSn }}
        </el-descriptions-item>
        <el-descriptions-item label="订单号">
          {{ detailRow.orderSn }}
        </el-descriptions-item>
        <el-descriptions-item label="会员">
          {{ detailRow.memberName }}
        </el-descriptions-item>
        <el-descriptions-item label="店铺">
          {{ detailRow.storeName }}
        </el-descriptions-item>
        <el-descriptions-item label="投诉状态">
          {{ detailRow.complainStatus }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detailRow.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="投诉主题" :span="2">
          {{ detailRow.complainTopic }}
        </el-descriptions-item>
        <el-descriptions-item label="申诉内容" :span="2">
          {{ detailRow.appealContent }}
        </el-descriptions-item>
        <el-descriptions-item label="申诉图片" :span="2">
          <div v-if="detailRow.appealImages?.length" class="appeal-image-preview">
            <img
              v-for="(image, index) in detailRow.appealImages"
              :key="`detail-appeal-${index}`"
              :src="image"
              alt="appeal"
              class="appeal-image-preview__img"
            />
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="仲裁结果" :span="2">
          {{ detailRow.arbitrationResult }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="complaint-action-bar">
        <el-input
          v-model="communicationContent"
          type="textarea"
          :rows="3"
          placeholder="请输入平台沟通内容"
        />
        <el-button
          type="primary"
          :loading="communicationSaving"
          @click="submitCommunication"
        >
          发送沟通
        </el-button>
      </div>

      <pure-table
        row-key="id"
        :data="detailRow.orderComplaintCommunications || []"
        :columns="communicationColumns"
      />
    </template>
  </el-drawer>
</template>

<style scoped>
.complaint-action-bar {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 16px;
  align-items: start;
  margin: 20px 0;
}

.image-upload-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.image-upload-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  background: #fafbfc;
}

.appeal-image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.appeal-image-preview__img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid #ebeef5;
}
</style>
