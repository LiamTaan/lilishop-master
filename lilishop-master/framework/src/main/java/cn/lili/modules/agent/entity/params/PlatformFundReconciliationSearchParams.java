package cn.lili.modules.agent.entity.params;

import cn.lili.common.vo.PageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 平台资金对账查询参数
 *
 * @author dawn
 * @since 2026/6/19
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlatformFundReconciliationSearchParams extends PageVO {

    @Schema(description = "关键词，支持代理商名称/会员ID/业务描述")
    private String keyword;

    @Schema(description = "资金流水业务类型")
    private String serviceType;

    @Schema(description = "代理商会员ID")
    private String agentMemberId;

    @Schema(description = "开始日期，格式 yyyy-MM-dd")
    private String startDate;

    @Schema(description = "结束日期，格式 yyyy-MM-dd")
    private String endDate;
}
