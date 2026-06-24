import { message } from "@/utils/message";
import { reactive, ref, onMounted } from "vue";
import type { PaginationProps } from "@pureadmin/table";

const notIntegratedMessage =
  "当前仓库未发现管理端在线会话列表/强退接口，页面暂不展示模板假数据";

export function useRole() {
  const form = reactive({
    username: ""
  });
  const dataList = ref([]);
  const loading = ref(true);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  const columns: TableColumnList = [
    {
      label: "会话ID",
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
      label: "登录时间",
      prop: "loginTime",
      minWidth: 180
    },
    {
      label: "操作",
      fixed: "right",
      slot: "operation"
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
    console.log("handleSelectionChange", val);
  }

  function handleOffline(_row?: Record<string, any>) {
    message(notIntegratedMessage, { type: "info" });
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
    onSearch,
    resetForm,
    handleOffline,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange
  };
}
