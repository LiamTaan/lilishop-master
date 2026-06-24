<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { editPasswordApi } from "@/api/user";
import { message } from "@/utils/message";
import { isSuccessResult } from "@/utils/result";
import type { FormInstance, FormRules } from "element-plus";

defineOptions({
  name: "ChangePasswordDialog"
});

const props = defineProps<{
  modelValue: boolean;
}>();

const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  success: [];
}>();

const visible = computed({
  get: () => props.modelValue,
  set: value => emit("update:modelValue", value)
});

const formRef = ref<FormInstance>();
const loading = ref(false);
const form = reactive({
  password: "",
  newPassword: "",
  confirmPassword: ""
});

const rules = reactive<FormRules<typeof form>>({
  password: [{ required: true, message: "请输入当前密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "新密码至少 6 位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error("两次输入的新密码不一致"));
          return;
        }
        callback();
      },
      trigger: "blur"
    }
  ]
});

function resetForm() {
  form.password = "";
  form.newPassword = "";
  form.confirmPassword = "";
  formRef.value?.clearValidate();
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    const response = await editPasswordApi(form.password, form.newPassword);
    if (!isSuccessResult(response)) {
      message(response?.message || "修改密码失败", { type: "error" });
      return;
    }
    message("密码修改成功", { type: "success" });
    visible.value = false;
    emit("success");
  } catch (error: any) {
    message(error?.message || "修改密码失败", { type: "error" });
  } finally {
    loading.value = false;
  }
}

watch(
  () => props.modelValue,
  value => {
    if (!value) {
      resetForm();
    }
  }
);
</script>

<template>
  <el-dialog
    v-model="visible"
    title="修改密码"
    width="460px"
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="当前密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          placeholder="请输入当前密码"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          show-password
          placeholder="请输入新密码"
        />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确认修改
      </el-button>
    </template>
  </el-dialog>
</template>
