import dayjs from "dayjs";
import { message } from "@/utils/message";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { isSuccessResult } from "@/utils/result";
import {
  clearManagerOperationLogs,
  deleteManagerOperationLogs,
  getManagerOperationLogPage
} from "@/api/system";
import type { PaginationProps } from "@pureadmin/table";
import { type Ref, reactive, ref, onMounted, toRaw } from "vue";

type OperationLogRow = {
  id: string;
  username: string;
  name: string;
  requestType: string;
  requestUrl: string;
  ip: string;
  ipInfo: string;
  createTime?: number | string;
};

function getErrorMessage(error: any, fallback: string) {
  return error?.response?.data?.message || error?.message || fallback;
}

function normalizeLogRow(item: Record<string, any>): OperationLogRow {
  return {
    id: String(item.id ?? ""),
    username: item.username || "-",
    name: item.name || item.customerLog || "-",
    requestType: item.requestType || "-",
    requestUrl: item.requestUrl || "-",
    ip: item.ip || "-",
    ipInfo: item.ipInfo || "-",
    createTime: item.createTime
  };
}

export function useRole(tableRef: Ref) {
  const form = reactive({
    operatorName: "",
    searchKey: "",
    operatingTime: ""
  });
  const dataList = ref<OperationLogRow[]>([]);
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
      label: "操作人员",
      prop: "username",
      minWidth: 120
    },
    {
      label: "操作名称",
      prop: "name",
      minWidth: 160
    },
    {
      label: "请求方式",
      prop: "requestType",
      minWidth: 100,
      cellRenderer: ({ row, props }) => (
        <el-tag size={props.size} type="info" effect="plain">
          {row.requestType}
        </el-tag>
      )
    },
    {
      label: "请求地址",
      prop: "requestUrl",
      minWidth: 240
    },
    {
      label: "操作 IP",
      prop: "ip",
      minWidth: 120
    },
    {
      label: "日志附加信息",
      prop: "ipInfo",
      minWidth: 220
    },
    {
      label: "操作时间",
      prop: "createTime",
      minWidth: 180,
      formatter: ({ createTime }) =>
        createTime ? dayjs(createTime).format("YYYY-MM-DD HH:mm:ss") : "-"
    }
  ];

  function buildListParams() {
    const params: Record<string, any> = {
      pageNumber: pagination.currentPage,
      pageSize: pagination.pageSize,
      searchKey: form.searchKey.trim()
    };
    if (form.operatorName.trim()) {
      params.operatorName = form.operatorName.trim();
    }
    if (Array.isArray(form.operatingTime) && form.operatingTime.length === 2) {
      params.startDate = dayjs(form.operatingTime[0]).format("YYYY-MM-DD");
      params.endDate = dayjs(form.operatingTime[1]).format("YYYY-MM-DD");
    }
    return params;
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
    selectedNum.value = val.length;
    tableRef.value.setAdaptive();
  }

  function onSelectionCancel() {
    selectedNum.value = 0;
    tableRef.value.getTableRef().clearSelection();
  }

  async function onbatchDel() {
    const curSelected = tableRef.value.getTableRef().getSelectionRows();
    const ids = curSelected
      .map(item => String(item.id || ""))
      .filter(Boolean);
    if (ids.length === 0) return;
    try {
      const response = await deleteManagerOperationLogs(ids);
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "批量删除操作日志失败");
      }
      message("操作日志删除成功", { type: "success" });
      tableRef.value.getTableRef().clearSelection();
      await onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "批量删除操作日志失败"), {
        type: "error"
      });
    }
  }

  async function clearAll() {
    try {
      const response = await clearManagerOperationLogs();
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "清空操作日志失败");
      }
      message("操作日志已清空", {
        type: "success"
      });
      await onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "清空操作日志失败"), {
        type: "error"
      });
    }
  }

  async function onSearch() {
    loading.value = true;
    try {
      const response = await getManagerOperationLogPage(buildListParams());
      if (isSuccessResult(response)) {
        const payload = extractApiPayload<Record<string, any>>(response) || {};
        const records = extractApiRecords<Record<string, any>>(response);
        dataList.value = records.map(normalizeLogRow);
        pagination.total = Number(payload.total ?? records.length ?? 0);
        pagination.pageSize = Number(payload.size ?? pagination.pageSize);
        pagination.currentPage = Number(
          payload.current ?? pagination.currentPage
        );
      }
    } finally {
      loading.value = false;
    }
  }

  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    form.searchKey = "";
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
