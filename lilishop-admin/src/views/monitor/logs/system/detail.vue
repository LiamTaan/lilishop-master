<script setup lang="tsx">
import { ref } from "vue";
import "vue-json-pretty/lib/styles.css";
import VueJsonPretty from "vue-json-pretty";

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
});

const summaryData = (props.data[0] as any) || {};

const columns = [
  {
    label: "操作人员",
    prop: "username"
  },
  {
    label: "操作名称",
    prop: "name"
  },
  {
    label: "请求方式",
    prop: "requestType"
  },
  {
    label: "IP 地址",
    prop: "ip"
  },
  {
    label: "附加信息",
    prop: "ipInfo"
  },
  {
    label: "请求时间",
    prop: "createTime"
  },
  {
    label: "请求耗时",
    prop: "costTime"
  },
  {
    label: "请求接口",
    prop: "requestUrl",
    copy: true
  }
];

function tryParseJson(value: unknown) {
  if (!value) return value;
  if (typeof value !== "string") return value;
  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
}

const dataList = ref([
  {
    title: "业务说明",
    name: "customerLog",
    data: tryParseJson(summaryData.customerLog)
  },
  {
    title: "请求参数",
    name: "requestParam",
    data: tryParseJson(summaryData.requestParam)
  },
  {
    title: "响应体",
    name: "responseBody",
    data: tryParseJson(summaryData.responseBody)
  }
]);

const summaryRows = [summaryData];
</script>

<template>
  <div class="system-log-detail">
    <div class="system-log-detail__hero">
      <div>
        <div class="system-log-detail__eyebrow">平台监控中心</div>
        <h3>系统请求详情</h3>
        <p>查看单次请求的操作人、请求接口、请求参数与响应体，辅助排查异常接口和慢请求。</p>
      </div>
      <div class="system-log-detail__meta">
        <div class="system-log-detail__meta-item">
          <span>操作人员</span>
          <strong>{{ summaryData.username || "-" }}</strong>
        </div>
        <div class="system-log-detail__meta-item">
          <span>请求耗时</span>
          <strong>{{ summaryData.costTime || "-" }} ms</strong>
        </div>
        <div class="system-log-detail__meta-item">
          <span>请求方式</span>
          <strong>{{ summaryData.requestType || "-" }}</strong>
        </div>
      </div>
    </div>
    <el-scrollbar>
      <PureDescriptions
        border
        :data="summaryRows"
        :columns="columns"
        :column="4"
      />
    </el-scrollbar>
    <el-tabs :modelValue="'requestParam'" type="border-card" class="mt-4">
      <el-tab-pane
        v-for="(item, index) in dataList"
        :key="index"
        :name="item.name"
        :label="item.title"
      >
        <el-scrollbar max-height="calc(100vh - 240px)">
          <vue-json-pretty v-model:data="item.data" />
        </el-scrollbar>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.system-log-detail {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.system-log-detail__hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #f2e6d9;
  border-radius: 18px;
  background: linear-gradient(135deg, #fff8ef 0%, #fff 100%);
}

.system-log-detail__eyebrow {
  margin-bottom: 8px;
  color: #d97706;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
}

.system-log-detail__hero h3 {
  margin: 0 0 10px;
  color: #2d2f33;
  font-size: 24px;
  font-weight: 700;
}

.system-log-detail__hero p {
  margin: 0;
  max-width: 780px;
  color: #7a8088;
  font-size: 13px;
  line-height: 1.7;
}

.system-log-detail__meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  min-width: 440px;
}

.system-log-detail__meta-item {
  padding: 14px 16px;
  border: 1px solid #f2e6d9;
  border-radius: 14px;
  background: #fff;
}

.system-log-detail__meta-item span {
  display: block;
  margin-bottom: 6px;
  color: #8c9098;
  font-size: 12px;
}

.system-log-detail__meta-item strong {
  color: #2d2f33;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

@media (width <= 1100px) {
  .system-log-detail__hero {
    flex-direction: column;
  }

  .system-log-detail__meta {
    min-width: auto;
    grid-template-columns: 1fr;
  }
}
</style>
