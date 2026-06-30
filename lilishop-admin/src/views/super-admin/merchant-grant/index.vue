<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { utils, writeFile } from "xlsx";
import AdminModuleShell from "@/components/AdminModuleShell";
import {
  createStoreMenu,
  deleteStoreMenu,
  getStoreMenuList,
  getStoreMenuTree,
  updateStoreMenu
} from "@/api/super-admin";
import { extractApiRecords } from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { isSuccessResult } from "@/utils/result";

defineOptions({
  name: "MerchantGrant"
});

type StoreMenuRow = {
  id: string;
  title: string;
  name: string;
  frontRoute: string;
  path: string;
  permission: string;
  parentId: string;
  level: number;
  sortOrder: number;
  children?: StoreMenuRow[];
};

function normalizeMenuTree(list: Record<string, any>[] = []): StoreMenuRow[] {
  return list.map(item => ({
    id: String(item.id ?? ""),
    title: item.title || "",
    name: item.name || "",
    frontRoute: item.frontRoute || "",
    path: item.path || "",
    permission: item.permission || "",
    parentId: String(item.parentId ?? "0"),
    level: Number(item.level ?? 0),
    sortOrder: Number(item.sortOrder ?? 0),
    children: normalizeMenuTree(item.children ?? [])
  }));
}

function findMenuById(list: StoreMenuRow[], id: string): StoreMenuRow | null {
  for (const item of list) {
    if (item.id === id) return item;
    const child = findMenuById(item.children ?? [], id);
    if (child) return child;
  }
  return null;
}

function getErrorMessage(error: any, fallback: string) {
  return error?.response?.data?.message || error?.message || fallback;
}

const list = ref<StoreMenuRow[]>([]);
const tree = ref<StoreMenuRow[]>([]);
const selectedRows = ref<StoreMenuRow[]>([]);
const loading = ref(false);
const createDialogVisible = ref(false);
const editDialogVisible = ref(false);
const createForm = ref({
  title: "",
  name: "",
  frontRoute: "",
  path: "",
  permission: "",
  parentId: "",
  sortOrder: 0
});
const editForm = ref({
  id: "",
  title: "",
  name: "",
  frontRoute: "",
  path: "",
  permission: "",
  parentId: "",
  sortOrder: 0
});
const treeProps = {
  value: "id",
  label: "title",
  children: "children"
};

const summaryCards = computed(() => [
  {
    label: "菜单节点",
    value: list.value.length,
    accent: "orange" as const,
    hint: "店铺菜单配置数量"
  },
  {
    label: "树形根节点",
    value: tree.value.length,
    accent: "blue" as const,
    hint: "店铺菜单树根节点数"
  },
  {
    label: "当前能力",
    value: "资源维护",
    accent: "green" as const,
    hint: "仅维护店铺菜单资源"
  },
  {
    label: "联调边界",
    value: "未混平台角色",
    accent: "purple" as const,
    hint: "不再错误复用平台角色授权"
  }
]);

const parentOptions = computed(() =>
  list.value.map(item => ({
    label: `${item.title} (${item.id})`,
    value: item.id
  }))
);

const selectedIds = computed(() =>
  selectedRows.value
    .map(item => String(item.id || "").trim())
    .filter(Boolean)
);

function handleSelectionChange(rows: StoreMenuRow[]) {
  selectedRows.value = rows;
}

function resolveMenuLevel(parentId: string) {
  if (!parentId || parentId === "0") return 0;
  const parent = findMenuById(tree.value, parentId);
  return parent ? Number(parent.level ?? 0) + 1 : 0;
}

async function loadMenus() {
  loading.value = true;
  try {
    const [listRes, treeRes] = await Promise.all([
      getStoreMenuList(),
      getStoreMenuTree()
    ]);
    list.value = normalizeMenuTree(extractApiRecords(listRes));
    tree.value = normalizeMenuTree(extractApiRecords(treeRes));
  } finally {
    loading.value = false;
  }
}

