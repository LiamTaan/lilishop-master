package cn.lili.modules.store.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺治理汇总视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class StoreAuditSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺总数")
    private Long totalCount;

    @Schema(description = "待审核数")
    private Long submittedCount;

    @Schema(description = "审核通过数")
    private Long approvedCount;

    @Schema(description = "审核驳回数")
    private Long rejectedCount;

    @Schema(description = "冻结数")
    private Long frozenCount;

    @Schema(description = "营业中数")
    private Long openCount;

    @Schema(description = "关闭数")
    private Long closedCount;
}
