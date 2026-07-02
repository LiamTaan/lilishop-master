package cn.lili.modules.agent.serviceimpl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.utils.CurrencyUtil;
import cn.lili.modules.agent.entity.enums.StoreDashboardRangeEnum;
import cn.lili.modules.agent.entity.params.StoreDashboardTrendQueryParams;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendDetailVO;
import cn.lili.modules.agent.entity.vos.StoreDashboardTrendVO;
import cn.lili.modules.agent.entity.vos.StoreWorkbenchMessageVO;
import cn.lili.modules.agent.entity.vos.StoreWorkbenchOverviewVO;
import cn.lili.modules.agent.service.StoreDashboardAnalyticsService;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.enums.GoodsAuthEnum;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import cn.lili.modules.message.entity.dos.StoreMessage;
import cn.lili.modules.message.entity.enums.MessageBizTypeEnum;
import cn.lili.modules.message.mapper.StoreMessageMapper;
import cn.lili.modules.order.aftersale.entity.dos.AfterSale;
import cn.lili.modules.order.aftersale.mapper.AfterSaleMapper;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.entity.enums.PayStatusEnum;
import cn.lili.modules.order.order.mapper.OrderMapper;
import cn.lili.modules.procurement.entity.dos.ProcurementInbound;
import cn.lili.modules.procurement.mapper.ProcurementInboundMapper;
import cn.lili.modules.statistics.service.GoodsStatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 供货商工作台统计服务实现
 *
 * @author dawn
 * @since 2026/7/2
 */
@Service
public class StoreDashboardAnalyticsServiceImpl implements StoreDashboardAnalyticsService {

    private static final String COMPARE_YESTERDAY_LABEL = "较昨日";
    private static final String STOCK_WARNING_HINT = "需要补货";

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AfterSaleMapper afterSaleMapper;

    @Autowired
    private ProcurementInboundMapper procurementInboundMapper;

    @Autowired
    private GoodsStatisticsService goodsStatisticsService;

    @Autowired
    private StoreMessageMapper storeMessageMapper;

    @Override
    public StoreWorkbenchOverviewVO overview(String storeId) {
        Date now = new Date();
        Date todayStart = DateUtil.beginOfDay(now);
        Date todayEnd = DateUtil.endOfDay(now);
        Date yesterdayStart = DateUtil.beginOfDay(DateUtil.offsetDay(now, -1));
        Date yesterdayEnd = DateUtil.endOfDay(DateUtil.offsetDay(now, -1));

        double todayTurnover = this.sumPaidOrderAmount(storeId, todayStart, todayEnd);
        double yesterdayTurnover = this.sumPaidOrderAmount(storeId, yesterdayStart, yesterdayEnd);
        long todayOrderTotal = this.countCreatedOrders(storeId, todayStart, todayEnd);
        long yesterdayOrderTotal = this.countCreatedOrders(storeId, yesterdayStart, yesterdayEnd);

        StoreWorkbenchOverviewVO vo = new StoreWorkbenchOverviewVO();
        vo.setTodayTurnover(todayTurnover);
        vo.setTodayTurnoverCompareRate(this.calculateCompareRate(todayTurnover, yesterdayTurnover));
        vo.setTodayTurnoverCompareLabel(COMPARE_YESTERDAY_LABEL);
        vo.setOrderTotal(todayOrderTotal);
        vo.setOrderTotalCompareRate(this.calculateCompareRate((double) todayOrderTotal, (double) yesterdayOrderTotal));
        vo.setOrderTotalCompareLabel(COMPARE_YESTERDAY_LABEL);
        vo.setOnSaleGoodsCount(goodsStatisticsService.goodsNum(storeId, GoodsStatusEnum.UPPER, GoodsAuthEnum.PASS));
        vo.setOnSaleCategoryCount(this.countOnSaleCategories(storeId));
        vo.setStockWarningCount(goodsStatisticsService.alertQuantityNum(storeId));
        vo.setStockWarningHint(STOCK_WARNING_HINT);
        vo.setNoticeMessage(this.buildMessage(storeId, MessageBizTypeEnum.PLATFORM.name()));
        vo.setGovernanceMessage(this.buildMessage(storeId, MessageBizTypeEnum.STORE_GOVERNANCE.name()));
        vo.setPendingShipmentCount(this.countPendingShipment(storeId));
        vo.setPendingAfterSaleCount(this.countPendingAfterSale(storeId));
        vo.setLogisticsExceptionCount(this.countLogisticsException(storeId));
        vo.setAfterSaleCount(this.countAfterSale(storeId));
        return vo;
    }

