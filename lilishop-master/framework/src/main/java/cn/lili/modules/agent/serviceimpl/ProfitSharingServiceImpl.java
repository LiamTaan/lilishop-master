package cn.lili.modules.agent.serviceimpl;

import cn.lili.common.utils.CurrencyUtil;
import cn.lili.common.utils.BeanUtil;
import cn.lili.modules.agent.entity.dos.ProfitSharingRule;
import cn.lili.modules.agent.entity.params.ProfitSharingRecordSearchParams;
import cn.lili.modules.agent.entity.vos.ProfitSharingBalanceVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingRecordVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingRuleVO;
import cn.lili.modules.agent.entity.vos.ProfitSharingSummaryVO;
import cn.lili.modules.agent.mapper.ProfitSharingRuleMapper;
import cn.lili.modules.agent.service.ProfitSharingService;
import cn.lili.modules.order.order.entity.dos.StoreFlow;
import cn.lili.modules.order.order.entity.enums.ProfitSharingStatusEnum;
import cn.lili.modules.order.order.mapper.StoreFlowMapper;
import cn.lili.modules.store.entity.dos.Bill;
import cn.lili.modules.store.mapper.BillMapper;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 平台分账治理实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class ProfitSharingServiceImpl implements ProfitSharingService {

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private StoreFlowMapper storeFlowMapper;

    @Autowired
    private ProfitSharingRuleMapper profitSharingRuleMapper;

    @Override
    public List<ProfitSharingRuleVO> ruleList() {
        List<ProfitSharingRule> rules = profitSharingRuleMapper.selectList(
                Wrappers.<ProfitSharingRule>lambdaQuery().orderByDesc(ProfitSharingRule::getCreateTime));
        if (rules == null || rules.isEmpty()) {
            ProfitSharingRuleVO rule = new ProfitSharingRuleVO();
            rule.setRuleId("DEFAULT");
            rule.setRuleName("默认平台分账规则");
            rule.setRoleType("PLATFORM");
            rule.setRatio(0D);
            rule.setStatus("ENABLE");
            return Arrays.asList(rule);
        }
        return rules.stream().map(item -> {
            ProfitSharingRuleVO vo = new ProfitSharingRuleVO();
            BeanUtil.copyProperties(item, vo);
            vo.setRuleId(item.getId());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<ProfitSharingRecordVO> recordPage(ProfitSharingRecordSearchParams searchParams) {
        ProfitSharingRecordSearchParams query = searchParams == null ? new ProfitSharingRecordSearchParams() : searchParams;
        LambdaQueryWrapper<Bill> wrapper = Wrappers.lambdaQuery();
        this.applyFilters(wrapper, query);
        IPage<Bill> page = billMapper.selectPage(PageUtil.initPage(query), wrapper);
        List<ProfitSharingRecordVO> records = page.getRecords().stream().map(item -> {
            ProfitSharingRecordVO vo = new ProfitSharingRecordVO();
            vo.setId(item.getId());
            vo.setSn(item.getSn());
            vo.setStoreId(item.getStoreId());
            vo.setStoreName(item.getStoreName());
            vo.setBillStatus(item.getBillStatus());
            vo.setBillPrice(item.getBillPrice());
            vo.setRefundPrice(item.getRefundPrice());
            vo.setCommissionAmount(item.getCommissionPrice());
            vo.setSettlementStatus(resolveSettlementStatus(item));
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        return PageUtil.convertPage(page, records);
    }

    @Override
    public ProfitSharingBalanceVO balance() {
        List<Bill> bills = billMapper.selectList(Wrappers.<Bill>lambdaQuery());
        ProfitSharingBalanceVO vo = new ProfitSharingBalanceVO();
        double totalBillAmount = 0D;
        double checkedAmount = 0D;
        double paidAmount = 0D;
        double refundAmount = 0D;
        double pendingShareAmount = 0D;
        for (Bill bill : bills) {
            double billPrice = bill.getBillPrice() == null ? 0D : bill.getBillPrice();
            totalBillAmount = CurrencyUtil.add(totalBillAmount, billPrice);
            if ("CHECK".equals(bill.getBillStatus())) {
                checkedAmount = CurrencyUtil.add(checkedAmount, billPrice);
            }
            if ("PAY".equals(bill.getBillStatus())) {
                paidAmount = CurrencyUtil.add(paidAmount, billPrice);
            }
            if ("OUT".equals(bill.getBillStatus())) {
                pendingShareAmount = CurrencyUtil.add(pendingShareAmount, billPrice);
            }
            if (bill.getRefundPrice() != null) {
                refundAmount = CurrencyUtil.add(refundAmount, bill.getRefundPrice());
            }
        }
        vo.setTotalBillAmount(totalBillAmount);
        vo.setCheckedAmount(checkedAmount);
        vo.setPaidAmount(paidAmount);
        vo.setRefundAmount(refundAmount);
        vo.setPendingShareAmount(pendingShareAmount);
        return vo;
    }

    @Override
    public ProfitSharingSummaryVO summary(ProfitSharingRecordSearchParams searchParams) {
        ProfitSharingRecordSearchParams query = searchParams == null ? new ProfitSharingRecordSearchParams() : searchParams;
        LambdaQueryWrapper<Bill> wrapper = Wrappers.lambdaQuery();
        this.applyFilters(wrapper, query);
        List<Bill> bills = billMapper.selectList(wrapper);
        ProfitSharingSummaryVO summaryVO = new ProfitSharingSummaryVO();
        long pendingCount = 0L;
        long checkedCount = 0L;
        long completedCount = 0L;
        double totalBillAmount = 0D;
        double totalCommissionAmount = 0D;
        double totalRefundAmount = 0D;
        for (Bill bill : bills) {
            totalBillAmount = CurrencyUtil.add(totalBillAmount, bill.getBillPrice() == null ? 0D : bill.getBillPrice());
            totalCommissionAmount = CurrencyUtil.add(totalCommissionAmount, bill.getCommissionPrice() == null ? 0D : bill.getCommissionPrice());
            totalRefundAmount = CurrencyUtil.add(totalRefundAmount, bill.getRefundPrice() == null ? 0D : bill.getRefundPrice());
            if ("OUT".equals(bill.getBillStatus())) {
                pendingCount++;
            } else if ("CHECK".equals(bill.getBillStatus())) {
                checkedCount++;
            } else if ("COMPLETE".equals(bill.getBillStatus())) {
                completedCount++;
            }
        }
        summaryVO.setTotalCount((long) bills.size());
        summaryVO.setPendingCount(pendingCount);
        summaryVO.setCheckedCount(checkedCount);
        summaryVO.setCompletedCount(completedCount);
        summaryVO.setTotalBillAmount(totalBillAmount);
        summaryVO.setTotalCommissionAmount(totalCommissionAmount);
        summaryVO.setTotalRefundAmount(totalRefundAmount);
        return summaryVO;
    }

    @Override
    public List<ProfitSharingRecordVO> recordList(ProfitSharingRecordSearchParams searchParams) {
        ProfitSharingRecordSearchParams query = searchParams == null ? new ProfitSharingRecordSearchParams() : searchParams;
        LambdaQueryWrapper<Bill> wrapper = Wrappers.lambdaQuery();
        this.applyFilters(wrapper, query);
        return billMapper.selectList(wrapper).stream().map(item -> {
            ProfitSharingRecordVO vo = new ProfitSharingRecordVO();
            vo.setId(item.getId());
            vo.setSn(item.getSn());
            vo.setStoreId(item.getStoreId());
            vo.setStoreName(item.getStoreName());
            vo.setBillStatus(item.getBillStatus());
            vo.setBillPrice(item.getBillPrice());
            vo.setRefundPrice(item.getRefundPrice());
            vo.setCommissionAmount(item.getCommissionPrice());
            vo.setSettlementStatus(resolveSettlementStatus(item));
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private void applyFilters(LambdaQueryWrapper<Bill> wrapper, ProfitSharingRecordSearchParams query) {
        String billStatus = StringUtils.isNotBlank(query.getBillStatus()) ? query.getBillStatus().toUpperCase(Locale.ROOT) : null;
        String settlementStatus = StringUtils.isNotBlank(query.getSettlementStatus()) ? query.getSettlementStatus().toUpperCase(Locale.ROOT) : null;
        wrapper.eq(StringUtils.isNotBlank(query.getStoreId()), Bill::getStoreId, query.getStoreId());
        wrapper.eq(StringUtils.isNotBlank(billStatus), Bill::getBillStatus, billStatus);
        if (StringUtils.isNotBlank(settlementStatus)) {
            if (ProfitSharingStatusEnum.FINISHED.name().equals(settlementStatus)) {
                wrapper.in(Bill::getBillStatus, Arrays.asList("PAY", "COMPLETE"));
            } else if (ProfitSharingStatusEnum.PROCESSING.name().equals(settlementStatus)) {
                wrapper.in(Bill::getBillStatus, Arrays.asList("OUT", "CHECK", "EXAMINE"));
            }
        }
        wrapper.and(StringUtils.isNotBlank(query.getKeyword()), w -> w
                .like(Bill::getSn, query.getKeyword())
                .or()
                .like(Bill::getStoreName, query.getKeyword()));
        wrapper.orderByDesc(Bill::getCreateTime);
    }

    private String resolveSettlementStatus(Bill bill) {
        if (bill == null || bill.getBillStatus() == null) {
            return ProfitSharingStatusEnum.PROCESSING.name();
        }
        return switch (bill.getBillStatus()) {
            case "OUT", "CHECK", "EXAMINE" -> ProfitSharingStatusEnum.PROCESSING.name();
            case "PAY", "COMPLETE" -> ProfitSharingStatusEnum.FINISHED.name();
            default -> ProfitSharingStatusEnum.PROCESSING.name();
        };
    }
}
