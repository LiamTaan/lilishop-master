<script setup lang="ts">
import { hasAuth, getAuths } from "@/router/utils";
import AdminModuleShell from "@/components/AdminModuleShell";

defineOptions({
  name: "PermissionButtonRouter"
});
</script>

<template>
  <AdminModuleShell
    title="按钮权限验证"
    description="承接接口权限码、按钮显隐规则与前端权限指令验证。"
    api-path="/permission/button"
    :tips="['组件鉴权', '函数鉴权', '指令鉴权', '权限码演示']"
    :summary-cards="[
      { label: '鉴权方式', value: '3 种', accent: 'orange', hint: '组件/函数/指令' },
      { label: '权限粒度', value: '按钮级', accent: 'blue', hint: '适配操作按钮显隐' },
      { label: '联动对象', value: '权限码', accent: 'green', hint: '对齐接口与菜单权限' },
      { label: '当前状态', value: '可用', accent: 'purple', hint: '已恢复到主菜单' }
    ]"
  >
  <div>
    <p class="mb-2!">
      当前生效的权限码列表：{{ getAuths() }}。该页用于核对批发商城管理端菜单权限、操作按钮显隐与接口权限码是否保持一致。
    </p>

    <el-card shadow="never" class="mb-2">
      <template #header>
        <div class="card-header">组件封装鉴权</div>
        <span class="mt-2 block text-sm text-[#8c9098]">
          适用于店铺审核、角色分配、商品治理等标准业务页，直接按权限码控制按钮显隐。
        </span>
      </template>
      <el-space wrap>
        <Auth value="permission:btn:add">
          <el-button plain type="warning">
            拥有 `permission:btn:add` 时可见
          </el-button>
        </Auth>
        <Auth :value="['permission:btn:edit']">
          <el-button plain type="primary">
            拥有 `permission:btn:edit` 时可见
          </el-button>
        </Auth>
        <Auth
          :value="[
            'permission:btn:add',
            'permission:btn:edit',
            'permission:btn:delete'
          ]"
        >
          <el-button plain type="danger">
            拥有新增/修改/删除权限集时可见
          </el-button>
        </Auth>
      </el-space>
    </el-card>

    <el-card shadow="never" class="mb-2">
      <template #header>
        <div class="card-header">函数逻辑鉴权</div>
        <span class="mt-2 block text-sm text-[#8c9098]">
          适用于批量操作、状态流转和组合条件判断，需要先计算再决定是否展示动作入口。
        </span>
      </template>
      <el-space wrap>
        <el-button v-if="hasAuth('permission:btn:add')" plain type="warning">
          拥有 `permission:btn:add` 时可见
        </el-button>
        <el-button v-if="hasAuth(['permission:btn:edit'])" plain type="primary">
          拥有 `permission:btn:edit` 时可见
        </el-button>
        <el-button
          v-if="
            hasAuth([
              'permission:btn:add',
              'permission:btn:edit',
              'permission:btn:delete'
            ])
          "
          plain
          type="danger"
        >
          拥有新增/修改/删除权限集时可见
        </el-button>
      </el-space>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          指令静态鉴权
        </div>
        <span class="mt-2 block text-sm text-[#8c9098]">
          适用于结构稳定的固定按钮区域；若权限集合会随上下文变化，优先使用组件或函数方式。
        </span>
      </template>
      <el-space wrap>
        <el-button v-auth="'permission:btn:add'" plain type="warning">
          拥有 `permission:btn:add` 时可见
        </el-button>
        <el-button v-auth="['permission:btn:edit']" plain type="primary">
          拥有 `permission:btn:edit` 时可见
        </el-button>
        <el-button
          v-auth="[
            'permission:btn:add',
            'permission:btn:edit',
            'permission:btn:delete'
          ]"
          plain
          type="danger"
        >
          拥有新增/修改/删除权限集时可见
        </el-button>
      </el-space>
    </el-card>
  </div>
  </AdminModuleShell>
</template>
