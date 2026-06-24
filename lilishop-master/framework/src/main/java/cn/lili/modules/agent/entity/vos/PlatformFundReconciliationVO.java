package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 平台资金对账视图
 *
 * @author dawn
 * @since 2026/6/19
 */
@Data
public class PlatformFundReconciliationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "流水ID")
    private String id;

    @Schema(description = "代理商会员ID")
    private String agentMemberId;

    @Schema(description = "代理商名称")
    private String agentName;

    @Schema(description = "业务类型")
    private String serviceType;

    @Schema(description = "变动金额")
    private Double money;

    @Schema(description = "业务描述")
    private String detail;

    @Schema(description = "创建时间")
    private Date createTime;
}
