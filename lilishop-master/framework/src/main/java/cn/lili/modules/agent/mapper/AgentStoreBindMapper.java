package cn.lili.modules.agent.mapper;

import cn.lili.modules.agent.entity.dos.AgentStoreBind;
import cn.lili.modules.agent.entity.vos.AgentStoreBindVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 代理商店铺绑定数据层
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface AgentStoreBindMapper extends BaseMapper<AgentStoreBind> {

    /**
     * 查询绑定列表
     *
     * @return 绑定列表
     */
    @Select("SELECT asb.id,asb.agent_member_id,asb.store_id,asb.store_name,asb.region_id,asb.bind_status,asb.audit_status,asb.audit_remark,asb.remark,asb.bind_time,asb.unbind_time," +
            "m.nick_name AS agent_member_name,r.name AS region_name " +
            "FROM agent_store_bind asb " +
            "LEFT JOIN li_member m ON asb.agent_member_id = m.id " +
            "LEFT JOIN li_region r ON asb.region_id = r.ad_code AND r.level = 'district' " +
            "WHERE asb.delete_flag = 0 ORDER BY asb.create_time DESC")
    List<AgentStoreBindVO> listAgentStoreBinds();

    /**
     * 查询代理商已审核通过的绑定店铺
     *
     * @param agentMemberId 代理商会员ID
     * @return 绑定列表
     */
    @Select("SELECT asb.id,asb.agent_member_id,asb.store_id,asb.store_name,asb.region_id,asb.bind_status,asb.audit_status,asb.audit_remark,asb.remark,asb.bind_time,asb.unbind_time," +
            "m.nick_name AS agent_member_name,r.name AS region_name " +
            "FROM agent_store_bind asb " +
            "LEFT JOIN li_member m ON asb.agent_member_id = m.id " +
            "LEFT JOIN li_region r ON asb.region_id = r.ad_code AND r.level = 'district' " +
            "WHERE asb.delete_flag = 0 AND asb.agent_member_id = #{agentMemberId} AND asb.audit_status = 'APPROVED' AND asb.bind_status = 'BOUND' ORDER BY asb.create_time DESC")
    List<AgentStoreBindVO> listApprovedBindsByAgentMemberId(String agentMemberId);
}
