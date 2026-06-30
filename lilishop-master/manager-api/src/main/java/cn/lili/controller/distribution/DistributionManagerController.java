package cn.lili.controller.distribution;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.Distribution;
import cn.lili.modules.distribution.entity.dto.DistributionSearchParams;
import cn.lili.modules.distribution.service.DistributionService;
import cn.lili.modules.goods.entity.vos.BrandVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 管理端,分销员管理接口
 *
 * @author pikachu
 * @since 2020-03-14 23:04:56
 */
@RestController
@RequestMapping("/manager/distribution/distribution")
public class DistributionManagerController {

    @Autowired
    private DistributionService distributionService;

    @GetMapping("/getByPage")
    public ResultMessage<IPage<Distribution>> getByPage(DistributionSearchParams distributionSearchParams, PageVO page) {
        return ResultUtil.data(distributionService.distributionPage(distributionSearchParams, page));
    }


    @PreventDuplicateSubmissions
    @PutMapping("/retreat/{id}")
    public ResultMessage<Object> retreat(
 @PathVariable String id) {
        if (distributionService.retreat(id)) {
            return ResultUtil.success();
        } else {
            throw new ServiceException(ResultCode.DISTRIBUTION_RETREAT_ERROR);
        }

    }

    @PreventDuplicateSubmissions
    @PutMapping("/resume/{id}")
    public ResultMessage<Object> resume(
 @PathVariable String id) {
        if (distributionService.resume(id)) {
            return ResultUtil.success();
        } else {
            throw new ServiceException(ResultCode.DISTRIBUTION_RETREAT_ERROR);
        }

    }

    @PreventDuplicateSubmissions
    @PutMapping("/audit/{id}")
    public ResultMessage<Object> audit(
 @NotNull @PathVariable String id, 
 @NotNull String status) {
        if (distributionService.audit(id, status)) {
            return ResultUtil.success();
        } else {
            throw new ServiceException(ResultCode.DISTRIBUTION_AUDIT_ERROR);
        }
    }


    @PutMapping("/{id}")
    public ResultMessage<Distribution> update(
 @PathVariable String id, @Valid Distribution distribution) {
        distribution.setId(id);
        if (distributionService.updateById(distribution)) {
            return ResultUtil.data(distribution);
        }
        throw new ServiceException(ResultCode.DISTRIBUTION_EDIT_ERROR);
    }
}
