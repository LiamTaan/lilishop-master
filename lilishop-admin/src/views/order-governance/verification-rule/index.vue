<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { getVerificationRuleSetting, saveVerificationRuleSetting } from "@/api/order-governance";
import { extractApiPayload } from "@/utils/admin-governance";
import { message } from "@/utils/message";

defineOptions({
  name: "VerificationRuleManage"
});

const loading = ref(false);
const saving = ref(false);
const form = reactive({
  pickupEnabled: true,
  virtualVerificationEnabled: true,
  buyerShowVerificationButton: true,
  buyerShowVerificationCode: true,
  verificationCodeLength: 12,
  ruleRemark: ""
});

const summaryCards = computed(() => [
  {
    label: "自提核销",
    value: form.pickupEnabled ? "启用" : "停用",
    accent: form.pickupEnabled ? "green" : "blue"
  },
  {
    label: "虚拟核验",
    value: form.virtualVerificationEnabled ? "启用" : "停用",
    accent: form.virtualVerificationEnabled ? "green" : "blue"
  },
  {
    label: "买家核销入口",
    value: form.buyerShowVerificationButton ? "展示" : "隐藏",
    accent: form.buyerShowVerificationButton ? "orange" : "blue"
  },
  {
    label: "核销码长度",
    value: `${form.verificationCodeLength} 位`,
    accent: "purple"
  }
]);

async function loadSetting() {
  loading.value = true;
  try {
    const res = await getVerificationRuleSetting();
    const payload = extractApiPayload<Record<string, any>>(res) || {};
    form.pickupEnabled = payload.pickupEnabled ?? true;
    form.virtualVerificationEnabled = payload.virtualVerificationEnabled ?? true;
    form.buyerShowVerificationButton =
      payload.buyerShowVerificationButton ?? true;
    form.buyerShowVerificationCode = payload.buyerShowVerificationCode ?? true;
    form.verificationCodeLength = Number(payload.verificationCodeLength ?? 12);
    form.ruleRemark = payload.ruleRemark || "";
  } catch (_error) {
    message("核销规则加载失败，已使用默认值", { type: "warning" });
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  saving.value = true;
  try {
    await saveVerificationRuleSetting({
      pickupEnabled: form.pickupEnabled,
      virtualVerificationEnabled: form.virtualVerificationEnabled,
      buyerShowVerificationButton: form.buyerShowVerificationButton,
      buyerShowVerificationCode: form.buyerShowVerificationCode,
      verificationCodeLength: form.verificationCodeLength,
      ruleRemark: form.ruleRemark
    });
    message("核销规则保存成功", { type: "success" });
  } catch (_error) {
    message("核销规则保存失败", { type: "error" });
  } finally {
    saving.value = false;
  }
}

onMounted(loadSetting);
</script>

<template>
  <div v-loading="loading" class="verification-rule-page">
    <section class="page-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">核销规则</h2>
          <p class="page-desc">
            平台统一维护自提订单与虚拟核验订单的履约规则，物流订单仍走确认收货链路。
          </p>
        </div>
        <div class="page-header__actions">
          <el-button @click="loadSetting">刷新</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存规则
          </el-button>
        </div>
      </div>
      <div class="summary-grid">
        <div
          v-for="card in summaryCards"
          :key="card.label"
          class="summary-card"
          :data-accent="card.accent"
        >
          <div class="summary-card__label">{{ card.label }}</div>
          <div class="summary-card__value">{{ card.value }}</div>
        </div>
      </div>
    </section>

    <section class="page-card">
      <h3 class="section-title">规则配置</h3>
      <el-form label-width="180px" class="rule-form">
        <el-form-item label="启用自提核销链">
          <el-switch v-model="form.pickupEnabled" />
          <span class="field-hint">支付成功后订单进入待核销，由店铺端完成最终核销。</span>
        </el-form-item>
        <el-form-item label="启用虚拟核验链">
          <el-switch v-model="form.virtualVerificationEnabled" />
          <span class="field-hint">虚拟订单进入待核销流程，核销成功后订单完成。</span>
        </el-form-item>
        <el-form-item label="展示买家核销入口">
          <el-switch v-model="form.buyerShowVerificationButton" />
          <span class="field-hint">买家端显示“立即核销”入口，但不直接完成订单。</span>
        </el-form-item>
        <el-form-item label="展示买家取件码">
          <el-switch v-model="form.buyerShowVerificationCode" />
          <span class="field-hint">自提订单详情可展示提货码与自提点信息。</span>
        </el-form-item>
        <el-form-item label="核销码长度">
          <el-input-number
            v-model="form.verificationCodeLength"
            :min="6"
            :max="18"
            controls-position="right"
          />
          <span class="field-hint">后端生成核销码时会按此长度截取，默认 12 位。</span>
        </el-form-item>
        <el-form-item label="平台规则说明">
          <el-input
            v-model="form.ruleRemark"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="请输入平台核销规则说明"
          />
        </el-form-item>
      </el-form>
    </section>

    <section class="page-card">
      <h3 class="section-title">状态流转</h3>
      <div class="flow-grid">
        <el-alert
          title="物流订单"
          type="info"
          :closable="false"
          description="PAID / 发货中 / 运输中 / 派送中 -> DELIVERED -> COMPLETED，仅在 DELIVERED 时允许确认收货。"
        />
        <el-alert
          title="自提订单"
          type="success"
          :closable="false"
          description="PAID -> STAY_PICKED_UP -> COMPLETED，买家侧显示待核销与取件码，店铺端完成核销。"
        />
        <el-alert
          title="虚拟核验订单"
          type="warning"
          :closable="false"
          description="PAID -> TAKE -> COMPLETED，统一按待核销展示，不再混入物流收货链路。"
        />
      </div>
    </section>

    <section class="page-card">
      <h3 class="section-title">职责说明</h3>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="买家端">
          仅进入“立即核销 / 出示取件码”页面，不直接把订单置完成。
        </el-descriptions-item>
        <el-descriptions-item label="店铺端">
          负责根据核销码执行真实核销，核销成功后订单完成并写入核销记录。
        </el-descriptions-item>
        <el-descriptions-item label="平台端">
          负责维护核销规则与查看核销记录，不再把验证码资源误当作核销规则。
        </el-descriptions-item>
      </el-descriptions>
    </section>
  </div>
</template>

<style scoped>
.verification-rule-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-card {
  padding: 20px;
  background: #fff;
  border: 1px solid var(--wholesale-card-border);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgb(21 26 38 / 4%);
}

.page-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 18px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.page-desc {
  margin: 8px 0 0;
  color: #667085;
  line-height: 1.6;
}

.page-header__actions {
  display: flex;
  gap: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.summary-card {
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
}

.summary-card[data-accent="green"] {
  background: #edfdf3;
}

.summary-card[data-accent="orange"] {
  background: #fff7ed;
}

.summary-card[data-accent="purple"] {
  background: #f5f3ff;
}

.summary-card__label {
  color: #667085;
  font-size: 13px;
}

.summary-card__value {
  margin-top: 10px;
  color: #111827;
  font-size: 22px;
  font-weight: 700;
}

.section-title {
  margin: 0 0 18px;
  font-size: 18px;
  color: #1f2937;
}

.rule-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.field-hint {
  margin-left: 12px;
  color: #667085;
  font-size: 13px;
}

.flow-grid {
  display: grid;
  gap: 14px;
}

@media (max-width: 900px) {
  .page-header {
    flex-direction: column;
  }

  .page-header__actions {
    width: 100%;
    justify-content: flex-end;
  }

  .field-hint {
    display: block;
    margin: 8px 0 0;
  }
}
</style>
