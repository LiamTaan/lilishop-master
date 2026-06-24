package cn.lili.modules.agent.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 代理商角色关系
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("agent_role_relation")
@Schema(description = "代理商角色关系")
public class AgentRoleRelation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会员ID")
    private String memberId;

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

    @Schema(description = "生效时间")
    private Date effectiveTime;

    @Schema(description = "失效时间")
    private Date expireTime;

    @Schema(description = "备注")
    private String remark;
}
