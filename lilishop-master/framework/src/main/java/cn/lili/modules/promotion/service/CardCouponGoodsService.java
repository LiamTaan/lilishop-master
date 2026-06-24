package cn.lili.modules.promotion.service;

import cn.lili.common.vo.PageVO;
import cn.lili.modules.promotion.entity.dto.CardCouponGoodsBindDTO;
import cn.lili.modules.promotion.entity.dto.search.CardCouponGoodsSearchParams;
import cn.lili.modules.promotion.entity.vos.CardCouponGoodsVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 卡券商品关联业务层
 *
 * @author dawn
 * @since 2026/6/17
 */
public interface CardCouponGoodsService {

    /**
     * 绑定卡券商品
     *
     * @param bindDTO 绑定请求
     */
    void bind(CardCouponGoodsBindDTO bindDTO);

    /**
     * 分页查询卡券商品
     *
     * @param searchParams 查询参数
     * @param pageVO       分页参数
     * @return 分页结果
     */
    IPage<CardCouponGoodsVO> page(CardCouponGoodsSearchParams searchParams, PageVO pageVO);

    /**
     * 根据优惠券查询绑定商品
     *
     * @param couponId 优惠券ID
     * @return 关联商品列表
     */
    List<CardCouponGoodsVO> listByCouponId(String couponId);

    /**
     * 根据SKU查询可用卡券商品关系
     *
     * @param skuId skuId
     * @return 关联商品列表
     */
    List<CardCouponGoodsVO> listBySkuId(String skuId);

    /**
     * 删除关联
     *
     * @param ids 关联ID列表
     */
    void delete(List<String> ids);
}
