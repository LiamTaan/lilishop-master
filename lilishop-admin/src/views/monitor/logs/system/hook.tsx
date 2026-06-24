import dayjs from "dayjs";
import Detail from "./detail.vue";
import { message } from "@/utils/message";
import { addDialog } from "@/components/ReDialog";
import { extractApiPayload, extractApiRecords } from "@/utils/admin-governance";
import { isSuccessResult } from "@/utils/result";
import {
  clearManagerOperationLogs,
  deleteManagerOperationLogs,
  getManagerOperationLogPage
} from "@/api/system";
import type { PaginationProps } from "@pureadmin/table";
import { type Ref, reactive, ref, onMounted } from "vue";
import { getKeyList, useCopyToClipboard } from "@pureadmin/utils";
import Info from "~icons/ri/question-line";

type SystemLogRow = {
  id: string;
  username: string;
  name: string;
  requestUrl: string;
  requestType: string;
  ip: string;
  ipInfo: string;
  costTime: number;
  createTime?: number | string;
  requestParam?: string;
  responseBody?: string;
  customerLog?: string;
};

function getErrorMessage(error: any, fallback: string) {
  return error?.response?.data?.message || error?.message || fallback;
}

function normalizeLogRow(item: Record<string, any>): SystemLogRow {
  return {
    id: String(item.id ?? ""),
    username: item.username || "-",
    name: item.name || item.customerLog || "-",
    requestUrl: item.requestUrl || "-",
    requestType: item.requestType || "-",
    ip: item.ip || "-",
    ipInfo: item.ipInfo || "-",
    costTime: Number(item.costTime ?? 0),
    createTime: item.createTime,
    requestParam: item.requestParam,
    responseBody: item.responseBody,
    customerLog: item.customerLog
  };
}

export function useRole(tableRef: Ref) {
  const form = reactive({
    operatorName: "",
    searchKey: "",
    requestTime: ""
  });
  const dataList = ref<SystemLogRow[]>([]);
  const loading = ref(true);
  const selectedNum = ref(0);
  const { copied, update } = useCopyToClipboard();

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
      headerRenderer: () => (
        <span class="flex-c">
          请求接口
          <iconify-icon-offline
            icon={Info}
            class="ml-1 cursor-help"
            v-tippy={{
              content: "双击下面请求接口进行拷贝"
            }}
          />
        </span>
      ),
      prop: "requestUrl",
      minWidth: 240
    },
    {
      label: "请求方法",
      prop: "requestType",
      minWidth: 100
    },
    {
      label: "IP 地址",
      prop: "ip",
      minWidth: 120
    },
    {
      label: "附加信息",
      prop: "ipInfo",
      minWidth: 220
    },
    {
      label: "请求耗时",
      prop: "costTime",
      minWidth: 110,
      cellRenderer: ({ row, props }) => (
        <el-tag
          size={props.size}
          type={row.costTime < 1000 ? "success" : "warning"}
          effect="plain"
        >
          {row.costTime} ms
        </el-tag>
      )
    },
    {
      label: "请求时间",
      prop: "createTime",
      minWidth: 180,
      formatter: ({ createTime }) =>
        createTime ? dayjs(createTime).format("YYYY-MM-DD HH:mm:ss") : "-"
    },
    {
      label: "操作",
      fixed: "right",
      slot: "operation"
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
    if (Array.isArray(form.requestTime) && form.requestTime.length === 2) {
      params.startDate = dayjs(form.requestTime[0]).format("YYYY-MM-DD");
      params.endDate = dayjs(form.requestTime[1]).format("YYYY-MM-DD");
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

  function handleCellDblclick({ requestUrl }, { property }) {
    if (property !== "requestUrl") return;
    update(requestUrl);
    copied.value
      ? message(`${requestUrl} 已拷贝`, { type: "success" })
      : message("拷贝失败", { type: "warning" });
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
        throw new Error(response?.message || "批量删除系统日志失败");
      }
      message(`已删除日志 ${getKeyList(curSelected, "id")}`, {
        type: "success"
      });
      tableRef.value.getTableRef().clearSelection();
      await onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "批量删除系统日志失败"), {
        type: "error"
      });
    }
  }

  async function clearAll() {
    try {
      const response = await clearManagerOperationLogs();
      if (!isSuccessResult(response)) {
        throw new Error(response?.message || "清空系统日志失败");
      }
      message("系统日志已清空", {
        type: "success"
      });
      await onSearch();
    } catch (error: any) {
      message(getErrorMessage(error, "清空系统日志失败"), {
        type: "error"
      });
    }
  }

  function onDetail(row: SystemLogRow) {
    addDialog({
      title: "系统请求详情",
      fullscreen: true,
      hideFooter: true,
      contentRenderer: () => Detail,
      props: {
        data: [row]
      }
    });
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
    onDetail,
    clearAll,
    resetForm,
    onbatchDel,
    handleSizeChange,
    onSelectionCancel,
    handleCellDblclick,
    handleCurrentChange,
    handleSelectionChange
  };
}
