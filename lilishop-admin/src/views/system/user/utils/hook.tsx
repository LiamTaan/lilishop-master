import "./reset.css";
import dayjs from "dayjs";
import roleForm from "../form/role.vue";
import editForm from "../form/index.vue";
import { message } from "@/utils/message";
import { isSuccessResult, unwrapResult } from "@/utils/result";
import userAvatar from "@/assets/user.jpg";
import { usePublicHooks } from "../../hooks";
import { addDialog } from "@/components/ReDialog";
import type { PaginationProps } from "@pureadmin/table";
import ReCropperPreview from "@/components/ReCropperPreview";
import type { FormItemProps, RoleFormItemProps } from "../utils/types";
import { getKeyList, deviceDetection } from "@pureadmin/utils";
import {
  createAdminUser,
  deleteAdminUsers,
  getAdminDepartmentTree,
  getAdminRolePage,
  getAdminUserPage,
  resetAdminUserPassword,
  uploadAdminUserAvatar,
  updateAdminUser,
  updateAdminUserStatus
} from "@/api/system";
import { ElMessageBox } from "element-plus";
import { type Ref, computed, h, onMounted, reactive, ref } from "vue";

type AdminRoleOption = {
  id: string;
  name: string;
};

type AdminUserRow = {
  id: string;
  avatar: string;
  username: string;
  nickname: string;
  phone: string;
  email: string;
  status: boolean;
  createTime: string;
  dept: {
    id?: string;
    name?: string;
  };
  departmentId: string;
  remark: string;
  description: string;
  roles: AdminRoleOption[];
  roleIds: string[];
  isSuper: boolean;
};

function getErrorMessage(error: any, fallback: string) {
  return error?.response?.data?.message || error?.message || fallback;
}

function isDialogCanceled(error: unknown) {
  return error === "cancel" || error === "close";
}

function normalizeDepartmentTree(list: Record<string, any>[] = []) {
  return list.map(item => ({
    ...item,
    name: item.name ?? item.title ?? "",
    children: normalizeDepartmentTree(item.children ?? [])
  }));
}

function normalizeAdminUserRow(item: Record<string, any>): AdminUserRow {
  const roleIds = Array.isArray(item.roles)
    ? item.roles
        .map(role => role?.id)
        .filter(Boolean)
        .map(String)
    : typeof item.roleIds === "string" && item.roleIds.length > 0
      ? item.roleIds.split(",").filter(Boolean)
      : [];

  return {
    id: String(item.id ?? ""),
    avatar: item.avatar || "",
    username: item.username || "",
    nickname: item.nickName || item.nickname || "",
    phone: item.mobile || item.phone || "",
    email: item.email || "",
    status: item.status !== false,
    createTime: item.createTime || "",
    dept: {
      id: item.departmentId ? String(item.departmentId) : "",
      name: item.departmentTitle || item.dept?.name || ""
    },
    departmentId: item.departmentId ? String(item.departmentId) : "",
    remark: item.description || item.remark || "",
    description: item.description || item.remark || "",
    roles: Array.isArray(item.roles)
      ? item.roles.map(role => ({
          id: String(role?.id ?? ""),
          name: role?.name || ""
        }))
      : [],
    roleIds,
    isSuper: Boolean(item.isSuper)
  };
}

function buildUserPayload(formData: FormItemProps) {
  return {
    id: formData.id,
    username: formData.username?.trim(),
    password: formData.password?.trim() || undefined,
    nickName: formData.nickname?.trim(),
    mobile: formData.phone?.trim(),
    email: formData.email?.trim(),
    description: formData.remark?.trim(),
    departmentId: formData.departmentId || undefined,
    status: formData.status,
    avatar: formData.avatar || undefined,
    isSuper: formData.isSuper ?? false,
    roles: formData.roleIds ?? []
  };
}

