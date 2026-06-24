package cn.lili.modules.agent.entity.params;

import cn.lili.common.vo.PageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代理商资金对账查询参数
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AgentFundReconciliationSearchParams extends PageVO {

    @Schema(description = "资金流水业务类型")
    private String serviceType;

    @Schema(description = "开始日期，格式 yyyy-MM-dd")
    private String startDate;

    @Schema(description = "结束日期，格式 yyyy-MM-dd")
    private String endDate;
}
