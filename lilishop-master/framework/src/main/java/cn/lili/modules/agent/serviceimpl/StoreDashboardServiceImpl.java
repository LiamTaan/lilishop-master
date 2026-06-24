package cn.lili.modules.agent.serviceimpl;

import cn.lili.common.utils.CurrencyUtil;
import cn.lili.modules.agent.entity.vos.StoreAssetOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardVO;
import cn.lili.modules.agent.service.StoreDashboardService;
import cn.lili.modules.store.entity.dos.Bill;
import cn.lili.modules.store.entity.enums.BillStatusEnum;
import cn.lili.modules.store.service.BillService;
import cn.lili.modules.statistics.entity.dto.GoodsStatisticsQueryParam;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.vo.GoodsStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.OrderOverviewVO;
import cn.lili.modules.statistics.entity.vo.StoreIndexStatisticsVO;
import cn.lili.modules.statistics.service.IndexStatisticsService;
import cn.lili.modules.statistics.service.OrderStatisticsService;
import cn.lili.modules.statistics.service.StoreFlowStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 供货端经营概览服务实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class StoreDashboardServiceImpl implements StoreDashboardService {

    @Autowired
    private IndexStatisticsService indexStatisticsService;

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Autowired
    private StoreFlowStatisticsService storeFlowStatisticsService;

    @Autowired
    private BillService billService;

    @Override
    public StoreDashboardVO dashboard(String storeId) {
        StoreDashboardVO vo = new StoreDashboardVO();
        StoreIndexStatisticsVO storeIndexStatistics = indexStatisticsService.storeIndexStatistics();
        StatisticsQueryParam statisticsQueryParam = new StatisticsQueryParam();
        statisticsQueryParam.setStoreId(storeId);
        OrderOverviewVO orderOverview = orderStatisticsService.overview(statisticsQueryParam);
        GoodsStatisticsQueryParam goodsStatisticsQueryParam = new GoodsStatisticsQueryParam();
        goodsStatisticsQueryParam.setStoreId(storeId);
        goodsStatisticsQueryParam.setType("PRICE");
        List<GoodsStatisticsDataVO> goodsRankList = storeFlowStatisticsService.getGoodsStatisticsData(goodsStatisticsQueryParam, 10);
        vo.setStoreIndexStatistics(storeIndexStatistics);
        vo.setOrderOverview(orderOverview);
        vo.setGoodsRankList(goodsRankList);
        return vo;
    }

    @Override
    public StoreAssetOverviewVO assets(String storeId) {
        StoreAssetOverviewVO vo = new StoreAssetOverviewVO();
        vo.setTotalIncome(0D);
        vo.setSettledAmount(0D);
        vo.setWithdrawableAmount(0D);
        vo.setFrozenAmount(0D);

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bill> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("store_id", storeId);
        List<Bill> bills = billService.list(queryWrapper);
        for (Bill bill : bills) {
            double billPrice = bill.getBillPrice() == null ? 0D : bill.getBillPrice();
            vo.setTotalIncome(CurrencyUtil.add(vo.getTotalIncome(), billPrice));
            if (BillStatusEnum.COMPLETE.name().equals(bill.getBillStatus())) {
                vo.setSettledAmount(CurrencyUtil.add(vo.getSettledAmount(), billPrice));
            } else if (BillStatusEnum.CHECK.name().equals(bill.getBillStatus())) {
                vo.setWithdrawableAmount(CurrencyUtil.add(vo.getWithdrawableAmount(), billPrice));
            } else if (BillStatusEnum.OUT.name().equals(bill.getBillStatus())) {
                vo.setFrozenAmount(CurrencyUtil.add(vo.getFrozenAmount(), billPrice));
            }
        }
        return vo;
    }
}
