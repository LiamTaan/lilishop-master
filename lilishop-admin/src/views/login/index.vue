<script setup lang="ts">
import { useI18n } from "vue-i18n";
import Motion from "./utils/motion";
import { useRouter } from "vue-router";
import { message } from "@/utils/message";
import { loginRules } from "./utils/rule";
import TypeIt from "@/components/ReTypeit";
import { debounce } from "@pureadmin/utils";
import { useNav } from "@/layout/hooks/useNav";
import { useEventListener } from "@vueuse/core";
import type { FormInstance } from "element-plus";
import { $t, transformI18n } from "@/plugins/i18n";
import { useLayout } from "@/layout/hooks/useLayout";
import LoginPhone from "./components/LoginPhone.vue";
import LoginUpdate from "./components/LoginUpdate.vue";
import { useUserStoreHook } from "@/store/modules/user";
import { initRouter, getTopMenu } from "@/router/utils";
import { bg, logo, illustration } from "./utils/static";
import { ref, toRaw, reactive, watch, computed } from "vue";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { useTranslationLang } from "@/layout/hooks/useTranslationLang";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";

import dayIcon from "@/assets/svg/day.svg?component";
import darkIcon from "@/assets/svg/dark.svg?component";
import globalization from "@/assets/svg/globalization.svg?component";
import Lock from "~icons/ri/lock-fill";
import Check from "~icons/ep/check";
import User from "~icons/ri/user-3-fill";
import Info from "~icons/ri/information-line";

defineOptions({
  name: "Login"
});

const loginDay = ref(7);
const router = useRouter();
const loading = ref(false);
const checked = ref(false);
const disabled = ref(false);
const ruleFormRef = ref<FormInstance>();
const currentPage = computed(() => {
  return useUserStoreHook().currentPage;
});

const { t } = useI18n();
const { initStorage } = useLayout();
initStorage();
const { dataTheme, themeMode, dataThemeChange } = useDataThemeChange();
dataThemeChange(themeMode.value);
const { title, getDropdownItemStyle, getDropdownItemClass } = useNav();
const { locale, translationCh, translationEn } = useTranslationLang();

const ruleForm = reactive({
  username: "admin",
  password: "admin123"
});

const onLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  try {
    await formEl.validate();
  } catch (_error) {
    return;
  }
  loading.value = true;
  useUserStoreHook()
    .loginByUsername({
      username: ruleForm.username,
      password: ruleForm.password
    })
    .then(async () => {
      // 获取后端路由
      await initRouter();
      disabled.value = true;
      router.push(getTopMenu(true).path).then(() => {
        message(t("login.pureLoginSuccess"), { type: "success" });
      });
    })
    .catch(_err => {
      message(t("login.pureLoginFail"), { type: "error" });
    })
    .finally(() => {
      disabled.value = false;
      loading.value = false;
    });
};

const immediateDebounce: any = debounce(
  formRef => onLogin(formRef),
  1000,
  true
);

useEventListener(document, "keydown", ({ code }) => {
  if (
    ["Enter", "NumpadEnter"].includes(code) &&
    !disabled.value &&
    !loading.value
  )
    immediateDebounce(ruleFormRef.value);
});

watch(checked, bool => {
  useUserStoreHook().SET_ISREMEMBERED(bool);
});
watch(loginDay, value => {
  useUserStoreHook().SET_LOGINDAY(value);
});
</script>

