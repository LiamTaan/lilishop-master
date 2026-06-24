import { message } from "@/utils/message";
import type { PaginationProps } from "@pureadmin/table";
import { type Ref, reactive, ref, onMounted } from "vue";

const notIntegratedMessage =
  "当前仓库未发现管理端独立登录日志接口，页面暂不展示伪造联调数据";

export function useRole(tableRef: Ref) {
  const form = reactive({
    username: "",
    status: "",
    loginTime: ""
  });
  const dataList = ref([]);
  const loading = ref(true);
  const selectedNum = ref(0);

  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  const columns: TableColumnList = [
    {
      label: "勾选列",
      type: "selection",
      fixed: "left",
      reserveSelection: true
    },
    {
      label: "日志ID",
      prop: "id",
      minWidth: 180
    },
    {
      label: "用户名",
      prop: "username",
      minWidth: 120
    },
    {
      label: "登录 IP",
      prop: "ip",
      minWidth: 140
    },
    {
      label: "登录地点",
      prop: "address",
      minWidth: 160
    },
    {
      label: "操作系统",
      prop: "system",
      minWidth: 120
    },
    {
      label: "浏览器类型",
      prop: "browser",
      minWidth: 120
    },
    {
      label: "登录状态",
      prop: "status",
      minWidth: 100
    },
    {
      label: "登录行为",
      prop: "behavior",
      minWidth: 120
    },
    {
      label: "登录时间",
      prop: "loginTime",
      minWidth: 180
    }
  ];

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
    selectedNum.value = val.length;
    tableRef.value.setAdaptive();
  }

  function onSelectionCancel() {
    selectedNum.value = 0;
    tableRef.value.getTableRef().clearSelection();
  }

  function onbatchDel() {
    message(notIntegratedMessage, {
      type: "info"
    });
  }

  function clearAll() {
    message(notIntegratedMessage, {
      type: "info"
    });
  }

  async function onSearch() {
    loading.value = true;
    dataList.value = [];
    pagination.total = 0;
    loading.value = false;
  }

  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    pagination.currentPage = 1;
    onSearch();
  };

  onMounted(() => {
    onSearch();
  });

  return {
    form,
    loading,
    columns,
    dataList,
    pagination,
    selectedNum,
    onSearch,
    clearAll,
    resetForm,
    onbatchDel,
    handleSizeChange,
    onSelectionCancel,
    handleCurrentChange,
    handleSelectionChange
  };
}
