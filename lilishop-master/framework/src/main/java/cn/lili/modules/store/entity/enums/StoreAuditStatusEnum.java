package cn.lili.modules.store.entity.enums;

/**
 * 店铺审核状态
 *
 * @author dawn
 * @since 2026/6/17
 */
public enum StoreAuditStatusEnum {
    /**
     * 草稿
     */
    DRAFT,
    /**
     * 待审核
     */
    SUBMITTED,
    /**
     * 审核通过
     */
    APPROVED,
    /**
     * 审核驳回
     */
    REJECTED,
    /**
     * 已冻结
     */
    FROZEN
}
