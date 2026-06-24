<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import Motion from "../utils/motion";
import { message } from "@/utils/message";
import { phoneRules } from "../utils/rule";
import type { FormInstance } from "element-plus";
import { $t, transformI18n } from "@/plugins/i18n";
import { useVerifyCode } from "../utils/verifyCode";
import { useUserStoreHook } from "@/store/modules/user";
import { sendLoginSmsCode } from "@/api/user";
import { initRouter, getTopMenu } from "@/router/utils";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import Iphone from "~icons/ep/iphone";
import Keyhole from "~icons/ri/shield-keyhole-line";

const { t } = useI18n();
const router = useRouter();
const loading = ref(false);
const createUuid = () =>
  globalThis.crypto?.randomUUID?.() ??
  `${Date.now()}-${Math.random().toString(16).slice(2)}`;
const smsUuid = ref(createUuid());
const ruleForm = reactive({
  phone: "",
  verifyCode: ""
});
const ruleFormRef = ref<FormInstance>();
const { isDisabled, text, start, end } = useVerifyCode();

const onLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  try {
    await formEl.validate();
  } catch (_error) {
    return;
  }
  loading.value = true;
  useUserStoreHook()
    .loginByMobile({
      mobile: ruleForm.phone,
      code: ruleForm.verifyCode,
      uuid: smsUuid.value
    })
    .then(async () => {
      await initRouter();
      router.push(getTopMenu(true).path).then(() => {
        message(transformI18n($t("login.pureLoginSuccess")), {
          type: "success"
        });
      });
    })
    .catch(() => {
      message(transformI18n($t("login.pureLoginFail")), {
        type: "error"
      });
    })
    .finally(() => {
      loading.value = false;
    });
};

const onSendSmsCode = async () => {
  try {
    await ruleFormRef.value?.validateField("phone");
    smsUuid.value = createUuid();
    await sendLoginSmsCode(ruleForm.phone, smsUuid.value);
    await start(ruleFormRef.value, "phone");
    message(transformI18n($t("login.pureSendVerifyCode")), {
      type: "success"
    });
  } catch (_error) {
    end();
  }
};

function onBack() {
  end();
  useUserStoreHook().SET_CURRENTPAGE(0);
}
</script>

<template>
  <el-form ref="ruleFormRef" :model="ruleForm" :rules="phoneRules" size="large">
    <Motion>
      <el-form-item prop="phone">
        <el-input
          v-model="ruleForm.phone"
          clearable
          :placeholder="t('login.purePhone')"
          :prefix-icon="useRenderIcon(Iphone)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="100">
      <el-form-item prop="verifyCode">
        <div class="w-full flex justify-between">
          <el-input
            v-model="ruleForm.verifyCode"
            clearable
            :placeholder="t('login.pureSmsVerifyCode')"
            :prefix-icon="useRenderIcon(Keyhole)"
          />
          <el-button
            :disabled="isDisabled"
            class="ml-2!"
            @click="onSendSmsCode"
          >
            {{
              text.length > 0
                ? text + t("login.pureInfo")
                : t("login.pureGetVerifyCode")
            }}
          </el-button>
        </div>
      </el-form-item>
    </Motion>

    <Motion :delay="150">
      <el-form-item>
        <div class="login-form-actions w-full">
          <el-button
            class="form-action-button"
            size="default"
            type="primary"
            :loading="loading"
            @click="onLogin(ruleFormRef)"
          >
            {{ t("login.pureLogin") }}
          </el-button>
        </div>
      </el-form-item>
    </Motion>

    <Motion :delay="200">
      <el-form-item>
        <div class="login-form-actions w-full">
          <el-button
            class="form-action-button form-switch-button"
            size="default"
            @click="onBack"
          >
            {{ t("login.pureBack") }}
          </el-button>
        </div>
      </el-form-item>
    </Motion>
  </el-form>
</template>
