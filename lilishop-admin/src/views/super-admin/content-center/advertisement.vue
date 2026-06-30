<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { utils, writeFile } from "xlsx";
import ImageUploadField from "@/components/ImageUploadField/index.vue";
import {
  getHomeAdvertisementConfig,
  saveHomeAdvertisementConfig
} from "@/api/content-operation";
import { extractApiPayload } from "@/utils/admin-governance";
import { message } from "@/utils/message";
import { isSuccessResult } from "@/utils/result";

defineOptions({
  name: "ContentAdvertisement"
});

type AdvertItem = {
  image: string;
  linkType: string;
  linkValue: string;
};

const clientTypeOptions = ["APP", "H5", "PC"];
const linkTypeOptions = [
  { label: "分类页", value: "CATEGORY" },
  { label: "商品详情", value: "GOODS_DETAIL" },
  { label: "优惠券", value: "COUPON" },
  { label: "专题页", value: "SPECIAL" },
  { label: "活动页", value: "ACTIVITY" },
  { label: "自定义链接", value: "CUSTOM_URL" }
];

const loading = ref(false);
const saving = ref(false);
const form = reactive({
  clientType: "APP",
  banners: [] as AdvertItem[],
  topAdvert: createAdvert()
});

const summaryCards = computed(() => [
  { label: "轮播图", value: form.banners.length, hint: "首页顶部宣传图" },
  { label: "顶部广告", value: form.topAdvert.image ? "已配置" : "未配置", hint: "首页单张广告位" },
  { label: "跳转类型", value: linkTypeOptions.length, hint: "支持多业务跳转" }
]);

function createAdvert(source?: Record<string, any>): AdvertItem {
  return {
    image: source?.image || "",
    linkType: source?.linkType || "GOODS_DETAIL",
    linkValue: source?.linkValue || source?.url || ""
  };
}

function addBanner() {
  form.banners.push(createAdvert());
}

function removeBanner(index: number) {
  form.banners.splice(index, 1);
}

async function loadData() {
  loading.value = true;
  try {
    const payload = extractApiPayload(await getHomeAdvertisementConfig(form.clientType)) || {};
    form.banners = Array.isArray(payload.banners)
      ? payload.banners.map((item: Record<string, any>) => createAdvert(item))
      : [];
    form.topAdvert = createAdvert(payload.topAdvert || {});
  } catch (_error) {
    form.banners = [];
    form.topAdvert = createAdvert();
    message("广告位加载失败，请确认管理端接口已启动", { type: "error" });
  } finally {
    loading.value = false;
  }
}

function buildPayload(publishNow: boolean) {
  return {
    clientType: form.clientType,
    publishNow,
    banners: form.banners
      .filter(item => item.image.trim())
      .map(item => ({
        image: item.image.trim(),
        url: item.linkValue.trim(),
        linkType: item.linkType,
        linkValue: item.linkValue.trim()
      })),
    topAdvert: form.topAdvert.image.trim()
      ? {
          image: form.topAdvert.image.trim(),
          url: form.topAdvert.linkValue.trim(),
          linkType: form.topAdvert.linkType,
          linkValue: form.topAdvert.linkValue.trim()
        }
      : undefined
  };
}

async function submit(publishNow = true) {
  const invalid = form.banners.find(item => !item.image.trim() || !item.linkValue.trim());
  if (invalid || (form.topAdvert.image.trim() && !form.topAdvert.linkValue.trim())) {
    message("请完整填写图片和跳转目标", { type: "warning" });
    return;
  }
  saving.value = true;
  try {
    const response = await saveHomeAdvertisementConfig(buildPayload(publishNow));
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || "广告位保存失败");
    }
    message("广告位保存成功", { type: "success" });
    await loadData();
  } catch (error: any) {
    message(error?.message || "广告位保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

function exportRows() {
  const rows = [
    ...form.banners.map((item, index) => ({
      位置: `轮播图${index + 1}`,
      图片: item.image,
      跳转类型: item.linkType,
      跳转目标: item.linkValue
    })),
    {
      位置: "顶部广告",
      图片: form.topAdvert.image,
      跳转类型: form.topAdvert.linkType,
      跳转目标: form.topAdvert.linkValue
    }
  ].filter(item => item.图片);
  if (!rows.length) {
    message("暂无可导出的广告位", { type: "warning" });
    return;
  }
  const workbook = utils.book_new();
  utils.book_append_sheet(workbook, utils.json_to_sheet(rows), "广告位管理");
  writeFile(workbook, "广告位管理.xlsx");
}

onMounted(loadData);
</script>

<template>
  <div v-loading="loading" class="operation-page">
    <section class="operation-toolbar">
      <div>
        <h1>广告位管理</h1>
        <p>维护首页顶部轮播和首页广告位，广告内容归内容与运营管理。</p>
      </div>
      <div class="operation-actions">
        <el-select v-model="form.clientType" style="width: 120px" @change="loadData">
          <el-option v-for="item in clientTypeOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button @click="loadData">刷新</el-button>
        <el-button @click="exportRows">导出</el-button>
        <el-button type="primary" :loading="saving" @click="submit(true)">保存</el-button>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </article>
    </section>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>首页顶部轮播</span>
          <el-button type="primary" plain @click="addBanner">新增轮播</el-button>
        </div>
      </template>
      <el-empty v-if="!form.banners.length" description="暂无轮播图" />
      <div v-else class="editor-list">
        <article v-for="(item, index) in form.banners" :key="index" class="editor-card">
          <div class="editor-title">
            <strong>轮播图 {{ index + 1 }}</strong>
            <el-button link type="danger" @click="removeBanner(index)">删除</el-button>
          </div>
          <div class="form-grid">
            <el-form-item label="图片">
              <ImageUploadField v-model="item.image" tip="上传广告图片" />
            </el-form-item>
            <el-form-item label="跳转类型">
              <el-select v-model="item.linkType" style="width: 100%">
                <el-option v-for="option in linkTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="跳转目标">
              <el-input v-model="item.linkValue" placeholder="请输入目标ID或链接" />
            </el-form-item>
          </div>
        </article>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header>首页广告位</template>
      <div class="form-grid">
        <el-form-item label="图片">
          <ImageUploadField v-model="form.topAdvert.image" tip="上传首页广告图" />
        </el-form-item>
        <el-form-item label="跳转类型">
          <el-select v-model="form.topAdvert.linkType" style="width: 100%">
            <el-option v-for="option in linkTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="跳转目标">
          <el-input v-model="form.topAdvert.linkValue" placeholder="请输入目标ID或链接" />
        </el-form-item>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.operation-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
}

.operation-toolbar,
.card-header,
.operation-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.operation-toolbar h1 {
  margin: 0 0 6px;
  font-size: 22px;
}

.operation-toolbar p {
  margin: 0;
  color: #6b7280;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px;
  background: #fff;
}

.summary-card strong {
  display: block;
  margin: 8px 0 4px;
  font-size: 22px;
}

.summary-card span,
.summary-card small {
  color: #6b7280;
}

.editor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.editor-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
}

.editor-title {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

@media (max-width: 960px) {
  .operation-toolbar,
  .operation-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .summary-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