async function handleDelete(row: Record<string, any>) {
  await ElMessageBox.confirm(`确认删除菜单「${row.title || row.name}」吗？`, "删除确认", {
    type: "warning"
  });
  const response = await deleteStoreMenu([String(row.id)]);
  if (!isSuccessResult(response)) {
    throw new Error(response?.message || "菜单删除失败");
  }
  message("菜单删除成功", { type: "success" });
  await loadMenus();
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message("请先勾选需要删除的菜单", { type: "warning" });
    return;
  }
  await ElMessageBox.confirm(
    `确认删除已勾选的 ${selectedIds.value.length} 个菜单吗？`,
    "批量删除确认",
    { type: "warning" }
  );
  const response = await deleteStoreMenu(selectedIds.value);
  if (!isSuccessResult(response)) {
    throw new Error(response?.message || "菜单批量删除失败");
  }
  selectedRows.value = [];
  message("菜单批量删除成功", { type: "success" });
  await loadMenus();
}

function openEdit(row: Record<string, any>) {
  editForm.value = {
    id: String(row.id || ""),
    title: row.title || "",
    name: row.name || "",
    frontRoute: row.frontRoute || "",
    path: row.path || "",
    permission: row.permission || "",
    parentId: String(row.parentId || ""),
    sortOrder: Number(row.sortOrder ?? 0)
  };
  editDialogVisible.value = true;
}

async function handleCreateMenu() {
  const parentId = createForm.value.parentId || "0";
  const response = await createStoreMenu({
    title: createForm.value.title,
    name: createForm.value.name,
    frontRoute: createForm.value.frontRoute || undefined,
    path: createForm.value.path || undefined,
    permission: createForm.value.permission || undefined,
    parentId,
    sortOrder: createForm.value.sortOrder,
    level: resolveMenuLevel(parentId)
  });
  if (!isSuccessResult(response)) {
    throw new Error(response?.message || "菜单新增失败");
  }
  message("菜单新增成功", { type: "success" });
  createDialogVisible.value = false;
  createForm.value = {
    title: "",
    name: "",
    frontRoute: "",
    path: "",
    permission: "",
    parentId: "",
    sortOrder: 0
  };
  await loadMenus();
}

async function handleEditMenu() {
  const parentId = editForm.value.parentId || "0";
  const response = await updateStoreMenu(editForm.value.id, {
    title: editForm.value.title,
    name: editForm.value.name,
    frontRoute: editForm.value.frontRoute || undefined,
    path: editForm.value.path || undefined,
    permission: editForm.value.permission || undefined,
    parentId,
    sortOrder: editForm.value.sortOrder,
    level: resolveMenuLevel(parentId)
  });
  if (!isSuccessResult(response)) {
    throw new Error(response?.message || "菜单修改失败");
  }
  message("菜单修改成功", { type: "success" });
  editDialogVisible.value = false;
  await loadMenus();
}

async function runAction(action: () => Promise<void>, fallback: string) {
  try {
    await action();
  } catch (error: any) {
    message(getErrorMessage(error, fallback), { type: "error" });
  }
}

function exportMenus() {
  if (!list.value.length) {
    message("暂无可导出的店铺菜单资源", { type: "warning" });
    return;
  }
  const table = list.value.map(item => ({
    菜单名称: item.title,
    路由名称: item.name,
    前端路由: item.frontRoute,
    菜单路径: item.path,
    权限接口: item.permission,
    父级菜单ID: item.parentId,
    层级: item.level,
    排序值: item.sortOrder
  }));
  const worksheet = utils.json_to_sheet(table);
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, worksheet, "店铺菜单资源");
  writeFile(workbook, "店铺菜单资源.xlsx");
  message("店铺菜单资源导出成功", { type: "success" });
}

onMounted(() => {
  loadMenus();
});
</script>

