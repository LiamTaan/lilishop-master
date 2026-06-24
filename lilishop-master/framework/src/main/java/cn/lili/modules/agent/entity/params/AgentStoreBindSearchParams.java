package cn.lili.modules.agent.entity.params;

import cn.lili.common.vo.PageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代理商店铺绑定分页查询参数
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "代理商店铺绑定分页查询参数")
public class AgentStoreBindSearchParams extends PageVO {

    @Schema(description = "代理商会员ID")
    private String agentMemberId;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "区域ID")
    private String regionId;

    @Schema(description = "绑定状态")
    private String bindStatus;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "关键字，支持店铺名称或代理商昵称")
    private String keyword;
}
