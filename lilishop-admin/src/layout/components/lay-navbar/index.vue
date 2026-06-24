<script setup lang="ts">
import { useNav } from "@/layout/hooks/useNav";
import HeaderActions from "../header-actions/index.vue";
import LayNavMix from "../lay-sidebar/NavMix.vue";
import LaySidebarTopCollapse from "../lay-sidebar/components/SidebarTopCollapse.vue";

const {
  layout,
  device,
  pureApp,
  toggleSideBar,
} = useNav();
</script>

<template>
  <div class="navbar">
    <LaySidebarTopCollapse
      v-if="device === 'mobile'"
      class="hamburger-container"
      :is-active="pureApp.sidebar.opened"
      @toggleClick="toggleSideBar"
    />

    <LayNavMix v-if="layout === 'mix'" />

    <div v-if="layout === 'vertical'" class="vertical-header-right">
      <HeaderActions variant="accent" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.navbar {
  width: 100%;
  height: 56px;
  overflow: hidden;
  background: linear-gradient(90deg, #ff7a1a 0%, #ff891f 100%);
  box-shadow: 0 10px 24px rgb(255 122 26 / 18%);

  .hamburger-container {
    float: left;
    height: 100%;
    line-height: 56px;
    cursor: pointer;
  }

  .vertical-header-right {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    min-width: 360px;
    height: 56px;
    padding-right: 16px;
    color: #fff;
  }
}
</style>
