import { computed, onMounted, reactive, ref } from "vue";
import {
  extractApiPayload,
  extractApiRecords
} from "@/utils/admin-governance";

export type SuperAdminPageConfig = {
  pageTitle: string;
  keywordField?: string;
  statusField?: string;
  statusOptions?: Array<{ label: string; value: string }>;
  listApi: (params?: Record<string, any>) => Promise<any>;
  normalizeRecord?: (item: Record<string, any>) => Record<string, any>;
  detailFields?: Array<{ key: string; label: string }>;
  primaryKey?: string;
};

export function createDefaultColumns(detailLabel = "详情"): TableColumnList {
  return [
    {
      label: "名称",
      prop: "displayName",
      minWidth: 220
    },
    {
      label: "状态",
      prop: "displayStatus",
      minWidth: 140
    },
    {
      label: "更新时间",
      prop: "displayTime",
      minWidth: 180
    },
    {
      label: "备注",
      prop: "displayRemark",
      minWidth: 220,
      showOverflowTooltip: true
    },
    {
      label: "操作",
      prop: "operation",
      fixed: "right",
      width: 200,
      slot: "operation"
    }
  ];
}

function findFirstDefined(
  item: Record<string, any>,
  keys: string[],
  fallback = "-"
) {
  for (const key of keys) {
    const value = item?.[key];
    if (value !== undefined && value !== null && value !== "") {
      return value;
    }
  }
  return fallback;
}

export function normalizeSuperAdminRecord(
  item: Record<string, any>,
  overrides?: (item: Record<string, any>) => Record<string, any>
) {
  const base = {
    ...item,
    id: findFirstDefined(item, [
      "id",
      "memberId",
      "gradeId",
      "groupId",
      "templateId",
      "signId",
      "versionId",
      "articleId",
      "regionId"
    ]),
    displayName: String(
      findFirstDefined(item, [
        "name",
        "title",
        "memberName",
        "nickname",
        "signName",
        "templateName",
        "version",
        "articleTitle",
        "reason"
      ])
    ),
    displayStatus: String(
      findFirstDefined(item, [
        "status",
        "auditStatus",
        "marketEnable",
        "verifyStatus",
        "settlementStatus",
        "disabled",
        "openStatus"
      ])
    ),
    displayTime: String(
      findFirstDefined(item, [
        "updateTime",
        "modifiedTime",
        "createTime",
        "gmtModified",
        "lastModifiedDate"
      ])
    ),
    displayRemark: String(
      findFirstDefined(item, [
        "remark",
        "description",
        "content",
        "reason",
        "operatorRemark"
      ])
    )
  };
  return overrides ? { ...base, ...overrides(item) } : base;
}

export function useSuperAdminPage(config: SuperAdminPageConfig) {
  const rows = ref<Record<string, any>[]>([]);
  const loading = ref(false);
  const detailVisible = ref(false);
  const currentRow = ref<Record<string, any> | null>(null);
  const query = reactive({
    keyword: "",
    status: ""
  });

  const keywordField = config.keywordField || "keyword";
  const statusField = config.statusField || "status";
  async function loadData() {
    loading.value = true;
    try {
      const params: Record<string, any> = {};
      if (query.keyword) params[keywordField] = query.keyword;
      if (query.status) params[statusField] = query.status;
      const result = await config.listApi(params);
      rows.value = extractApiRecords(result).map(item =>
        normalizeSuperAdminRecord(item, config.normalizeRecord)
      );
    } finally {
      loading.value = false;
    }
  }

  function handleSearch(payload: { keyword: string; status: string }) {
    query.keyword = payload.keyword;
    query.status = payload.status;
    loadData();
  }

  function handleReset() {
    query.keyword = "";
    query.status = "";
    loadData();
  }

  function showDetail(row: Record<string, any>) {
    currentRow.value = row;
    detailVisible.value = true;
  }

  const summaryCards = computed(() => [
    {
      label: "记录总数",
      value: rows.value.length,
      accent: "orange" as const,
      hint: "当前筛选结果"
    },
    {
      label: "已启用/有效",
      value: rows.value.filter(item =>
        ["ENABLE", "ENABLED", "OPEN", "UP", "APPROVED", "NORMAL", "true", "1"]
          .map(flag => flag.toUpperCase())
          .some(flag =>
            String(item.displayStatus).toUpperCase().includes(flag)
          )
      ).length,
      accent: "green" as const,
      hint: "当前已生效记录"
    },
    {
      label: "待处理/待审",
      value: rows.value.filter(item =>
        ["PENDING", "WAIT", "AUDIT", "INIT", "NEW"].some(flag =>
          String(item.displayStatus).toUpperCase().includes(flag)
        )
      ).length,
      accent: "blue" as const,
      hint: "待后续治理动作"
    },
    {
      label: "联调状态",
      value: "已挂接",
      accent: "purple" as const,
      hint: "接口已进入 API 层"
    }
  ]);

  const detailEntries = computed(() => {
    const source = currentRow.value || {};
    if (config.detailFields?.length) {
      return config.detailFields.map(item => ({
        label: item.label,
        value: source[item.key] ?? "-"
      }));
    }
    return Object.keys(source)
      .slice(0, 12)
      .map(key => ({ label: key, value: source[key] ?? "-" }));
  });

  onMounted(() => {
    loadData();
  });

  return {
    rows,
    loading,
    query,
    summaryCards,
    detailVisible,
    currentRow,
    detailEntries,
    statusOptions: config.statusOptions || [
      { label: "启用/有效", value: "ENABLE" },
      { label: "停用/无效", value: "DISABLE" },
      { label: "待处理", value: "PENDING" }
    ],
    handleSearch,
    handleReset,
    loadData,
    showDetail,
  };
}

export function unwrapApiObject(response: any) {
  return extractApiPayload<Record<string, any>>(response) || {};
}
