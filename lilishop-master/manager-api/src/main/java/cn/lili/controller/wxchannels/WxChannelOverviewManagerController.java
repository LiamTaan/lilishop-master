package cn.lili.controller.wxchannels;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.wxchannels.entity.vo.WxChannelOverviewDailyVO;
import cn.lili.modules.wxchannels.entity.vo.WxChannelOverviewSummaryVO;
import cn.lili.modules.wxchannels.service.WxChannelOverviewService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager/wxchannels/overview")
public class WxChannelOverviewManagerController {

    @Autowired
    private WxChannelOverviewService overviewService;

    @GetMapping("/summary")
    public ResultMessage<WxChannelOverviewSummaryVO> summary(Long startTime, Long endTime) {
        return ResultUtil.data(overviewService.summary(startTime, endTime));
    }

    @GetMapping("/daily")
    public ResultMessage<List<WxChannelOverviewDailyVO>> daily(Long startTime, Long endTime) {
        return ResultUtil.data(overviewService.daily(startTime, endTime));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response, Long startTime, Long endTime) {
        overviewService.export(response, startTime, endTime);
    }
}
