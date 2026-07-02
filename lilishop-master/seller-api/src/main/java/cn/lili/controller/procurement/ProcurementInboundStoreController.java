package cn.lili.controller.procurement;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.procurement.entity.dos.ProcurementInbound;
import cn.lili.modules.procurement.entity.params.ProcurementInboundSearchParams;
import cn.lili.modules.procurement.service.ProcurementInboundService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 代理商端采购入库查询接口
 * 采购入库由订单完成链路自动生成，店铺端仅保留查询能力。
 * @author Bulbasaur
 * @since 2025-12-18
 */
@Tag(name = "代理商端,采购入库查询接口")
@RestController
@RequestMapping("/store/procurement/inbound")
public class ProcurementInboundStoreController {

    @Autowired
    private ProcurementInboundService procurementInboundService;

    @Operation(summary = "代理商分页查询采购入库单")
    @GetMapping
    public ResultMessage<IPage<ProcurementInbound>> page(ProcurementInboundSearchParams params) {
        return ResultUtil.data(procurementInboundService.page(params));
    }

    @Operation(summary = "代理商查询采购入库单详情")
    @Parameter(name = "id", description = "入库单ID", required = true)
    @GetMapping("/{id}")
    public ResultMessage<ProcurementInbound> detail(@PathVariable String id) {
        return ResultUtil.data(procurementInboundService.getDetail(id));
    }
}
