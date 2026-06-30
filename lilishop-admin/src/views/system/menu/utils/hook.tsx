import editForm from "../form.vue";
import { message } from "@/utils/message";
import { isSuccessResult } from "@/utils/result";
import {
  createManagerMenu,
  deleteManagerMenus,
  getManagerMenuTree,
  updateManagerMenu
} from "@/api/system";
import { addDialog } from "@/components/ReDialog";
import { reactive, ref, onMounted, h, computed } from "vue";
import type { FormItemProps } from "../utils/types";
import { cloneDeep, deviceDetection } from "@pureadmin/utils";
import { extractApiRecords } from "@/utils/admin-governance";
import { resolveBackendMenuBinding } from "@/router/menu-sync";

type MenuRow = {
  id: string;
  title: string;
  name: string;
  path: string;
  frontRoute: string;
  permission: string;
  level: number;
  parentId: string;
  sortOrder: number;
  currentTitle: string;
  currentPath: string;
  currentParentTitle: string;
  visibleInSidebar: boolean;
  children?: MenuRow[];
};

function getErrorMessage(error: any, fallback: string) {
  return error?.response?.data?.message || error?.message || fallback;
}

function normalizeMenuTree(list: Record<string, any>[] = []): MenuRow[] {
  return list.map(item => {
    const currentBinding = resolveBackendMenuBinding(item);
    return {
      id: String(item.id ?? ""),
      title: item.title || "",
      name: item.name || "",
      path: item.path || "",
      frontRoute: item.frontRoute || "",
      permission: item.permission || "",
      level: Number(item.level ?? 0),
      parentId: String(item.parentId ?? "0"),
      sortOrder: Number(item.sortOrder ?? 0),
      currentTitle: currentBinding?.currentTitle || "-",
      currentPath: currentBinding?.currentPath || "-",
      currentParentTitle: currentBinding?.currentParentTitle || "-",
      visibleInSidebar: Boolean(currentBinding?.visibleInSidebar),
      children: normalizeMenuTree(item.children ?? [])
    };
  });
}

function filterMenuTree(list: MenuRow[], keyword: string): MenuRow[] {
  if (!keyword) return list;
  return list
    .map(item => {
      const children = filterMenuTree(item.children ?? [], keyword);
      if (item.title.includes(keyword) || children.length > 0) {
        return { ...item, children };
      }
      return null;
    })
    .filter(Boolean) as MenuRow[];
}

function findMenuById(list: MenuRow[], id: string): MenuRow | null {
  for (const item of list) {
    if (item.id === id) return item;
    const child = findMenuById(item.children ?? [], id);
    if (child) return child;
  }
  return null;
}

