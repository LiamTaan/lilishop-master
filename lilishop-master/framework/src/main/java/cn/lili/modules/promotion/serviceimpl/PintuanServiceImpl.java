package cn.lili.modules.promotion.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import cn.lili.common.enums.PromotionTypeEnum;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.properties.RocketmqCustomProperties;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.GoodsSkuSearchParams;
import cn.lili.modules.goods.entity.enums.GoodsAuthEnum;
import cn.lili.modules.goods.entity.enums.GoodsSalesModeEnum;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.entity.dto.OrderSearchParams;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.entity.enums.PayStatusEnum;
import cn.lili.modules.order.order.service.OrderService;
import cn.lili.modules.promotion.entity.dos.Pintuan;
import cn.lili.modules.promotion.entity.dos.PromotionGoods;
import cn.lili.modules.promotion.entity.dto.PintuanGoodsManagerDTO;
import cn.lili.modules.promotion.entity.dto.search.PromotionGoodsSearchParams;
import cn.lili.modules.promotion.entity.enums.PromotionsScopeTypeEnum;
import cn.lili.modules.promotion.entity.enums.PromotionsStatusEnum;
import cn.lili.modules.promotion.entity.vos.PintuanMemberVO;
import cn.lili.modules.promotion.entity.vos.PintuanShareVO;
import cn.lili.modules.promotion.entity.vos.PintuanVO;
import cn.lili.modules.promotion.mapper.PintuanMapper;
import cn.lili.modules.promotion.service.PintuanService;
import cn.lili.modules.promotion.service.PromotionGoodsService;
import cn.lili.modules.promotion.tools.PromotionTools;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.service.StoreService;
import cn.lili.trigger.enums.DelayTypeEnums;
import cn.lili.trigger.interfaces.TimeTrigger;
import cn.lili.trigger.model.TimeExecuteConstant;
import cn.lili.trigger.model.TimeTriggerMsg;
import cn.lili.trigger.util.DelayQueueTools;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 拼团业务层实现
 *
 * @author Chopper
 * @since 2020/8/21
 */
@Service
public class PintuanServiceImpl extends AbstractPromotionsServiceImpl<PintuanMapper, Pintuan> implements PintuanService {

    /**
     * 促销商品
     */
    @Autowired
    private PromotionGoodsService promotionGoodsService;
    /**
     * 规格商品
     */
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private StoreService storeService;
    /**
     * 客户
     */
    @Autowired
    private MemberService memberService;
    /**
     * 订单
     */
    @Autowired
    private OrderService orderService;

    /**
     * 延时任务
     */
    @Autowired
    private TimeTrigger timeTrigger;

    /**
     * RocketMQ
     */
    @Autowired
    private RocketmqCustomProperties rocketmqCustomProperties;

    /**
     * 获取当前拼团的客户
     *
     * @param pintuanId 拼图id
     * @return 当前拼团的客户列表
     */
    @Override
    public List<PintuanMemberVO> getPintuanMember(String pintuanId) {
        List<PintuanMemberVO> members = new ArrayList<>();
        Pintuan pintuan = this.getById(pintuanId);
        if (pintuan == null) {
            log.error("拼团活动为" + pintuanId + "的拼团活动不存在！");
            return new ArrayList<>();
        }
        OrderSearchParams searchParams = new OrderSearchParams();
        searchParams.setOrderStatus(OrderStatusEnum.PAID.name());
        searchParams.setPromotionId(pintuanId);
        searchParams.setOrderPromotionType(PromotionTypeEnum.PINTUAN.name());
        searchParams.setParentOrderSn("");
        searchParams.setMemberId("");
        List<Order> orders = orderService.queryListByParams(searchParams);
        //遍历订单状态为已支付，为团长的拼团订单
        for (Order order : orders) {
            Member member = memberService.getById(order.getMemberId());
            PintuanMemberVO memberVO = new PintuanMemberVO(member);
            //获取已参团人数
            this.setMemberVONum(memberVO, pintuan.getRequiredNum(), order.getSn());
            memberVO.setOrderSn(order.getSn());
            if (memberVO.getToBeGroupedNum() > 0) {
                members.add(memberVO);
            }
        }
        return members;
    }

