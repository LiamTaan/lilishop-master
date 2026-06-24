package cn.lili.controller.order;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.verification.entity.params.VerificationRecordSearchParams;
import cn.lili.modules.verification.entity.vos.VerificationRecordVO;
import cn.lili.modules.verification.service.VerificationRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 店铺端,核销记录接口
 *
 * @author dawn
 * @since 2026/6/17
 */
@RestController
@Tag(name = "店铺端,核销记录接口")
@RequestMapping("/store/order/verificationRecord")
public class VerificationRecordStoreController {

    @Autowired
    private VerificationRecordService verificationRecordService;

    @Operation(summary = "分页获取店铺核销记录")
    @GetMapping
    public ResultMessage<IPage<VerificationRecordVO>> page(VerificationRecordSearchParams searchParams, PageVO pageVO) {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        return ResultUtil.data(verificationRecordService.pageByStoreId(storeId, searchParams, pageVO));
    }
}
