package cn.lili.controller.order;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.order.aftersale.entity.dos.AfterSale;
import cn.lili.modules.order.aftersale.entity.vo.AfterSaleManageSummaryVO;
import cn.lili.modules.order.aftersale.entity.vo.AfterSaleNumVO;
import cn.lili.modules.order.aftersale.entity.vo.AfterSaleSearchParams;
import cn.lili.modules.order.aftersale.entity.vo.AfterSaleVO;
import cn.lili.modules.order.aftersale.service.AfterSaleService;
import cn.lili.modules.store.entity.dto.StoreAfterSaleAddressDTO;
import cn.lili.modules.system.entity.vo.Traces;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端,售后接口
 *
 * @author Bulbasaur
 * @since 2021/1/6 14:11
 */
@RestController
@RequestMapping("/manager/order/afterSale")
public class AfterSaleManagerController {

    /**
     * 售后
     */
    @Autowired
    private AfterSaleService afterSaleService;

    @GetMapping("/page")
    public ResultMessage<IPage<AfterSaleVO>> getByPage(AfterSaleSearchParams searchParams) {
        return ResultUtil.data(afterSaleService.getAfterSalePages(searchParams));
    }

    @GetMapping("/afterSaleNumVO")
    public ResultMessage<AfterSaleNumVO> getAfterSaleNumVO(AfterSaleSearchParams afterSaleSearchParams) {
        return ResultUtil.data(afterSaleService.getAfterSaleNumVO(afterSaleSearchParams));
    }

    @GetMapping("/summary")
    public ResultMessage<AfterSaleManageSummaryVO> getSummary(AfterSaleSearchParams afterSaleSearchParams) {
        return ResultUtil.data(afterSaleService.getAfterSaleManageSummaryVO(afterSaleSearchParams));
    }

    @GetMapping("/exportAfterSaleOrder")
    public ResultMessage<List<AfterSale>> exportAfterSaleOrder(AfterSaleSearchParams searchParams) {
        return ResultUtil.data(afterSaleService.exportAfterSaleOrder(searchParams));
    }

    @GetMapping("/get/{sn}")
    public ResultMessage<AfterSaleVO> get(@NotNull(message = "售后单号") @PathVariable("sn") String sn) {
        return ResultUtil.data(afterSaleService.getAfterSale(sn));
    }

    @GetMapping("/getDeliveryTraces/{sn}")
    public ResultMessage<Traces> getDeliveryTraces(@PathVariable String sn) {
        return ResultUtil.data(afterSaleService.deliveryTraces(sn));
    }

    @PreventDuplicateSubmissions
    @PutMapping("/refund/{afterSaleSn}")
    public ResultMessage<AfterSale> refund(@NotNull(message = "请选择售后单") @PathVariable String afterSaleSn,
                                           @RequestParam String remark) {

        return ResultUtil.data(afterSaleService.refund(afterSaleSn, remark));
    }

    @PreventDuplicateSubmissions
    @PutMapping("/review/{afterSaleSn}")
    public ResultMessage<AfterSale> review(@NotNull(message = "请选择售后单") @PathVariable String afterSaleSn,
                                           @NotNull(message = "请审核") String serviceStatus,
                                           String remark,
                                           Double actualRefundPrice) {

        return ResultUtil.data(afterSaleService.review(afterSaleSn, serviceStatus, remark,actualRefundPrice));
    }

    @GetMapping("/getStoreAfterSaleAddress/{sn}")
    public ResultMessage<StoreAfterSaleAddressDTO> getStoreAfterSaleAddress(@NotNull(message = "售后单号") @PathVariable("sn") String sn) {
        return ResultUtil.data(afterSaleService.getStoreAfterSaleAddressDTO(sn));
    }
}
