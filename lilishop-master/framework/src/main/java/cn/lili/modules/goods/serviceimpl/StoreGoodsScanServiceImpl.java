package cn.lili.modules.goods.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.vos.StoreGoodsScanVO;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.goods.service.StoreGoodsScanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 店铺端商品扫码服务实现
 *
 * @author dawn
 * @since 2026/6/22
 */
@Service
public class StoreGoodsScanServiceImpl implements StoreGoodsScanService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsSkuService goodsSkuService;

    @Override
    public StoreGoodsScanVO scan(String storeId, String barcode) {
        StoreGoodsScanVO vo = new StoreGoodsScanVO();
        vo.setBarcode(barcode);
        vo.setMatched(Boolean.FALSE);
        vo.setMatchType("NONE");
        if (CharSequenceUtil.isBlank(barcode)) {
            return vo;
        }

        List<GoodsSku> skuMatches = goodsSkuService.list(new LambdaQueryWrapper<GoodsSku>()
                .eq(GoodsSku::getStoreId, storeId)
                .eq(GoodsSku::getBarcode, barcode)
                .eq(GoodsSku::getDeleteFlag, Boolean.FALSE)
                .orderByDesc(GoodsSku::getCreateTime)
                .last("limit 1"));
        if (!skuMatches.isEmpty()) {
            GoodsSku goodsSku = skuMatches.get(0);
            Goods goods = goodsService.getById(goodsSku.getGoodsId());
            vo.setMatched(Boolean.TRUE);
            vo.setMatchType("SKU");
            vo.setGoodsId(goodsSku.getGoodsId());
            vo.setGoodsName(goods != null ? goods.getGoodsName() : goodsSku.getGoodsName());
            vo.setGoodsBarcode(goods != null ? goods.getBarcode() : null);
            vo.setSkuId(goodsSku.getId());
            vo.setSkuName(goodsSku.getGoodsName());
            vo.setSkuBarcode(goodsSku.getBarcode());
            vo.setSimpleSpecs(goodsSku.getSimpleSpecs());
            return vo;
        }

        List<Goods> goodsMatches = goodsService.list(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStoreId, storeId)
                .eq(Goods::getBarcode, barcode)
                .eq(Goods::getDeleteFlag, Boolean.FALSE)
                .orderByDesc(Goods::getCreateTime)
                .last("limit 1"));
        if (!goodsMatches.isEmpty()) {
            Goods goods = goodsMatches.get(0);
            vo.setMatched(Boolean.TRUE);
            vo.setMatchType("GOODS");
            vo.setGoodsId(goods.getId());
            vo.setGoodsName(goods.getGoodsName());
            vo.setGoodsBarcode(goods.getBarcode());
            List<GoodsSku> skus = goodsSkuService.listByGoodsIdAndStoreId(goods.getId(), storeId);
            if (skus.size() == 1) {
                GoodsSku goodsSku = skus.get(0);
                vo.setSkuId(goodsSku.getId());
                vo.setSkuName(goodsSku.getGoodsName());
                vo.setSkuBarcode(goodsSku.getBarcode());
                vo.setSimpleSpecs(goodsSku.getSimpleSpecs());
            }
        }
        return vo;
    }
}
