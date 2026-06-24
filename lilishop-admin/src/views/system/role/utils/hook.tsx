import dayjs from "dayjs";
import editForm from "../form.vue";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";
import { transformI18n } from "@/plugins/i18n";
import { addDialog } from "@/components/ReDialog";
import type { FormItemProps } from "../utils/types";
import type { PaginationProps } from "@pureadmin/table";
import { deviceDetection } from "@pureadmin/utils";
import {
  createManagerRole,
  deleteManagerRoles,
  getManagerMenuTree,
  getManagerRoleMenuList,
  getManagerRolePage,
  saveManagerRoleMenuList,
  updateManagerRole
} from "@/api/system";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { isSuccessResult } from "@/utils/result";
import { type Ref, reactive, ref, onMounted, h, watch } from "vue";

type RoleRow = {
  id: string;
  name: string;
  defaultRole: boolean;
  description: string;
  createTime?: string;
};

function getErrorMessage(error: any, fallback: string) {
  return error?.response?.data?.message || error?.message || fallback;
}

function normalizeRoleRow(item: Record<string, any>): RoleRow {
  return {
    id: String(item.id ?? ""),
    name: item.name || "",
    defaultRole: Boolean(item.defaultRole),
    description: item.description || item.remark || "",
    createTime: item.createTime || ""
  };
}

function normalizeMenuTree(list: Record<string, any>[] = []) {
  return list.map(item => ({
    ...item,
    id: String(item.id ?? ""),
    title: item.title || item.name || "",
    children: normalizeMenuTree(item.children ?? [])
  }));
}

function collectTreeIds(list: Record<string, any>[] = []) {
  return list.flatMap(item => [
    String(item.id),
    ...collectTreeIds(item.children ?? [])
  ]);
}

