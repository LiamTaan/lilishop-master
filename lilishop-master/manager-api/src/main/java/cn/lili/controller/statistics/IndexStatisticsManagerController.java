package cn.lili.controller.statistics;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.statistics.entity.dto.GoodsStatisticsQueryParam;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.enums.StatisticsQuery;
import cn.lili.modules.statistics.entity.vo.GoodsStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.IndexNoticeVO;
import cn.lili.modules.statistics.entity.vo.IndexStatisticsVO;
import cn.lili.modules.statistics.entity.vo.StoreStatisticsDataVO;
import cn.lili.modules.statistics.service.IndexStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端,首页统计数据接口
 *
 * @author Bulbasaur
 * @since 2020/12/15 17:53
 */
@Slf4j
@RestController
@RequestMapping("/manager/statistics/index")
public class IndexStatisticsManagerController {

    /**
     * 首页统计
     */
    @Autowired
    private IndexStatisticsService indexStatisticsService;

    @GetMapping
    @PreventDuplicateSubmissions
    public ResultMessage<IndexStatisticsVO> index() {
        try {
            return ResultUtil.data(indexStatisticsService.indexStatistics());
        } catch (Exception e) {
            log.error("获取首页查询数据错误",e);
        }
        return null;
    }

    @GetMapping("/goodsStatistics")
    @PreventDuplicateSubmissions
    public ResultMessage<List<GoodsStatisticsDataVO>> goodsStatistics(GoodsStatisticsQueryParam goodsStatisticsQueryParam) {

        //按照金额查询
        goodsStatisticsQueryParam.setType(StatisticsQuery.PRICE.name());
        return ResultUtil.data(indexStatisticsService.goodsStatistics(goodsStatisticsQueryParam));
    }

    @GetMapping("/storeStatistics")
    @PreventDuplicateSubmissions
    public ResultMessage<List<StoreStatisticsDataVO>> storeStatistics(StatisticsQueryParam statisticsQueryParam) {
        return ResultUtil.data(indexStatisticsService.storeStatistics(statisticsQueryParam));
    }

    @GetMapping("/notice")
    public ResultMessage<IndexNoticeVO> notice() {
        return ResultUtil.data(indexStatisticsService.indexNotice());
    }
}
