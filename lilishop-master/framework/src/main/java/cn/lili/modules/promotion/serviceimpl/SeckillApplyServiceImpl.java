package cn.lili.modules.promotion.serviceimpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.enums.PromotionTypeEnum;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.GoodsSkuSearchParams;
import cn.lili.modules.goods.entity.enums.GoodsAuthEnum;
import cn.lili.modules.goods.entity.enums.GoodsSalesModeEnum;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.promotion.entity.dos.BasePromotions;
import cn.lili.modules.promotion.entity.dos.PromotionGoods;
import cn.lili.modules.promotion.entity.dos.Seckill;
import cn.lili.modules.promotion.entity.dos.SeckillApply;
import cn.lili.modules.promotion.entity.dto.SeckillApplyManagerDTO;
import cn.lili.modules.promotion.entity.dto.search.PromotionGoodsSearchParams;
import cn.lili.modules.promotion.entity.dto.search.SeckillSearchParams;
import cn.lili.modules.promotion.entity.enums.PromotionsApplyStatusEnum;
import cn.lili.modules.promotion.entity.vos.SeckillApplyVO;
import cn.lili.modules.promotion.entity.vos.SeckillGoodsVO;
import cn.lili.modules.promotion.entity.vos.SeckillTimelineVO;
import cn.lili.modules.promotion.mapper.SeckillApplyMapper;
import cn.lili.modules.promotion.service.PromotionGoodsService;
import cn.lili.modules.promotion.service.SeckillApplyService;
import cn.lili.modules.promotion.service.SeckillService;
import cn.lili.modules.promotion.tools.PromotionTools;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreBizTypeEnum;
import cn.lili.modules.store.service.StoreService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 秒杀申请业务层实现
 *
 * @author Chopper
 * @since 2020/8/21
 */
@Service
@Slf4j
public class SeckillApplyServiceImpl extends ServiceImpl<SeckillApplyMapper, SeckillApply> implements SeckillApplyService {

    /**
     * 缓存
     */
    @Autowired
    private Cache<Object> cache;
    /**
     * 规格商品
     */
    @Autowired
    @Lazy
    private GoodsSkuService goodsSkuService;
    /**
     * 促销商品
     */
    @Autowired
    private PromotionGoodsService promotionGoodsService;
    /**
     * 秒杀
     */
    @Lazy
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private StoreService storeService;

