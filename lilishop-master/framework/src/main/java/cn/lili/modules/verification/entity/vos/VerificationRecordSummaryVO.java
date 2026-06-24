package cn.lili.modules.verification.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 核销记录汇总视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class VerificationRecordSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "核销记录总数")
    private Long totalCount;

    @Schema(description = "成功核销数")
    private Long successCount;

    @Schema(description = "失败核销数")
    private Long failCount;

    @Schema(description = "异常核销数")
    private Long exceptionCount;

    @Schema(description = "买家端发起数")
    private Long buyerSourceCount;

    @Schema(description = "店铺端发起数")
    private Long storeSourceCount;

    @Schema(description = "平台端发起数")
    private Long managerSourceCount;
}
