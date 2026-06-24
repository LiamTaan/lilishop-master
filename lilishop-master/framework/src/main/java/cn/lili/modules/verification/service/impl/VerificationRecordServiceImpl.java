package cn.lili.modules.verification.service.impl;

import cn.lili.common.vo.PageVO;
import cn.lili.modules.verification.entity.dos.VerificationRecord;
import cn.lili.modules.verification.entity.params.VerificationRecordSearchParams;
import cn.lili.modules.verification.entity.vos.VerificationExceptionSummaryVO;
import cn.lili.modules.verification.entity.vos.VerificationRecordSummaryVO;
import cn.lili.modules.verification.entity.vos.VerificationRecordVO;
import cn.lili.modules.verification.mapper.VerificationRecordMapper;
import cn.lili.modules.verification.service.VerificationRecordService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 核销记录业务层实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class VerificationRecordServiceImpl extends ServiceImpl<VerificationRecordMapper, VerificationRecord> implements VerificationRecordService {

    @Override
    public IPage<VerificationRecordVO> page(VerificationRecordSearchParams searchParams, PageVO pageVO) {
        return this.queryPage(this.buildWrapper(searchParams), pageVO);
    }

    @Override
    public IPage<VerificationRecordVO> pageByMemberId(String memberId, VerificationRecordSearchParams searchParams, PageVO pageVO) {
        LambdaQueryWrapper<VerificationRecord> wrapper = this.buildWrapper(searchParams);
        wrapper.eq(VerificationRecord::getMemberId, memberId);
        return this.queryPage(wrapper, pageVO);
    }

    @Override
    public IPage<VerificationRecordVO> pageByStoreId(String storeId, VerificationRecordSearchParams searchParams, PageVO pageVO) {
        LambdaQueryWrapper<VerificationRecord> wrapper = this.buildWrapper(searchParams);
        wrapper.eq(VerificationRecord::getStoreId, storeId);
        return this.queryPage(wrapper, pageVO);
    }

    @Override
    public VerificationRecordSummaryVO summary(VerificationRecordSearchParams searchParams) {
        List<VerificationRecord> records = this.list(this.buildWrapper(searchParams));
        VerificationRecordSummaryVO summary = new VerificationRecordSummaryVO();
        long successCount = 0L;
        long failCount = 0L;
        long exceptionCount = 0L;
        long buyerSourceCount = 0L;
        long storeSourceCount = 0L;
        long managerSourceCount = 0L;
        for (VerificationRecord record : records) {
            if ("SUCCESS".equals(record.getResultType())) {
                successCount++;
            } else if ("FAIL".equals(record.getResultType())) {
                failCount++;
                if (record.getRemark() != null && !record.getRemark().isBlank()) {
                    exceptionCount++;
                }
            }
            if ("BUYER".equals(record.getSourceType())) {
                buyerSourceCount++;
            } else if ("STORE".equals(record.getSourceType())) {
                storeSourceCount++;
            } else if ("MANAGER".equals(record.getSourceType())) {
                managerSourceCount++;
            }
        }
        summary.setTotalCount((long) records.size());
        summary.setSuccessCount(successCount);
        summary.setFailCount(failCount);
        summary.setExceptionCount(exceptionCount);
        summary.setBuyerSourceCount(buyerSourceCount);
        summary.setStoreSourceCount(storeSourceCount);
        summary.setManagerSourceCount(managerSourceCount);
        return summary;
    }

    @Override
    public IPage<VerificationRecordVO> exceptionPage(VerificationRecordSearchParams searchParams, PageVO pageVO) {
        LambdaQueryWrapper<VerificationRecord> wrapper = this.buildExceptionWrapper(searchParams);
        return this.queryPage(wrapper, pageVO);
    }

    @Override
    public List<VerificationRecordVO> exceptionList(VerificationRecordSearchParams searchParams) {
        return this.list(this.buildExceptionWrapper(searchParams)).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public VerificationExceptionSummaryVO exceptionSummary(VerificationRecordSearchParams searchParams) {
        List<VerificationRecord> records = this.list(this.buildExceptionWrapper(searchParams));
        VerificationExceptionSummaryVO summaryVO = new VerificationExceptionSummaryVO();
        long storeExceptionCount = 0L;
        long managerExceptionCount = 0L;
        long buyerExceptionCount = 0L;
        for (VerificationRecord record : records) {
            if ("STORE".equals(record.getSourceType())) {
                storeExceptionCount++;
            } else if ("MANAGER".equals(record.getSourceType())) {
                managerExceptionCount++;
            } else if ("BUYER".equals(record.getSourceType())) {
                buyerExceptionCount++;
            }
        }
        summaryVO.setExceptionCount((long) records.size());
        summaryVO.setStoreExceptionCount(storeExceptionCount);
        summaryVO.setManagerExceptionCount(managerExceptionCount);
        summaryVO.setBuyerExceptionCount(buyerExceptionCount);
        return summaryVO;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveFailureRecord(VerificationRecord record) {
        this.save(record);
    }

    private IPage<VerificationRecordVO> queryPage(LambdaQueryWrapper<VerificationRecord> wrapper, PageVO pageVO) {
        Page<VerificationRecord> page = PageUtil.initPage(pageVO);
        long total = this.baseMapper.selectCount(wrapper);
        List<VerificationRecord> pageRecords = new ArrayList<>();
        if (total > 0) {
            long offset = Math.max(0L, (page.getCurrent() - 1) * page.getSize());
            LambdaQueryWrapper<VerificationRecord> pageWrapper = wrapper.clone();
            pageWrapper.last("LIMIT " + offset + "," + page.getSize());
            pageRecords = this.baseMapper.selectList(pageWrapper);
        }
        page.setTotal(total);
        page.setRecords(pageRecords);
        List<VerificationRecordVO> records = pageRecords.stream()
                .map(this::convert)
                .collect(Collectors.toList());
        return PageUtil.convertPage(page, records);
    }

    private LambdaQueryWrapper<VerificationRecord> buildWrapper(VerificationRecordSearchParams searchParams) {
        VerificationRecordSearchParams params = searchParams == null ? new VerificationRecordSearchParams() : searchParams;
        return Wrappers.<VerificationRecord>lambdaQuery()
                .eq(params.getSourceType() != null, VerificationRecord::getSourceType, params.getSourceType())
                .eq(params.getResultType() != null, VerificationRecord::getResultType, params.getResultType())
                .eq(params.getStoreId() != null, VerificationRecord::getStoreId, params.getStoreId())
                .eq(params.getMemberId() != null, VerificationRecord::getMemberId, params.getMemberId())
                .eq(params.getOperatorId() != null, VerificationRecord::getOperatorId, params.getOperatorId())
                .like(params.getOrderSn() != null, VerificationRecord::getOrderSn, params.getOrderSn())
                .like(params.getStoreName() != null, VerificationRecord::getStoreName, params.getStoreName())
                .like(params.getMemberName() != null, VerificationRecord::getMemberName, params.getMemberName())
                .like(params.getVerificationCode() != null, VerificationRecord::getVerificationCode, params.getVerificationCode())
                .like(params.getOperatorName() != null, VerificationRecord::getOperatorName, params.getOperatorName())
                .ge(params.getStartDateTime() != null, VerificationRecord::getVerifyTime, params.getStartDateTime())
                .le(params.getEndDateTime() != null, VerificationRecord::getVerifyTime, params.getEndDateTime())
                .orderByDesc(VerificationRecord::getVerifyTime)
                .orderByDesc(VerificationRecord::getCreateTime);
    }

    private VerificationRecordVO convert(VerificationRecord record) {
        VerificationRecordVO vo = new VerificationRecordVO();
        vo.setId(record.getId());
        vo.setOrderSn(record.getOrderSn());
        vo.setStoreId(record.getStoreId());
        vo.setStoreName(record.getStoreName());
        vo.setMemberId(record.getMemberId());
        vo.setMemberName(record.getMemberName());
        vo.setVerificationCode(record.getVerificationCode());
        vo.setOperatorId(record.getOperatorId());
        vo.setOperatorName(record.getOperatorName());
        vo.setSourceType(record.getSourceType());
        vo.setResultType(record.getResultType());
        vo.setRemark(record.getRemark());
        vo.setVerifyTime(record.getVerifyTime());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private LambdaQueryWrapper<VerificationRecord> buildExceptionWrapper(VerificationRecordSearchParams searchParams) {
        LambdaQueryWrapper<VerificationRecord> wrapper = this.buildWrapper(searchParams);
        wrapper.eq(VerificationRecord::getResultType, "FAIL");
        wrapper.isNotNull(VerificationRecord::getRemark);
        wrapper.ne(VerificationRecord::getRemark, "");
        return wrapper;
    }
}
