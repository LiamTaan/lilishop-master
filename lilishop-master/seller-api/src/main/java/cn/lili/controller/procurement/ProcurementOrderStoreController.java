package cn.lili.controller.procurement;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.procurement.entity.dos.ProcurementOrder;
import cn.lili.modules.procurement.entity.params.ProcurementOrderSearchParams;
import cn.lili.modules.procurement.entity.vos.ProcurementOrderVO;
import cn.lili.modules.procurement.service.ProcurementOrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 代理商端采购单查询接口
 * 采购单由代理商订单链路自动生成，店铺端仅保留查询能力。
 * @author Bulbasaur
 * @since 2025-12-18
 */
@Tag(name = "代理商端,采购单查询接口")
@RestController
@RequestMapping("/store/procurement/order")
public class ProcurementOrderStoreController {

    @Autowired
    private ProcurementOrderService procurementOrderService;

    @Operation(summary = "代理商查询采购单详情")
    @Parameter(name = "id", description = "采购单ID", required = true)
    @GetMapping("/{id}")
    public ResultMessage<ProcurementOrderVO> detail(@PathVariable String id) {
        return ResultUtil.data(procurementOrderService.getDetail(id));
    }

    @Operation(summary = "代理商分页查询采购单")
    @GetMapping
    public ResultMessage<IPage<ProcurementOrder>> page(ProcurementOrderSearchParams params) {
        return ResultUtil.data(procurementOrderService.page(params));
    }
}