    /**
     * 查询拼团活动详情
     *
     * @param id 拼团ID
     * @return 拼团活动详情
     */
    @Override
    public PintuanVO getPintuanVO(String id) {
        Pintuan pintuan = this.getById(id);
        if (pintuan == null) {
            log.error("拼团活动id[" + id + "]的拼团活动不存在！");
            throw new ServiceException(ResultCode.PINTUAN_NOT_EXIST_ERROR);
        }
        PintuanVO pintuanVO = new PintuanVO(pintuan);
        PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
        searchParams.setPromotionId(pintuan.getId());
        searchParams.setPromotionType(PromotionTypeEnum.PINTUAN.name());
        pintuanVO.setPromotionGoodsList(promotionGoodsService.listFindAll(searchParams));
        return pintuanVO;
    }

    @Override
    public PintuanVO getDisplayPintuanVO() {
        List<Pintuan> pintuanList = this.list(new LambdaQueryWrapper<Pintuan>()
                .eq(Pintuan::getStoreId, PromotionTools.PLATFORM_ID)
                .eq(Pintuan::getDeleteFlag, false)
                .isNotNull(Pintuan::getStartTime)
                .orderByDesc(Pintuan::getStartTime)
                .orderByDesc(Pintuan::getCreateTime));
        for (Pintuan pintuan : pintuanList) {
            if (!PromotionsStatusEnum.START.name().equals(pintuan.getPromotionStatus())) {
                continue;
            }
            List<PromotionGoods> goodsList = getPintuanGoodsList(pintuan.getId());
            if (goodsList == null || goodsList.isEmpty()) {
                continue;
            }
            PintuanVO pintuanVO = new PintuanVO(pintuan);
            pintuanVO.setPromotionGoodsList(goodsList);
            return pintuanVO;
        }
        return null;
    }

    @Override
    public IPage<GoodsSku> getAvailableSkuPage(String pintuanId, GoodsSkuSearchParams searchParams) {
        Pintuan pintuan = this.getById(pintuanId);
        if (pintuan == null) {
            throw new ServiceException(ResultCode.PINTUAN_NOT_EXIST_ERROR);
        }
        List<String> registeredSkuIds = promotionGoodsService.list(new LambdaQueryWrapper<PromotionGoods>()
                        .eq(PromotionGoods::getPromotionId, pintuanId)
                        .eq(PromotionGoods::getPromotionType, PromotionTypeEnum.PINTUAN.name()))
                .stream()
                .map(PromotionGoods::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
        List<String> overlapSkuIds = promotionGoodsService.findOverlapSkuIds(
                Arrays.asList(PromotionTypeEnum.SECKILL, PromotionTypeEnum.PINTUAN),
                pintuan.getStartTime(),
                pintuan.getEndTime(),
                pintuan.getId()
        );

        LambdaQueryWrapper<GoodsSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsSku::getSalesModel, GoodsSalesModeEnum.RETAIL.name());
        queryWrapper.in(GoodsSku::getStoreId, listPromotionSelectableStoreIds());
        queryWrapper.eq(GoodsSku::getAuthFlag, GoodsAuthEnum.PASS.name());
        queryWrapper.eq(GoodsSku::getMarketEnable, GoodsStatusEnum.UPPER.name());
        queryWrapper.eq(GoodsSku::getDeleteFlag, false);
        queryWrapper.notIn(!registeredSkuIds.isEmpty(), GoodsSku::getId, registeredSkuIds);
        queryWrapper.notIn(overlapSkuIds != null && !overlapSkuIds.isEmpty(), GoodsSku::getId, overlapSkuIds);
        queryWrapper.eq(CharSequenceUtil.isNotBlank(searchParams.getGoodsId()), GoodsSku::getGoodsId, searchParams.getGoodsId());
        queryWrapper.eq(CharSequenceUtil.isNotBlank(searchParams.getStoreId()), GoodsSku::getStoreId, searchParams.getStoreId());
        queryWrapper.like(CharSequenceUtil.isNotBlank(searchParams.getGoodsName()), GoodsSku::getGoodsName, searchParams.getGoodsName());
        queryWrapper.and(CharSequenceUtil.isNotBlank(searchParams.getSkuKeyword()), wrapper -> wrapper
                .like(GoodsSku::getGoodsName, searchParams.getSkuKeyword())
                .or()
                .like(GoodsSku::getSimpleSpecs, searchParams.getSkuKeyword())
                .or()
                .like(GoodsSku::getSn, searchParams.getSkuKeyword())
                .or()
                .like(GoodsSku::getBarcode, searchParams.getSkuKeyword()));
        queryWrapper.orderByDesc(GoodsSku::getCreateTime);
        return goodsSkuService.page(PageUtil.initPage(searchParams), queryWrapper);
    }

