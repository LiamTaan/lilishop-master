package cn.lili.modules.statistics.service;

import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.enums.GoodsAuthEnum;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品统计业务层
 *
 * @author Bulbasaur
 * @since 2020/12/9 11:06
 */
public interface GoodsStatisticsService extends IService<Goods> {

    /**
     * 获取所有的已上架的商品数量
     *
     * @param goodsStatusEnum 商品状态枚举
     * @param goodsAuthEnum   商品审核枚举
     * @return 所有的已上架的商品数量
     */
    long goodsNum(GoodsStatusEnum goodsStatusEnum, GoodsAuthEnum goodsAuthEnum);

    /**
     * 按指定店铺获取商品数量
     *
     * @param storeId          店铺ID
     * @param goodsStatusEnum  商品状态枚举
     * @param goodsAuthEnum    商品审核枚举
     * @return 商品数量
     */
    long goodsNum(String storeId, GoodsStatusEnum goodsStatusEnum, GoodsAuthEnum goodsAuthEnum);
    /**
     * 获取今天的已上架的商品数量
     *
     * @return 今天的已上架的商品数量
     */
    long todayUpperNum();

    /**
     * 预警库存数
     * @return
     */
    long alertQuantityNum();

    /**
     * 按指定店铺获取预警库存数
     *
     * @param storeId 店铺ID
     * @return 预警库存数
     */
    long alertQuantityNum(String storeId);

}
