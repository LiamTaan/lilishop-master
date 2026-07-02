package cn.lili.modules.procurement.service;

import cn.lili.modules.procurement.entity.dos.ProcurementInbound;
import cn.lili.modules.procurement.entity.params.ProcurementInboundSearchParams;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 采购入库业务接口
 * 采购入库由订单完成链路自动驱动，接口仅保留查询能力。
 * @author Bulbasaur
 * @since 2025-12-18
 */
public interface ProcurementInboundService extends IService<ProcurementInbound> {
    /**
     * 分页查询采购入库单
     * @param params 分页查询参数
     * @return 采购入库单分页数据
     */
    IPage<ProcurementInbound> page(ProcurementInboundSearchParams params);
    /**
     * 查询采购入库单详情
     * @param id 采购入库单ID
     * @return 采购入库单详情
     */
    ProcurementInbound getDetail(String id);
}