<template>
  <div class="select-none">
    <img :src="bg" class="wave" />
    <div class="flex-c absolute right-5 top-3">
      <!-- 主题 -->
      <el-switch
        v-model="dataTheme"
        inline-prompt
        :active-icon="dayIcon"
        :inactive-icon="darkIcon"
        @change="dataThemeChange"
      />
      <!-- 国际化 -->
      <el-dropdown trigger="click">
        <globalization
          class="hover:text-primary hover:bg-transparent! size-5 ml-1.5 cursor-pointer outline-hidden duration-300"
        />
        <template #dropdown>
          <el-dropdown-menu class="translation">
            <el-dropdown-item
              :style="getDropdownItemStyle(locale, 'zh')"
              :class="['dark:text-white!', getDropdownItemClass(locale, 'zh')]"
              @click="translationCh"
            >
              <IconifyIconOffline
                v-show="locale === 'zh'"
                class="check-zh"
                :icon="Check"
              />
              简体中文
            </el-dropdown-item>
            <el-dropdown-item
              :style="getDropdownItemStyle(locale, 'en')"
              :class="['dark:text-white!', getDropdownItemClass(locale, 'en')]"
              @click="translationEn"
            >
              <span v-show="locale === 'en'" class="check-en">
                <IconifyIconOffline :icon="Check" />
              </span>
              English
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="login-container">
      <div class="img">
        <component :is="toRaw(illustration)" />
      </div>
      <div class="login-box">
        <div class="login-form">
          <Motion>
            <div class="login-brand">
              <img :src="logo" alt="PF 批发商城" class="brand-mark" />
              <div class="brand-copy">
                <p class="brand-kicker">PF 批发商城管理端</p>
                <h2 class="outline-hidden">
                  <TypeIt
                    :options="{ strings: [title], cursor: false, speed: 100 }"
                  />
                </h2>
              </div>
            </div>
          </Motion>

          <el-form
            v-if="currentPage === 0"
            ref="ruleFormRef"
            :model="ruleForm"
            :rules="loginRules"
            size="large"
          >
            <Motion :delay="100">
              <el-form-item
                :rules="[
                  {
                    required: true,
                    message: transformI18n($t('login.pureUsernameReg')),
                    trigger: 'blur'
                  }
                ]"
                prop="username"
              >
                <el-input
                  v-model="ruleForm.username"
                  clearable
                  :placeholder="t('login.pureUsername')"
                  :prefix-icon="useRenderIcon(User)"
                />
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
              <el-form-item>
                <div class="w-full h-5 flex-bc">
                  <el-checkbox v-model="checked">
                    <span class="flex">
                      <select
                        v-model="loginDay"
                        :style="{
                          width: loginDay < 10 ? '10px' : '16px',
                          outline: 'none',
                          background: 'none',
                          appearance: 'none',
                          border: 'none'
                        }"
                      >
                        <option value="1">1</option>
                        <option value="7">7</option>
                        <option value="30">30</option>
                      </select>
                      {{ t("login.pureRemember") }}
                      <IconifyIconOffline
                        v-tippy="{
                          content: t('login.pureRememberInfo'),
                          placement: 'top'
                        }"
                        :icon="Info"
                        class="ml-1"
                      />
                    </span>
                  </el-checkbox>
                  <el-button
                    link
                    class="forget-link"
                    @click="useUserStoreHook().SET_CURRENTPAGE(4)"
                  >
                    {{ t("login.pureForget") }}
                  </el-button>
                </div>
                <div class="login-form-actions w-full mt-4!">
                  <el-button
                    class="form-action-button"
                    size="default"
                    type="primary"
                    :loading="loading"
                    :disabled="disabled"
                    @click="onLogin(ruleFormRef)"
                  >
                    {{ t("login.pureLogin") }}
                  </el-button>
                </div>
              </el-form-item>
            </Motion>

            <Motion :delay="250">
              <el-form-item>
                <div class="login-form-actions w-full">
                  <el-button
                    class="form-action-button form-switch-button mt-4!"
                    size="default"
                    @click="useUserStoreHook().SET_CURRENTPAGE(1)"
                  >
                    {{ t("login.purePhoneLogin") }}
                  </el-button>
                </div>
              </el-form-item>
            </Motion>
          </el-form>
          <!-- 手机号登录 -->
          <LoginPhone v-if="currentPage === 1" />
          <!-- 忘记密码 -->
          <LoginUpdate v-if="currentPage === 4" />
        </div>
      </div>
    </div>
    <div class="login-footer">
      Copyright © 2026-present {{ title }}管理端
    </div>
  </div>
</template>

<style>
@import url("@/style/login.css");
</style>

<style lang="scss" scoped>
:deep(.el-input-group__append, .el-input-group__prepend) {
  padding: 0;
}

.translation {
  :deep(.el-dropdown-menu__item) {
    padding: 5px 40px;
  }

  .check-zh {
    position: absolute;
    left: 20px;
  }

  .check-en {
    position: absolute;
    left: 20px;
  }
}

.login-brand {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 18px;
  margin-bottom: 24px;
  text-align: left;
}

.brand-mark {
  display: inline-block;
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  border-radius: 18px;
  box-shadow: 0 18px 34px rgb(255 111 24 / 24%);
  object-fit: contain;
}

.brand-copy {
  min-width: 0;
}

.brand-kicker {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  color: #ff7a1a;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.brand-copy h2 {
  margin: 0;
}

.forget-link {
  padding: 0 !important;
  color: #ff7a1a !important;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.forget-link:hover,
.forget-link:focus-visible {
  color: #ff8f32 !important;
  background: transparent !important;
}

.login-footer {
  position: absolute;
  right: 0;
  bottom: 20px;
  left: 0;
  display: flex;
  justify-content: center;
  font-size: 13px;
  color: rgb(80 63 51 / 72%);
}
</style>
