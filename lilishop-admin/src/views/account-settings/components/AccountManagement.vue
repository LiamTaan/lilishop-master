<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getMine, normalizeUserInfo } from "@/api/user";
import { isSuccessResult, unwrapResult } from "@/utils/result";
import { deviceDetection } from "@pureadmin/utils";
import ChangePasswordDialog from "@/components/ChangePasswordDialog/index.vue";

defineOptions({
  name: "AccountManagement"
});

const passwordDialogVisible = ref(false);
const userInfo = ref({
  username: "",
  phone: "",
  email: ""
});

function maskPhone(phone: string) {
  if (!phone || phone.length < 7) return phone || "未绑定";
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`;
}

function maskEmail(email: string) {
  if (!email.includes("@")) return email || "未设置";
  const [name, domain] = email.split("@");
  const safeName =
    name.length <= 2 ? `${name.slice(0, 1)}*` : `${name.slice(0, 2)}***`;
  return `${safeName}@${domain}`;
}

const list = computed(() => [
  {
    title: "登录账号",
    illustrate: userInfo.value.username || "未获取到账号信息"
  },
  {
    title: "账户密码",
    illustrate: "为保障账号安全，建议定期修改登录密码",
    button: "修改密码",
    action: "password"
  },
  {
    title: "绑定手机",
    illustrate: userInfo.value.phone
      ? `已绑定手机：${maskPhone(userInfo.value.phone)}`
      : "暂未绑定手机"
  },
  {
    title: "绑定邮箱",
    illustrate: userInfo.value.email
      ? `已绑定邮箱：${maskEmail(userInfo.value.email)}`
      : "暂未设置邮箱"
  }
]);

function onClick(action?: string) {
  if (action === "password") {
    passwordDialogVisible.value = true;
  }
}

onMounted(async () => {
  const response = await getMine();
  if (isSuccessResult(response)) {
    userInfo.value = normalizeUserInfo(unwrapResult(response));
  }
});
</script>

<template>
  <div :class="['min-w-45', deviceDetection() ? 'max-w-full' : 'max-w-[70%]']">
    <h3 class="my-8!">账户管理</h3>
    <div v-for="(item, index) in list" :key="index">
      <div class="flex items-center">
        <div class="flex-1">
          <p>{{ item.title }}</p>
          <el-text class="mx-1" type="info">{{ item.illustrate }}</el-text>
        </div>
        <el-button
          v-if="item.button"
          type="primary"
          text
          @click="onClick(item.action)"
        >
          {{ item.button }}
        </el-button>
      </div>
      <el-divider />
    </div>
    <ChangePasswordDialog v-model="passwordDialogVisible" />
  </div>
</template>

<style lang="scss" scoped>
.el-divider--horizontal {
  border-top: 0.1px var(--el-border-color) var(--el-border-style);
}
</style>
