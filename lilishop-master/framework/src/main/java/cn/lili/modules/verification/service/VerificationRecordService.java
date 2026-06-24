package cn.lili.modules.verification.service;

import cn.lili.common.vo.PageVO;
import cn.lili.modules.verification.entity.dos.VerificationRecord;
import cn.lili.modules.verification.entity.params.VerificationRecordSearchParams;
import cn.lili.modules.verification.entity.vos.VerificationExceptionSummaryVO;
import cn.lili.modules.verification.entity.vos.VerificationRecordSummaryVO;
import cn.lili.modules.verification.entity.vos.VerificationRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 核销记录业务层
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface VerificationRecordService extends IService<VerificationRecord> {

    /**
     * 分页查询核销记录
     *
     * @param record 核销记录查询条件
     * @param pageVO 分页参数
     * @return 分页结果
     */
    IPage<VerificationRecordVO> page(VerificationRecordSearchParams searchParams, PageVO pageVO);

    /**
     * 按当前会员分页查询核销记录
     *
     * @param memberId 会员ID
     * @param record 查询条件
     * @param pageVO 分页参数
     * @return 分页结果
     */
    IPage<VerificationRecordVO> pageByMemberId(String memberId, VerificationRecordSearchParams searchParams, PageVO pageVO);

    /**
     * 按当前店铺分页查询核销记录
     *
     * @param storeId 店铺ID
     * @param record 查询条件
     * @param pageVO 分页参数
     * @return 分页结果
     */
    IPage<VerificationRecordVO> pageByStoreId(String storeId, VerificationRecordSearchParams searchParams, PageVO pageVO);

    /**
     * 汇总核销记录
     *
     * @param searchParams 查询条件
     * @return 汇总结果
     */
    VerificationRecordSummaryVO summary(VerificationRecordSearchParams searchParams);

    /**
     * 核销异常分页
     *
     * @param searchParams 查询条件
     * @param pageVO 分页参数
     * @return 分页结果
     */
    IPage<VerificationRecordVO> exceptionPage(VerificationRecordSearchParams searchParams, PageVO pageVO);

    /**
     * 核销异常导出查询
     *
     * @param searchParams 查询条件
     * @return 列表结果
     */
    List<VerificationRecordVO> exceptionList(VerificationRecordSearchParams searchParams);

    /**
     * 核销异常治理汇总
     *
     * @param searchParams 查询条件
     * @return 汇总结果
     */
    VerificationExceptionSummaryVO exceptionSummary(VerificationRecordSearchParams searchParams);

    /**
     * 使用独立事务保存核销失败记录，避免外层业务异常回滚后丢失异常留痕
     *
     * @param record 核销记录
     */
    void saveFailureRecord(VerificationRecord record);
}
