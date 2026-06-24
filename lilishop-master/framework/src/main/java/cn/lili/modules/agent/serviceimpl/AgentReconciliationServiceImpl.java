package cn.lili.modules.agent.serviceimpl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.utils.BeanUtil;
import cn.lili.common.utils.CurrencyUtil;
import cn.lili.modules.agent.entity.params.AgentFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.AgentProcurementReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.PlatformFundReconciliationSearchParams;
import cn.lili.modules.agent.entity.params.PlatformProcurementReconciliationSearchParams;
import cn.lili.modules.agent.entity.vos.AgentReconciliationOverviewVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentFundReconciliationVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationSummaryVO;
import cn.lili.modules.agent.entity.vos.AgentProcurementReconciliationVO;
import cn.lili.modules.agent.entity.vos.AgentRoleRelationVO;
import cn.lili.modules.agent.entity.vos.AgentStoreBindVO;
import cn.lili.modules.agent.entity.vos.PlatformFundReconciliationVO;
import cn.lili.modules.agent.entity.vos.PlatformProcurementReconciliationVO;
import cn.lili.modules.agent.service.AgentReconciliationService;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import cn.lili.modules.procurement.entity.dos.ProcurementOrder;
import cn.lili.modules.procurement.entity.dos.ProcurementOrderItem;
import cn.lili.modules.procurement.mapper.ProcurementOrderMapper;
import cn.lili.modules.procurement.service.ProcurementOrderItemService;
import cn.lili.modules.wallet.entity.dos.WalletLog;
import cn.lili.modules.wallet.entity.enums.DepositServiceTypeEnum;
import cn.lili.modules.wallet.mapper.WalletLogMapper;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代理商对账业务实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class AgentReconciliationServiceImpl implements AgentReconciliationService {

    @Autowired
    private AgentRoleRelationService agentRoleRelationService;

    @Autowired
    private AgentStoreBindService agentStoreBindService;

    @Autowired
    private ProcurementOrderMapper procurementOrderMapper;

    @Autowired
    private ProcurementOrderItemService procurementOrderItemService;

    @Autowired
    private WalletLogMapper walletLogMapper;

    @Override
    public IPage<AgentProcurementReconciliationVO> procurementPage(String agentMemberId, AgentProcurementReconciliationSearchParams params) {
        this.checkAgent(agentMemberId);
        List<String> storeIds = agentStoreBindService.listApprovedStoreIdsByAgentMemberId(agentMemberId);
        if (storeIds.isEmpty()) {
            return this.emptyPage(params);
        }
        LambdaQueryWrapper<ProcurementOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.in(ProcurementOrder::getStoreId, storeIds);
        wrapper.eq(CharSequenceUtil.isNotBlank(params.getOrderSn()), ProcurementOrder::getOrderSn, params.getOrderSn());
        wrapper.eq(CharSequenceUtil.isNotBlank(params.getStatus()), ProcurementOrder::getStatus, params.getStatus());
        wrapper.ge(params.getStartCreateTime() != null, ProcurementOrder::getCreateTime, params.getStartCreateTime());
        wrapper.le(params.getEndCreateTime() != null, ProcurementOrder::getCreateTime, params.getEndCreateTime());
        if (CharSequenceUtil.isNotBlank(params.getGoodsName())) {
            List<String> orderIds = procurementOrderItemService.list(Wrappers.<ProcurementOrderItem>lambdaQuery()
                            .in(ProcurementOrderItem::getProcurementOrderId, this.queryStoreOrderIds(storeIds))
                            .like(ProcurementOrderItem::getGoodsName, params.getGoodsName()))
                    .stream()
                    .map(ProcurementOrderItem::getProcurementOrderId)
                    .distinct()
                    .collect(Collectors.toList());
            if (orderIds.isEmpty()) {
                return this.emptyPage(params);
            }
            wrapper.in(ProcurementOrder::getId, orderIds);
        }
        wrapper.orderByDesc(ProcurementOrder::getCreateTime);
        IPage<ProcurementOrder> page = procurementOrderMapper.selectPage(PageUtil.initPage(params), wrapper);
        List<AgentProcurementReconciliationVO> records = page.getRecords().stream().map(this::convertProcurement).collect(Collectors.toList());
        return PageUtil.convertPage(page, records);
    }

    @Override
    public AgentProcurementReconciliationSummaryVO procurementSummary(String agentMemberId, AgentProcurementReconciliationSearchParams params) {
        this.checkAgent(agentMemberId);
        List<String> storeIds = agentStoreBindService.listApprovedStoreIdsByAgentMemberId(agentMemberId);
        AgentProcurementReconciliationSummaryVO summary = new AgentProcurementReconciliationSummaryVO();
        summary.setTotalAmount(0D);
        summary.setTotalQuantity(0);
        summary.setTotalCount(0L);
        summary.setPendingInboundCount(0L);
        summary.setCompletedCount(0L);
        summary.setClosedCount(0L);
        if (storeIds.isEmpty()) {
            return summary;
        }
        LambdaQueryWrapper<ProcurementOrder> wrapper = this.buildProcurementWrapper(storeIds, params);
        List<ProcurementOrder> orders = procurementOrderMapper.selectList(wrapper);
        summary.setTotalCount((long) orders.size());
        double totalAmount = 0D;
        int totalQuantity = 0;
        long pendingInboundCount = 0L;
        long completedCount = 0L;
        long closedCount = 0L;
        for (ProcurementOrder order : orders) {
            totalAmount = CurrencyUtil.add(totalAmount, order.getTotalAmount() == null ? 0D : order.getTotalAmount());
            totalQuantity += order.getTotalQuantity() == null ? 0 : order.getTotalQuantity();
            if ("PENDING_INBOUND".equals(order.getStatus()) || "PARTIAL_INBOUND".equals(order.getStatus())) {
                pendingInboundCount++;
            }
            if ("COMPLETED".equals(order.getStatus())) {
                completedCount++;
            }
            if ("CLOSED".equals(order.getStatus())) {
                closedCount++;
            }
        }
        summary.setTotalAmount(totalAmount);
        summary.setTotalQuantity(totalQuantity);
        summary.setPendingInboundCount(pendingInboundCount);
        summary.setCompletedCount(completedCount);
        summary.setClosedCount(closedCount);
        return summary;
    }

    @Override
    public AgentProcurementReconciliationVO procurementDetail(String agentMemberId, String id) {
        this.checkAgent(agentMemberId);
        ProcurementOrder order = procurementOrderMapper.selectById(id);
        if (order == null) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        List<String> storeIds = agentStoreBindService.listApprovedStoreIdsByAgentMemberId(agentMemberId);
        if (!storeIds.contains(order.getStoreId())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        return this.convertProcurement(order);
    }

    @Override
    public List<AgentProcurementReconciliationVO> procurementList(String agentMemberId, AgentProcurementReconciliationSearchParams params) {
        this.checkAgent(agentMemberId);
        List<String> storeIds = agentStoreBindService.listApprovedStoreIdsByAgentMemberId(agentMemberId);
        if (storeIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ProcurementOrder> wrapper = this.buildProcurementWrapper(storeIds, params);
        wrapper.orderByDesc(ProcurementOrder::getCreateTime);
        return procurementOrderMapper.selectList(wrapper).stream().map(this::convertProcurement).collect(Collectors.toList());
    }

    @Override
    public IPage<AgentFundReconciliationVO> fundPage(String agentMemberId, AgentFundReconciliationSearchParams params) {
        this.checkAgent(agentMemberId);
        LambdaQueryWrapper<WalletLog> wrapper = this.buildFundWrapper(agentMemberId, params);
        wrapper.orderByDesc(WalletLog::getCreateTime);
        IPage<WalletLog> page = walletLogMapper.selectPage(PageUtil.initPage(params), wrapper);
        List<AgentFundReconciliationVO> records = page.getRecords().stream().map(this::convertFund).collect(Collectors.toList());
        return PageUtil.convertPage(page, records);
    }

    @Override
    public AgentFundReconciliationSummaryVO fundSummary(String agentMemberId, AgentFundReconciliationSearchParams params) {
        this.checkAgent(agentMemberId);
        LambdaQueryWrapper<WalletLog> wrapper = this.buildFundWrapper(agentMemberId, params);
        List<WalletLog> logs = walletLogMapper.selectList(wrapper);
        AgentFundReconciliationSummaryVO summary = new AgentFundReconciliationSummaryVO();
        summary.setTotalCount((long) logs.size());
        summary.setTotalAmount(0D);
        summary.setRechargeAmount(0D);
        summary.setWithdrawalAmount(0D);
        summary.setPayAmount(0D);
        summary.setRefundAmount(0D);
        summary.setCommissionAmount(0D);
        double totalAmount = 0D;
        double rechargeAmount = 0D;
        double withdrawalAmount = 0D;
        double payAmount = 0D;
        double refundAmount = 0D;
        double commissionAmount = 0D;
        for (WalletLog log : logs) {
            double money = log.getMoney() == null ? 0D : log.getMoney();
            totalAmount = CurrencyUtil.add(totalAmount, money);
            if (DepositServiceTypeEnum.WALLET_RECHARGE.name().equals(log.getServiceType())) {
                rechargeAmount = CurrencyUtil.add(rechargeAmount, money);
            } else if (DepositServiceTypeEnum.WALLET_WITHDRAWAL.name().equals(log.getServiceType())) {
                withdrawalAmount = CurrencyUtil.add(withdrawalAmount, money);
            } else if (DepositServiceTypeEnum.WALLET_PAY.name().equals(log.getServiceType())) {
                payAmount = CurrencyUtil.add(payAmount, money);
            } else if (DepositServiceTypeEnum.WALLET_REFUND.name().equals(log.getServiceType())) {
                refundAmount = CurrencyUtil.add(refundAmount, money);
            } else if (DepositServiceTypeEnum.WALLET_COMMISSION.name().equals(log.getServiceType())) {
                commissionAmount = CurrencyUtil.add(commissionAmount, money);
            }
        }
        summary.setTotalAmount(totalAmount);
        summary.setRechargeAmount(rechargeAmount);
        summary.setWithdrawalAmount(withdrawalAmount);
        summary.setPayAmount(payAmount);
        summary.setRefundAmount(refundAmount);
        summary.setCommissionAmount(commissionAmount);
        return summary;
    }

    @Override
    public AgentFundReconciliationVO fundDetail(String agentMemberId, String id) {
        this.checkAgent(agentMemberId);
        WalletLog walletLog = walletLogMapper.selectById(id);
        if (walletLog == null) {
            throw new ServiceException(ResultCode.PARAMS_ERROR);
        }
        if (!agentMemberId.equals(walletLog.getMemberId())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        return this.convertFund(walletLog);
    }

    @Override
    public List<AgentFundReconciliationVO> fundList(String agentMemberId, AgentFundReconciliationSearchParams params) {
        this.checkAgent(agentMemberId);
        LambdaQueryWrapper<WalletLog> wrapper = this.buildFundWrapper(agentMemberId, params);
        wrapper.orderByDesc(WalletLog::getCreateTime);
        return walletLogMapper.selectList(wrapper).stream().map(this::convertFund).collect(Collectors.toList());
    }

    @Override
    public AgentReconciliationOverviewVO overview() {
        AgentReconciliationOverviewVO overviewVO = new AgentReconciliationOverviewVO();
        overviewVO.setAgentCount(agentRoleRelationService.count(Wrappers.lambdaQuery()));
        AgentProcurementReconciliationSummaryVO procurementSummary = new AgentProcurementReconciliationSummaryVO();
        procurementSummary.setTotalCount(0L);
        procurementSummary.setTotalAmount(0D);
        procurementSummary.setTotalQuantity(0);
        procurementSummary.setPendingInboundCount(0L);
        procurementSummary.setCompletedCount(0L);
        procurementSummary.setClosedCount(0L);
        AgentFundReconciliationSummaryVO fundSummary = new AgentFundReconciliationSummaryVO();
        fundSummary.setTotalCount(0L);
        fundSummary.setTotalAmount(0D);
        fundSummary.setRechargeAmount(0D);
        fundSummary.setWithdrawalAmount(0D);
        fundSummary.setPayAmount(0D);
        fundSummary.setRefundAmount(0D);
        fundSummary.setCommissionAmount(0D);
        agentRoleRelationService.list(Wrappers.lambdaQuery()).forEach(agent -> {
            AgentProcurementReconciliationSummaryVO procurementItem = this.procurementSummary(agent.getMemberId(), new AgentProcurementReconciliationSearchParams());
            procurementSummary.setTotalCount(procurementSummary.getTotalCount() + procurementItem.getTotalCount());
            procurementSummary.setTotalAmount(CurrencyUtil.add(procurementSummary.getTotalAmount(), procurementItem.getTotalAmount()));
            procurementSummary.setTotalQuantity(procurementSummary.getTotalQuantity() + procurementItem.getTotalQuantity());
            procurementSummary.setPendingInboundCount(procurementSummary.getPendingInboundCount() + procurementItem.getPendingInboundCount());
            procurementSummary.setCompletedCount(procurementSummary.getCompletedCount() + procurementItem.getCompletedCount());
            procurementSummary.setClosedCount(procurementSummary.getClosedCount() + procurementItem.getClosedCount());
            AgentFundReconciliationSummaryVO fundItem = this.fundSummary(agent.getMemberId(), new AgentFundReconciliationSearchParams());
            fundSummary.setTotalCount(fundSummary.getTotalCount() + fundItem.getTotalCount());
            fundSummary.setTotalAmount(CurrencyUtil.add(fundSummary.getTotalAmount(), fundItem.getTotalAmount()));
            fundSummary.setRechargeAmount(CurrencyUtil.add(fundSummary.getRechargeAmount(), fundItem.getRechargeAmount()));
            fundSummary.setWithdrawalAmount(CurrencyUtil.add(fundSummary.getWithdrawalAmount(), fundItem.getWithdrawalAmount()));
            fundSummary.setPayAmount(CurrencyUtil.add(fundSummary.getPayAmount(), fundItem.getPayAmount()));
            fundSummary.setRefundAmount(CurrencyUtil.add(fundSummary.getRefundAmount(), fundItem.getRefundAmount()));
            fundSummary.setCommissionAmount(CurrencyUtil.add(fundSummary.getCommissionAmount(), fundItem.getCommissionAmount()));
        });
        overviewVO.setProcurementSummary(procurementSummary);
        overviewVO.setFundSummary(fundSummary);
        return overviewVO;
    }

    @Override
    public IPage<PlatformProcurementReconciliationVO> platformProcurementPage(PlatformProcurementReconciliationSearchParams params) {
        PlatformProcurementReconciliationSearchParams query = params == null ? new PlatformProcurementReconciliationSearchParams() : params;
        List<AgentStoreBindVO> binds = this.listAllApprovedAgentBinds(query.getAgentMemberId());
        if (binds.isEmpty()) {
            return this.emptyPage(query);
        }
        Map<String, List<AgentStoreBindVO>> bindsByStore = binds.stream()
                .filter(item -> StringUtils.isNotBlank(item.getStoreId()))
                .collect(Collectors.groupingBy(AgentStoreBindVO::getStoreId, LinkedHashMap::new, Collectors.toList()));
        if (bindsByStore.isEmpty()) {
            return this.emptyPage(query);
        }
        LambdaQueryWrapper<ProcurementOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.in(ProcurementOrder::getStoreId, bindsByStore.keySet());
        String status = StringUtils.isNotBlank(query.getStatus()) ? query.getStatus().trim().toUpperCase(Locale.ROOT) : null;
        wrapper.eq(StringUtils.isNotBlank(status), ProcurementOrder::getStatus, status);
        wrapper.orderByDesc(ProcurementOrder::getCreateTime);
        List<PlatformProcurementReconciliationVO> records = new ArrayList<>();
        for (ProcurementOrder order : procurementOrderMapper.selectList(wrapper)) {
            List<AgentStoreBindVO> storeBinds = bindsByStore.getOrDefault(order.getStoreId(), Collections.emptyList());
            for (AgentStoreBindVO bind : storeBinds) {
                PlatformProcurementReconciliationVO vo = new PlatformProcurementReconciliationVO();
                vo.setId(bind.getAgentMemberId() + "-" + order.getId());
                vo.setOrderId(order.getId());
                vo.setOrderSn(order.getOrderSn());
                vo.setAgentMemberId(bind.getAgentMemberId());
                vo.setAgentName(StringUtils.defaultIfBlank(bind.getAgentMemberName(), bind.getAgentMemberId()));
                vo.setStoreId(order.getStoreId());
                vo.setStoreName(StringUtils.defaultIfBlank(order.getStoreName(), bind.getStoreName()));
                vo.setTotalAmount(order.getTotalAmount());
                vo.setTotalQuantity(order.getTotalQuantity());
                vo.setStatus(order.getStatus());
                vo.setRemark(order.getRemark());
                vo.setCreateTime(order.getCreateTime());
                records.add(vo);
            }
        }
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            records = records.stream()
                    .filter(item -> StringUtils.containsIgnoreCase(item.getOrderSn(), keyword)
                            || StringUtils.containsIgnoreCase(item.getAgentName(), keyword)
                            || StringUtils.containsIgnoreCase(item.getStoreName(), keyword))
                    .collect(Collectors.toList());
        }
        return this.buildPage(query, records);
    }

    @Override
    public IPage<PlatformFundReconciliationVO> platformFundPage(PlatformFundReconciliationSearchParams params) {
        PlatformFundReconciliationSearchParams query = params == null ? new PlatformFundReconciliationSearchParams() : params;
        List<AgentRoleRelationVO> agents = agentRoleRelationService.listAgentRoles().stream()
                .filter(item -> StringUtils.isBlank(query.getAgentMemberId()) || StringUtils.equals(item.getMemberId(), query.getAgentMemberId()))
                .collect(Collectors.toList());
        if (agents.isEmpty()) {
            return this.emptyPage(query);
        }
        Map<String, AgentRoleRelationVO> agentMap = agents.stream()
                .filter(item -> StringUtils.isNotBlank(item.getMemberId()))
                .collect(Collectors.toMap(AgentRoleRelationVO::getMemberId, item -> item, (left, right) -> left, LinkedHashMap::new));
        LambdaQueryWrapper<WalletLog> wrapper = Wrappers.lambdaQuery();
        wrapper.in(WalletLog::getMemberId, agentMap.keySet());
        String serviceType = StringUtils.isNotBlank(query.getServiceType()) ? query.getServiceType().trim().toUpperCase(Locale.ROOT) : null;
        wrapper.eq(StringUtils.isNotBlank(serviceType), WalletLog::getServiceType, serviceType);
        if (StringUtils.isNotBlank(query.getStartDate()) && StringUtils.isNotBlank(query.getEndDate())) {
            wrapper.between(WalletLog::getCreateTime, DateUtil.parseDate(query.getStartDate()), DateUtil.endOfDay(DateUtil.parseDate(query.getEndDate())));
        }
        wrapper.orderByDesc(WalletLog::getCreateTime);
        List<PlatformFundReconciliationVO> records = walletLogMapper.selectList(wrapper).stream().map(item -> {
            PlatformFundReconciliationVO vo = new PlatformFundReconciliationVO();
            AgentRoleRelationVO agent = agentMap.get(item.getMemberId());
            vo.setId(item.getId());
            vo.setAgentMemberId(item.getMemberId());
            vo.setAgentName(agent != null && StringUtils.isNotBlank(agent.getMemberName()) ? agent.getMemberName() : item.getMemberName());
            vo.setServiceType(item.getServiceType());
            vo.setMoney(item.getMoney());
            vo.setDetail(item.getDetail());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        if (StringUtils.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            records = records.stream()
                    .filter(item -> StringUtils.containsIgnoreCase(item.getAgentName(), keyword)
                            || StringUtils.containsIgnoreCase(item.getAgentMemberId(), keyword)
                            || StringUtils.containsIgnoreCase(item.getDetail(), keyword))
                    .collect(Collectors.toList());
        }
        return this.buildPage(query, records);
    }

    private void checkAgent(String agentMemberId) {
        if (!agentRoleRelationService.isAgent(agentMemberId)) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
    }

    private List<String> queryStoreOrderIds(List<String> storeIds) {
        if (storeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return procurementOrderMapper.selectList(Wrappers.<ProcurementOrder>lambdaQuery()
                        .select(ProcurementOrder::getId)
                        .in(ProcurementOrder::getStoreId, storeIds))
                .stream()
                .map(ProcurementOrder::getId)
                .collect(Collectors.toList());
    }

    private AgentProcurementReconciliationVO convertProcurement(ProcurementOrder order) {
        AgentProcurementReconciliationVO vo = new AgentProcurementReconciliationVO();
        BeanUtil.copyProperties(order, vo);
        return vo;
    }

    private AgentFundReconciliationVO convertFund(WalletLog walletLog) {
        AgentFundReconciliationVO vo = new AgentFundReconciliationVO();
        BeanUtil.copyProperties(walletLog, vo);
        return vo;
    }

    private <T> IPage<T> emptyPage(cn.lili.common.vo.PageVO pageVO) {
        Page<T> page = new Page<>(pageVO.getPageNumber(), pageVO.getPageSize());
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        return page;
    }

    private List<AgentStoreBindVO> listAllApprovedAgentBinds(String agentMemberId) {
        return agentRoleRelationService.listAgentRoles().stream()
                .filter(item -> StringUtils.isBlank(agentMemberId) || StringUtils.equals(item.getMemberId(), agentMemberId))
                .flatMap(item -> agentStoreBindService.listApprovedBindsByAgentMemberId(item.getMemberId()).stream())
                .collect(Collectors.toList());
    }

    private <T> IPage<T> buildPage(cn.lili.common.vo.PageVO pageVO, List<T> records) {
        Page<T> page = new Page<>(pageVO.getPageNumber(), pageVO.getPageSize());
        int fromIndex = (int) ((page.getCurrent() - 1) * page.getSize());
        if (fromIndex >= records.size()) {
            page.setRecords(Collections.emptyList());
            page.setTotal(records.size());
            return page;
        }
        int toIndex = (int) Math.min(fromIndex + page.getSize(), records.size());
        page.setRecords(records.subList(fromIndex, toIndex));
        page.setTotal(records.size());
        return page;
    }

    private LambdaQueryWrapper<ProcurementOrder> buildProcurementWrapper(List<String> storeIds, AgentProcurementReconciliationSearchParams params) {
        LambdaQueryWrapper<ProcurementOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.in(ProcurementOrder::getStoreId, storeIds);
        wrapper.eq(CharSequenceUtil.isNotBlank(params.getOrderSn()), ProcurementOrder::getOrderSn, params.getOrderSn());
        wrapper.eq(CharSequenceUtil.isNotBlank(params.getStatus()), ProcurementOrder::getStatus, params.getStatus());
        wrapper.ge(params.getStartCreateTime() != null, ProcurementOrder::getCreateTime, params.getStartCreateTime());
        wrapper.le(params.getEndCreateTime() != null, ProcurementOrder::getCreateTime, params.getEndCreateTime());
        if (CharSequenceUtil.isNotBlank(params.getGoodsName())) {
            List<String> orderIds = procurementOrderItemService.list(Wrappers.<ProcurementOrderItem>lambdaQuery()
                            .in(ProcurementOrderItem::getProcurementOrderId, this.queryStoreOrderIds(storeIds))
                            .like(ProcurementOrderItem::getGoodsName, params.getGoodsName()))
                    .stream()
                    .map(ProcurementOrderItem::getProcurementOrderId)
                    .distinct()
                    .collect(Collectors.toList());
            if (orderIds.isEmpty()) {
                wrapper.eq(ProcurementOrder::getId, "0");
                return wrapper;
            }
            wrapper.in(ProcurementOrder::getId, orderIds);
        }
        return wrapper;
    }

    private LambdaQueryWrapper<WalletLog> buildFundWrapper(String agentMemberId, AgentFundReconciliationSearchParams params) {
        LambdaQueryWrapper<WalletLog> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WalletLog::getMemberId, agentMemberId);
        wrapper.eq(CharSequenceUtil.isNotBlank(params.getServiceType()), WalletLog::getServiceType, params.getServiceType());
        if (CharSequenceUtil.isNotBlank(params.getStartDate()) && CharSequenceUtil.isNotBlank(params.getEndDate())) {
            wrapper.between(WalletLog::getCreateTime, DateUtil.parseDate(params.getStartDate()), DateUtil.endOfDay(DateUtil.parseDate(params.getEndDate())));
        }
        return wrapper;
    }
}
