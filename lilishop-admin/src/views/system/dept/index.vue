<script setup lang="ts">
import { computed, ref } from "vue";
import dayjs from "dayjs";
import { utils, writeFile } from "xlsx";
import { useDept } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import AdminModuleShell from "@/components/AdminModuleShell";

import Delete from "~icons/ep/delete";
import EditPen from "~icons/ep/edit-pen";
import Refresh from "~icons/ep/refresh";
import AddFill from "~icons/ri/add-circle-line";

defineOptions({
  name: "SystemDept"
});

const formRef = ref();
const tableRef = ref();
const {
  form,
  loading,
  columns,
  dataList,
  onSearch,
  resetForm,
  openDialog,
  handleDelete,
  onbatchDel,
  selectedNum,
  handleSelectionChange
} = useDept();

const summaryCards = computed(() => [
  {
    label: "根部门",
    value: dataList.value.length,
    accent: "orange" as const,
    hint: "当前树形根节点结果"
  },
  {
    label: "部门总览",
    value: "树形结构",
    accent: "green" as const,
    hint: "已切换真实部门树"
  },
  {
    label: "组织治理",
    value: "树形结构",
    accent: "blue" as const,
    hint: "支持层级新增与编辑"
  },
  {
    label: "当前状态",
    value: "可用",
    accent: "purple" as const,
    hint: "组织结构已纳入主菜单"
  }
]);

function onFullscreen() {
  // 重置表格高度
  tableRef.value.setAdaptive();
}

function flattenDepartments(list: Record<string, any>[], parentName = "") {
  return list.flatMap(item => [
    {
      部门名称: item.title,
      上级部门: parentName || "-",
      部门ID: item.id,
      排序: item.sortOrder,
      创建时间: item.createTime
        ? dayjs(item.createTime).format("YYYY-MM-DD HH:mm:ss")
        : "-"
    },
    ...flattenDepartments(item.children ?? [], item.title || parentName)
  ]);
}

function exportDepartments() {
  if (!dataList.value.length) return;
  const worksheet = utils.json_to_sheet(
    flattenDepartments(dataList.value as any[])
  );
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "组织架构配置");
  writeFile(workbook, "组织架构配置.xlsx");
}
</script>

<template>
  <AdminModuleShell
    title="组织架构配置"
    description="承接平台部门层级与组织结构的树形维护。"
    api-path="/manager/permission/department"
    :tips="['部门树维护', '名称搜索', '层级新增', '组织结构治理']"
    :summary-cards="summaryCards"
  >
  <div class="main">
    <el-form
      ref="formRef"
      :inline="true"
      :model="form"
      class="search-form bg-bg_color w-full pl-8 pt-3 overflow-auto"
    >
      <el-form-item label="部门名称：" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入部门名称"
          clearable
          class="w-45!"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          :icon="useRenderIcon('ri/search-line')"
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
      title="组织部门台账"
      :columns="columns"
      :tableRef="tableRef?.getTableRef()"
      @refresh="onSearch"
      @fullscreen="onFullscreen"
    >
      <template #buttons>
        <el-button :disabled="!dataList.length" @click="exportDepartments">导出</el-button>
        <el-popconfirm title="是否确认批量删除已选部门?" @confirm="onbatchDel">
          <template #reference>
            <el-button type="danger" :disabled="!selectedNum">批量删除</el-button>
          </template>
        </el-popconfirm>
        <el-button
          type="primary"
          :icon="useRenderIcon(AddFill)"
          @click="openDialog()"
        >
          新增部门
        </el-button>
      </template>
      <template v-slot="{ size, dynamicColumns }">
        <pure-table
          ref="tableRef"
          adaptive
          :adaptiveConfig="{ offsetBottom: 45 }"
          align-whole="center"
          row-key="id"
          showOverflowTooltip
          table-layout="auto"
          default-expand-all
          :loading="loading"
          :size="size"
          :data="dataList"
          :columns="dynamicColumns"
          :header-cell-style="{
            background: 'var(--el-fill-color-light)',
            color: 'var(--el-text-color-primary)'
          }"
          @selection-change="handleSelectionChange"
        >
          <template #operation="{ row }">
            <el-button
              class="reset-margin"
              link
              type="primary"
              :size="size"
              :icon="useRenderIcon(EditPen)"
              @click="openDialog('修改', row)"
            >
              修改
            </el-button>
            <el-button
              class="reset-margin"
              link
              type="primary"
              :size="size"
              :icon="useRenderIcon(AddFill)"
              @click="openDialog('新增', { parentId: row.id } as any)"
            >
              新增
            </el-button>
            <el-popconfirm
              :title="`是否确认删除部门名称为${row.title}的这条数据`"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button
                  class="reset-margin"
                  link
                  type="primary"
                  :size="size"
                  :icon="useRenderIcon(Delete)"
                >
                  删除
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
:deep(.el-table__inner-wrapper::before) {
  height: 0;
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