    @Override
    public List<SeckillTimelineVO> getSeckillTimeline() {
        try {
            //秒杀活动缓存key
            return getSeckillTimelineInfo();
        } catch (Exception e) {
            log.error("获取秒杀时间轴失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public SeckillTimelineVO getHomeSeckillTimeline(int goodsLimit) {
        try {
            return getHomeSeckillTimelineInfo(goodsLimit);
        } catch (Exception e) {
            log.error("获取首页秒杀信息失败", e);
            return null;
        }
    }

    @Override
    public List<SeckillGoodsVO> getSeckillGoods(Integer timeline) {
        try {
            List<SeckillGoodsVO> seckillGoodsVoS = new ArrayList<>();
            //获取
            List<SeckillTimelineVO> seckillTimelineToCache = getSeckillTimelineInfo();
            Optional<SeckillTimelineVO> first = seckillTimelineToCache.stream().filter(i -> i.getTimeLine().equals(timeline)).findFirst();
            if (first.isPresent()) {
                seckillGoodsVoS = first.get().getSeckillGoodsList();
            }
            return seckillGoodsVoS;
        } catch (Exception e) {
            log.error("获取秒杀商品失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public IPage<SeckillGoodsVO> getSeckillGoodsPage(Integer timeline, String goodsName, String categoryPath, List<String> visibleStoreIds, PageVO pageVo) {
        Page<SeckillGoodsVO> resultPage = new Page<>(pageVo.getPageNumber(), pageVo.getPageSize());
        List<String> seckillIds = buildTodayTimelineCandidates().stream()
                .filter(item -> item.getTimeLine().equals(timeline))
                .map(SeckillTimelineCandidate::getSeckillId)
                .distinct()
                .collect(Collectors.toList());
        if (seckillIds.isEmpty()) {
            resultPage.setRecords(Collections.emptyList());
            resultPage.setTotal(0);
            return resultPage;
        }

        LambdaQueryWrapper<SeckillApply> queryWrapper = new LambdaQueryWrapper<SeckillApply>()
                .in(SeckillApply::getSeckillId, seckillIds)
                .eq(SeckillApply::getTimeLine, timeline)
                .eq(SeckillApply::getPromotionApplyStatus, PromotionsApplyStatusEnum.PASS.name())
                .eq(SeckillApply::getDeleteFlag, false)
                .like(StringUtils.isNotBlank(goodsName), SeckillApply::getGoodsName, goodsName)
                .in(visibleStoreIds != null && !visibleStoreIds.isEmpty(), SeckillApply::getStoreId, visibleStoreIds)
                .orderByDesc(SeckillApply::getCreateTime);

        List<String> filteredSkuIds = querySkuIdsByCategory(categoryPath, visibleStoreIds);
        if (StringUtils.isNotBlank(categoryPath) && filteredSkuIds.isEmpty()) {
            resultPage.setRecords(Collections.emptyList());
            resultPage.setTotal(0);
            return resultPage;
        }
        queryWrapper.in(!filteredSkuIds.isEmpty(), SeckillApply::getSkuId, filteredSkuIds);

        IPage<SeckillApply> applyPage = this.page(new Page<>(pageVo.getPageNumber(), pageVo.getPageSize()), queryWrapper);
        resultPage.setTotal(applyPage.getTotal());
        resultPage.setSize(applyPage.getSize());
        resultPage.setCurrent(applyPage.getCurrent());
        resultPage.setPages(applyPage.getPages());
        resultPage.setRecords(buildSeckillGoodsVOList(applyPage.getRecords()));
        return resultPage;
    }

    @Override
    public IPage<SeckillApply> getSeckillApplyPage(SeckillSearchParams queryParam, PageVO pageVo) {
        IPage<SeckillApply> seckillApplyPage = this.page(PageUtil.initPage(pageVo), queryParam.queryWrapper());
        if (seckillApplyPage != null && !seckillApplyPage.getRecords().isEmpty()) {

            //获取skuId
            List<String> skuIds = seckillApplyPage.getRecords().stream()
                    .map(SeckillApply::getSkuId).collect(Collectors.toList());

            //循环获取 店铺/全平台 参与的促销商品库存进行填充
            if (!skuIds.isEmpty()) {
                List<Integer> skuStock = promotionGoodsService.getPromotionGoodsStock(PromotionTypeEnum.SECKILL, queryParam.getSeckillId(), skuIds);
                for (int i = 0; i < skuIds.size(); i++) {
                    seckillApplyPage.getRecords().get(i).setQuantity(skuStock.get(i));
                }
            }
        }
        return seckillApplyPage;
    }

    /**
     * 分页查询限时请购申请列表
     *
     * @param queryParam 秒杀活动申请查询参数
     * @return 限时请购申请列表
     */
    @Override
    public List<SeckillApply> getSeckillApplyList(SeckillSearchParams queryParam) {
        return this.list(queryParam.queryWrapper());
    }

    /**
     * 查询限时请购申请列表总数
     *
     * @param queryParam 查询条件
     * @return 限时请购申请列表总数
     */
    @Override
    public long getSeckillApplyCount(SeckillSearchParams queryParam) {
        return this.count(queryParam.queryWrapper());
    }

    /**
     * 查询限时请购申请
     *
     * @param queryParam 秒杀活动申请查询参数
     * @return 限时请购申请
     */
    @Override
    public SeckillApply getSeckillApply(SeckillSearchParams queryParam) {
        return this.getOne(queryParam.queryWrapper(), false);
    }

    @Override
    public IPage<GoodsSku> getAvailableSkuPage(String seckillId, GoodsSkuSearchParams searchParams, Integer timeLine) {
        Seckill seckill = this.seckillService.getById(seckillId);
        if (seckill == null) {
            throw new ServiceException(ResultCode.SECKILL_NOT_EXIST_ERROR);
        }
        List<String> registeredSkuIds = this.list(new LambdaQueryWrapper<SeckillApply>()
                        .eq(SeckillApply::getSeckillId, seckillId))
                .stream()
                .map(SeckillApply::getSkuId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        LambdaQueryWrapper<GoodsSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsSku::getSalesModel, GoodsSalesModeEnum.RETAIL.name());
        queryWrapper.in(GoodsSku::getStoreId, listPromotionSelectableStoreIds());
        queryWrapper.eq(GoodsSku::getAuthFlag, GoodsAuthEnum.PASS.name());
        queryWrapper.eq(GoodsSku::getMarketEnable, GoodsStatusEnum.UPPER.name());
        queryWrapper.eq(GoodsSku::getDeleteFlag, false);
        queryWrapper.notIn(!registeredSkuIds.isEmpty(), GoodsSku::getId, registeredSkuIds);
        Date overlapStartTime = seckill.getStartTime();
        if (timeLine != null) {
            overlapStartTime = DateUtil.offsetHour(DateUtil.beginOfDay(seckill.getStartTime()), timeLine);
        }
        List<String> overlapSkuIds = promotionGoodsService.findOverlapSkuIds(
                Arrays.asList(PromotionTypeEnum.SECKILL, PromotionTypeEnum.PINTUAN),
                overlapStartTime,
                seckill.getEndTime(),
                seckill.getId()
        );
        queryWrapper.notIn(overlapSkuIds != null && !overlapSkuIds.isEmpty(), GoodsSku::getId, overlapSkuIds);
        queryWrapper.eq(StringUtils.isNotBlank(searchParams.getGoodsId()), GoodsSku::getGoodsId, searchParams.getGoodsId());
        queryWrapper.eq(StringUtils.isNotBlank(searchParams.getStoreId()), GoodsSku::getStoreId, searchParams.getStoreId());
        queryWrapper.like(StringUtils.isNotBlank(searchParams.getGoodsName()), GoodsSku::getGoodsName, searchParams.getGoodsName());
        queryWrapper.and(StringUtils.isNotBlank(searchParams.getSkuKeyword()), wrapper -> wrapper
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
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return storeService.listByIds(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, item -> item, (left, right) -> left));
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
    public void addSeckillApply(String seckillId, String storeId, List<SeckillApplyVO> seckillApplyList) {
        Seckill seckill = this.seckillService.getById(seckillId);
        if (seckill == null) {
            throw new ServiceException(ResultCode.SECKILL_NOT_EXIST_ERROR);
        }
        if (seckillApplyList == null || seckillApplyList.isEmpty()) {
            return;
        }
        //检查秒杀活动申请是否合法
        checkSeckillApplyList(seckill.getHours(), seckillApplyList);
        //获取已参与活动的秒杀活动活动申请列表
        List<String> skuIds = seckillApplyList.stream().map(SeckillApply::getSkuId).collect(Collectors.toList());
        List<SeckillApply> originList = new ArrayList<>();
        List<PromotionGoods> promotionGoodsList = new ArrayList<>();
        for (SeckillApplyVO seckillApply : seckillApplyList) {
            //获取参与活动的商品信息
            GoodsSku goodsSku = goodsSkuService.getCanPromotionGoodsSkuByIdFromCache(seckillApply.getSkuId());
            if (!goodsSku.getStoreId().equals(storeId)) {
                continue;
            }
            //获取秒杀活动时间段
            DateTime startTime = DateUtil.offsetHour(DateUtil.beginOfDay(seckill.getStartTime()), seckillApply.getTimeLine());
            //检测是否可以发布促销商品
            checkSeckillGoodsSku(seckill, seckillApply, goodsSku, startTime);
            //设置秒杀申请默认内容
            seckillApply.setOriginalPrice(goodsSku.getPrice());
            seckillApply.setPromotionApplyStatus(PromotionsApplyStatusEnum.PASS.name());
            seckillApply.setSalesNum(0);
            originList.add(seckillApply);
            //获取促销商品
            PromotionGoods promotionGoods = this.setSeckillGoods(goodsSku, seckillApply, seckill);
            promotionGoodsList.add(promotionGoods);
        }
        boolean result = true;
        this.remove(new LambdaQueryWrapper<SeckillApply>().eq(SeckillApply::getSeckillId, seckillId).in(SeckillApply::getSkuId, skuIds));
        this.saveBatch(originList);
        //保存促销活动商品信息
        if (!promotionGoodsList.isEmpty()) {
            PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
            searchParams.setPromotionId(seckillId);
            searchParams.setStoreId(storeId);
            searchParams.setPromotionType(PromotionTypeEnum.SECKILL.name());
            searchParams.setSkuIds(promotionGoodsList.stream().map(PromotionGoods::getSkuId).collect(Collectors.toList()));
            promotionGoodsService.deletePromotionGoods(searchParams);
            //初始化促销商品
            PromotionTools.promotionGoodsInit(promotionGoodsList, seckill, PromotionTypeEnum.SECKILL);
            result = promotionGoodsService.saveBatch(promotionGoodsList);
        }
        //设置秒杀活动的商品数量、店铺数量
        seckillService.updateSeckillGoodsNum(seckillId);
        cache.vagueDel(CachePrefix.STORE_ID_SECKILL);
        if (result) {
            this.seckillService.updateEsGoodsSeckill(seckill, originList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSeckillApplyByManager(String seckillId, List<SeckillApplyManagerDTO> applyList) {
        if (applyList == null || applyList.isEmpty()) {
            return;
        }
        Set<String> applyingSkuIds = applyList.stream()
                .map(SeckillApplyManagerDTO::getSkuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> registeredSkuIds = new HashSet<>();
        if (!applyingSkuIds.isEmpty()) {
            registeredSkuIds = this.list(new LambdaQueryWrapper<SeckillApply>()
                            .eq(SeckillApply::getSeckillId, seckillId)
                            .in(SeckillApply::getSkuId, applyingSkuIds))
                    .stream()
                    .map(SeckillApply::getSkuId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
        }
        Map<String, List<SeckillApplyVO>> storeApplyMap = new LinkedHashMap<>();
        Map<String, Store> storeMap = loadStoreMapBySkuIds(applyingSkuIds);
        for (SeckillApplyManagerDTO applyDTO : applyList) {
            if (registeredSkuIds.contains(applyDTO.getSkuId())) {
                continue;
            }
            GoodsSku goodsSku = goodsSkuService.getCanPromotionGoodsSkuByIdFromCache(applyDTO.getSkuId());
            checkPromotionSelectableSku(goodsSku, storeMap);
            SeckillApplyVO applyVO = new SeckillApplyVO();
            applyVO.setSeckillId(seckillId);
            applyVO.setSkuId(goodsSku.getId());
            applyVO.setGoodsName(goodsSku.getGoodsName());
            applyVO.setStoreId(goodsSku.getStoreId());
            applyVO.setStoreName(goodsSku.getStoreName());
            applyVO.setOriginalPrice(goodsSku.getPrice());
            applyVO.setPrice(applyDTO.getPrice());
            applyVO.setQuantity(applyDTO.getQuantity());
            applyVO.setTimeLine(applyDTO.getTimeLine());
            storeApplyMap.computeIfAbsent(goodsSku.getStoreId(), key -> new ArrayList<>()).add(applyVO);
        }
        for (Map.Entry<String, List<SeckillApplyVO>> entry : storeApplyMap.entrySet()) {
            addSeckillApply(seckillId, entry.getKey(), entry.getValue());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSeckillApplyByManager(String seckillId, String id, SeckillApplyManagerDTO apply) {
        Seckill seckill = this.seckillService.getById(seckillId);
        if (seckill == null) {
            throw new ServiceException(ResultCode.SECKILL_NOT_EXIST_ERROR);
        }
        SeckillApply origin = this.getById(id);
        if (origin == null || !seckillId.equals(origin.getSeckillId())) {
            throw new ServiceException(ResultCode.SECKILL_APPLY_NOT_EXIST_ERROR);
        }
        GoodsSku goodsSku = goodsSkuService.getCanPromotionGoodsSkuByIdFromCache(origin.getSkuId());
        checkPromotionSelectableSku(goodsSku, loadStoreMapBySkuIds(Collections.singleton(origin.getSkuId())));

        SeckillApplyVO applyVO = new SeckillApplyVO();
        BeanUtil.copyProperties(origin, applyVO);
        applyVO.setPrice(apply.getPrice());
        applyVO.setQuantity(apply.getQuantity());
        applyVO.setTimeLine(apply.getTimeLine());
        applyVO.setOriginalPrice(goodsSku.getPrice());
        checkSeckillApplyList(seckill.getHours(), Collections.singletonList(applyVO));

        DateTime startTime = DateUtil.offsetHour(DateUtil.beginOfDay(seckill.getStartTime()), applyVO.getTimeLine());
        checkSeckillGoodsSku(seckill, applyVO, goodsSku, startTime);

        origin.setPrice(applyVO.getPrice());
        origin.setQuantity(applyVO.getQuantity());
        origin.setTimeLine(applyVO.getTimeLine());
        origin.setOriginalPrice(goodsSku.getPrice());
        this.updateById(origin);

        promotionGoodsService.deletePromotionGoods(seckillId, Collections.singletonList(origin.getSkuId()));
        PromotionGoods promotionGoods = this.setSeckillGoods(goodsSku, origin, seckill);
        PromotionTools.promotionGoodsInit(Collections.singletonList(promotionGoods), seckill, PromotionTypeEnum.SECKILL);
        promotionGoodsService.save(promotionGoods);

        cache.vagueDel(CachePrefix.STORE_ID_SECKILL);
        this.seckillService.updateEsGoodsSeckill(seckill, Collections.singletonList(origin));
    }


    /**
     * 批量删除秒杀活动申请
     *
     * @param seckillId 秒杀活动活动id
     * @param id        id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSeckillApply(String seckillId, String id) {
        Seckill seckill = this.seckillService.getById(seckillId);
        if (seckill == null) {
            throw new ServiceException(ResultCode.SECKILL_NOT_EXIST_ERROR);
        }
        SeckillApply seckillApply = this.getById(id);
        if (seckillApply == null) {
            throw new ServiceException(ResultCode.SECKILL_APPLY_NOT_EXIST_ERROR);
        }


        //清除秒杀活动中的商品
        this.remove(new LambdaQueryWrapper<SeckillApply>()
                .eq(SeckillApply::getSeckillId, seckillId)
                .in(SeckillApply::getId, id));

        this.seckillService.deleteEsGoodsSeckill(seckill, Collections.singletonList(seckillApply.getSkuId()));
        //删除促销商品
        this.promotionGoodsService.deletePromotionGoods(seckillId, Collections.singletonList(seckillApply.getSkuId()));
    }

    /**
     * 更新秒杀商品出售数量
     *
     * @param seckillId 秒杀活动id
     * @param skuId     商品skuId
     * @param saleNum   库存
     */
    @Override
    public void updateSeckillApplySaleNum(String seckillId, String skuId, Integer saleNum) {
        LambdaUpdateWrapper<SeckillApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SeckillApply::getSeckillId, seckillId).eq(SeckillApply::getSkuId, skuId);
        updateWrapper.set(SeckillApply::getSalesNum, saleNum);
        this.update(updateWrapper);
    }

    /**
     * 更新秒杀活动时间
     *
     * @param seckill 秒杀活动
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSeckillApplyTime(Seckill seckill) {
        boolean result = false;
        List<PromotionGoods> promotionGoodsList = new ArrayList<>();
        LambdaQueryWrapper<SeckillApply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SeckillApply::getSeckillId, seckill.getId());

        List<SeckillApply> list = this.list(queryWrapper).stream().filter(i -> i.getTimeLine() != null && seckill.getHours().contains(i.getTimeLine().toString())).collect(Collectors.toList());

        for (SeckillApply seckillApply : list) {
            //获取参与活动的商品信息
            GoodsSku goodsSku = goodsSkuService.getCanPromotionGoodsSkuByIdFromCache(seckillApply.getSkuId());
            //获取促销商品
            PromotionGoods promotionGoods = this.setSeckillGoods(goodsSku, seckillApply, seckill);
            promotionGoodsList.add(promotionGoods);
        }
        //保存促销活动商品信息
        if (!promotionGoodsList.isEmpty()) {
            PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
            searchParams.setPromotionType(PromotionTypeEnum.SECKILL.name());
            searchParams.setPromotionId(seckill.getId());
            promotionGoodsService.deletePromotionGoods(searchParams);
            //初始化促销商品
            List<PromotionGoods> promotionGoods = PromotionTools.promotionGoodsInit(promotionGoodsList, seckill, PromotionTypeEnum.SECKILL);
            result = promotionGoodsService.saveBatch(promotionGoods);
            this.seckillService.updateEsGoodsSeckill(seckill, list);

            LambdaQueryWrapper<SeckillApply> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(SeckillApply::getSeckillId, seckill.getId());
            deleteWrapper.notIn(SeckillApply::getSkuId, promotionGoodsList.stream().map(PromotionGoods::getSkuId).collect(Collectors.toList()));
            this.remove(deleteWrapper);
        }

        seckillService.updateSeckillGoodsNum(seckill.getId());

        return result;
    }

    /**
     * 检查秒杀活动申请列表参数信息
     *
     * @param hours            秒杀活动时间段
     * @param seckillApplyList 秒杀活动申请列表
     */
    private void checkSeckillApplyList(String hours, List<SeckillApplyVO> seckillApplyList) {
        List<String> existSku = new ArrayList<>();
        for (SeckillApplyVO seckillApply : seckillApplyList) {
            if (seckillApply.getPrice() > seckillApply.getOriginalPrice()) {
                throw new ServiceException(ResultCode.SECKILL_PRICE_ERROR);
            }
            //检查秒杀活动申请的时刻，是否存在在秒杀活动的时间段内
            String[] rangeHours = hours.split(",");
            boolean containsSame = Arrays.stream(rangeHours).anyMatch(i -> i.equals(seckillApply.getTimeLine().toString()));
            if (!containsSame) {
                throw new ServiceException(ResultCode.SECKILL_TIME_ERROR);
            }
            //检查商品是否参加多个时间段的活动
            if (existSku.contains(seckillApply.getSkuId())) {
                throw new ServiceException(seckillApply.getGoodsName() + "该商品不能同时参加多个时间段的活动");
            } else {
                existSku.add(seckillApply.getSkuId());
            }

        }
    }

    /**
     * 获取秒杀活动信息
     *
     * @return 秒杀活动信息
     */
    private List<SeckillTimelineVO> getSeckillTimelineInfo() {
        List<SeckillTimelineVO> timelineList = new ArrayList<>();
        for (SeckillTimelineCandidate candidate : buildTodayTimelineCandidates()) {
            timelineList.add(buildTimelineVO(candidate, wrapperSeckillGoods(candidate.getTimeLine(), candidate.getSeckillId())));
        }
        return timelineList;
    }

    /**
     * 获取首页展示的秒杀场次，商品查询限制在首页展示数量内。
     *
     * @param goodsLimit 商品数量上限
     * @return 首页秒杀场次
     */
    private SeckillTimelineVO getHomeSeckillTimelineInfo(int goodsLimit) {
        List<SeckillTimelineCandidate> candidates = sortTimelineCandidatesByDistance(buildTodayTimelineCandidates());
        if (candidates.isEmpty()) {
            return null;
        }
        int limit = goodsLimit <= 0 ? 1 : Math.min(goodsLimit, 50);
        for (SeckillTimelineCandidate candidate : candidates) {
            List<SeckillGoodsVO> goodsList = wrapperSeckillGoods(candidate.getTimeLine(), candidate.getSeckillId(), limit);
            if (!goodsList.isEmpty()) {
                return buildTimelineVO(candidate, goodsList);
            }
        }
        return buildTimelineVO(candidates.get(0), Collections.emptyList());
    }

    /**
     * 生成当天有效秒杀活动的场次候选。
     *
     * @return 场次候选
     */
    private List<SeckillTimelineCandidate> buildTodayTimelineCandidates() {
        List<SeckillTimelineCandidate> candidates = new ArrayList<>();
        LambdaQueryWrapper<Seckill> queryWrapper = new LambdaQueryWrapper<>();
        // 查询当天有效的秒杀活动，支持活动跨天覆盖当天。
        Date now = new Date();
        queryWrapper.le(BasePromotions::getStartTime, DateUtil.endOfDay(now));
        queryWrapper.ge(BasePromotions::getEndTime, DateUtil.beginOfDay(now));
        queryWrapper.eq(Seckill::getDeleteFlag, false);
        List<Seckill> seckillList = this.seckillService.list(queryWrapper);
        SimpleDateFormat format = new SimpleDateFormat(DatePattern.NORM_DATE_PATTERN);
        String date = format.format(now);
        long currentTime = DateUtil.currentSeconds();
        for (Seckill seckill : seckillList) {
            String[] split = seckill.getHours().split(",");
            int[] hoursSored = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
            Arrays.sort(hoursSored);
            for (int i = 0; i < hoursSored.length; i++) {
                long timeLine = cn.lili.common.utils.DateUtil.getDateline(date + " " + hoursSored[i], "yyyy-MM-dd HH");
                Long distanceTime = timeLine - currentTime < 0 ? 0 : timeLine - currentTime;
                candidates.add(new SeckillTimelineCandidate(seckill.getId(), hoursSored[i], timeLine, distanceTime));
            }
        }
        candidates.sort(Comparator
                .comparing(SeckillTimelineCandidate::getTimeLine, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SeckillTimelineCandidate::getStartTime, Comparator.nullsLast(Long::compareTo))
                .thenComparing(SeckillTimelineCandidate::getSeckillId, Comparator.nullsLast(String::compareTo)));
        return candidates;
    }

    private List<SeckillTimelineCandidate> sortTimelineCandidatesByDistance(List<SeckillTimelineCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        return candidates.stream()
                .sorted(Comparator
                        .comparing(SeckillTimelineCandidate::getDistanceStartTime, Comparator.nullsLast(Long::compareTo))
                        .thenComparing(SeckillTimelineCandidate::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * 组装秒杀场次视图。
     *
     * @param candidate 场次候选
     * @param goodsList 商品列表
     * @return 秒杀场次视图
     */
    private SeckillTimelineVO buildTimelineVO(SeckillTimelineCandidate candidate, List<SeckillGoodsVO> goodsList) {
        SeckillTimelineVO tempTimeline = new SeckillTimelineVO();
        tempTimeline.setDistanceStartTime(candidate.getDistanceStartTime());
        tempTimeline.setStartTime(candidate.getStartTime());
        tempTimeline.setTimeLine(candidate.getTimeLine());
        tempTimeline.setSeckillGoodsList(goodsList == null ? Collections.emptyList() : goodsList);
        return tempTimeline;
    }

    /**
     * 组装当时间秒杀活动的商品数据
     * w
     *
     * @param startTimeline 秒杀活动开始时刻
     * @return 当时间秒杀活动的商品数据
     */
    private List<SeckillGoodsVO> wrapperSeckillGoods(Integer startTimeline, String seckillId) {
        return wrapperSeckillGoods(startTimeline, seckillId, 0);
    }

    /**
     * 组装指定场次秒杀商品数据。
     *
     * @param startTimeline 秒杀场次
     * @param seckillId 秒杀活动编号
     * @param goodsLimit 商品数量上限，0表示不限制
     * @return 秒杀商品列表
     */
    private List<SeckillGoodsVO> wrapperSeckillGoods(Integer startTimeline, String seckillId, int goodsLimit) {
        List<SeckillGoodsVO> seckillGoodsVoS = new ArrayList<>();
        LambdaQueryWrapper<SeckillApply> queryWrapper = new LambdaQueryWrapper<SeckillApply>()
                .eq(SeckillApply::getSeckillId, seckillId)
                .eq(SeckillApply::getTimeLine, startTimeline)
                .eq(SeckillApply::getPromotionApplyStatus, PromotionsApplyStatusEnum.PASS.name())
                .eq(SeckillApply::getDeleteFlag, false)
                .orderByDesc(SeckillApply::getCreateTime);
        if (goodsLimit > 0) {
            queryWrapper.last("limit " + goodsLimit);
        }
        List<SeckillApply> seckillApplyList = this.list(queryWrapper);
        for (SeckillApply seckillApply : seckillApplyList) {
            GoodsSku goodsSku = goodsSkuService.getCanPromotionGoodsSkuByIdFromCache(seckillApply.getSkuId());
            if (goodsSku != null) {
                SeckillGoodsVO goodsVO = new SeckillGoodsVO();
                BeanUtil.copyProperties(seckillApply, goodsVO);
                goodsVO.setGoodsImage(goodsSku.getThumbnail());
                goodsVO.setGoodsId(goodsSku.getGoodsId());
                goodsVO.setGoodsName(goodsSku.getGoodsName());
                seckillGoodsVoS.add(goodsVO);
            }
        }
        return seckillGoodsVoS;
    }

    private List<String> querySkuIdsByCategory(String categoryPath, List<String> visibleStoreIds) {
        if (StringUtils.isBlank(categoryPath)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<GoodsSku> queryWrapper = new LambdaQueryWrapper<GoodsSku>()
                .eq(GoodsSku::getDeleteFlag, false)
                .apply("CONCAT(',', category_path, ',') LIKE {0}", "%," + categoryPath + ",%");
        if (visibleStoreIds != null && !visibleStoreIds.isEmpty()) {
            queryWrapper.in(GoodsSku::getStoreId, visibleStoreIds);
        }
        return goodsSkuService.list(queryWrapper).stream()
                .map(GoodsSku::getId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<SeckillGoodsVO> buildSeckillGoodsVOList(List<SeckillApply> seckillApplyList) {
        if (seckillApplyList == null || seckillApplyList.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, GoodsSku> skuMap = goodsSkuService.listByIds(seckillApplyList.stream()
                        .map(SeckillApply::getSkuId)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(GoodsSku::getId, item -> item, (left, right) -> left));
        List<SeckillGoodsVO> result = new ArrayList<>();
        for (SeckillApply seckillApply : seckillApplyList) {
            GoodsSku goodsSku = skuMap.get(seckillApply.getSkuId());
            if (goodsSku == null) {
                continue;
            }
            SeckillGoodsVO goodsVO = new SeckillGoodsVO();
            BeanUtil.copyProperties(seckillApply, goodsVO);
            goodsVO.setGoodsImage(goodsSku.getThumbnail());
            goodsVO.setGoodsId(goodsSku.getGoodsId());
            goodsVO.setGoodsName(goodsSku.getGoodsName());
            result.add(goodsVO);
        }
        return result;
    }

    /**
     * 检测秒杀申请的商品
     *
     * @param seckill      秒杀活动
     * @param seckillApply 秒杀活动申请
     * @param goodsSku     商品SKU
     * @param startTime    秒杀时段开启时间
     */
    private void checkSeckillGoodsSku(Seckill seckill, SeckillApplyVO seckillApply, GoodsSku goodsSku, DateTime startTime) {
        //活动库存不能大于商品库存
        if (goodsSku.getQuantity() < seckillApply.getQuantity()) {
            throw new ServiceException(seckillApply.getGoodsName() + ",此商品库存不足");
        }
        //查询是否在同一时间段参与了拼团活动
        if (promotionGoodsService.findInnerOverlapPromotionGoods(PromotionTypeEnum.PINTUAN.name(), goodsSku.getId(), startTime, seckill.getEndTime(), seckill.getId()) > 0) {
            throw new ServiceException("商品[" + goodsSku.getGoodsName() + "]已经在重叠的时间段参加了拼团活动，不能参加秒杀活动");
        }
        //查询是否在同一时间段参与了秒杀活动活动
        if (promotionGoodsService.findInnerOverlapPromotionGoods(PromotionTypeEnum.SECKILL.name(), goodsSku.getId(), startTime, seckill.getEndTime(), seckill.getId()) > 0) {
            throw new ServiceException("商品[" + goodsSku.getGoodsName() + "]已经在重叠的时间段参加了秒杀活动，不能参加秒杀活动活动");
        }
    }

    /**
     * 获取秒杀活动促销商品
     *
     * @param goodsSku     商品SKU
     * @param seckillApply 秒杀活动申请
     * @param seckill      秒杀活动
     * @return 秒杀活动促销商品
     */
    private PromotionGoods setSeckillGoods(GoodsSku goodsSku, SeckillApply seckillApply, Seckill seckill) {
        //设置促销商品默认内容
        PromotionGoods promotionGoods = new PromotionGoods(goodsSku);
        promotionGoods.setPrice(seckillApply.getPrice());
        promotionGoods.setQuantity(seckillApply.getQuantity());
        //设置单独每个促销商品的结束时间
        DateTime startTime = DateUtil.offsetHour(DateUtil.beginOfDay(seckill.getStartTime()), seckillApply.getTimeLine());
        promotionGoods.setStartTime(startTime);
        if (seckill.getEndTime() == null) {
            promotionGoods.setEndTime(DateUtil.endOfDay(startTime));
        } else {
            promotionGoods.setEndTime(seckill.getEndTime());
        }
        return promotionGoods;
    }

    /**
     * 首页秒杀场次候选。
     */
    private static class SeckillTimelineCandidate {

        private final String seckillId;

        private final Integer timeLine;

        private final Long startTime;

        private final Long distanceStartTime;

        private SeckillTimelineCandidate(String seckillId, Integer timeLine, Long startTime, Long distanceStartTime) {
            this.seckillId = seckillId;
            this.timeLine = timeLine;
            this.startTime = startTime;
            this.distanceStartTime = distanceStartTime;
        }

        public String getSeckillId() {
            return seckillId;
        }

        public Integer getTimeLine() {
            return timeLine;
        }

        public Long getStartTime() {
            return startTime;
        }

        public Long getDistanceStartTime() {
            return distanceStartTime;
        }
    }

}
