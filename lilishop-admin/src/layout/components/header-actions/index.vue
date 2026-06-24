<script setup lang="ts">
import { useNav } from "@/layout/hooks/useNav";
import LaySearch from "../lay-search/index.vue";
import LayNotice from "../lay-notice/index.vue";

import AccountSettingsIcon from "~icons/ri/user-settings-line";
import ArrowDownSLine from "~icons/ri/arrow-down-s-line";
import LogoutCircleRLine from "~icons/ri/logout-circle-r-line";
import User3Line from "~icons/ri/user-3-line";

const props = withDefaults(
  defineProps<{
    variant?: "accent" | "plain";
  }>(),
  {
    variant: "accent"
  }
);

const { logout, username, userAvatar, toAccountSettings } = useNav();
</script>

<template>
  <div
    class="header-action-group"
    :class="`header-action-group--${props.variant}`"
  >
    <LaySearch id="header-search" class="header-action-group__item" />
    <LayNotice id="header-notice" class="header-action-group__item" />

    <span class="header-action-group__divider" />

    <el-dropdown trigger="click">
      <span class="header-user navbar-bg-hover select-none">
        <span class="header-user__name">{{ username || "管理员" }}</span>
        <IconifyIconOffline
          :icon="ArrowDownSLine"
          class="header-user__arrow"
        />
        <el-avatar :size="40" :src="userAvatar" class="header-user__avatar">
          <IconifyIconOffline :icon="User3Line" />
        </el-avatar>
      </span>
      <template #dropdown>
        <el-dropdown-menu class="header-user-dropdown">
          <el-dropdown-item @click="toAccountSettings">
            <IconifyIconOffline :icon="AccountSettingsIcon" class="mr-2" />
            账号设置
          </el-dropdown-item>
          <el-dropdown-item @click="logout">
            <IconifyIconOffline :icon="LogoutCircleRLine" class="mr-2" />
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<style lang="scss" scoped>
.header-action-group {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.header-action-group__divider {
  display: inline-block;
  flex: 0 0 1px;
  width: 1px;
  height: 24px;
  margin: 0 4px 0 2px;
}

.header-user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 56px;
  padding: 8px 10px 8px 6px;
  cursor: pointer;
}

.header-user__name {
  font-size: 15px;
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
}

.header-user__arrow {
  font-size: 15px;
}

.header-user__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  font-size: 20px;
  border-radius: 999px;
}

.header-user-dropdown {
  min-width: 132px;

  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
  }
}

.header-action-group--accent {
  color: #fff;

  .header-action-group__divider {
    background: rgb(255 255 255 / 36%);
  }

  .header-user {
    color: #fff;
  }

  .header-user__avatar {
    color: #ff7a1a;
    background: #fff;
    box-shadow: 0 10px 24px rgb(108 56 10 / 16%);
  }

  :deep(.search-container),
  :deep(.dropdown-badge) {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    font-size: 18px;
    color: #fff;
    background: rgb(255 255 255 / 16%);
    border: 1px solid rgb(255 255 255 / 18%);
    border-radius: 999px;
    transition:
      background-color 0.2s ease,
      border-color 0.2s ease,
      transform 0.2s ease;
  }

  :deep(.search-container:hover),
  :deep(.dropdown-badge:hover),
  .header-user:hover {
    background: rgb(255 255 255 / 22%);
  }

  :deep(.search-container .iconify),
  :deep(.dropdown-badge .iconify) {
    font-size: 18px;
  }
}

.header-action-group--plain {
  color: var(--pure-theme-sub-menu-active-text);

  .header-action-group__divider {
    background: rgb(255 122 26 / 20%);
  }

  .header-user {
    height: 48px;
    color: var(--pure-theme-sub-menu-active-text);
    padding-right: 6px;
  }

  .header-user__avatar {
    color: #fff;
    background: linear-gradient(135deg, #ff7a1a 0%, #ff962f 100%);
  }

  :deep(.search-container),
  :deep(.dropdown-badge) {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    font-size: 16px;
    color: var(--pure-theme-sub-menu-active-text);
    border-radius: 999px;
    transition:
      background-color 0.2s ease,
      color 0.2s ease;
  }

  :deep(.search-container:hover),
  :deep(.dropdown-badge:hover),
  .header-user:hover {
    background: var(--pure-theme-menu-hover);
  }
}
</style>
