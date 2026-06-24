<script setup lang="ts">
import { computed, ref } from "vue";
import { useRole } from "./hook";
import { getPickerShortcuts } from "../../utils";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import AdminModuleShell from "@/components/AdminModuleShell";

import View from "~icons/ep/view";
import Delete from "~icons/ep/delete";
import Refresh from "~icons/ep/refresh";

defineOptions({
  name: "SystemLog"
});

const formRef = ref();
const tableRef = ref();

const {
  form,
  loading,
  columns,
  dataList,
  pagination,
  selectedNum,
  onSearch,
  onDetail,
  clearAll,
  resetForm,
  onbatchDel,
  handleSizeChange,
  onSelectionCancel,
  handleCellDblclick,
  handleCurrentChange,
  handleSelectionChange
} = useRole(tableRef);

const summaryCards = computed(() => [
  {
    label: "日志条数",
    value: dataList.value.length,
    accent: "orange" as const,
    hint: "当前分页结果"
  },
  {
    label: "操作人员",
    value: [...new Set(dataList.value.map(item => item.username).filter(Boolean))]
      .length,
    accent: "green" as const,
    hint: "当前涉及操作人数量"
  },
  {
    label: "慢请求",
    value: dataList.value.filter(item => Number(item.costTime) >= 1000).length,
    accent: "blue" as const,
    hint: "请求耗时大于等于 1000ms"
  },
  {
    label: "详情追踪",
    value: "已接入",
    accent: "purple" as const,
    hint: "支持详情与接口复制"
  }
]);
</script>

<template>
  <AdminModuleShell
    title="系统请求台账"
    description="承接系统级请求日志、异常排查与请求详情审计查看。"
    api-path="/manager/setting/log/getAllByPage"
    :tips="['操作人筛选', '请求关键字检索', '详情查看', '日志清空']"
    :summary-cards="summaryCards"
  >
  <div class="main">
    <el-form
      ref="formRef"
      :inline="true"
      :model="form"
      class="search-form bg-bg_color w-full pl-8 pt-3 overflow-auto"
    >
      <el-form-item label="操作人员" prop="operatorName">
        <el-input
          v-model="form.operatorName"
          placeholder="请输入操作人员"
          clearable
          class="w-42.5!"
        />
      </el-form-item>
      <el-form-item label="关键字" prop="searchKey">
        <el-input
          v-model="form.searchKey"
          placeholder="请输入操作名称或接口地址"
          clearable
          class="w-50!"
        />
      </el-form-item>
      <el-form-item label="请求时间" prop="requestTime">
        <el-date-picker
          v-model="form.requestTime"
          :shortcuts="getPickerShortcuts()"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期时间"
          end-placeholder="结束日期时间"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          :icon="useRenderIcon('ri:search-line')"
          :loading="loading"
          @click="onSearch"
        >
          搜索
        </el-button>
        <el-button :icon="useRenderIcon(Refresh)" @click="resetForm(formRef)">
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <PureTableBar
      title="系统请求台账"
      :columns="columns"
      @refresh="onSearch"
    >
      <template #buttons>
        <el-popconfirm title="确定要删除所有日志数据吗？" @confirm="clearAll">
          <template #reference>
            <el-button type="danger" :icon="useRenderIcon(Delete)">
              清空日志
            </el-button>
          </template>
        </el-popconfirm>
      </template>
      <template v-slot="{ size, dynamicColumns }">
        <div
          v-if="selectedNum > 0"
          v-motion-fade
          class="bg-(--el-fill-color-light) w-full h-11.5 mb-2 pl-4 flex items-center"
        >
          <div class="flex-auto">
            <span
              style="font-size: var(--el-font-size-base)"
              class="text-[rgba(42,46,54,0.5)] dark:text-[rgba(220,220,242,0.5)]"
            >
              已选 {{ selectedNum }} 项
            </span>
            <el-button type="primary" text @click="onSelectionCancel">
              取消选择
            </el-button>
          </div>
          <el-popconfirm title="是否确认删除?" @confirm="onbatchDel">
            <template #reference>
              <el-button type="danger" text class="mr-1!"> 批量删除 </el-button>
            </template>
          </el-popconfirm>
        </div>
        <pure-table
          ref="tableRef"
          row-key="id"
          align-whole="center"
          table-layout="auto"
          :loading="loading"
          :size="size"
          adaptive
          :adaptiveConfig="{ offsetBottom: 108 }"
          :data="dataList"
          :columns="dynamicColumns"
          :pagination="{ ...pagination, size }"
          :header-cell-style="{
            background: 'var(--el-fill-color-light)',
            color: 'var(--el-text-color-primary)'
          }"
          @selection-change="handleSelectionChange"
          @page-size-change="handleSizeChange"
          @page-current-change="handleCurrentChange"
          @cell-dblclick="handleCellDblclick"
        >
          <template #operation="{ row }">
            <el-button
              class="reset-margin outline-hidden!"
              link
              type="primary"
              :size="size"
              :icon="useRenderIcon(View)"
              @click="onDetail(row)"
            >
              详情
            </el-button>
          </template>
        </pure-table>
      </template>
    </PureTableBar>
  </div>
  </AdminModuleShell>
</template>

<style lang="scss" scoped>
:deep(.el-dropdown-menu__item i) {
  margin: 0;
}

.main-content {
  margin: 24px 24px 0 !important;
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style>
