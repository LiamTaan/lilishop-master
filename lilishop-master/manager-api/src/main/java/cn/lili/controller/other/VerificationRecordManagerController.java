package cn.lili.controller.other;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.verification.entity.params.VerificationRecordSearchParams;
import cn.lili.modules.verification.entity.vos.VerificationExceptionSummaryVO;
import cn.lili.modules.verification.entity.vos.VerificationRecordSummaryVO;
import cn.lili.modules.verification.entity.vos.VerificationRecordVO;
import cn.lili.modules.verification.service.VerificationRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端,核销记录接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@RequestMapping("/manager/other/verificationRecord")
public class VerificationRecordManagerController {

    @Autowired
    private VerificationRecordService verificationRecordService;

    @GetMapping
    public ResultMessage<IPage<VerificationRecordVO>> page(VerificationRecordSearchParams searchParams, PageVO pageVO) {
        return ResultUtil.data(verificationRecordService.page(searchParams, pageVO));
    }

    @GetMapping("/summary")
    public ResultMessage<VerificationRecordSummaryVO> summary(VerificationRecordSearchParams searchParams) {
        return ResultUtil.data(verificationRecordService.summary(searchParams));
    }

    @GetMapping("/exception")
    public ResultMessage<IPage<VerificationRecordVO>> exceptionPage(VerificationRecordSearchParams searchParams, PageVO pageVO) {
        return ResultUtil.data(verificationRecordService.exceptionPage(searchParams, pageVO));
    }

    @GetMapping("/exception/list")
    public ResultMessage<List<VerificationRecordVO>> exceptionList(VerificationRecordSearchParams searchParams) {
        return ResultUtil.data(verificationRecordService.exceptionList(searchParams));
    }

    @GetMapping("/exception/summary")
    public ResultMessage<VerificationExceptionSummaryVO> exceptionSummary(VerificationRecordSearchParams searchParams) {
        return ResultUtil.data(verificationRecordService.exceptionSummary(searchParams));
    }
}
