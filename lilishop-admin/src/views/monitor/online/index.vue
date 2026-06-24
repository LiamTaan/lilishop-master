<script setup lang="ts">
import { computed, ref } from "vue";
import { useRole } from "./hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import AdminModuleShell from "@/components/AdminModuleShell";

import Plane from "~icons/ri/plane-line";
import Refresh from "~icons/ep/refresh";

defineOptions({
  name: "OnlineUser"
});

const formRef = ref();
const {
  form,
  loading,
  columns,
  dataList,
  pagination,
  onSearch,
  resetForm,
  handleOffline,
  handleSizeChange,
  handleCurrentChange,
  handleSelectionChange
} = useRole();

const summaryCards = computed(() => [
  {
    label: "接口状态",
    value: "未接入",
    accent: "orange" as const,
    hint: "当前仓库未发现真实后端接口"
  },
  {
    label: "模板数据",
    value: "已停用",
    accent: "green" as const,
    hint: "不再展示旧模板数据"
  },
  {
    label: "后端能力",
    value: "待补齐",
    accent: "blue" as const,
    hint: "需要会话列表接口"
  },
  {
    label: "治理动作",
    value: "不可用",
    accent: "purple" as const,
    hint: "强退能力待后端补齐"
  }
]);
</script>

<template>
  <AdminModuleShell
    title="在线会话台账"
    description="当前仓库未发现管理端在线会话列表与强退接口，页面仅保留结构，不再展示模板假数据。"
    api-path="未接入真实管理端在线会话接口"
    :tips="['当前不展示模板数据', '待后端补齐会话列表与强退能力']"
    :summary-cards="summaryCards"
  >
  <div class="main">
    <el-form
      ref="formRef"
      :inline="true"
      :model="form"
      class="search-form bg-bg_color w-full pl-8 pt-3 overflow-auto"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          placeholder="请输入用户名"
          clearable
          class="w-45!"
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
      title="在线会话台账"
      :columns="columns"
      @refresh="onSearch"
    >
      <template v-slot="{ size, dynamicColumns }">
        <pure-table
          align-whole="center"
          showOverflowTooltip
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
        >
          <template #operation="{ row }">
            <el-popconfirm
              :title="`是否强制下线${row.username}`"
              @confirm="handleOffline(row)"
            >
              <template #reference>
                <el-button
                  class="reset-margin"
                  link
                  type="primary"
                  :size="size"
                  :icon="useRenderIcon(Plane)"
                >
                  强退
                </el-button>
              </template>
            </el-popconfirm>
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
