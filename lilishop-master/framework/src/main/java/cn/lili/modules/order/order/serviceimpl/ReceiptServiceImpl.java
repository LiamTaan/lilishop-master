package cn.lili.modules.order.order.serviceimpl;

import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.mybatis.util.PageUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.agent.service.AgentRoleRelationService;
import cn.lili.modules.agent.service.AgentStoreBindService;
import cn.lili.modules.order.order.entity.dos.Receipt;
import cn.lili.modules.order.order.entity.dto.OrderReceiptDTO;
import cn.lili.modules.order.order.entity.dto.ReceiptInvoicingDTO;
import cn.lili.modules.order.order.entity.dto.ReceiptSearchParams;
import cn.lili.modules.order.order.mapper.ReceiptMapper;
import cn.lili.modules.order.order.service.ReceiptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 发票业务层实现
 *
 * @author Bulbasaur
 * @since 2020/11/17 7:38 下午
 */
@Service
public class ReceiptServiceImpl extends ServiceImpl<ReceiptMapper, Receipt> implements ReceiptService {

    @Autowired
    private AgentRoleRelationService agentRoleRelationService;

    @Autowired
    private AgentStoreBindService agentStoreBindService;

    @Override
    public IPage<OrderReceiptDTO> getReceiptData(ReceiptSearchParams searchParams, PageVO pageVO) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        if (currentUser.getRole() == UserEnums.MEMBER) {
            searchParams.setMemberId(currentUser.getId());
        } else if (currentUser.getRole() == UserEnums.STORE && searchParams.getStoreId() == null) {
            searchParams.setStoreId(currentUser.getStoreId());
        }
        return this.baseMapper.getReceipt(PageUtil.initPage(pageVO), searchParams.wrapper());
    }

    @Override
    public Receipt getByOrderSn(String orderSn) {
        LambdaQueryWrapper<Receipt> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(Receipt::getOrderSn, orderSn);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public Receipt getDetail(String id) {
        Receipt receipt = this.getById(id);
        this.checkReceiptPermission(receipt);
        return receipt;
    }

    @Override
    public Receipt saveReceipt(Receipt receipt) {
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        receipt.setMemberId(currentUser.getId());
        LambdaQueryWrapper<Receipt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Receipt::getReceiptTitle, receipt.getReceiptTitle());
        queryWrapper.eq(Receipt::getMemberId, receipt.getMemberId());
        if (receipt.getId() != null) {
            queryWrapper.ne(Receipt::getId, receipt.getId());
        }
        if (this.getOne(queryWrapper) == null) {
            this.saveOrUpdate(receipt);
            return receipt;
        }
        return null;
    }

    @Override
    public Receipt invoicing(String receiptId, ReceiptInvoicingDTO receiptInvoicingDTO) {
        //根据id查询发票信息
        Receipt receipt = this.getById(receiptId);
        if (receipt != null) {
            this.checkReceiptPermission(receipt);
            if (receiptInvoicingDTO == null || receiptInvoicingDTO.getInvoiceAddress() == null || receiptInvoicingDTO.getInvoiceAddress().trim().isEmpty()) {
                throw new ServiceException(ResultCode.PARAMS_ERROR);
            }
            receipt.setInvoiceAddress(receiptInvoicingDTO.getInvoiceAddress().trim());
            receipt.setReceiptStatus(1);
            this.saveOrUpdate(receipt);
            return receipt;
        }
        throw new ServiceException(ResultCode.USER_RECEIPT_NOT_EXIST);
    }

    private void checkReceiptPermission(Receipt receipt) {
        if (receipt == null) {
            throw new ServiceException(ResultCode.USER_RECEIPT_NOT_EXIST);
        }
        AuthUser currentUser = Objects.requireNonNull(UserContext.getCurrentUser());
        if (currentUser.getRole() == UserEnums.MANAGER) {
            return;
        }
        if (currentUser.getRole() == UserEnums.STORE) {
            if (!Objects.equals(currentUser.getStoreId(), receipt.getStoreId())) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            }
            return;
        }
        if (!currentUser.getId().equals(receipt.getMemberId())) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        if (agentRoleRelationService.isAgent(currentUser.getId())) {
            List<String> permittedStoreIds = agentStoreBindService.listApprovedStoreIdsByAgentMemberId(currentUser.getId());
            if (!permittedStoreIds.contains(receipt.getStoreId())) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            }
        }
    }
}
