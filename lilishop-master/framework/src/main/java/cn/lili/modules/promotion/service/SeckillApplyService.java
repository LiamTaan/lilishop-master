package cn.lili.modules.promotion.service;

import cn.lili.common.vo.PageVO;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.GoodsSkuSearchParams;
import cn.lili.modules.promotion.entity.dos.Seckill;
import cn.lili.modules.promotion.entity.dos.SeckillApply;
import cn.lili.modules.promotion.entity.dto.SeckillApplyManagerDTO;
import cn.lili.modules.promotion.entity.dto.search.SeckillSearchParams;
import cn.lili.modules.promotion.entity.vos.SeckillApplyVO;
import cn.lili.modules.promotion.entity.vos.SeckillGoodsVO;
import cn.lili.modules.promotion.entity.vos.SeckillTimelineVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 秒杀申请业务层
 *
 * @author Chopper
 * @since 2020/11/18 9:45 上午
 */
public interface SeckillApplyService extends IService<SeckillApply> {


    /**
     * 获取当天秒杀活动信息列表（时刻及对应时刻下的商品）
     *
     * @return 秒杀活动信息列表
     */
    List<SeckillTimelineVO> getSeckillTimeline();

    /**
     * 获取首页展示用秒杀时段，只返回一个有效时段及有限数量商品。
     *
     * @param goodsLimit 商品数量上限
     * @return 首页秒杀时段
     */
    SeckillTimelineVO getHomeSeckillTimeline(int goodsLimit);

    /**
     * 获取当天某个时刻的秒杀活动商品列表
     *
     * @param timeline 指定时刻
     * @return 秒杀活动商品列表
     */
    List<SeckillGoodsVO> getSeckillGoods(Integer timeline);

    /**
     * 分页获取当天某个时刻的秒杀活动商品列表。
     *
     * @param timeline        指定时刻
     * @param goodsName       商品名称
     * @param categoryPath    分类路径
     * @param visibleStoreIds 当前身份可见店铺
     * @param pageVo          分页参数
     * @return 秒杀活动商品分页
     */
    IPage<SeckillGoodsVO> getSeckillGoodsPage(Integer timeline, String goodsName, String categoryPath, List<String> visibleStoreIds, PageVO pageVo);

    /**
     * 分页查询限时请购申请列表
     *
     * @param queryParam 秒杀活动申请查询参数
     * @param pageVo     分页参数
     * @return 限时请购申请列表
     */
    IPage<SeckillApply> getSeckillApplyPage(SeckillSearchParams queryParam, PageVO pageVo);

    /**
     * 查询限时请购申请列表
     *
     * @param queryParam 秒杀活动申请查询参数
     * @return 限时请购申请列表
     */
    List<SeckillApply> getSeckillApplyList(SeckillSearchParams queryParam);

    /**
     * 查询限时请购申请列表总数
     *
     * @param queryParam 查询条件
     * @return 限时请购申请列表总数
     */
    long getSeckillApplyCount(SeckillSearchParams queryParam);

    /**
     * 查询限时请购申请
     *
     * @param queryParam 秒杀活动申请查询参数
     * @return 限时请购申请
     */
    SeckillApply getSeckillApply(SeckillSearchParams queryParam);

    /**
     * 管理端查询可添加到秒杀活动的商品SKU，排除当前活动已报名SKU。
     *
     * @param seckillId     秒杀活动编号
     * @param searchParams  SKU查询参数
     * @param timeLine      秒杀场次
     * @return 可添加SKU分页
     */
    IPage<GoodsSku> getAvailableSkuPage(String seckillId, GoodsSkuSearchParams searchParams, Integer timeLine);

    /**
     * 添加秒杀活动申请
     * 检测是否商品是否同时参加多个活动
     * 将秒杀商品信息存入秒杀活动中
     * 保存秒杀活动商品，促销商品信息
     *
     * @param seckillId        秒杀活动编号
     * @param storeId          商家id
     * @param seckillApplyList 秒杀活动申请列表
     */
    void addSeckillApply(String seckillId, String storeId, List<SeckillApplyVO> seckillApplyList);

    /**
     * 管理端代添加秒杀活动商品。
     *
     * @param seckillId 秒杀活动编号
     * @param applyList 秒杀活动商品列表
     */
    void addSeckillApplyByManager(String seckillId, List<SeckillApplyManagerDTO> applyList);

    /**
     * 管理端更新秒杀活动商品。
     *
     * @param seckillId 秒杀活动编号
     * @param id        秒杀活动商品申请编号
     * @param apply     秒杀活动商品
     */
    void updateSeckillApplyByManager(String seckillId, String id, SeckillApplyManagerDTO apply);

    /**
     * 批量删除秒杀活动商品
     *
     * @param seckillId 秒杀活动活动id
     * @param id        秒杀活动商品
     */
    void removeSeckillApply(String seckillId, String id);

    /**
     * 更新秒杀商品出售数量
     *
     * @param seckillId 秒杀活动id
     * @param skuId 商品skuId
     * @param saleNum 出售数量
     */
    void updateSeckillApplySaleNum(String seckillId, String skuId, Integer saleNum);

    /**
     * 更新秒杀活动时间
     *
     * @param seckill 秒杀活动
     * @return 是否更新成功
     */
    boolean updateSeckillApplyTime(Seckill seckill);

}
