package cn.lili.modules.verification.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 核销异常治理汇总
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class VerificationExceptionSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "异常核销总数")
    private Long exceptionCount;

    @Schema(description = "店铺端异常数")
    private Long storeExceptionCount;

    @Schema(description = "平台端异常数")
    private Long managerExceptionCount;

    @Schema(description = "买家端异常数")
    private Long buyerExceptionCount;
}
