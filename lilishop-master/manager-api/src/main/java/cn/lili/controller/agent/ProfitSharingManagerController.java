package cn.lili.controller.agent;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.agent.entity.params.ProfitSharingRecordSearchParams;
import cn.lili.modules.agent.entity.vos.ProfitSharingBalanceVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingRecordVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingRuleVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingSummaryVO;
import cn.lili.modules.agent.service.ProfitSharingService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台分账治理接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@RequestMapping("/manager/profitsharing")
public class ProfitSharingManagerController {

    @Autowired
    private ProfitSharingService profitSharingService;

    @GetMapping("/rule/list")
    public ResultMessage<List<ProfitSharingRuleVO>> ruleList() {
        return ResultUtil.data(profitSharingService.ruleList());
    }

    @GetMapping("/record")
    public ResultMessage<IPage<ProfitSharingRecordVO>> recordPage(ProfitSharingRecordSearchParams searchParams) {
        return ResultUtil.data(profitSharingService.recordPage(searchParams));
    }

    @GetMapping("/record/list")
    public ResultMessage<List<ProfitSharingRecordVO>> recordList(ProfitSharingRecordSearchParams searchParams) {
        return ResultUtil.data(profitSharingService.recordList(searchParams));
    }

    @GetMapping("/balance")
    public ResultMessage<ProfitSharingBalanceVO> balance() {
        return ResultUtil.data(profitSharingService.balance());
    }

    @GetMapping("/summary")
    public ResultMessage<ProfitSharingSummaryVO> summary(ProfitSharingRecordSearchParams searchParams) {
        return ResultUtil.data(profitSharingService.summary(searchParams));
    }
}
