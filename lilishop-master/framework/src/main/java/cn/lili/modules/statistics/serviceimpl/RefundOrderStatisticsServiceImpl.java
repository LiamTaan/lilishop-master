package cn.lili.modules.statistics.serviceimpl;

import cn.lili.common.utils.DateUtil;
import cn.lili.common.utils.StringUtils;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.order.order.entity.dos.StoreFlow;
import cn.lili.modules.order.order.entity.enums.FlowTypeEnum;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.enums.SearchTypeEnum;
import cn.lili.modules.statistics.entity.enums.TimeTypeEnum;
import cn.lili.modules.statistics.entity.vo.RefundOrderStatisticsDataVO;
import cn.lili.modules.statistics.mapper.RefundOrderStatisticsMapper;
import cn.lili.modules.statistics.service.RefundOrderStatisticsService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Map;

/**
 * 退款订单统计业务层实现
 *
 * @author Bulbasaur
 * @since 2020/12/10 11:30
 */
@Service
public class RefundOrderStatisticsServiceImpl extends ServiceImpl<RefundOrderStatisticsMapper, StoreFlow> implements RefundOrderStatisticsService {

    @Override
    public IPage<RefundOrderStatisticsDataVO> getRefundOrderStatisticsData(PageVO pageVO, StatisticsQueryParam statisticsQueryParam) {
        QueryWrapper queryWrapper = getQueryWrapper(statisticsQueryParam);
        return this.baseMapper.getRefundStatisticsData(PageUtil.initPage(pageVO), queryWrapper);
    }

    @Override
    public Double getRefundOrderStatisticsPrice(StatisticsQueryParam statisticsQueryParam) {

        QueryWrapper queryWrapper = getQueryWrapper(statisticsQueryParam);
        queryWrapper.select("SUM(final_price) AS price");
        Map<String, Object> result = this.getMap(queryWrapper);
        Object price = result == null ? null : result.get("price");
        if (price == null) {
            return 0D;
        }
        return Double.parseDouble(price.toString());
    }


    private QueryWrapper getQueryWrapper(StatisticsQueryParam statisticsQueryParam) {
        StatisticsQueryParam effectiveQueryParam = statisticsQueryParam == null ? new StatisticsQueryParam() : statisticsQueryParam;
        Calendar calendar = Calendar.getInstance();

        if (effectiveQueryParam.getSearchType() == null
                && effectiveQueryParam.getTimeType() == null
                && effectiveQueryParam.getYear() == null
                && effectiveQueryParam.getMonth() == null) {
            effectiveQueryParam.setSearchType(SearchTypeEnum.TODAY.name());
        }
        if (effectiveQueryParam.getSearchType() != null && effectiveQueryParam.getTimeType() == null) {
            effectiveQueryParam.setTimeType(TimeTypeEnum.MONTH.name());
        }
        if (effectiveQueryParam.getYear() == null) {
            effectiveQueryParam.setYear(calendar.get(Calendar.YEAR));
        }
        if (effectiveQueryParam.getMonth() == null) {
            effectiveQueryParam.setMonth(calendar.get(Calendar.MONTH) + 1);
        }

        QueryWrapper queryWrapper = Wrappers.query();

        //判断搜索类型是：年、月
        if (TimeTypeEnum.MONTH.name().equals(effectiveQueryParam.getTimeType())) {
            queryWrapper.between("create_time", DateUtil.getBeginTime(effectiveQueryParam.getYear(), effectiveQueryParam.getMonth()), DateUtil.getEndTime(effectiveQueryParam.getYear(), effectiveQueryParam.getMonth()));
        } else {
            queryWrapper.between("create_time", DateUtil.getBeginTime(effectiveQueryParam.getYear(), 1), DateUtil.getEndTime(effectiveQueryParam.getYear(), 12));
        }

        //设置店铺ID
        queryWrapper.eq(!StringUtils.isEmpty(effectiveQueryParam.getStoreId()), "store_id", effectiveQueryParam.getStoreId());

        //设置为退款查询
        queryWrapper.eq("flow_type", FlowTypeEnum.REFUND);
        return queryWrapper;
    }
}
