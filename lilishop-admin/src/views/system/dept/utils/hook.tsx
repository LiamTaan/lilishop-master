import dayjs from "dayjs";
import editForm from "../form.vue";
import { message } from "@/utils/message";
import { isSuccessResult } from "@/utils/result";
import {
  createManagerDepartment,
  deleteManagerDepartments,
  getManagerDepartmentTree,
  updateManagerDepartment
} from "@/api/system";
import { addDialog } from "@/components/ReDialog";
import { reactive, ref, onMounted, h, computed } from "vue";
import type { FormItemProps } from "../utils/types";
import { cloneDeep, deviceDetection } from "@pureadmin/utils";
import { extractApiRecords } from "@/utils/admin-governance";

type DepartmentRow = {
  id: string;
  title: string;
  parentId: string;
  sortOrder: number;
  createTime?: string;
  children?: DepartmentRow[];
};

function getErrorMessage(error: any, fallback: string) {
  return error?.response?.data?.message || error?.message || fallback;
}

function normalizeDepartmentTree(list: Record<string, any>[] = []): DepartmentRow[] {
  return list.map(item => ({
    id: String(item.id ?? ""),
    title: item.title || "",
    parentId: String(item.parentId ?? "0"),
    sortOrder: Number(item.sortOrder ?? 0),
    createTime: item.createTime || "",
    children: normalizeDepartmentTree(item.children ?? [])
  }));
}

function filterDepartmentTree(list: DepartmentRow[], keyword: string): DepartmentRow[] {
  if (!keyword) return list;
  return list
    .map(item => {
      const children = filterDepartmentTree(item.children ?? [], keyword);
      if (item.title.includes(keyword) || children.length > 0) {
        return { ...item, children };
      }
      return null;
    })
    .filter(Boolean) as DepartmentRow[];
}

export function useDept() {
  const form = reactive({
    title: ""
  });

  const formRef = ref();
  const dataList = ref<DepartmentRow[]>([]);
  const selectedRows = ref<DepartmentRow[]>([]);
  const sourceTree = ref<DepartmentRow[]>([]);
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
      label: "部门名称",
      prop: "title",
      minWidth: 220,
      align: "left"
    },
    {
      label: "部门ID",
      prop: "id",
      minWidth: 180
    },
    {
      label: "排序",
      prop: "sortOrder",
      minWidth: 90
    },
    {
      label: "创建时间",
      minWidth: 200,
      prop: "createTime",
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
      const response = await getManagerDepartmentTree();
      if (isSuccessResult(response)) {
        sourceTree.value = normalizeDepartmentTree(extractApiRecords(response));
        dataList.value = filterDepartmentTree(
          sourceTree.value,
          form.title.trim()
        );
      }
    } finally {
      loading.value = false;
    }
  }

  function formatHigherDeptOptions(treeList: DepartmentRow[]) {
    return treeList.map(item => ({
      ...item,
      children: formatHigherDeptOptions(item.children ?? [])
    }));
  }

  function openDialog(title = "新增", row?: Partial<DepartmentRow>) {
    addDialog({
      title: `${title}部门`,
      props: {
        formInline: {
          higherDeptOptions: formatHigherDeptOptions(cloneDeep(sourceTree.value)),
          parentId: row?.parentId ?? "0",
          title: row?.title ?? "",
          sortOrder: row?.sortOrder ?? 0
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
            id: row?.id,
            title: curData.title?.trim(),
            parentId: String(curData.parentId || "0"),
            sortOrder: Number(curData.sortOrder ?? 0)
          };
          try {
            if (title === "新增") {
              const response = await createManagerDepartment(payload);
              if (!isSuccessResult(response)) {
                throw new Error(response?.message || "新增部门失败");
              }
            } else {
              const response = await updateManagerDepartment(String(row?.id), payload);
              if (!isSuccessResult(response)) {
                throw new Error(response?.message || "修改部门失败");
              }
            }
            message(`部门${title}成功`, { type: "success" });
            done();
            await onSearch();
          } catch (error: any) {
            message(getErrorMessage(error, `${title}部门失败`), {
              type: "error"
            });
          }
        });
      }
    });
  }

  async function handleDelete(row: DepartmentRow) {
    try {
      const response = await deleteManagerDepartments([row.id]);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "删除部门失败");
      }
      message("部门删除成功", { type: "success" });
      await onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "删除部门失败"), { type: "error" });
    }
  }

  async function onbatchDel() {
    const ids = selectedRows.value.map(item => item.id).filter(Boolean);
    if (!ids.length) return;
    try {
      const response = await deleteManagerDepartments(ids);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "批量删除部门失败");
      }
      selectedRows.value = [];
      message(`已删除部门编号为 ${ids.join(", ")} 的数据`, {
        type: "success"
      });
      await onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "批量删除部门失败"), { type: "error" });
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
