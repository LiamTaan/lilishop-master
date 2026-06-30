package cn.lili.controller.statistics;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.vo.BusinessCompositionDataVO;
import cn.lili.modules.statistics.entity.vo.OverViewDataVO;
import cn.lili.modules.statistics.entity.vo.SourceDataVO;
import cn.lili.modules.statistics.service.OverViewStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端,营业概览接口
 *
 * @author Bulbasaur
 * @since 2025/08/25 7:31 下午
 */
@Slf4j
@RestController
@RequestMapping("/manager/statistics/overview")
public class OverViewStatisticsManagerController {

    @Autowired
    private OverViewStatisticsService overViewStatisticsService;

    @GetMapping
    public ResultMessage<OverViewDataVO> overViewDataVO(StatisticsQueryParam statisticsQueryParam) {
        try {
            return ResultUtil.data(overViewStatisticsService.getOverViewDataVO(statisticsQueryParam));
        } catch (Exception e) {
            log.error("获取营业概览统计错误",e);
        }
        return null;
    }

    @GetMapping("/source")
    public ResultMessage<List<SourceDataVO>> sourceDataVOList(StatisticsQueryParam statisticsQueryParam) {
        try {
            return ResultUtil.data(overViewStatisticsService.getSourceDataVOList(statisticsQueryParam));
        } catch (Exception e) {
            log.error("收款构成列表错误",e);
        }
        return null;
    }

    @GetMapping("/businessComposition")
    public ResultMessage<BusinessCompositionDataVO> businessCompositionDataVO(StatisticsQueryParam statisticsQueryParam) {
        try {
            return ResultUtil.data(overViewStatisticsService.businessCompositionDataVO(statisticsQueryParam));
        } catch (Exception e) {
            log.error("营业构成信息",e);
        }
        return null;
    }
}
