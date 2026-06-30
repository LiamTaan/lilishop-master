<script setup lang="ts">
import { computed, ref } from "vue";
import type { UploadRequestOptions } from "element-plus";
import { uploadCommonFile } from "@/api/common";
import { message } from "@/utils/message";
import { isSuccessResult, unwrapResult } from "@/utils/result";

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    tip?: string;
    disabled?: boolean;
    limitMb?: number;
    directoryPath?: string;
  }>(),
  {
    modelValue: "",
    tip: "支持 JPG / PNG / WEBP，上传后自动回填资源地址",
    disabled: false,
    limitMb: 5,
    directoryPath: "default"
  }
);

const emit = defineEmits<{
  "update:modelValue": [value: string];
  change: [value: string];
}>();

const uploading = ref(false);

function normalizePreviewUrl(value: string) {
  const trimmed = value.trim();
  if (!trimmed) return "";
  if (/^(https?:)?\/\//i.test(trimmed) || trimmed.startsWith("data:")) {
    return trimmed;
  }
  try {
    return new URL(trimmed, window.location.origin).toString();
  } catch {
    return trimmed;
  }
}

const previewUrl = computed(() => normalizePreviewUrl(props.modelValue?.trim() || ""));

function updateValue(value: string) {
  emit("update:modelValue", value);
  emit("change", value);
}

function beforeUpload(rawFile: File) {
  const isImage = rawFile.type.startsWith("image/");
  if (!isImage) {
    message("请上传图片文件", { type: "warning" });
    return false;
  }
  const isWithinLimit = rawFile.size / 1024 / 1024 < props.limitMb;
  if (!isWithinLimit) {
    message(`图片大小不能超过 ${props.limitMb}MB`, { type: "warning" });
    return false;
  }
  return true;
}

async function uploadImage(options: UploadRequestOptions) {
  const formData = new FormData();
  formData.append("file", options.file);
  uploading.value = true;
  try {
    const response = await uploadCommonFile(formData, props.directoryPath);
    if (!isSuccessResult(response)) {
      throw new Error(response?.message || "上传失败");
    }
    const nextValue = String(unwrapResult(response) || "").trim();
    if (!nextValue) {
      throw new Error("上传成功但未返回图片地址");
    }
    updateValue(nextValue);
    options.onSuccess?.(response);
    message("图片上传成功", { type: "success" });
  } catch (error: any) {
    options.onError?.(error);
    message(error?.message || "图片上传失败", { type: "error" });
  } finally {
    uploading.value = false;
  }
}

function clearValue() {
  updateValue("");
}
</script>

<template>
  <div class="image-upload-field">
    <div class="image-upload-field__controls">
      <el-upload
        action="#"
        :show-file-list="false"
        :disabled="props.disabled || uploading"
        :before-upload="beforeUpload"
        :http-request="uploadImage"
      >
        <el-button type="primary" plain :loading="uploading">
          {{ previewUrl ? "重新上传" : "上传图片" }}
        </el-button>
      </el-upload>
      <el-button
        v-if="previewUrl"
        text
        type="danger"
        :disabled="props.disabled || uploading"
        @click="clearValue"
      >
        清空
      </el-button>
    </div>
    <p class="image-upload-field__tip">{{ props.tip }}</p>
    <div v-if="previewUrl" class="image-upload-field__preview">
      <img :src="previewUrl" alt="uploaded" class="image-upload-field__img" />
      <el-input :model-value="previewUrl" readonly />
    </div>
  </div>
</template>

<style scoped>
.image-upload-field {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.image-upload-field__controls {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.image-upload-field__tip {
  margin: 0;
  color: #8a8f97;
  font-size: 12px;
  line-height: 1.6;
}

.image-upload-field__preview {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border: 1px dashed #f0d3ad;
  border-radius: 14px;
  background: #fff9f1;
}

.image-upload-field__img {
  width: 100%;
  max-width: 220px;
  max-height: 160px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid #f3e1c8;
}
</style>