    private List<String> listPromotionSelectableStoreIds() {
        List<String> storeIds = storeService.list(new LambdaQueryWrapper<Store>()
                        .in(Store::getStoreType, Arrays.asList(
                                StoreBizTypeEnum.AGENT.name(),
                                StoreBizTypeEnum.SUPPLIER.name()
                        )))
                .stream()
                .map(Store::getId)
                .collect(Collectors.toList());
        return storeIds.isEmpty() ? Collections.singletonList("__NO_PROMOTION_SELECTABLE_STORE__") : storeIds;
    }

    private Map<String, Store> loadStoreMapBySkuIds(Set<String> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> storeIds = goodsSkuService.listByIds(skuIds).stream()
                .map(GoodsSku::getStoreId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return storeService.listByIds(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, Function.identity(), (left, right) -> left));
    }

    private void checkPromotionSelectableSku(GoodsSku goodsSku, Map<String, Store> storeMap) {
        if (goodsSku == null) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }
        if (!GoodsSalesModeEnum.RETAIL.name().equals(goodsSku.getSalesModel())) {
            throw new ServiceException(ResultCode.PROMOTION_GOODS_DO_NOT_JOIN_WHOLESALE);
        }
        if (!GoodsAuthEnum.PASS.name().equals(goodsSku.getAuthFlag())
                || !GoodsStatusEnum.UPPER.name().equals(goodsSku.getMarketEnable())
                || Boolean.TRUE.equals(goodsSku.getDeleteFlag())) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST);
        }
        Store store = storeMap == null ? null : storeMap.get(goodsSku.getStoreId());
        if (store == null || !Arrays.asList(
                StoreBizTypeEnum.AGENT.name(),
                StoreBizTypeEnum.SUPPLIER.name()
        ).contains(store.getStoreType())) {
            throw new ServiceException(ResultCode.GOODS_NOT_EXIST_STORE);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean manualUpdateDisplayStatus(String pintuanId, Boolean open, Long startTime, Long endTime) {
        Pintuan pintuan = this.getById(pintuanId);
        if (pintuan == null) {
            throw new ServiceException(ResultCode.PINTUAN_NOT_EXIST_ERROR);
        }
        if (!Boolean.TRUE.equals(open)) {
            return super.updateStatus(Collections.singletonList(pintuanId), null, null);
        }
        if (startTime == null || endTime == null) {
            throw new ServiceException(ResultCode.PROMOTION_TIME_NOT_EXIST);
        }
        if (getPintuanGoodsList(pintuanId).isEmpty()) {
            throw new ServiceException(ResultCode.PROMOTION_GOODS_ERROR);
        }
        List<String> closeIds = this.list(new LambdaQueryWrapper<Pintuan>()
                        .eq(Pintuan::getStoreId, PromotionTools.PLATFORM_ID)
                        .eq(Pintuan::getDeleteFlag, false)
                        .ne(Pintuan::getId, pintuanId)
                        .isNotNull(Pintuan::getStartTime))
                .stream()
                .filter(item -> PromotionsStatusEnum.START.name().equals(item.getPromotionStatus())
                        || PromotionsStatusEnum.NEW.name().equals(item.getPromotionStatus()))
                .map(Pintuan::getId)
                .collect(Collectors.toList());
        if (!closeIds.isEmpty()) {
            this.update(new LambdaUpdateWrapper<Pintuan>()
                    .in(Pintuan::getId, closeIds)
                    .set(Pintuan::getStartTime, null)
                    .set(Pintuan::getEndTime, null));
            for (String closeId : closeIds) {
                Pintuan closed = this.getById(closeId);
                if (closed != null) {
                    this.updateEsGoodsIndex(closed);
                }
            }
        }
        return super.updateStatus(Collections.singletonList(pintuanId), startTime, endTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPintuanGoods(String pintuanId, List<PintuanGoodsManagerDTO> goodsList) {
        Pintuan pintuan = this.getById(pintuanId);
        if (pintuan == null) {
            throw new ServiceException(ResultCode.PINTUAN_NOT_EXIST_ERROR);
        }
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        List<String> requestSkuIds = goodsList.stream()
                .map(PintuanGoodsManagerDTO::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
        Set<String> skuIds = requestSkuIds.stream()
                .collect(Collectors.toSet());
        if (requestSkuIds.size() != skuIds.size()) {
            throw new ServiceException("已添加商品不能重复添加");
        }
        if (!skuIds.isEmpty()) {
            long exists = promotionGoodsService.count(new LambdaQueryWrapper<PromotionGoods>()
                    .eq(PromotionGoods::getPromotionId, pintuanId)
                    .eq(PromotionGoods::getPromotionType, PromotionTypeEnum.PINTUAN.name())
                    .in(PromotionGoods::getSkuId, skuIds));
            if (exists > 0) {
                throw new ServiceException("已添加商品不能重复添加");
            }
        }

        List<PromotionGoods> promotionGoodsList = new ArrayList<>();
        Map<String, Store> storeMap = loadStoreMapBySkuIds(skuIds);
        for (PintuanGoodsManagerDTO goodsDTO : goodsList) {
            GoodsSku goodsSku = goodsSkuService.getCanPromotionGoodsSkuByIdFromCache(goodsDTO.getSkuId());
            checkPromotionSelectableSku(goodsSku, storeMap);
            if (goodsDTO.getQuantity() != null && goodsSku.getQuantity() < goodsDTO.getQuantity()) {
                throw new ServiceException("商品[" + goodsSku.getGoodsName() + "]库存不足");
            }
            PromotionGoods promotionGoods = new PromotionGoods(goodsSku);
            promotionGoods.setPrice(goodsDTO.getPrice());
            promotionGoods.setQuantity(goodsDTO.getQuantity());
            promotionGoods.setLimitNum(goodsDTO.getLimitNum() == null ? pintuan.getLimitNum() : goodsDTO.getLimitNum());
            promotionGoods.setScopeId(goodsSku.getId());
            promotionGoods.setScopeType(PromotionsScopeTypeEnum.PORTION_GOODS.name());
            promotionGoodsList.add(promotionGoods);
        }
        List<PromotionGoods> initializedGoods = PromotionTools.promotionGoodsInit(promotionGoodsList, pintuan, PromotionTypeEnum.PINTUAN);
        checkPintuanPromotionGoods(pintuan, initializedGoods);
        promotionGoodsService.saveBatch(initializedGoods);
        PintuanVO pintuanVO = new PintuanVO(pintuan);
        pintuanVO.setPromotionGoodsList(getPintuanGoodsList(pintuanId));
        this.updateEsGoodsIndex(pintuanVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePintuanGoods(String pintuanId, String promotionGoodsId, PintuanGoodsManagerDTO goodsDTO) {
        Pintuan pintuan = this.getById(pintuanId);
        if (pintuan == null) {
            throw new ServiceException(ResultCode.PINTUAN_NOT_EXIST_ERROR);
        }
        PromotionGoods promotionGoods = promotionGoodsService.getOne(new LambdaQueryWrapper<PromotionGoods>()
                .eq(PromotionGoods::getId, promotionGoodsId)
                .eq(PromotionGoods::getPromotionId, pintuanId)
                .eq(PromotionGoods::getPromotionType, PromotionTypeEnum.PINTUAN.name()), false);
        if (promotionGoods == null) {
            throw new ServiceException(ResultCode.PINTUAN_GOODS_NOT_EXIST_ERROR);
        }
        if (CharSequenceUtil.isNotBlank(goodsDTO.getSkuId()) && !promotionGoods.getSkuId().equals(goodsDTO.getSkuId())) {
            throw new ServiceException("拼团商品SKU不能修改");
        }
        GoodsSku goodsSku = goodsSkuService.getCanPromotionGoodsSkuByIdFromCache(promotionGoods.getSkuId());
        checkPromotionSelectableSku(goodsSku, loadStoreMapBySkuIds(Collections.singleton(promotionGoods.getSkuId())));
        if (goodsDTO.getQuantity() != null && goodsSku.getQuantity() < goodsDTO.getQuantity()) {
            throw new ServiceException("商品[" + goodsSku.getGoodsName() + "]库存不足");
        }
        promotionGoods.setPrice(goodsDTO.getPrice());
        promotionGoods.setQuantity(goodsDTO.getQuantity());
        promotionGoods.setLimitNum(goodsDTO.getLimitNum() == null ? pintuan.getLimitNum() : goodsDTO.getLimitNum());
        checkPintuanPromotionGoods(pintuan, Collections.singletonList(promotionGoods));
        promotionGoodsService.updateById(promotionGoods);
        PintuanVO pintuanVO = new PintuanVO(pintuan);
        pintuanVO.setPromotionGoodsList(getPintuanGoodsList(pintuanId));
        this.updateEsGoodsIndex(pintuanVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePintuanGoods(String pintuanId, String promotionGoodsId) {
        Pintuan pintuan = this.getById(pintuanId);
        if (pintuan == null) {
            throw new ServiceException(ResultCode.PINTUAN_NOT_EXIST_ERROR);
        }
        PromotionGoods promotionGoods = promotionGoodsService.getOne(new LambdaQueryWrapper<PromotionGoods>()
                .eq(PromotionGoods::getId, promotionGoodsId)
                .eq(PromotionGoods::getPromotionId, pintuanId)
                .eq(PromotionGoods::getPromotionType, PromotionTypeEnum.PINTUAN.name()), false);
        if (promotionGoods == null) {
            throw new ServiceException(ResultCode.PINTUAN_GOODS_NOT_EXIST_ERROR);
        }
        promotionGoodsService.remove(new LambdaQueryWrapper<PromotionGoods>()
                .eq(PromotionGoods::getPromotionId, pintuanId)
                .eq(PromotionGoods::getPromotionType, PromotionTypeEnum.PINTUAN.name())
                .eq(PromotionGoods::getSkuId, promotionGoods.getSkuId()));
        PintuanVO pintuanVO = new PintuanVO(pintuan);
        pintuanVO.setPromotionGoodsList(getPintuanGoodsList(pintuanId));
        this.updateEsGoodsIndex(pintuanVO);
    }

    /**
     * 获取拼团分享信息
     *
     * @param parentOrderSn 拼团团长订单sn
     * @param skuId         商品skuId
     * @return 拼团分享信息
     */
    @Override
    public PintuanShareVO getPintuanShareInfo(String parentOrderSn, String skuId) {
        PintuanShareVO pintuanShareVO = new PintuanShareVO();
        pintuanShareVO.setPintuanMemberVOS(new ArrayList<>());
        //查找团长订单和已和当前拼团订单拼团的订单
        List<Order> orders = orderService.queryListByPromotion(PromotionTypeEnum.PINTUAN.name(), PayStatusEnum.PAID.name(), parentOrderSn, parentOrderSn);
        this.setPintuanOrderInfo(orders, pintuanShareVO, skuId);
        //如果为根据团员订单sn查询拼团订单信息时，找到团长订单sn，然后找到所有参与到同一拼团的订单信息
        if (!orders.isEmpty() && pintuanShareVO.getPromotionGoods() == null) {
            List<Order> parentOrders = orderService.queryListByPromotion(PromotionTypeEnum.PINTUAN.name(), PayStatusEnum.PAID.name(), orders.get(0).getParentOrderSn(), orders.get(0).getParentOrderSn());
            this.setPintuanOrderInfo(parentOrders, pintuanShareVO, skuId);
        }
        return pintuanShareVO;
    }

    /**
     * 更新促销商品信息
     *
     * @param promotions 促销实体
     * @return 是否更新成功
     */
    @Override
    public boolean updatePromotionsGoods(Pintuan promotions) {
        boolean result = super.updatePromotionsGoods(promotions);
        if (!PromotionsStatusEnum.CLOSE.name().equals(promotions.getPromotionStatus())
                && PromotionsScopeTypeEnum.PORTION_GOODS.name().equals(promotions.getScopeType())
                && promotions instanceof PintuanVO) {
            PintuanVO pintuanVO = (PintuanVO) promotions;
            this.updatePintuanPromotionGoods(pintuanVO);
            TimeTriggerMsg timeTriggerMsg = new TimeTriggerMsg(TimeExecuteConstant.PROMOTION_EXECUTOR,
                    promotions.getEndTime().getTime(),
                    promotions,
                    DelayQueueTools.wrapperUniqueKey(DelayTypeEnums.PINTUAN_ORDER, (promotions.getId())),
                    rocketmqCustomProperties.getPromotionTopic());
            //发送促销活动开始的延时任务
            this.timeTrigger.addDelay(timeTriggerMsg);
        }
        if (promotions.getEndTime() == null && promotions.getStartTime() == null) {
            //过滤父级拼团订单，根据父级拼团订单分组
            this.orderService.checkFictitiousOrder(promotions.getId(), promotions.getRequiredNum(), promotions.getFictitious());
        }
        return result;
    }

    /**
     * 更新促销信息到商品索引
     *
     * @param promotions 促销实体
     */
    @Override
    public void updateEsGoodsIndex(Pintuan promotions) {
        Pintuan pintuan = promotions;
        super.updateEsGoodsIndex(pintuan);
    }

    /**
     * 删除拼团活动前保护正在进行的活动和已有有效订单的活动。
     *
     * @param ids 促销活动id集合
     * @return 是否移除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removePromotions(List<String> ids) {
        for (String id : ids) {
            Pintuan pintuan = this.getById(id);
            if (pintuan == null) {
                throw new ServiceException(ResultCode.PINTUAN_NOT_EXIST_ERROR);
            }
            if (PromotionsStatusEnum.START.name().equals(pintuan.getPromotionStatus())) {
                throw new ServiceException("进行中的拼团活动不能直接删除，请先关闭活动");
            }
            OrderSearchParams searchParams = new OrderSearchParams();
            searchParams.setPromotionId(id);
            searchParams.setOrderPromotionType(PromotionTypeEnum.PINTUAN.name());
            searchParams.setPayStatus(PayStatusEnum.PAID.name());
            if (!orderService.queryListByParams(searchParams).isEmpty()) {
                throw new ServiceException("已有有效订单的拼团活动不能删除");
            }
        }
        return super.removePromotions(ids);
    }

    /**
     * 当前促销类型
     *
     * @return 当前促销类型
     */
    @Override
    public PromotionTypeEnum getPromotionType() {
        return PromotionTypeEnum.PINTUAN;
    }

    /**
     * 根据订单信息，从中提取出拼团信息，设置拼团信息
     *
     * @param orders         订单列表
     * @param pintuanShareVO 拼团信息
     * @param skuId          商品skuId（用于获取拼团商品信息）
     */
    private void setPintuanOrderInfo(List<Order> orders, PintuanShareVO pintuanShareVO, String skuId) {
        for (Order order : orders) {
            if (pintuanShareVO.getPintuanMemberVOS().stream().anyMatch(i -> i.getMemberId().equals(order.getMemberId()))) {
                continue;
            }
            Member member = memberService.getById(order.getMemberId());
            PintuanMemberVO memberVO = new PintuanMemberVO(member);
            if (CharSequenceUtil.isEmpty(order.getParentOrderSn())) {
                memberVO.setOrderSn("");
                PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
                searchParams.setPromotionStatus(PromotionsStatusEnum.START.name());
                searchParams.setPromotionId(order.getPromotionId());
                searchParams.setSkuId(skuId);
                PromotionGoods promotionGoods = promotionGoodsService.getPromotionsGoods(searchParams);
                if (promotionGoods == null) {
                    throw new ServiceException(ResultCode.PINTUAN_GOODS_NOT_EXIST_ERROR);
                }
                pintuanShareVO.setPromotionGoods(promotionGoods);
                Pintuan pintuanById = this.getById(order.getPromotionId());
                //获取已参团人数
                this.setMemberVONum(memberVO, pintuanById.getRequiredNum(), order.getSn());
            }
            pintuanShareVO.getPintuanMemberVOS().add(memberVO);
        }
    }

    private void setMemberVONum(PintuanMemberVO memberVO, Integer requiredNum, String orderSn) {
        long count = this.orderService.queryCountByPromotion(PromotionTypeEnum.PINTUAN.name(), PayStatusEnum.PAID.name(), orderSn, orderSn);
        Order order = orderService.getBySn(orderSn);
        long toBoGrouped = requiredNum - count;
        if(order.getOrderStatus().equals(OrderStatusEnum.UNDELIVERED.name())){
            toBoGrouped = 0L;
        }
        //获取待参团人数
        memberVO.setGroupNum(requiredNum);
        memberVO.setGroupedNum(count);
        memberVO.setToBeGroupedNum(toBoGrouped);
    }

    /**
     * 更新记录的促销商品信息
     *
     * @param pintuan 拼团信息
     */
    private void updatePintuanPromotionGoods(PintuanVO pintuan) {

        if (pintuan.getPromotionGoodsList() != null && !pintuan.getPromotionGoodsList().isEmpty()) {
            List<PromotionGoods> promotionGoods = PromotionTools.promotionGoodsInit(pintuan.getPromotionGoodsList(), pintuan, PromotionTypeEnum.PINTUAN);
            checkPintuanPromotionGoods(pintuan, promotionGoods);
            PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
            searchParams.setPromotionId(pintuan.getId());
            searchParams.setPromotionType(PromotionTypeEnum.PINTUAN.name());
            promotionGoodsService.deletePromotionGoods(searchParams);
            promotionGoodsService.saveOrUpdateBatch(promotionGoods);
        }
    }

    private void checkPintuanPromotionGoods(Pintuan pintuan, List<PromotionGoods> promotionGoodsList) {
        for (PromotionGoods promotionGood : promotionGoodsList) {
            if (goodsSkuService.getCanPromotionGoodsSkuByIdFromCache(promotionGood.getSkuId()) == null) {
                log.error("商品[" + promotionGood.getGoodsName() + "]不存在或处于不可售卖状态！");
                throw new ServiceException("商品[" + promotionGood.getGoodsName() + "]不存在或处于不可售卖状态！");
            }
            Integer count = promotionGoodsService.findInnerOverlapPromotionGoods(PromotionTypeEnum.SECKILL.name(), promotionGood.getSkuId(), pintuan.getStartTime(), pintuan.getEndTime(), pintuan.getId());
            count += promotionGoodsService.findInnerOverlapPromotionGoods(PromotionTypeEnum.PINTUAN.name(), promotionGood.getSkuId(), pintuan.getStartTime(), pintuan.getEndTime(), pintuan.getId());
            if (count > 0) {
                log.error("商品[" + promotionGood.getGoodsName() + "]已经在重叠的时间段参加了秒杀活动或拼团活动，不能参加拼团活动");
                throw new ServiceException("商品[" + promotionGood.getGoodsName() + "]已经在重叠的时间段参加了秒杀活动或拼团活动，不能参加拼团活动");
            }
        }
    }

    private List<PromotionGoods> getPintuanGoodsList(String pintuanId) {
        PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
        searchParams.setPromotionId(pintuanId);
        searchParams.setPromotionType(PromotionTypeEnum.PINTUAN.name());
        return promotionGoodsService.listFindAll(searchParams);
    }

}
