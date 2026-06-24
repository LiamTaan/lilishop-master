<script setup lang="ts">
import { computed, ref } from "vue";
import tree from "./tree.vue";
import { useUser } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import AdminModuleShell from "@/components/AdminModuleShell";

import Upload from "~icons/ri/upload-line";
import Role from "~icons/ri/admin-line";
import Password from "~icons/ri/lock-password-line";
import More from "~icons/ep/more-filled";
import Delete from "~icons/ep/delete";
import EditPen from "~icons/ep/edit-pen";
import Refresh from "~icons/ep/refresh";
import Filter from "~icons/ri/node-tree";
import AddFill from "~icons/ri/add-circle-line";

defineOptions({
  name: "SystemUser"
});

const treeRef = ref();
const formRef = ref();
const tableRef = ref();
const showDeptTree = ref(false);

const {
  form,
  loading,
  columns,
  dataList,
  treeData,
  treeLoading,
  selectedNum,
  pagination,
  buttonClass,
  deviceDetection,
  onSearch,
  resetForm,
  onbatchDel,
  openDialog,
  onTreeSelect,
  handleDelete,
  handleUpload,
  handleReset,
  handleRole,
  handleSizeChange,
  onSelectionCancel,
  handleCurrentChange,
  handleSelectionChange
} = useUser(tableRef, treeRef);

const summaryCards = computed(() => [
  {
    label: "账号总数",
    value: dataList.value.length,
    accent: "orange" as const,
    hint: "当前分页结果"
  },
  {
    label: "启用账号",
    value: dataList.value.filter(item => item.status === true).length,
    accent: "green" as const,
    hint: "当前启用中的平台账号"
  },
  {
    label: "部门覆盖",
    value: treeData.value.length,
    accent: "blue" as const,
    hint: "组织树根节点数"
  },
  {
    label: "角色分配",
    value: "已接入",
    accent: "purple" as const,
    hint: "支持角色授权与密码重置"
  }
]);
</script>

<template>
  <AdminModuleShell
    title="平台账号台账"
    description="承接平台账号、组织归属、角色分配和基础账号动作维护。"
    api-path="/system/user"
    :tips="['账号查询', '角色分配', '组织树联动', '支持重置密码']"
    :summary-cards="summaryCards"
  >
  <div :class="['flex', 'justify-between', deviceDetection() && 'flex-wrap']">
    <tree
      v-if="showDeptTree"
      ref="treeRef"
      :class="['mr-2', deviceDetection() ? 'w-full' : 'min-w-50']"
      :treeData="treeData"
      :treeLoading="treeLoading"
      @tree-select="onTreeSelect"
    />
    <div
      :class="[
        deviceDetection() || !showDeptTree
          ? ['w-full', showDeptTree ? 'mt-2' : '']
          : 'w-[calc(100%-200px)]'
      ]"
    >
      <el-form
        ref="formRef"
        :inline="true"
        :model="form"
        class="search-form bg-bg_color w-full"
      >
        <el-form-item label="登录账号：" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入登录账号"
            clearable
            class="search-field"
          />
        </el-form-item>
        <el-form-item label="用户昵称：" prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入用户昵称"
            clearable
            class="search-field"
          />
        </el-form-item>
        <el-form-item label="手机号码：" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号码"
            clearable
            class="search-field"
          />
        </el-form-item>
        <el-form-item label="状态：" prop="status">
          <el-select
            v-model="form.status"
            placeholder="请选择"
            clearable
            class="search-field"
          >
            <el-option label="已启用" :value="true" />
            <el-option label="已停用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            :icon="useRenderIcon(Filter)"
            @click="showDeptTree = !showDeptTree"
          >
            {{ showDeptTree ? "收起部门筛选" : "展开部门筛选" }}
          </el-button>
        </el-form-item>
        <el-form-item class="search-form-actions">
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
        title="平台账号台账"
        :columns="columns"
        @refresh="onSearch"
      >
        <template #buttons>
          <el-button
            type="primary"
            :icon="useRenderIcon(AddFill)"
            @click="openDialog()"
          >
            新增账号
          </el-button>
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
                <el-button type="danger" text class="mr-1!">
                  批量删除
                </el-button>
              </template>
            </el-popconfirm>
          </div>
          <pure-table
            ref="tableRef"
            row-key="id"
            adaptive
            :adaptiveConfig="{ offsetBottom: 108 }"
            align-whole="center"
            table-layout="auto"
            :loading="loading"
            :size="size"
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
              <el-popconfirm
                :title="`是否确认删除用户编号为${row.id}的这条数据`"
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
              <el-dropdown>
                <el-button
                  class="ml-3! mt-0.5!"
                  link
                  type="primary"
                  :size="size"
                  :icon="useRenderIcon(More)"
                />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>
                      <el-button
                        :class="buttonClass"
                        link
                        type="primary"
                        :size="size"
                        :icon="useRenderIcon(Upload)"
                        @click="handleUpload(row)"
                      >
                        上传头像
                      </el-button>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-button
                        :class="buttonClass"
                        link
                        type="primary"
                        :size="size"
                        :icon="useRenderIcon(Password)"
                        @click="handleReset(row)"
                      >
                        重置密码
                      </el-button>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-button
                        :class="buttonClass"
                        link
                        type="primary"
                        :size="size"
                        :icon="useRenderIcon(Role)"
                        @click="handleRole(row)"
                      >
                        分配角色
                      </el-button>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </pure-table>
        </template>
      </PureTableBar>
    </div>
  </div>
  </AdminModuleShell>
</template>

<style lang="scss" scoped>
:deep(.el-dropdown-menu__item i) {
  margin: 0;
}

:deep(.el-button:focus-visible) {
  outline: none;
}

.main-content {
  margin: 24px 24px 0 !important;
}

.search-form {
  padding: 12px 24px 0;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 12px 16px;

  :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 0;
  }

  :deep(.el-form-item__content) {
    max-width: 100%;
  }
}

.search-field {
  width: 220px;
  max-width: 100%;
}

.search-form-actions {
  margin-left: auto;
}

@media (max-width: 1280px) {
  .search-form-actions {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .search-form {
    padding: 12px 16px 0;
  }

  .search-field {
    width: 100%;
  }
}
</style>
