package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.dos.AgentStoreBind;
import cn.lili.modules.agent.entity.dto.AgentStoreBindAuditDTO;
import cn.lili.modules.agent.entity.dto.AgentStoreBindDTO;
import cn.lili.modules.agent.entity.params.AgentStoreBindSearchParams;
import cn.lili.modules.agent.entity.vos.AgentStoreBindVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 代理商店铺绑定业务层
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface AgentStoreBindService extends IService<AgentStoreBind> {

    /**
     * 创建代理商店铺绑定
     *
     * @param dto 绑定请求
     * @return 绑定关系
     */
    AgentStoreBind createBind(AgentStoreBindDTO dto);

    /**
     * 审核绑定关系
     *
     * @param bindId 绑定ID
     * @param dto 审核请求
     * @return 更新后的绑定关系
     */
    AgentStoreBind auditBind(String bindId, AgentStoreBindAuditDTO dto);

    /**
     * 解绑店铺
     *
     * @param bindId 绑定ID
     * @return 操作结果
     */
    boolean unbind(String bindId);

    /**
     * 查询绑定列表
     *
     * @return 列表
     */
    IPage<AgentStoreBindVO> pageAgentStoreBinds(AgentStoreBindSearchParams searchParams);

    /**
     * 查询当前代理商绑定店铺
     *
     * @param agentMemberId 代理商会员ID
     * @return 绑定列表
     */
    List<AgentStoreBindVO> listApprovedBindsByAgentMemberId(String agentMemberId);

    /**
     * 查询代理商已绑定店铺ID
     *
     * @param agentMemberId 代理商会员ID
     * @return 店铺ID集合
     */
    List<String> listApprovedStoreIdsByAgentMemberId(String agentMemberId);
}
