package cn.lili.modules.promotion.serviceimpl;

import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.promotion.entity.dos.CardCouponGoods;
import cn.lili.modules.promotion.entity.dos.Coupon;
import cn.lili.modules.promotion.entity.dto.CardCouponGoodsBindDTO;
import cn.lili.modules.promotion.entity.dto.search.CardCouponGoodsSearchParams;
import cn.lili.modules.promotion.entity.enums.PromotionsScopeTypeEnum;
import cn.lili.modules.promotion.entity.vos.CardCouponGoodsVO;
import cn.lili.modules.promotion.entity.vos.CouponVO;
import cn.lili.modules.promotion.mapper.CardCouponGoodsMapper;
import cn.lili.modules.promotion.service.CardCouponGoodsService;
import cn.lili.modules.promotion.service.CouponService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 卡券商品关联业务层实现
 *
 * @author dawn
 * @since 2026/6/17
 */
@Service
public class CardCouponGoodsServiceImpl extends ServiceImpl<CardCouponGoodsMapper, CardCouponGoods> implements CardCouponGoodsService {

    @Autowired
    private CouponService couponService;

    @Autowired
    private GoodsSkuService goodsSkuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bind(CardCouponGoodsBindDTO bindDTO) {
        Coupon coupon = couponService.getById(bindDTO.getCouponId());
        if (coupon == null) {
            throw new ServiceException("优惠券不存在");
        }
        if (!PromotionsScopeTypeEnum.PORTION_GOODS.name().equals(coupon.getScopeType())
                && !PromotionsScopeTypeEnum.ALL.name().equals(coupon.getScopeType())) {
            throw new ServiceException("当前优惠券范围不支持卡券商品绑定");
        }
        this.remove(new LambdaQueryWrapper<CardCouponGoods>().eq(CardCouponGoods::getCouponId, bindDTO.getCouponId()));
        List<CardCouponGoods> saveList = new ArrayList<>();
        for (String skuId : bindDTO.getSkuIds()) {
            GoodsSku goodsSku = goodsSkuService.getGoodsSkuByIdFromCache(skuId);
            if (goodsSku == null) {
                throw new ServiceException("商品不存在:" + skuId);
            }
            CardCouponGoods item = new CardCouponGoods();
            item.setCouponId(coupon.getId());
            item.setCouponName(coupon.getCouponName());
            item.setGoodsId(goodsSku.getGoodsId());
            item.setSkuId(goodsSku.getId());
            item.setGoodsName(goodsSku.getGoodsName());
            item.setThumbnail(goodsSku.getThumbnail());
            item.setOriginalPrice(goodsSku.getPrice());
            item.setStoreId(goodsSku.getStoreId());
            item.setStoreName(goodsSku.getStoreName());
            saveList.add(item);
        }
        if (!saveList.isEmpty()) {
            this.saveBatch(saveList);
        }
    }

    @Override
    public IPage<CardCouponGoodsVO> page(CardCouponGoodsSearchParams searchParams, PageVO pageVO) {
        IPage<CardCouponGoods> page = this.page(PageUtil.initPage(pageVO), searchParams.queryWrapper());
        List<CardCouponGoodsVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageUtil.convertPage(page, records);
    }

    @Override
    public List<CardCouponGoodsVO> listByCouponId(String couponId) {
        return this.list(new LambdaQueryWrapper<CardCouponGoods>()
                .eq(CardCouponGoods::getCouponId, couponId)
                .orderByDesc(CardCouponGoods::getCreateTime)).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<CardCouponGoodsVO> listBySkuId(String skuId) {
        return this.list(new LambdaQueryWrapper<CardCouponGoods>()
                .eq(CardCouponGoods::getSkuId, skuId)
                .orderByDesc(CardCouponGoods::getCreateTime)).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        this.removeByIds(ids);
    }

    private CardCouponGoodsVO toVO(CardCouponGoods item) {
        CardCouponGoodsVO vo = new CardCouponGoodsVO();
        BeanUtils.copyProperties(item, vo);
        CouponVO couponVO = couponService.getDetail(item.getCouponId());
        vo.setCoupon(couponVO);
        return vo;
    }
}
