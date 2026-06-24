package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.dos.AgentRoleRelation;
import cn.lili.modules.agent.entity.dto.AgentRoleCreateDTO;
import cn.lili.modules.agent.entity.vos.AgentRoleRelationVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 代理商角色关系业务层
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface AgentRoleRelationService extends IService<AgentRoleRelation> {

    /**
     * 创建代理商身份
     *
     * @param dto 创建请求
     * @return 代理商关系
     */
    AgentRoleRelation createAgentRole(AgentRoleCreateDTO dto);

    /**
     * 按会员补齐代理身份，存在则返回已存在身份
     *
     * @param dto 创建请求
     * @return 代理商关系
     */
    AgentRoleRelation ensureAgentRole(AgentRoleCreateDTO dto);

    /**
     * 查询代理商列表
     *
     * @return 代理商列表
     */
    List<AgentRoleRelationVO> listAgentRoles();

    /**
     * 获取当前会员启用中的代理身份
     *
     * @param memberId 会员ID
     * @return 代理身份
     */
    AgentRoleRelation getEnabledRoleByMemberId(String memberId);

    /**
     * 获取当前会员最近一条代理身份记录
     *
     * @param memberId 会员ID
     * @return 代理身份
     */
    AgentRoleRelation getLatestRoleByMemberId(String memberId);

    /**
     * 判断当前会员是否为代理商
     *
     * @param memberId 会员ID
     * @return 是否代理商
     */
    boolean isAgent(String memberId);
}
