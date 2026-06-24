package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 代理商店铺绑定视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentStoreBindVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "绑定关系ID")
    private String id;

    @Schema(description = "代理商会员ID")
    private String agentMemberId;

    @Schema(description = "代理商会员昵称")
    private String agentMemberName;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "区域ID")
    private String regionId;

    @Schema(description = "区域名称")
    private String regionName;

    @Schema(description = "绑定状态")
    private String bindStatus;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "业务备注")
    private String remark;

    @Schema(description = "绑定时间")
    private Date bindTime;

    @Schema(description = "解绑时间")
    private Date unbindTime;
}
