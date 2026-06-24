<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { ref, reactive } from "vue";
import Motion from "../utils/motion";
import { message } from "@/utils/message";
import { updateRules } from "../utils/rule";
import type { FormInstance } from "element-plus";
import { useVerifyCode } from "../utils/verifyCode";
import { $t, transformI18n } from "@/plugins/i18n";
import { resetPasswordApi, sendResetSmsCode, verifyResetSmsCode } from "@/api/user";
import { useUserStoreHook } from "@/store/modules/user";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import Lock from "~icons/ri/lock-fill";
import Iphone from "~icons/ep/iphone";
import Keyhole from "~icons/ri/shield-keyhole-line";

const { t } = useI18n();
const loading = ref(false);
const createUuid = () =>
  globalThis.crypto?.randomUUID?.() ??
  `${Date.now()}-${Math.random().toString(16).slice(2)}`;
const smsUuid = ref(createUuid());
const ruleForm = reactive({
  phone: "",
  verifyCode: "",
  password: "",
  repeatPassword: ""
});
const ruleFormRef = ref<FormInstance>();
const { isDisabled, text, start, end } = useVerifyCode();
const repeatPasswordRule = [
  {
    validator: (rule, value, callback) => {
      if (value === "") {
        callback(new Error(transformI18n($t("login.purePassWordSureReg"))));
      } else if (ruleForm.password !== value) {
        callback(
          new Error(transformI18n($t("login.purePassWordDifferentReg")))
        );
      } else {
        callback();
      }
    },
    trigger: "blur"
  }
];

const onUpdate = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  try {
    await formEl.validate();
  } catch (_error) {
    return;
  }
  loading.value = true;
  verifyResetSmsCode(ruleForm.phone, ruleForm.verifyCode, smsUuid.value)
    .then(() =>
      resetPasswordApi({
        mobile: ruleForm.phone,
        code: ruleForm.verifyCode,
        password: ruleForm.password,
        uuid: smsUuid.value
      })
    )
    .then(() => {
      message(transformI18n($t("login.purePassWordUpdateReg")), {
        type: "success"
      });
      end();
      useUserStoreHook().SET_CURRENTPAGE(0);
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
    await sendResetSmsCode(ruleForm.phone, smsUuid.value);
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
  <el-form
    ref="ruleFormRef"
    :model="ruleForm"
    :rules="updateRules"
    size="large"
  >
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
      <el-form-item prop="password">
        <el-input
          v-model="ruleForm.password"
          clearable
          show-password
          :placeholder="t('login.purePassword')"
          :prefix-icon="useRenderIcon(Lock)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="200">
      <el-form-item :rules="repeatPasswordRule" prop="repeatPassword">
        <el-input
          v-model="ruleForm.repeatPassword"
          clearable
          show-password
          :placeholder="t('login.pureSure')"
          :prefix-icon="useRenderIcon(Lock)"
        />
      </el-form-item>
    </Motion>

    <Motion :delay="250">
      <el-form-item>
        <div class="login-form-actions w-full">
          <el-button
            class="form-action-button"
            size="default"
            type="primary"
            :loading="loading"
            @click="onUpdate(ruleFormRef)"
          >
            {{ t("login.pureDefinite") }}
          </el-button>
        </div>
      </el-form-item>
    </Motion>

    <Motion :delay="300">
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
