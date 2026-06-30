package cn.lili.controller.statistics;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.vo.MemberDistributionVO;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.vo.OnlineMemberVO;
import cn.lili.modules.statistics.entity.vo.PlatformViewVO;
import cn.lili.modules.statistics.service.PlatformViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端,流量统计接口
 *
 * @author Chopper
 * @since 2021/2/9 11:19
 */
@RestController
@RequestMapping("/manager/statistics/view")
public class ViewStatisticsManagerController {
    @Autowired
    private PlatformViewService platformViewService;

    @GetMapping("/list")
    public ResultMessage<List<PlatformViewVO>> getByPage(StatisticsQueryParam queryParam) {
        return ResultUtil.data(platformViewService.list(queryParam));
    }

    @GetMapping("/online/current")
    public ResultMessage<Long> currentNumberPeopleOnline() {
        return ResultUtil.data(platformViewService.online());
    }


    @GetMapping("/online/distribution")
    public ResultMessage<List<MemberDistributionVO>> memberDistribution() {
        return ResultUtil.data(platformViewService.memberDistribution());
    }

    @GetMapping("/online/history")
    public ResultMessage<List<OnlineMemberVO>> history() {
        return ResultUtil.data(platformViewService.statisticsOnline());
    }

}
