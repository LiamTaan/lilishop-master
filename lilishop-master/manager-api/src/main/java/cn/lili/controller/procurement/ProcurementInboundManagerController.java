package cn.lili.controller.procurement;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.OperationalJudgment;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.procurement.entity.dos.ProcurementInbound;
import cn.lili.modules.procurement.entity.dos.ProcurementInboundItem;
import cn.lili.modules.procurement.entity.params.ProcurementInboundSearchParams;
import cn.lili.modules.procurement.service.ProcurementInboundItemService;
import cn.lili.modules.procurement.service.ProcurementInboundService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端,采购入库接口
 * 提供入库单分页、详情与明细查询
 *
 * @author Bulbasaur
 * @since 2025-12-18
 */
@RestController
@RequestMapping("/manager/procurement/inbound")
public class ProcurementInboundManagerController {

    @Autowired
    private ProcurementInboundService procurementInboundService;
    @Autowired
    private ProcurementInboundItemService procurementInboundItemService;

    @GetMapping
    public ResultMessage<IPage<ProcurementInbound>> page(ProcurementInboundSearchParams params) {
        return ResultUtil.data(procurementInboundService.page(params));
    }

    @GetMapping("/{id}")
    public ResultMessage<ProcurementInbound> detail(@PathVariable String id) {
        return ResultUtil.data(procurementInboundService.getDetail(id));
    }

    @GetMapping("/{id}/items")
    public ResultMessage<List<ProcurementInboundItem>> items(@PathVariable String id) {
        ProcurementInbound inbound = OperationalJudgment.judgment(procurementInboundService.getById(id));
        return ResultUtil.data(procurementInboundItemService.list(Wrappers.<ProcurementInboundItem>lambdaQuery()
                .eq(ProcurementInboundItem::getInboundId, inbound.getId())));
    }
}
