package cn.lili.controller.distribution;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.context.ThreadContextHolder;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.DistributionCash;
import cn.lili.modules.distribution.entity.vos.DistributionCashSearchParams;
import cn.lili.modules.distribution.service.DistributionCashService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端,分销佣金管理接口
 *
 * @author pikachu
 * @since 2020-03-14 23:04:56
 */
@RestController
@RequestMapping("/manager/distribution/cash")
public class DistributionCashManagerController {

    @Autowired
    private DistributionCashService distributorCashService;

    @GetMapping("/get/{id}")
    public ResultMessage<DistributionCash> get(
 @PathVariable String id) {
        return ResultUtil.data(distributorCashService.getById(id));
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<DistributionCash>> getByPage(DistributionCashSearchParams distributionCashSearchParams) {

        return ResultUtil.data(distributorCashService.getDistributionCash(distributionCashSearchParams));
    }


    @PreventDuplicateSubmissions
    @PostMapping("/audit/{id}")
    public ResultMessage<DistributionCash> audit(
 @PathVariable String id, 
 @NotNull String result) {
        return ResultUtil.data(distributorCashService.audit(id, result));
    }


    @GetMapping("/queryExport")
    public void queryExport(DistributionCashSearchParams distributionCashSearchParams) {
        HttpServletResponse response = ThreadContextHolder.getHttpResponse();
        distributorCashService.queryExport(response, distributionCashSearchParams);
    }
}