export function useMenu() {
  const form = reactive({
    title: ""
  });

  const formRef = ref();
  const dataList = ref<MenuRow[]>([]);
  const selectedRows = ref<MenuRow[]>([]);
  const sourceTree = ref<MenuRow[]>([]);
  const loading = ref(true);
  const selectedNum = computed(() => selectedRows.value.length);

  const columns: TableColumnList = [
    {
      type: "selection",
      width: 54,
      reserveSelection: true,
      align: "center"
    },
    {
      label: "后端菜单",
      prop: "title",
      align: "left",
      minWidth: 180
    },
    {
      label: "管理端菜单",
      prop: "currentTitle",
      minWidth: 180
    },
    {
      label: "路由名称",
      prop: "name",
      minWidth: 140
    },
    {
      label: "后端前端路由",
      prop: "frontRoute",
      minWidth: 180
    },
    {
      label: "管理端路径",
      prop: "currentPath",
      minWidth: 180
    },
    {
      label: "后端路径",
      prop: "path",
      minWidth: 160
    },
    {
      label: "所属菜单组",
      prop: "currentParentTitle",
      minWidth: 150
    },
    {
      label: "权限接口",
      prop: "permission",
      minWidth: 180
    },
    {
      label: "侧边栏显示",
      prop: "visibleInSidebar",
      width: 110,
      cellRenderer: ({ row }) => (row.visibleInSidebar ? "显示" : "隐藏")
    },
    {
      label: "层级",
      prop: "level",
      width: 90
    },
    {
      label: "排序",
      prop: "sortOrder",
      width: 90
    },
    {
      label: "操作",
      fixed: "right",
      width: 210,
      slot: "operation"
    }
  ];

  function handleSelectionChange(val) {
    selectedRows.value = Array.isArray(val) ? val : [];
  }

  function resetForm(formEl) {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  }

  async function onSearch() {
    loading.value = true;
    try {
      const response = await getManagerMenuTree();
      if (isSuccessResult(response)) {
        sourceTree.value = normalizeMenuTree(extractApiRecords(response));
        dataList.value = filterMenuTree(sourceTree.value, form.title.trim());
      }
    } finally {
      loading.value = false;
    }
  }

  function formatHigherMenuOptions(treeList: MenuRow[]) {
    return treeList.map(item => ({
      ...item,
      children: formatHigherMenuOptions(item.children ?? [])
    }));
  }

  function resolveMenuLevel(parentId: string) {
    if (!parentId || parentId === "0") return 0;
    const parent = findMenuById(sourceTree.value, parentId);
    return parent ? Number(parent.level ?? 0) + 1 : 0;
  }

  function openDialog(title = "新增", row?: Partial<MenuRow>) {
    addDialog({
      title: `${title}菜单`,
      props: {
        formInline: {
          higherMenuOptions: formatHigherMenuOptions(cloneDeep(sourceTree.value)),
          parentId: row?.parentId ?? "0",
          title: row?.title ?? "",
          name: row?.name ?? "",
          frontRoute: row?.frontRoute ?? "",
          path: row?.path ?? "",
          permission: row?.permission ?? "",
          sortOrder: row?.sortOrder ?? 0
        }
      },
      width: "45%",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: () => h(editForm, { ref: formRef, formInline: null }),
      beforeSure: (done, { options }) => {
        const FormRef = formRef.value.getRef();
        const curData = options.props.formInline as FormItemProps;
        FormRef.validate(async valid => {
          if (!valid) return;
          const parentId = String(curData.parentId || "0");
          const payload = {
            title: curData.title?.trim(),
            name: curData.name?.trim(),
            frontRoute: curData.frontRoute?.trim() || undefined,
            path: curData.path?.trim() || undefined,
            permission: curData.permission?.trim() || undefined,
            parentId,
            sortOrder: Number(curData.sortOrder ?? 0),
            level: resolveMenuLevel(parentId)
          };
          try {
            if (title === "新增") {
              const response = await createManagerMenu(payload);
              if (!isSuccessResult(response)) {
                throw new Error(response?.message || "新增菜单失败");
              }
            } else {
              const response = await updateManagerMenu(String(row?.id), payload);
              if (!isSuccessResult(response)) {
                throw new Error(response?.message || "修改菜单失败");
              }
            }
            message(`菜单${title}成功`, { type: "success" });
            done();
            await onSearch();
          } catch (error: any) {
            message(getErrorMessage(error, `${title}菜单失败`), {
              type: "error"
            });
          }
        });
      }
    });
  }

  async function handleDelete(row: MenuRow) {
    try {
      const response = await deleteManagerMenus([row.id]);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "删除菜单失败");
      }
      message("菜单删除成功", { type: "success" });
      await onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "删除菜单失败"), { type: "error" });
    }
  }

  async function onbatchDel() {
    const ids = selectedRows.value.map(item => item.id).filter(Boolean);
    if (!ids.length) return;
    try {
      const response = await deleteManagerMenus(ids);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "批量删除菜单失败");
      }
      selectedRows.value = [];
      message(`已删除菜单编号为 ${ids.join(", ")} 的数据`, {
        type: "success"
      });
      await onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "批量删除菜单失败"), { type: "error" });
    }
  }

  onMounted(() => {
    onSearch();
  });

  return {
    form,
    loading,
    columns,
    dataList,
    onSearch,
    resetForm,
    openDialog,
    handleDelete,
    onbatchDel,
    selectedNum,
    handleSelectionChange
  };
}
