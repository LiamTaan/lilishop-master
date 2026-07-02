package cn.lili.modules.procurement.service;

import cn.lili.modules.procurement.entity.dos.ProcurementOrder;
import cn.lili.modules.procurement.entity.params.ProcurementOrderSearchParams;
import cn.lili.modules.procurement.entity.vos.ProcurementOrderVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 采购单业务接口
 * 采购单生命周期由订单链路自动驱动，接口仅保留查询能力。
 * @author Bulbasaur
 * @since 2025-12-18
 */
public interface ProcurementOrderService extends IService<ProcurementOrder> {
    /**
     * 查询采购单详情
     * @param id 采购单ID
     * @return 采购单详情VO
     */
    ProcurementOrderVO getDetail(String id);
    /**
     * 分页查询采购单
     * @param params 分页查询参数
     * @return 采购单分页数据
     */
    IPage<ProcurementOrder> page(ProcurementOrderSearchParams params);
}
