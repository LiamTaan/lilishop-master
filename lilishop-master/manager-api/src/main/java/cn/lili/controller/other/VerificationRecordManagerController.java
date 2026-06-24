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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "管理端,核销记录接口")
@RequestMapping("/manager/other/verificationRecord")
public class VerificationRecordManagerController {

    @Autowired
    private VerificationRecordService verificationRecordService;

    @Operation(summary = "分页获取核销记录")
    @GetMapping
    public ResultMessage<IPage<VerificationRecordVO>> page(VerificationRecordSearchParams searchParams, PageVO pageVO) {
        return ResultUtil.data(verificationRecordService.page(searchParams, pageVO));
    }

    @Operation(summary = "获取核销治理汇总")
    @GetMapping("/summary")
    public ResultMessage<VerificationRecordSummaryVO> summary(VerificationRecordSearchParams searchParams) {
        return ResultUtil.data(verificationRecordService.summary(searchParams));
    }

    @Operation(summary = "分页获取异常核销记录")
    @GetMapping("/exception")
    public ResultMessage<IPage<VerificationRecordVO>> exceptionPage(VerificationRecordSearchParams searchParams, PageVO pageVO) {
        return ResultUtil.data(verificationRecordService.exceptionPage(searchParams, pageVO));
    }

    @Operation(summary = "查询异常核销导出列表")
    @GetMapping("/exception/list")
    public ResultMessage<List<VerificationRecordVO>> exceptionList(VerificationRecordSearchParams searchParams) {
        return ResultUtil.data(verificationRecordService.exceptionList(searchParams));
    }

    @Operation(summary = "获取异常核销治理汇总")
    @GetMapping("/exception/summary")
    public ResultMessage<VerificationExceptionSummaryVO> exceptionSummary(VerificationRecordSearchParams searchParams) {
        return ResultUtil.data(verificationRecordService.exceptionSummary(searchParams));
    }
}
