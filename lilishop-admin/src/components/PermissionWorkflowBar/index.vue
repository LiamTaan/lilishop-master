<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";

type PermissionStepKey =
  | "platform-menu"
  | "platform-role"
  | "platform-dept"
  | "platform-user"
  | "store-menu";

const props = defineProps<{
  current: PermissionStepKey;
}>();

const router = useRouter();

const platformSteps = [
  {
    key: "platform-menu" as const,
    title: "1. 菜单权限",
    description: "先定义平台菜单节点、权限码和入口资源。",
    path: "/merchant-grant/menu-permission"
  },
  {
    key: "platform-role" as const,
    title: "2. 角色权限",
    description: "再把平台菜单权限绑定到具体角色。",
    path: "/merchant-grant/role-permission"
  },
  {
    key: "platform-dept" as const,
    title: "3. 组织架构",
    description: "维护部门层级，为账号归属和筛选提供组织维度。",
    path: "/merchant-grant/department-manage"
  },
  {
    key: "platform-user" as const,
    title: "4. 后台账号",
    description: "最后给具体后台账号分配部门、角色和头像等信息。",
    path: "/merchant-grant/backend-account"
  }
];

const storeSteps = [
  {
    key: "store-menu" as const,
    title: "店铺菜单资源",
    description: "仅维护店铺端菜单资源，不承载平台账号角色授权链路。",
    path: "/merchant-grant/store-menu"
  }
];

const currentStep = computed(() => {
  return [...platformSteps, ...storeSteps].find(item => item.key === props.current);
});

function go(path: string) {
  if (router.currentRoute.value.path === path) return;
  router.push(path);
}
</script>

<template>
  <section class="permission-workflow-bar">
    <div class="permission-workflow-bar__header">
      <div>
        <h3>权限配置流程</h3>
        <p>
          平台侧按“菜单资源 -> 角色绑定 -> 组织归属 -> 账号分配”维护；
          店铺侧仅保留菜单资源维护，不再和平台权限链路混讲。
        </p>
      </div>
      <div class="permission-workflow-bar__current">
        当前页：{{ currentStep?.title || "-" }}
      </div>
    </div>

    <div class="permission-workflow-bar__section">
      <div class="permission-workflow-bar__section-title">平台侧权限链路</div>
      <div class="permission-workflow-bar__grid">
        <button
          v-for="item in platformSteps"
          :key="item.key"
          type="button"
          :class="[
            'permission-workflow-bar__card',
            item.key === current ? 'is-active' : ''
          ]"
          @click="go(item.path)"
        >
          <strong>{{ item.title }}</strong>
          <span>{{ item.description }}</span>
        </button>
      </div>
    </div>

    <div class="permission-workflow-bar__section">
      <div class="permission-workflow-bar__section-title">店铺侧资源维护</div>
      <div class="permission-workflow-bar__grid permission-workflow-bar__grid--single">
        <button
          v-for="item in storeSteps"
          :key="item.key"
          type="button"
          :class="[
            'permission-workflow-bar__card',
            item.key === current ? 'is-active' : ''
          ]"
          @click="go(item.path)"
        >
          <strong>{{ item.title }}</strong>
          <span>{{ item.description }}</span>
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.permission-workflow-bar {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px 20px;
  background: linear-gradient(180deg, #fffaf5 0%, #fff 100%);
  border: 1px solid #f4dfcb;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgb(21 26 38 / 4%);
}

.permission-workflow-bar__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.permission-workflow-bar__header h3 {
  margin: 0;
  color: #2f3440;
  font-size: 18px;
  font-weight: 700;
}

.permission-workflow-bar__header p {
  margin: 6px 0 0;
  color: #6b7280;
  line-height: 1.6;
}

.permission-workflow-bar__current {
  flex-shrink: 0;
  padding: 8px 12px;
  color: #ff7a1a;
  background: #fff1e6;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.permission-workflow-bar__section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.permission-workflow-bar__section-title {
  color: #8a5a31;
  font-size: 13px;
  font-weight: 700;
}

.permission-workflow-bar__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.permission-workflow-bar__grid--single {
  grid-template-columns: minmax(0, 1fr);
}

.permission-workflow-bar__card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
  padding: 14px 16px;
  text-align: left;
  background: #fff;
  border: 1px solid #ebedf2;
  border-radius: 14px;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.permission-workflow-bar__card:hover {
  border-color: #ffb57d;
  box-shadow: 0 8px 24px rgb(255 122 26 / 10%);
  transform: translateY(-1px);
}

.permission-workflow-bar__card strong {
  color: #2f3440;
  font-size: 15px;
}

.permission-workflow-bar__card span {
  color: #6b7280;
  line-height: 1.6;
  font-size: 13px;
}

.permission-workflow-bar__card.is-active {
  background: #fff6ef;
  border-color: #ff7a1a;
  box-shadow: 0 8px 24px rgb(255 122 26 / 14%);
}

.permission-workflow-bar__card.is-active strong {
  color: #ff7a1a;
}

@media (width <= 1200px) {
  .permission-workflow-bar__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (width <= 768px) {
  .permission-workflow-bar {
    padding: 16px;
  }

  .permission-workflow-bar__header {
    flex-direction: column;
  }

  .permission-workflow-bar__grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