    @Override
    public StoreDashboardTrendVO trend(String storeId, StoreDashboardTrendQueryParams params) {
        TrendComputation trendComputation = this.computeTrend(storeId, params);
        StoreDashboardTrendVO vo = new StoreDashboardTrendVO();
        vo.setLabels(trendComputation.getLabels());
        vo.setSalesCostSeries(trendComputation.getSalesCostSeries());
        vo.setSalesProfitSeries(trendComputation.getSalesProfitSeries());
        return vo;
    }

    @Override
    public List<StoreDashboardTrendDetailVO> trendDetail(String storeId, StoreDashboardTrendQueryParams params) {
        return this.computeTrend(storeId, params).getDetails();
    }

    private double sumPaidOrderAmount(String storeId, Date start, Date end) {
        return orderMapper.selectList(this.paidOrderWrapper(storeId)
                        .ge(Order::getPaymentTime, start)
                        .le(Order::getPaymentTime, end))
                .stream()
                .map(order -> order.getFlowPrice() == null ? 0D : order.getFlowPrice())
                .reduce(0D, CurrencyUtil::add);
    }

    private long countCreatedOrders(String storeId, Date start, Date end) {
        return orderMapper.selectCount(this.createdOrderWrapper(storeId)
                .ge(Order::getCreateTime, start)
                .le(Order::getCreateTime, end));
    }

    private long countOnSaleCategories(String storeId) {
        List<Goods> goodsList = goodsStatisticsService.list(Wrappers.<Goods>lambdaQuery()
                .select(Goods::getCategoryPath)
                .eq(Goods::getDeleteFlag, false)
                .eq(Goods::getStoreId, storeId)
                .eq(Goods::getMarketEnable, GoodsStatusEnum.UPPER.name())
                .eq(Goods::getAuthFlag, GoodsAuthEnum.PASS.name()));
        Set<String> categoryIds = new LinkedHashSet<>();
        for (Goods goods : goodsList) {
            String categoryId = this.resolveLeafCategoryId(goods.getCategoryPath());
            if (CharSequenceUtil.isNotBlank(categoryId)) {
                categoryIds.add(categoryId);
            }
        }
        return categoryIds.size();
    }

