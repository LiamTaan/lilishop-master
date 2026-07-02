package cn.lili.modules.promotion.service;

import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.GoodsSkuSearchParams;
import cn.lili.modules.promotion.entity.dos.Pintuan;
import cn.lili.modules.promotion.entity.dto.PintuanGoodsManagerDTO;
import cn.lili.modules.promotion.entity.vos.PintuanMemberVO;
import cn.lili.modules.promotion.entity.vos.PintuanShareVO;
import cn.lili.modules.promotion.entity.vos.PintuanVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 拼图活动业务层
 *
 * @author Chopper
 * @since 2020/11/18 9:45 上午
 */
public interface PintuanService extends AbstractPromotionsService<Pintuan> {


    /**
     * 获取当前拼团的客户
     *
     * @param pintuanId 拼图id
     * @return 当前拼团的客户列表
     */
    List<PintuanMemberVO> getPintuanMember(String pintuanId);

    /**
     * 查询拼团活动详情
     *
     * @param id 拼团ID
     * @return 拼团活动详情
     */
    PintuanVO getPintuanVO(String id);

    /**
     * 查询移动端当前展示的拼团活动。
     *
     * @return 当前展示的拼团活动，没有则返回 null
     */
    PintuanVO getDisplayPintuanVO();

    /**
     * 获取拼团分享信息
     *
     * @param parentOrderSn 拼团团长订单sn
     * @param skuId         商品skuId
     * @return 拼团分享信息
     */
    PintuanShareVO getPintuanShareInfo(String parentOrderSn, String skuId);

    /**
     * 查询可添加到拼团活动的商品SKU，排除已添加和重叠活动SKU。
     *
     * @param pintuanId     拼团活动ID
     * @param searchParams  SKU查询参数
     * @return 可添加SKU分页
     */
    IPage<GoodsSku> getAvailableSkuPage(String pintuanId, GoodsSkuSearchParams searchParams);

    /**
     * 管理端添加拼团商品。
     *
     * @param pintuanId  拼团活动ID
     * @param goodsList  拼团商品列表
     */
    void addPintuanGoods(String pintuanId, List<PintuanGoodsManagerDTO> goodsList);

    /**
     * 更新拼团活动商品。
     *
     * @param pintuanId          拼团活动ID
     * @param promotionGoodsId   促销商品ID
     * @param goodsDTO           拼团商品信息
     */
    void updatePintuanGoods(String pintuanId, String promotionGoodsId, PintuanGoodsManagerDTO goodsDTO);

    /**
     * 移除拼团活动商品。
     *
     * @param pintuanId          拼团活动ID
     * @param promotionGoodsId   促销商品ID
     */
    void removePintuanGoods(String pintuanId, String promotionGoodsId);

    /**
     * 管理端手动开启或关闭移动端展示拼团活动。
     *
     * @param pintuanId 拼团活动ID
     * @param open 是否开启
     * @param startTime 开始时间，开启时必填
     * @param endTime 结束时间，开启时必填
     * @return 是否成功
     */
    boolean manualUpdateDisplayStatus(String pintuanId, Boolean open, Long startTime, Long endTime);

}
