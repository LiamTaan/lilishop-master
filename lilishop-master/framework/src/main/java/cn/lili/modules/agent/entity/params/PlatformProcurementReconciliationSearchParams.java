package cn.lili.modules.agent.entity.params;

import cn.lili.common.vo.PageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 平台采购对账查询参数
 *
 * @author dawn
 * @since 2026/6/19
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlatformProcurementReconciliationSearchParams extends PageVO {

    @Schema(description = "关键词，支持采购单号/代理商名称/店铺名称")
    private String keyword;

    @Schema(description = "采购状态")
    private String status;

    @Schema(description = "代理商会员ID")
    private String agentMemberId;
}
