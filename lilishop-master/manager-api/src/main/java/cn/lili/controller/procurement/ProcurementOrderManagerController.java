package cn.lili.controller.procurement;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.procurement.entity.dos.ProcurementOrder;
import cn.lili.modules.procurement.entity.params.ProcurementOrderSearchParams;
import cn.lili.modules.procurement.entity.vos.ProcurementOrderVO;
import cn.lili.modules.procurement.service.ProcurementOrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端,采购单接口
 * 提供采购单分页与详情查询
 *
 * @author Bulbasaur
 * @since 2025-12-18
 */
@RestController
@RequestMapping("/manager/procurement/order")
public class ProcurementOrderManagerController {

    @Autowired
    private ProcurementOrderService procurementOrderService;

    @GetMapping("/{id}")
    public ResultMessage<ProcurementOrderVO> detail(@PathVariable String id) {
        return ResultUtil.data(procurementOrderService.getDetail(id));
    }

    @GetMapping
    public ResultMessage<IPage<ProcurementOrder>> page(ProcurementOrderSearchParams params) {
        return ResultUtil.data(procurementOrderService.page(params));
    }
}
