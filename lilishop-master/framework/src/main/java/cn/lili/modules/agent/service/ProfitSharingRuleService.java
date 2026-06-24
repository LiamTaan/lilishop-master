package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.dos.ProfitSharingRule;
import cn.lili.modules.agent.entity.dto.ProfitSharingRuleDTO;
import cn.lili.modules.agent.entity.vos.ProfitSharingRulePageVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 平台分账规则业务接口
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface ProfitSharingRuleService extends IService<ProfitSharingRule> {

    /**
     * 分页查询
     *
     * @param page 页参数
     * @return 分页结果
     */
    IPage<ProfitSharingRulePageVO> page(cn.lili.common.vo.PageVO page);

    /**
     * 新增规则
     *
     * @param dto 请求
     * @return 规则
     */
    ProfitSharingRule create(ProfitSharingRuleDTO dto);

    /**
     * 修改规则
     *
     * @param id 规则ID
     * @param dto 请求
     * @return 规则
     */
    ProfitSharingRule update(String id, ProfitSharingRuleDTO dto);

    /**
     * 删除规则
     *
     * @param id 规则ID
     */
    void delete(String id);
}
