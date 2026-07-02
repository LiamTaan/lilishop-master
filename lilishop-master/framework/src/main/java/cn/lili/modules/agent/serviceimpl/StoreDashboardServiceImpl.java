package cn.lili.modules.agent.serviceimpl;

import cn.lili.common.utils.CurrencyUtil;
import cn.lili.modules.agent.entity.params.StoreDashboardTrendQueryParams;
import cn.lili.modules.agent.entity.vos.StoreAssetOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreWorkbenchOverviewVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendDetailVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendVO;
import cn.lili.modules.agent.service.StoreDashboardAnalyticsService;
import cn.lili.modules.agent.service.StoreDashboardService;
import cn.lili.modules.store.entity.dos.Bill;
import cn.lili.modules.store.entity.enums.BillStatusEnum;
import cn.lili.modules.store.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 供货端工作台服务实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class StoreDashboardServiceImpl implements StoreDashboardService {

    @Autowired
    private StoreDashboardAnalyticsService storeDashboardAnalyticsService;

    @Autowired
    private BillService billService;

    @Override
    public StoreWorkbenchOverviewVO dashboard(String storeId) {
        return storeDashboardAnalyticsService.overview(storeId);
    }

    @Override
    public StoreDashboardTrendVO trend(String storeId, StoreDashboardTrendQueryParams params) {
        return storeDashboardAnalyticsService.trend(storeId, params);
    }

    @Override
    public List<StoreDashboardTrendDetailVO> trendDetail(String storeId, StoreDashboardTrendQueryParams params) {
        return storeDashboardAnalyticsService.trendDetail(storeId, params);
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
