<script setup lang="ts">
import { hasPerms } from "@/utils/auth";
import { useUserStoreHook } from "@/store/modules/user";

const { permissions } = useUserStoreHook();

defineOptions({
  name: "PermissionButtonLogin"
});
</script>

<template>
  <div>
    <p class="mb-2!">
      当前按钮权限码列表：{{ permissions }}。该页用于核对登录后落到前端的按钮级权限集合是否与角色授权结果一致。
    </p>
    <p v-show="permissions?.[0] === '*:*:*'" class="mb-2!">
      `*:*:*` 代表当前账号拥有全部按钮级别权限。
    </p>

    <el-card shadow="never" class="mb-2">
      <template #header>
        <div class="card-header">组件封装鉴权</div>
        <span class="mt-2 block text-sm text-[#8c9098]">
          适用于审核、分配、上下架等业务动作由组件直接包裹的场景。
        </span>
      </template>
      <el-space wrap>
        <Perms value="permission:btn:add">
          <el-button plain type="warning">
            拥有 `permission:btn:add` 时可见
          </el-button>
        </Perms>
        <Perms :value="['permission:btn:edit']">
          <el-button plain type="primary">
            拥有 `permission:btn:edit` 时可见
          </el-button>
        </Perms>
        <Perms
          :value="[
            'permission:btn:add',
            'permission:btn:edit',
            'permission:btn:delete'
          ]"
        >
          <el-button plain type="danger">
            拥有新增/修改/删除权限集时可见
          </el-button>
        </Perms>
      </el-space>
    </el-card>

    <el-card shadow="never" class="mb-2">
      <template #header>
        <div class="card-header">函数逻辑鉴权</div>
        <span class="mt-2 block text-sm text-[#8c9098]">
          适用于需结合记录状态、组织范围或数据归属再渲染动作按钮的场景。
        </span>
      </template>
      <el-space wrap>
        <el-button v-if="hasPerms('permission:btn:add')" plain type="warning">
          拥有 `permission:btn:add` 时可见
        </el-button>
        <el-button
          v-if="hasPerms(['permission:btn:edit'])"
          plain
          type="primary"
        >
          拥有 `permission:btn:edit` 时可见
        </el-button>
        <el-button
          v-if="
            hasPerms([
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
          适用于固定工具栏、详情页页头等结构稳定、仅需做显隐控制的静态按钮权限。
        </span>
      </template>
      <el-space wrap>
        <el-button v-perms="'permission:btn:add'" plain type="warning">
          拥有 `permission:btn:add` 时可见
        </el-button>
        <el-button v-perms="['permission:btn:edit']" plain type="primary">
          拥有 `permission:btn:edit` 时可见
        </el-button>
        <el-button
          v-perms="[
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
</template>