<template>
  <AdminModuleShell
    title="店铺菜单资源"
    description="仅承接店铺端菜单资源维护，避免与平台账号角色权限链路混用。"
    api-path="/manager/permission/storeMenu"
    :tips="['店铺菜单树', '资源维护', '边界独立']"
    :summary-cards="summaryCards"
  >
    <el-alert
      type="info"
      :closable="false"
      title="当前管理端只接入店铺菜单资源接口；店铺角色与店铺角色菜单授权仍属于店铺端独立体系，不纳入平台后台账号权限配置链路。"
    />
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <span>菜单节点台账</span>
              <div class="flex items-center gap-2">
                <el-button size="small" @click="exportMenus">导出</el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  @click="runAction(handleBatchDelete, '菜单批量删除失败')"
                >
                  批量删除
                </el-button>
                <el-button type="primary" size="small" @click="createDialogVisible = true">
                  新增菜单
                </el-button>
              </div>
            </div>
          </template>
          <el-table
            :data="list"
            row-key="id"
            height="520"
            v-loading="loading"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="54" />
            <el-table-column prop="title" label="菜单名称" min-width="140" />
            <el-table-column prop="name" label="路由名称" min-width="120" />
            <el-table-column prop="frontRoute" label="前端路由" min-width="150" />
            <el-table-column prop="path" label="路径" min-width="150" />
            <el-table-column prop="permission" label="权限接口" min-width="150" />
            <el-table-column prop="level" label="层级" width="80" />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button
                  link
                  type="danger"
                  @click="runAction(() => handleDelete(row), '菜单删除失败')"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <span>店铺菜单树预览</span>
          </template>
          <el-tree :data="tree" :props="treeProps" node-key="id" default-expand-all>
            <template #default="{ data }">
              <div class="flex items-center justify-between w-full pr-2">
                <span>{{ data.title }}</span>
                <span class="text-[12px] text-[#909399]">L{{ data.level }}</span>
              </div>
            </template>
          </el-tree>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="createDialogVisible" title="新增店铺菜单" width="560px">
      <el-form label-width="90px">
        <el-form-item label="菜单名称">
          <el-input v-model="createForm.title" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由名称">
          <el-input v-model="createForm.name" placeholder="请输入路由名称" />
        </el-form-item>
        <el-form-item label="前端路由">
          <el-input v-model="createForm.frontRoute" placeholder="请输入前端路由" />
        </el-form-item>
        <el-form-item label="菜单路径">
          <el-input v-model="createForm.path" placeholder="请输入菜单路径" />
        </el-form-item>
        <el-form-item label="权限接口">
          <el-input v-model="createForm.permission" placeholder="请输入权限接口匹配串" />
        </el-form-item>
        <el-form-item label="父级菜单">
          <el-select v-model="createForm.parentId" clearable placeholder="根节点可留空">
            <el-option
              v-for="item in parentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="createForm.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="runAction(handleCreateMenu, '菜单新增失败')">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑店铺菜单" width="560px">
      <el-form label-width="90px">
        <el-form-item label="菜单名称">
          <el-input v-model="editForm.title" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由名称">
          <el-input v-model="editForm.name" placeholder="请输入路由名称" />
        </el-form-item>
        <el-form-item label="前端路由">
          <el-input v-model="editForm.frontRoute" placeholder="请输入前端路由" />
        </el-form-item>
        <el-form-item label="菜单路径">
          <el-input v-model="editForm.path" placeholder="请输入菜单路径" />
        </el-form-item>
        <el-form-item label="权限接口">
          <el-input v-model="editForm.permission" placeholder="请输入权限接口匹配串" />
        </el-form-item>
        <el-form-item label="父级菜单">
          <el-select v-model="editForm.parentId" clearable placeholder="根节点可留空">
            <el-option
              v-for="item in parentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="editForm.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="runAction(handleEditMenu, '菜单修改失败')">
          保存
        </el-button>
      </template>
    </el-dialog>
  </AdminModuleShell>
</template>