export function useUser(tableRef: Ref, treeRef: Ref) {
  const form = reactive({
    deptId: "",
    username: "",
    nickname: "",
    phone: "",
    status: null as boolean | null
  });
  const formRef = ref();
  const dataList = ref<AdminUserRow[]>([]);
  const loading = ref(true);
  const switchLoadMap = ref<Record<number, { loading?: boolean }>>({});
  const { switchStyle } = usePublicHooks();
  const higherDeptOptions = ref<Record<string, any>[]>([]);
  const treeData = ref<Record<string, any>[]>([]);
  const treeLoading = ref(true);
  const selectedNum = ref(0);
  const avatarInfo = ref<{
    base64?: string;
    blob?: Blob;
    info?: Record<string, any>;
  } | null>(null);
  const cropRef = ref();
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  const roleOptions = ref<AdminRoleOption[]>([]);

  const columns: TableColumnList = [
    {
      label: "勾选列",
      type: "selection",
      fixed: "left",
      reserveSelection: true
    },
    {
      label: "账号ID",
      prop: "id",
      minWidth: 180
    },
    {
      label: "用户头像",
      prop: "avatar",
      cellRenderer: ({ row }) => (
        <el-image
          fit="cover"
          preview-teleported={true}
          src={row.avatar || userAvatar}
          preview-src-list={Array.of(row.avatar || userAvatar)}
          class="size-6 rounded-full align-middle"
        />
      ),
      width: 90
    },
    {
      label: "登录账号",
      prop: "username",
      minWidth: 140
    },
    {
      label: "用户昵称",
      prop: "nickname",
      minWidth: 140
    },
    {
      label: "权限级别",
      prop: "isSuper",
      minWidth: 110,
      cellRenderer: ({ row, props }) => (
        <el-tag
          size={props.size}
          type={row.isSuper ? "danger" : "info"}
          effect="plain"
        >
          {row.isSuper ? "超级管理员" : "普通管理员"}
        </el-tag>
      )
    },
    {
      label: "绑定角色",
      prop: "roles",
      minWidth: 220,
      formatter: ({ roles }) =>
        Array.isArray(roles) && roles.length > 0
          ? roles
              .map(role => role?.name)
              .filter(Boolean)
              .join(" / ")
          : "-"
    },
    {
      label: "部门",
      prop: "dept.name",
      minWidth: 140,
      formatter: ({ dept }) => dept?.name || "-"
    },
    {
      label: "手机号码",
      prop: "phone",
      minWidth: 140,
      formatter: ({ phone }) => phone || "-"
    },
    {
      label: "状态",
      prop: "status",
      minWidth: 110,
      cellRenderer: scope => (
        <el-switch
          size={scope.props.size === "small" ? "small" : "default"}
          loading={switchLoadMap.value[scope.index]?.loading}
          v-model={scope.row.status}
          active-value={true}
          inactive-value={false}
          active-text="已启用"
          inactive-text="已停用"
          inline-prompt
          style={switchStyle.value}
          onChange={() => onChange(scope as any)}
        />
      )
    },
    {
      label: "创建时间",
      minWidth: 180,
      prop: "createTime",
      formatter: ({ createTime }) =>
        createTime ? dayjs(createTime).format("YYYY-MM-DD HH:mm:ss") : "-"
    },
    {
      label: "操作",
      fixed: "right",
      width: 180,
      slot: "operation"
    }
  ];

  const buttonClass = computed(() => {
    return [
      "h-5!",
      "reset-margin",
      "text-gray-500!",
      "dark:text-white!",
      "dark:hover:text-primary!"
    ];
  });

  function buildRoleIds(row?: Partial<AdminUserRow>) {
    if (!row) return [];
    if (Array.isArray(row.roleIds)) {
      return row.roleIds.filter(Boolean).map(String);
    }
    if (Array.isArray(row.roles)) {
      return row.roles
        .map(role => role?.id)
        .filter(Boolean)
        .map(String);
    }
    return [];
  }

  function buildListParams() {
    const params: Record<string, any> = {
      pageNumber: pagination.currentPage,
      pageSize: pagination.pageSize,
      sort: "create_time",
      order: "desc"
    };

    if (form.deptId) params.departmentId = form.deptId;
    if (form.username.trim()) params.username = form.username.trim();
    if (form.nickname.trim()) params.nickName = form.nickname.trim();
    if (form.phone.trim()) params.mobile = form.phone.trim();
    if (typeof form.status === "boolean") params.status = form.status;

    return params;
  }

  async function persistAvatar(row: AdminUserRow, avatar: string) {
    const response = await updateAdminUser({
      id: row.id,
      username: row.username,
      nickName: row.nickname || undefined,
      mobile: row.phone || undefined,
      email: row.email || undefined,
      avatar,
      description: row.description || undefined,
      departmentId: row.departmentId || undefined,
      isSuper: row.isSuper,
      status: row.status,
      roles: buildRoleIds(row)
    });

    if (!isSuccessResult(response)) {
      throw new Error(response?.message || "保存头像失败");
    }
  }

  async function onChange({ row, index }) {
    const targetStatus = row.status;
    try {
      await ElMessageBox.confirm(
        `确认要<strong>${
          targetStatus ? "启用" : "停用"
        }</strong><strong style='color:var(--el-color-primary)'>${
          row.username
        }</strong>账号吗?`,
        "系统提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
          dangerouslyUseHTMLString: true,
          draggable: true
        }
      );

      switchLoadMap.value[index] = {
        ...switchLoadMap.value[index],
        loading: true
      };

      const response = await updateAdminUserStatus(row.id, targetStatus);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "状态更新失败");
      }

      message("已成功修改账号状态", { type: "success" });
    } catch (error: any) {
      row.status = !targetStatus;
      if (!isDialogCanceled(error)) {
        message(getErrorMessage(error, "状态更新失败"), { type: "error" });
      }
    } finally {
      switchLoadMap.value[index] = {
        ...switchLoadMap.value[index],
        loading: false
      };
    }
  }

  async function handleDelete(row: AdminUserRow) {
    try {
      const response = await deleteAdminUsers([row.id]);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "删除失败");
      }
      if (dataList.value.length === 1 && pagination.currentPage > 1) {
        pagination.currentPage -= 1;
      }
      message(`已删除登录账号为 ${row.username} 的数据`, { type: "success" });
      onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "删除失败"), { type: "error" });
    }
  }

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    pagination.currentPage = 1;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    onSearch();
  }

  function handleSelectionChange(val) {
    selectedNum.value = val.length;
    tableRef.value.setAdaptive();
  }

  function onSelectionCancel() {
    selectedNum.value = 0;
    tableRef.value.getTableRef().clearSelection();
  }

  async function onbatchDel() {
    const curSelected = tableRef.value.getTableRef().getSelectionRows();
    const ids = getKeyList(curSelected, "id");
    try {
      const response = await deleteAdminUsers(ids);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "批量删除失败");
      }
      tableRef.value.getTableRef().clearSelection();
      selectedNum.value = 0;
      if (curSelected.length >= dataList.value.length && pagination.currentPage > 1) {
        pagination.currentPage -= 1;
      }
      message(`已删除账号编号为 ${ids.join(", ")} 的数据`, {
        type: "success"
      });
      onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "批量删除失败"), { type: "error" });
    }
  }

  async function onSearch() {
    loading.value = true;
    try {
      const response = await getAdminUserPage(buildListParams());
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "获取账号列表失败");
      }

      const data = unwrapResult(response) ?? {};
      const records = Array.isArray(data.records) ? data.records : [];
      dataList.value = records.map(normalizeAdminUserRow);
      pagination.total = Number(data.total ?? records.length);
      pagination.pageSize = Number(data.size ?? pagination.pageSize);
      pagination.currentPage = Number(data.current ?? pagination.currentPage);
    } catch (error: any) {
      dataList.value = [];
      pagination.total = 0;
      message(getErrorMessage(error, "获取账号列表失败"), { type: "error" });
    } finally {
      loading.value = false;
    }
  }

  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    form.deptId = "";
    form.status = null;
    pagination.currentPage = 1;
    treeRef.value.onTreeReset();
    onSearch();
  };

  function onTreeSelect({ id, selected }) {
    form.deptId = selected ? String(id) : "";
    pagination.currentPage = 1;
    onSearch();
  }

  async function submitUserForm(title: string, formData: FormItemProps) {
    const payload = buildUserPayload(formData);
    const response =
      title === "新增"
        ? await createAdminUser(payload)
        : await updateAdminUser({
            ...payload,
            password: undefined
          });

    if (!isSuccessResult(response)) {
      throw new Error(response?.message || `${title}失败`);
    }

    pagination.currentPage = 1;
    message(`已${title}登录账号为 ${payload.username} 的数据`, {
      type: "success"
    });
  }

  function openDialog(title = "新增", row?: AdminUserRow) {
    addDialog({
      title: `${title}账号`,
      props: {
        formInline: {
          id: row?.id,
          title,
          higherDeptOptions: higherDeptOptions.value,
          departmentId: row?.departmentId ?? "",
          nickname: row?.nickname ?? "",
          username: row?.username ?? "",
          password: "",
          phone: row?.phone ?? "",
          email: row?.email ?? "",
          status: row?.status ?? true,
          roleIds: buildRoleIds(row),
          remark: row?.remark ?? "",
          avatar: row?.avatar ?? "",
          isSuper: row?.isSuper ?? false,
          description: row?.description ?? ""
        }
      },
      width: "46%",
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
          try {
            await submitUserForm(title, curData);
            done();
            onSearch();
          } catch (error: any) {
            message(getErrorMessage(error, `${title}失败`), { type: "error" });
          }
        });
      }
    });
  }

  function handleUpload(row: AdminUserRow) {
    avatarInfo.value = null;
    addDialog({
      title: `上传 ${row.username} 的头像`,
      width: "40%",
      closeOnClickModal: false,
      fullscreen: deviceDetection(),
      contentRenderer: () =>
        h(ReCropperPreview, {
          ref: cropRef,
          imgSrc: row.avatar || userAvatar,
          onCropper: info => (avatarInfo.value = info)
        }),
      beforeSure: async done => {
        try {
          const blob = avatarInfo.value?.blob;
          if (!blob) {
            message("请先完成头像裁剪后再提交", {
              type: "warning"
            });
            return;
          }

          const file = new File([blob], `${row.username || "avatar"}.png`, {
            type: blob.type || "image/png"
          });
          const formData = new FormData();
          formData.append("file", file);
          formData.append("directoryPath", "manager-avatar");

          const uploadResponse = await uploadAdminUserAvatar(formData);
          if (!isSuccessResult(uploadResponse)) {
            throw new Error(uploadResponse?.message || "头像上传失败");
          }

          const avatarUrl = String(unwrapResult(uploadResponse) || "");
          if (!avatarUrl) {
            throw new Error("头像上传成功但未返回文件地址");
          }

          await persistAvatar(row, avatarUrl);
          message(`已更新 ${row.username} 的头像`, {
            type: "success"
          });
          done();
          onSearch();
        } catch (error: any) {
          message(getErrorMessage(error, "头像上传失败"), {
            type: "error"
          });
        }
      },
      closeCallBack: () => {
        avatarInfo.value = null;
        cropRef.value?.hidePopover?.();
      }
    });
  }

  async function handleReset(row: AdminUserRow) {
    try {
      await ElMessageBox.confirm(
        `确认将 ${row.username} 的密码重置为默认密码 123456 吗？`,
        "系统提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
          draggable: true
        }
      );
      const response = await resetAdminUserPassword([row.id]);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "重置密码失败");
      }
      message(`已将 ${row.username} 的密码重置为默认密码 123456`, {
        type: "success"
      });
    } catch (error: any) {
      if (!isDialogCanceled(error)) {
        message(getErrorMessage(error, "重置密码失败"), { type: "error" });
      }
    }
  }

  async function handleRole(row: AdminUserRow) {
    addDialog({
      title: `分配 ${row.username} 的角色`,
      props: {
        formInline: {
          username: row.username,
          nickname: row.nickname,
          roleOptions: roleOptions.value,
          ids: buildRoleIds(row)
        }
      },
      width: "400px",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: () => h(roleForm),
      beforeSure: async (done, { options }) => {
        const curData = options.props.formInline as RoleFormItemProps;
        try {
          const response = await updateAdminUser({
            id: row.id,
            username: row.username,
            nickName: row.nickname,
            mobile: row.phone || undefined,
            email: row.email || undefined,
            avatar: row.avatar || undefined,
            description: row.description || undefined,
            departmentId: row.departmentId || undefined,
            isSuper: row.isSuper,
            status: row.status,
            roles: curData.ids ?? []
          });
          if (!isSuccessResult(response)) {
            throw new Error(response?.message || "分配角色失败");
          }
          message(`已更新 ${row.username} 的角色配置`, { type: "success" });
          done();
          onSearch();
        } catch (error: any) {
          message(getErrorMessage(error, "分配角色失败"), { type: "error" });
        }
      }
    });
  }

  onMounted(async () => {
    treeLoading.value = true;
    loading.value = true;
    try {
      const [departmentResponse, roleResponse] = await Promise.all([
        getAdminDepartmentTree(),
        getAdminRolePage({ pageNumber: 1, pageSize: 999 })
      ]);

      if (isSuccessResult(departmentResponse)) {
        const data = normalizeDepartmentTree(unwrapResult(departmentResponse) ?? []);
        higherDeptOptions.value = data;
        treeData.value = data;
      }

      if (isSuccessResult(roleResponse)) {
        const data = unwrapResult(roleResponse) ?? {};
        const records = Array.isArray(data.records) ? data.records : [];
        roleOptions.value = records.map(item => ({
          id: String(item?.id ?? ""),
          name: item?.name || ""
        }));
      }
    } catch (error: any) {
      message(getErrorMessage(error, "初始化账号数据失败"), { type: "error" });
    } finally {
      treeLoading.value = false;
      onSearch();
    }
  });

  return {
    form,
    loading,
    columns,
    dataList,
    treeData,
    treeLoading,
    selectedNum,
    pagination,
    buttonClass,
    deviceDetection,
    onSearch,
    resetForm,
    onbatchDel,
    openDialog,
    onTreeSelect,
    handleDelete,
    handleUpload,
    handleReset,
    handleRole,
    handleSizeChange,
    onSelectionCancel,
    handleCurrentChange,
    handleSelectionChange
  };
}