export function useRole(treeRef: Ref) {
  const form = reactive({
    name: "",
    defaultRole: ""
  });
  const curRow = ref<RoleRow | null>(null);
  const formRef = ref();
  const dataList = ref<RoleRow[]>([]);
  const treeIds = ref<string[]>([]);
  const treeData = ref<Record<string, any>[]>([]);
  const isShow = ref(false);
  const loading = ref(true);
  const isLinkage = ref(false);
  const treeSearchValue = ref("");
  const isExpandAll = ref(false);
  const isSelectAll = ref(false);
  const treeProps = {
    value: "id",
    label: "title",
    children: "children"
  };
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  const columns: TableColumnList = [
    {
      label: "角色编号",
      prop: "id"
    },
    {
      label: "角色名称",
      prop: "name"
    },
    {
      label: "默认角色",
      minWidth: 110,
      cellRenderer: ({ row, props }) => (
        <el-tag
          size={props.size}
          type={row.defaultRole ? "warning" : "info"}
          effect="plain"
        >
          {row.defaultRole ? "默认角色" : "普通角色"}
        </el-tag>
      )
    },
    {
      label: "备注",
      prop: "description",
      minWidth: 160
    },
    {
      label: "创建时间",
      prop: "createTime",
      minWidth: 160,
      formatter: ({ createTime }) =>
        createTime ? dayjs(createTime).format("YYYY-MM-DD HH:mm:ss") : "-"
    },
    {
      label: "操作",
      fixed: "right",
      width: 210,
      slot: "operation"
    }
  ];

  function buildListParams() {
    const params: Record<string, any> = {
      pageNumber: pagination.currentPage,
      pageSize: pagination.pageSize
    };
    if (form.name.trim()) params.name = form.name.trim();
    if (form.defaultRole !== "") params.defaultRole = form.defaultRole === "true";
    return params;
  }

  async function onSearch() {
    loading.value = true;
    try {
      const response = await getManagerRolePage(buildListParams());
      if (isSuccessResult(response)) {
        const payload = extractApiPayload<Record<string, any>>(response) || {};
        const records = extractApiRecords<Record<string, any>>(response);
        dataList.value = records.map(normalizeRoleRow);
        pagination.total = Number(payload.total ?? records.length ?? 0);
        pagination.pageSize = Number(
          payload.size ?? payload.pageSize ?? pagination.pageSize
        );
        pagination.currentPage = Number(
          payload.current ?? payload.pageNumber ?? pagination.currentPage
        );
      }
    } finally {
      loading.value = false;
    }
  }

  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  };

  function openDialog(title = "新增", row?: RoleRow) {
    addDialog({
      title: `${title}角色`,
      props: {
        formInline: {
          name: row?.name ?? "",
          defaultRole: row?.defaultRole ?? false,
          remark: row?.description ?? ""
        }
      },
      width: "40%",
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
          const payload = {
            name: curData.name?.trim(),
            defaultRole: curData.defaultRole,
            description: curData.remark?.trim() || undefined
          };
          try {
            if (title === "新增") {
              const response = await createManagerRole(payload);
              if (!isSuccessResult(response)) {
                throw new Error(response?.message || "新增角色失败");
              }
            } else {
              const response = await updateManagerRole(row?.id, payload);
              if (!isSuccessResult(response)) {
                throw new Error(response?.message || "修改角色失败");
              }
            }
            message(`角色${title}成功`, { type: "success" });
            done();
            await onSearch();
          } catch (error: any) {
            message(getErrorMessage(error, `${title}角色失败`), {
              type: "error"
            });
          }
        });
      }
    });
  }

  async function handleDelete(row: RoleRow) {
    try {
      const response = await deleteManagerRoles([row.id]);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "删除角色失败");
      }
      if (dataList.value.length === 1 && pagination.currentPage > 1) {
        pagination.currentPage -= 1;
      }
      message("角色删除成功", { type: "success" });
      await onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "删除角色失败"), { type: "error" });
    }
  }

  async function handleSizeChange(val: number) {
    pagination.pageSize = val;
    pagination.currentPage = 1;
    await onSearch();
  }

  async function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    await onSearch();
  }

  function handleSelectionChange(val) {
    console.log("handleSelectionChange", val);
  }

  async function handleMenu(row?: RoleRow) {
    const id = row?.id;
    if (id) {
      curRow.value = row;
      isShow.value = true;
      const response = await getManagerRoleMenuList(id);
      if (isSuccessResult(response) && treeRef.value) {
        const checkedKeys = extractApiRecords<Record<string, any>>(response).map(
          item => String(item.menuId || item.id)
        );
        treeRef.value.setCheckedKeys(checkedKeys);
      }
      return;
    }
    curRow.value = null;
    isShow.value = false;
  }

  function rowStyle({ row: { id } }) {
    return {
      cursor: "pointer",
      background: id === curRow.value?.id ? "var(--el-fill-color-light)" : ""
    };
  }

  async function handleSave() {
    if (!curRow.value || !treeRef.value) return;
    try {
      const payload = treeRef.value.getCheckedKeys().map(menuId => ({
        roleId: curRow.value.id,
        menuId: String(menuId),
        isSuper: false
      }));
      const response = await saveManagerRoleMenuList(curRow.value.id, payload);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "保存角色菜单权限失败");
      }
      message(`角色名称为${curRow.value.name}的菜单权限修改成功`, {
        type: "success"
      });
    } catch (error: any) {
      message(getErrorMessage(error, "保存角色菜单权限失败"), {
        type: "error"
      });
    }
  }

  const onQueryChanged = (query: string) => {
    treeRef.value?.filter(query);
  };

  const filterMethod = (query: string, node) => {
    return transformI18n(node.title)!.includes(query);
  };

  onMounted(async () => {
    await onSearch();
    const response = await getManagerMenuTree();
    if (isSuccessResult(response)) {
      treeData.value = normalizeMenuTree(extractApiRecords(response));
      treeIds.value = collectTreeIds(treeData.value);
    }
  });

  watch(isExpandAll, val => {
    if (!treeRef.value) return;
    val ? treeRef.value.setExpandedKeys(treeIds.value) : treeRef.value.setExpandedKeys([]);
  });

  watch(isSelectAll, val => {
    if (!treeRef.value) return;
    val ? treeRef.value.setCheckedKeys(treeIds.value) : treeRef.value.setCheckedKeys([]);
  });

  return {
    form,
    isShow,
    curRow,
    loading,
    columns,
    rowStyle,
    dataList,
    treeData,
    treeProps,
    isLinkage,
    pagination,
    isExpandAll,
    isSelectAll,
    treeSearchValue,
    onSearch,
    resetForm,
    openDialog,
    handleMenu,
    handleSave,
    handleDelete,
    filterMethod,
    transformI18n,
    onQueryChanged,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange
  };
}
