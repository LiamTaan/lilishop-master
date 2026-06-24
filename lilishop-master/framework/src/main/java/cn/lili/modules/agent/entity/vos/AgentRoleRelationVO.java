package cn.lili.modules.agent.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 代理商身份视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class AgentRoleRelationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "代理商关系ID")
    private String id;

    @Schema(description = "会员ID")
    private String memberId;

    @Schema(description = "会员昵称")
    private String memberName;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "区域ID")
    private String regionId;

    @Schema(description = "区域名称")
    private String regionName;

    @Schema(description = "代理等级")
    private String agentLevel;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "生效时间")
    private Date effectiveTime;

    @Schema(description = "失效时间")
    private Date expireTime;
}
