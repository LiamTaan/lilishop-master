package cn.lili.modules.agent.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 代理商店铺绑定关系
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("agent_store_bind")
@Schema(description = "代理商店铺绑定")
public class AgentStoreBind extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "代理商会员ID")
    private String agentMemberId;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "区域ID")
    private String regionId;

    @Schema(description = "绑定状态")
    private String bindStatus;

    @Schema(description = "绑定时间")
    private Date bindTime;

    @Schema(description = "解绑时间")
    private Date unbindTime;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "业务备注")
    private String remark;
}
