<script setup lang="ts">
import { message } from "@/utils/message";
import { initRouter } from "@/router/utils";
import { storageLocal } from "@pureadmin/utils";
import { type CSSProperties, ref, computed } from "vue";
import { useUserStoreHook } from "@/store/modules/user";
import { usePermissionStoreHook } from "@/store/modules/permission";
import AdminModuleShell from "@/components/AdminModuleShell";

defineOptions({
  name: "PermissionPage"
});

const elStyle = computed((): CSSProperties => {
  return {
    width: "85vw",
    justifyContent: "start"
  };
});

const username = ref(useUserStoreHook()?.username);

const options = [
  {
    value: "admin",
    label: "管理员角色"
  },
  {
    value: "common",
    label: "普通角色"
  }
];

function onChange() {
  useUserStoreHook()
    .loginByUsername({ username: username.value, password: "admin123" })
    .then(() => {
      storageLocal().removeItem("async-routes");
      usePermissionStoreHook().clearAllCachePage();
      initRouter();
    })
    .catch(err => {
      message(err, { type: "error" });
    });
}
</script>

<template>
  <AdminModuleShell
    title="页面权限验证"
    description="承接角色级页面访问控制、菜单差异验证与权限联动检查。"
    api-path="/permission/page"
    :tips="['角色切换验证', '页面可见性检查', '权限联动测试']"
    :summary-cards="[
      { label: '权限模型', value: '角色驱动', accent: 'orange', hint: '按角色返回菜单与路由' },
      { label: '验证方式', value: '切换角色', accent: 'blue', hint: '支持页面级验证' },
      { label: '联动范围', value: '菜单/路由', accent: 'green', hint: '与系统菜单配置配套' },
      { label: '当前状态', value: '可用', accent: 'purple', hint: '已恢复到主菜单' }
    ]"
  >
  <div>
    <p class="mb-2!">
      通过切换角色身份验证菜单可见范围与页面访问权限，确认平台角色、系统管理、监控中心和业务治理菜单的授权边界是否符合预期。
    </p>
    <el-card shadow="never" :style="elStyle">
      <template #header>
        <div class="card-header">
          <span>当前角色：{{ username }}</span>
        </div>
        <span class="mt-2 block text-sm text-[#8c9098]">
          切换后将刷新权限缓存，并重新计算左侧菜单、页面路由与按钮授权范围。
        </span>
      </template>
      <el-select v-model="username" class="w-40!" @change="onChange">
        <el-option
          v-for="item in options"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-card>
  </div>
  </AdminModuleShell>
</template>