    private String resolveLeafCategoryId(String categoryPath) {
        if (CharSequenceUtil.isBlank(categoryPath)) {
            return null;
        }
        String normalized = categoryPath.replace("，", ",").trim();
        if (normalized.endsWith(",")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        String[] parts = normalized.split(",");
        if (parts.length == 0) {
            return normalized;
        }
        return parts[parts.length - 1].trim();
    }

    private StoreWorkbenchMessageVO buildMessage(String storeId, String bizType) {
        StoreMessage message = storeMessageMapper.selectLatestByStoreIdAndBizType(storeId, bizType);
        if (message == null) {
            return null;
        }
        StoreWorkbenchMessageVO vo = new StoreWorkbenchMessageVO();
        vo.setId(message.getId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setBizType(message.getBizType());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private long countPendingShipment(String storeId) {
        return orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                .eq(Order::getStoreId, storeId)
                .eq(Order::getOrderStatus, OrderStatusEnum.UNDELIVERED.name()));
    }

    private long countPendingAfterSale(String storeId) {
        return afterSaleMapper.selectCount(Wrappers.<AfterSale>lambdaQuery()
                .eq(AfterSale::getStoreId, storeId)
                .eq(AfterSale::getServiceStatus, cn.lili.modules.order.trade.entity.enums.AfterSaleStatusEnum.APPLY.name()));
    }

    private long countAfterSale(String storeId) {
        return afterSaleMapper.selectCount(Wrappers.<AfterSale>lambdaQuery()
                .eq(AfterSale::getStoreId, storeId));
    }

    private long countLogisticsException(String storeId) {
        // 当前仓内没有已落地、可稳定复用的物流异常来源；首页先避免以投诉单误报物流异常。
        return 0L;
    }

    private Double calculateCompareRate(Double current, Double previous) {
        if (previous == null || previous == 0D) {
            return (current == null || current == 0D) ? 0D : 100D;
        }
        return CurrencyUtil.mul(CurrencyUtil.div(CurrencyUtil.sub(current, previous), previous, 4), 100D);
    }

    private TrendComputation computeTrend(String storeId, StoreDashboardTrendQueryParams params) {
        StoreDashboardRangeEnum range = this.resolveRange(params);
        Date now = new Date();
        Date rangeStart = this.resolveTrendStart(range, now);
        Date rangeEnd = now;

        List<TimeBucket> buckets = this.buildBuckets(range, now);
        List<Order> paidOrders = orderMapper.selectList(this.paidOrderWrapper(storeId)
                .ge(Order::getPaymentTime, rangeStart)
                .le(Order::getPaymentTime, rangeEnd));
        List<ProcurementInbound> inbounds = procurementInboundMapper.selectList(Wrappers.<ProcurementInbound>lambdaQuery()
                .eq(ProcurementInbound::getStoreId, storeId)
                .ge(ProcurementInbound::getInboundTime, rangeStart)
                .le(ProcurementInbound::getInboundTime, rangeEnd));

        Map<String, Double> revenueMap = new LinkedHashMap<>();
        Map<String, Double> costMap = new LinkedHashMap<>();
        for (TimeBucket bucket : buckets) {
            revenueMap.put(bucket.getLabel(), 0D);
            costMap.put(bucket.getLabel(), 0D);
        }

        for (Order order : paidOrders) {
            String label = this.findBucketLabel(buckets, order.getPaymentTime());
            if (label != null) {
                revenueMap.put(label, CurrencyUtil.add(revenueMap.get(label), order.getFlowPrice() == null ? 0D : order.getFlowPrice()));
            }
        }

        for (ProcurementInbound inbound : inbounds) {
            String label = this.findBucketLabel(buckets, inbound.getInboundTime());
            if (label != null) {
                costMap.put(label, CurrencyUtil.add(costMap.get(label), inbound.getTotalCost() == null ? 0D : inbound.getTotalCost()));
            }
        }

        TrendComputation result = new TrendComputation();
        List<String> labels = new ArrayList<>();
        List<Double> salesCostSeries = new ArrayList<>();
        List<Double> salesProfitSeries = new ArrayList<>();
        List<StoreDashboardTrendDetailVO> details = new ArrayList<>();
        for (TimeBucket bucket : buckets) {
            Double salesCost = costMap.get(bucket.getLabel());
            Double salesProfit = CurrencyUtil.sub(revenueMap.get(bucket.getLabel()), salesCost);
            labels.add(bucket.getLabel());
            salesCostSeries.add(salesCost);
            salesProfitSeries.add(salesProfit);

            StoreDashboardTrendDetailVO detailVO = new StoreDashboardTrendDetailVO();
            detailVO.setLabel(bucket.getLabel());
            detailVO.setSalesCost(salesCost);
            detailVO.setSalesProfit(salesProfit);
            details.add(detailVO);
        }
        result.setLabels(labels);
        result.setSalesCostSeries(salesCostSeries);
        result.setSalesProfitSeries(salesProfitSeries);
        result.setDetails(details);
        return result;
    }

    private LambdaQueryWrapper<Order> createdOrderWrapper(String storeId) {
        return Wrappers.<Order>lambdaQuery()
                .eq(Order::getStoreId, storeId)
                .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.name());
    }

    private LambdaQueryWrapper<Order> paidOrderWrapper(String storeId) {
        return Wrappers.<Order>lambdaQuery()
                .eq(Order::getStoreId, storeId)
                .eq(Order::getPayStatus, PayStatusEnum.PAID.name())
                .ne(Order::getOrderStatus, OrderStatusEnum.CANCELLED.name());
    }

    private StoreDashboardRangeEnum resolveRange(StoreDashboardTrendQueryParams params) {
        String rangeValue = params == null ? null : params.getRange();
        if (CharSequenceUtil.isBlank(rangeValue)) {
            return StoreDashboardRangeEnum.YEAR;
        }
        try {
            return StoreDashboardRangeEnum.valueOf(rangeValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
    }

    private Date resolveTrendStart(StoreDashboardRangeEnum range, Date now) {
        return switch (range) {
            case WEEK -> DateUtil.beginOfWeek(now);
            case MONTH -> DateUtil.beginOfMonth(now);
            case YEAR -> DateUtil.beginOfYear(now);
        };
    }

    private List<TimeBucket> buildBuckets(StoreDashboardRangeEnum range, Date now) {
        List<TimeBucket> buckets = new ArrayList<>();
        switch (range) {
            case WEEK, MONTH -> {
                Date cursor = this.resolveTrendStart(range, now);
                while (!cursor.after(now)) {
                    Date start = DateUtil.beginOfDay(cursor);
                    Date end = DateUtil.endOfDay(cursor);
                    buckets.add(new TimeBucket(DateUtil.format(start, "MM-dd"), start, end));
                    cursor = DateUtil.offsetDay(cursor, 1);
                }
            }
            case YEAR -> {
                Date cursor = DateUtil.beginOfYear(now);
                Date currentMonthStart = DateUtil.beginOfMonth(now);
                while (!cursor.after(currentMonthStart)) {
                    Date start = DateUtil.beginOfMonth(cursor);
                    Date end = DateUtil.endOfMonth(cursor);
                    buckets.add(new TimeBucket(DateUtil.format(start, "MM'月'"), start, end));
                    cursor = DateUtil.offsetMonth(cursor, 1);
                }
            }
        }
        return buckets;
    }

    private String findBucketLabel(List<TimeBucket> buckets, Date date) {
        if (date == null) {
            return null;
        }
        for (TimeBucket bucket : buckets) {
            if (!date.before(bucket.getStart()) && !date.after(bucket.getEnd())) {
                return bucket.getLabel();
            }
        }
        return null;
    }

    private static class TimeBucket {
        private final String label;
        private final Date start;
        private final Date end;

        private TimeBucket(String label, Date start, Date end) {
            this.label = label;
            this.start = start;
            this.end = end;
        }

        public String getLabel() {
            return label;
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }
    }

    private static class TrendComputation {
        private List<String> labels;
        private List<Double> salesCostSeries;
        private List<Double> salesProfitSeries;
        private List<StoreDashboardTrendDetailVO> details;

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public List<Double> getSalesCostSeries() {
            return salesCostSeries;
        }

        public void setSalesCostSeries(List<Double> salesCostSeries) {
            this.salesCostSeries = salesCostSeries;
        }

        public List<Double> getSalesProfitSeries() {
            return salesProfitSeries;
        }

        public void setSalesProfitSeries(List<Double> salesProfitSeries) {
            this.salesProfitSeries = salesProfitSeries;
        }

        public List<StoreDashboardTrendDetailVO> getDetails() {
            return details;
        }

        public void setDetails(List<StoreDashboardTrendDetailVO> details) {
            this.details = details;
        }
    }
}
