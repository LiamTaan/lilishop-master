<script setup lang="ts">
import { updateMineProfile, normalizeUserInfo } from "@/api/user";
import { message } from "@/utils/message";
import { isSuccessResult, unwrapResult } from "@/utils/result";
import { computed, onMounted, reactive, ref } from "vue";
import { type UserInfo, getMine } from "@/api/user";
import type { FormInstance, FormRules } from "element-plus";
import { deviceDetection } from "@pureadmin/utils";
import { useUserStoreHook } from "@/store/modules/user";

defineOptions({
  name: "Profile"
});

const userInfoFormRef = ref<FormInstance>();
const userStore = useUserStoreHook();

const userInfos = reactive({
  avatar: "",
  nickname: "",
  email: "",
  phone: "",
  description: ""
});

const rules = reactive<FormRules<UserInfo>>({
  nickname: [{ required: true, message: "昵称必填", trigger: "blur" }],
  email: [
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: ["blur", "change"]
    }
  ],
  phone: [
    {
      pattern: /^$|^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: ["blur", "change"]
    }
  ],
  description: [{ max: 255, message: "简介最多255个字符", trigger: "blur" }]
});

const saveButtonText = computed(() =>
  userInfos.phone ? "保存资料" : "保存资料（未绑定手机号）"
);

const syncMine = async () => {
  const response = await getMine();
  if (isSuccessResult(response)) {
    Object.assign(userInfos, normalizeUserInfo(unwrapResult(response)));
  }
};

// 更新信息
const onSubmit = async (formEl?: FormInstance) => {
  if (!formEl) return;
  await formEl.validate((valid, fields) => {
    if (valid) {
      const payload = {
        avatar: userInfos.avatar.trim(),
        nickName: userInfos.nickname.trim(),
        email: userInfos.email.trim(),
        mobile: userInfos.phone.trim(),
        description: userInfos.description.trim()
      };
      updateMineProfile({
        ...payload
      })
        .then(response => {
          if (!isSuccessResult(response)) {
            message(response?.message || "更新信息失败", { type: "error" });
            return;
          }
          Object.assign(userInfos, {
            avatar: payload.avatar,
            nickname: payload.nickName,
            email: payload.email,
            phone: payload.mobile,
            description: payload.description
          });
          userStore.UPDATE_PROFILE({
            avatar: payload.avatar,
            nickname: payload.nickName
          });
          message("资料更新成功", { type: "success" });
          void syncMine();
        })
        .catch(error => {
          message(error?.message || "更新信息失败", { type: "error" });
        });
    } else {
      console.log("error submit!", fields);
    }
  });
};

onMounted(syncMine);
</script>

<template>
  <div :class="['min-w-45', deviceDetection() ? 'max-w-full' : 'max-w-[70%]']">
    <h3 class="my-8!">个人信息</h3>
    <el-form
      ref="userInfoFormRef"
      label-position="top"
      :rules="rules"
      :model="userInfos"
    >
      <el-form-item label="头像">
        <div class="profile-avatar-field">
          <el-avatar :size="80" :src="userInfos.avatar" />
          <div class="profile-avatar-field__meta">
            <el-input
              v-model="userInfos.avatar"
              placeholder="请输入头像图片地址"
              clearable
            />
            <el-text type="info">
              当前支持直接保存头像地址，保存后右上角和账号设置会同步刷新
            </el-text>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="userInfos.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="userInfos.email" placeholder="请输入邮箱地址" clearable />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="userInfos.phone" placeholder="请输入手机号" clearable />
        <el-text type="info">
          修改手机号后，将影响手机号登录和短信找回密码；系统会校验管理员手机号唯一
        </el-text>
      </el-form-item>
      <el-form-item label="简介" prop="description">
        <el-input
          v-model="userInfos.description"
          placeholder="请输入个人简介"
          type="textarea"
          :autosize="{ minRows: 6, maxRows: 8 }"
          maxlength="255"
          show-word-limit
        />
      </el-form-item>
      <el-button type="primary" @click="onSubmit(userInfoFormRef)">
        {{ saveButtonText }}
      </el-button>
    </el-form>
  </div>
</template>

<style scoped>
.profile-avatar-field {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.profile-avatar-field__meta {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 8px;
}
</style>
