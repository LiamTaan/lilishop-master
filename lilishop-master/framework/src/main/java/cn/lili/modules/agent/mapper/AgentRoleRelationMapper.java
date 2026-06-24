package cn.lili.modules.agent.mapper;

import cn.lili.modules.agent.entity.dos.AgentRoleRelation;
import cn.lili.modules.agent.entity.vos.AgentRoleRelationVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 代理商角色关系数据层
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface AgentRoleRelationMapper extends BaseMapper<AgentRoleRelation> {

    /**
     * 查询代理商列表
     *
     * @return 代理商列表
     */
    @Select("SELECT arr.id,arr.member_id,arr.role_code,arr.region_id,arr.region_name,arr.agent_level,arr.status,arr.remark,arr.effective_time,arr.expire_time,m.nick_name AS member_name,m.mobile " +
            "FROM agent_role_relation arr LEFT JOIN li_member m ON arr.member_id = m.id WHERE arr.delete_flag = 0 ORDER BY arr.create_time DESC")
    List<AgentRoleRelationVO> listAgentRoles();
}
