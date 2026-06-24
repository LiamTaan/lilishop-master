package cn.lili.modules.goods.service;

import cn.lili.modules.goods.entity.vos.StoreGoodsScanVO;

/**
 * 店铺端商品扫码服务
 *
 * @author dawn
 * @since 2026/6/22
 */
public interface StoreGoodsScanService {

    /**
     * 根据条码扫描商品
     *
     * @param storeId 店铺ID
     * @param barcode 条码
     * @return 扫码结果
     */
    StoreGoodsScanVO scan(String storeId, String barcode);
}
