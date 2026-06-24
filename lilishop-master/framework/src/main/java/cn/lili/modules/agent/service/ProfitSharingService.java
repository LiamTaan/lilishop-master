package cn.lili.modules.agent.service;

import cn.lili.modules.agent.entity.params.ProfitSharingRecordSearchParams;
import cn.lili.modules.agent.entity.vos.ProfitSharingBalanceVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingRecordVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingRuleVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingSummaryVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 平台分账治理接口
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface ProfitSharingService {

    /**
     * 分账规则列表
     *
     * @return 规则列表
     */
    java.util.List<ProfitSharingRuleVO> ruleList();

    /**
     * 分账记录分页
     *
     * @param searchParams 结算单筛选
     * @return 结算单分页
     */
    IPage<ProfitSharingRecordVO> recordPage(ProfitSharingRecordSearchParams searchParams);

    /**
     * 分账余额概览
     *
     * @return 概览
     */
    ProfitSharingBalanceVO balance();

    /**
     * 分账治理汇总
     *
     * @param searchParams 查询参数
     * @return 汇总结果
     */
    ProfitSharingSummaryVO summary(ProfitSharingRecordSearchParams searchParams);

    /**
     * 分账记录导出查询
     *
     * @param searchParams 查询参数
     * @return 记录列表
     */
    List<ProfitSharingRecordVO> recordList(ProfitSharingRecordSearchParams searchParams);
}
