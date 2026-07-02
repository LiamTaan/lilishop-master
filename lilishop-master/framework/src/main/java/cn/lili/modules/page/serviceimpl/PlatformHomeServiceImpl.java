package cn.lili.modules.page.serviceimpl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import cn.lili.common.enums.ClientTypeEnum;
import cn.lili.common.enums.PromotionTypeEnum;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.enums.GoodsAuthEnum;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.member.entity.dos.FootPrint;
import cn.lili.modules.member.entity.dos.GoodsCollection;
import cn.lili.modules.member.entity.dos.StoreCollection;
import cn.lili.modules.member.service.FootprintService;
import cn.lili.modules.member.service.GoodsCollectionService;
import cn.lili.modules.member.service.StoreCollectionService;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.entity.dos.OrderItem;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.service.OrderItemService;
import cn.lili.modules.order.order.service.OrderService;
import cn.lili.modules.page.entity.dto.PageDataDTO;
import cn.lili.modules.page.entity.enums.PageEnum;
import cn.lili.modules.page.entity.vos.PageDataVO;
import cn.lili.modules.page.entity.vos.PlatformHomeBannerVO;
import cn.lili.modules.page.entity.vos.PlatformHomeFloorModuleVO;
import cn.lili.modules.page.entity.vos.PlatformHomeGoodsCardVO;
import cn.lili.modules.page.entity.vos.PlatformHomeRecommendationConfigVO;
import cn.lili.modules.page.entity.vos.PlatformHomeRecommendationModuleVO;
import cn.lili.modules.page.entity.vos.PlatformHomeSeckillVO;
import cn.lili.modules.page.entity.vos.PlatformHomeShortcutNavVO;
import cn.lili.modules.page.entity.vos.PlatformHomeStoreCardVO;
import cn.lili.modules.page.entity.vos.PlatformHomeVO;
import cn.lili.modules.page.service.OperationShortcutNavService;
import cn.lili.modules.page.service.PageDataService;
import cn.lili.modules.page.service.PlatformHomeService;
import cn.lili.modules.promotion.entity.dos.PromotionGoods;
import cn.lili.modules.promotion.entity.enums.PromotionsStatusEnum;
import cn.lili.modules.promotion.entity.vos.SeckillGoodsVO;
import cn.lili.modules.promotion.entity.vos.SeckillTimelineVO;
import cn.lili.modules.promotion.service.PromotionGoodsService;
import cn.lili.modules.promotion.service.SeckillApplyService;
import cn.lili.modules.promotion.tools.PromotionTools;
import cn.lili.modules.statistics.entity.dto.GoodsStatisticsQueryParam;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.vo.GoodsStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.StoreStatisticsDataVO;
import cn.lili.modules.statistics.service.IndexStatisticsService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import cn.lili.modules.store.service.StoreService;
import cn.lili.modules.store.support.BuyerStoreScopeSupport;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 平台首页聚合服务实现
 *
 * @author codex
 * @since 2026/6/25
 */
@Service
public class PlatformHomeServiceImpl implements PlatformHomeService {

    private static final String SECTION_TYPE_CAROUSEL = "carousel";
    private static final String SECTION_TYPE_TOP_ADVERT = "topAdvert";
    private static final String SECTION_TYPE_WHOLESALE_FLOOR = "wholesaleFloor";
    private static final String SECTION_TYPE_RECOMMENDATION_CONFIG = "recommendationConfig";
    private static final int DEFAULT_HOT_GOODS_LIMIT = 6;
    private static final int DEFAULT_NEW_GOODS_LIMIT = 6;
    private static final int DEFAULT_SECKILL_GOODS_LIMIT = 6;
    private static final int DEFAULT_FREQUENT_STORE_LIMIT = 4;
    private static final int BEHAVIOR_ORDER_LIMIT = 20;
    private static final int BEHAVIOR_FOOTPRINT_LIMIT = 50;
    private static final int BEHAVIOR_COLLECTION_LIMIT = 20;
    private static final int GOODS_QUERY_BUFFER = 5;
    private static final int MAX_GOODS_QUERY_SIZE = 200;
    private static final String RECOMMENDATION_MODULE_TYPE_STORE = "STORE";
    private static final String RECOMMENDATION_MODULE_TYPE_GOODS = "GOODS";

    @Autowired
    private PageDataService pageDataService;
    @Autowired
    private OperationShortcutNavService operationShortcutNavService;
    @Autowired
    private SeckillApplyService seckillApplyService;
    @Autowired
    private IndexStatisticsService indexStatisticsService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsSkuService goodsSkuService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private StoreCollectionService storeCollectionService;
    @Autowired
    private GoodsCollectionService goodsCollectionService;
    @Autowired
    private FootprintService footprintService;
    @Autowired
    private PromotionGoodsService promotionGoodsService;
    @Autowired
    private BuyerStoreScopeSupport buyerStoreScopeSupport;

    @Override
    public PlatformHomeVO getPlatformHome(String clientType) {
        String effectiveClientType = normalizeClientType(clientType);
        PageDataVO pageDataVO = getIndexPageData(effectiveClientType);
        RecommendationConfigBundle configBundle = resolveRecommendationConfig(pageDataVO);
        List<String> visibleStoreIds = buyerStoreScopeSupport.listVisibleStoreIds();

        PlatformHomeVO vo = new PlatformHomeVO();
        vo.setClientType(effectiveClientType);
        vo.setBanners(resolveBanners(pageDataVO));
        vo.setShortcutNavList(resolveShortcutNav(effectiveClientType));
        vo.setFloorModules(resolveFloorModules(pageDataVO));

        PlatformHomeSeckillVO seckill = resolveSeckill(visibleStoreIds);
        List<PlatformHomeGoodsCardVO> monthlyHotGoods = resolveMonthlyHotGoods(DEFAULT_HOT_GOODS_LIMIT, visibleStoreIds);
        List<PlatformHomeGoodsCardVO> newGoods = resolveNewGoods(DEFAULT_NEW_GOODS_LIMIT, visibleStoreIds);
        List<PlatformHomeStoreCardVO> frequentStores = resolveFrequentStores(configBundle.getFrequentStoresConfig(), visibleStoreIds);

        List<PlatformHomeGoodsCardVO> guessYouLikeGoods = resolveGuessYouLike(configBundle.getGuessYouLikeConfig(), new LinkedHashSet<>(), visibleStoreIds);
        List<PlatformHomeGoodsCardVO> lowPriceZoneGoods = resolveLowPriceZone(configBundle.getLowPriceZoneConfig(), new LinkedHashSet<>(), visibleStoreIds);
        List<PlatformHomeGoodsCardVO> todaySpecialGoods = resolveTodaySpecial(configBundle.getTodaySpecialConfig(), new LinkedHashSet<>(), visibleStoreIds);
        normalizeGoodsCardStoreNames(
                seckill == null ? null : seckill.getGoodsList(),
                monthlyHotGoods,
                newGoods,
                guessYouLikeGoods,
                lowPriceZoneGoods,
                todaySpecialGoods
        );
        vo.setSeckill(seckill);
        vo.setMonthlyHotGoods(monthlyHotGoods);
        vo.setNewGoods(newGoods);
        vo.setFrequentStores(frequentStores);
        vo.setGuessYouLikeGoods(guessYouLikeGoods);
        vo.setLowPriceZoneGoods(lowPriceZoneGoods);
        vo.setTodaySpecialGoods(todaySpecialGoods);
        vo.setRecommendationModules(buildRecommendationModules(
                configBundle,
                frequentStores,
                guessYouLikeGoods,
                lowPriceZoneGoods,
                todaySpecialGoods
        ));
        return vo;
    }

    private String normalizeClientType(String clientType) {
        if (CharSequenceUtil.isBlank(clientType)) {
            return ClientTypeEnum.H5.name();
        }
        try {
            return ClientTypeEnum.valueOf(clientType.trim().toUpperCase()).name();
        } catch (IllegalArgumentException exception) {
            return ClientTypeEnum.H5.name();
        }
    }

    private List<PlatformHomeBannerVO> resolveBanners(PageDataVO pageDataVO) {
        if (pageDataVO == null || CharSequenceUtil.isBlank(pageDataVO.getPageData())) {
            return Collections.emptyList();
        }
        try {
            JSONObject root = JSON.parseObject(pageDataVO.getPageData());
            JSONArray sectionList = root.getJSONArray("list");
            if (sectionList == null || sectionList.isEmpty()) {
                return Collections.emptyList();
            }
            List<PlatformHomeBannerVO> banners = new ArrayList<>();
            for (int i = 0; i < sectionList.size(); i++) {
                JSONObject section = sectionList.getJSONObject(i);
                if (section == null) {
                    continue;
                }
                String type = section.getString("type");
                if (SECTION_TYPE_CAROUSEL.equals(type)) {
                    JSONObject options = section.getJSONObject("options");
                    JSONArray list = options == null ? null : options.getJSONArray("list");
                    if (list == null) {
                        continue;
                    }
                    for (int j = 0; j < list.size(); j++) {
                        JSONObject item = list.getJSONObject(j);
                        PlatformHomeBannerVO banner = new PlatformHomeBannerVO();
                        banner.setImage(item.getString("img"));
                        banner.setUrl(item.getString("url"));
                        banner.setLinkType(item.getString("linkType"));
                        banner.setLinkValue(firstNotBlank(item.getString("linkValue"), item.getString("url")));
                        if (CharSequenceUtil.isNotBlank(banner.getImage())) {
                            banners.add(banner);
                        }
                    }
                } else if (SECTION_TYPE_TOP_ADVERT.equals(type)) {
                    PlatformHomeBannerVO banner = new PlatformHomeBannerVO();
                    banner.setImage(section.getString("img"));
                    banner.setUrl(section.getString("url"));
                    banner.setLinkType(section.getString("linkType"));
                    banner.setLinkValue(firstNotBlank(section.getString("linkValue"), section.getString("url")));
                    if (CharSequenceUtil.isNotBlank(banner.getImage())) {
                        banners.add(banner);
                    }
                }
            }
            return banners;
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    private PageDataVO getIndexPageData(String clientType) {
        PageDataDTO dto = new PageDataDTO(PageEnum.INDEX.name());
        dto.setPageClientType(clientType);
        PageDataVO pageDataVO = pageDataService.getPageData(dto);
        if (pageDataVO == null && ClientTypeEnum.APP.name().equals(clientType)) {
            dto.setPageClientType(ClientTypeEnum.H5.name());
            return pageDataService.getPageData(dto);
        }
        return pageDataVO;
    }

    private List<PlatformHomeShortcutNavVO> resolveShortcutNav(String clientType) {
        List<PlatformHomeShortcutNavVO> result = operationShortcutNavService.listBuyerNav(clientType).stream()
                .map(item -> {
                    PlatformHomeShortcutNavVO navVO = new PlatformHomeShortcutNavVO();
                    navVO.setTitle(item.getTitle());
                    navVO.setSubtitle(item.getSubtitle());
                    navVO.setImage(item.getImage());
                    navVO.setLinkType(item.getLinkType());
                    navVO.setLinkValue(item.getLinkValue());
                    return navVO;
                })
                .collect(Collectors.toList());
        if (result.isEmpty() && ClientTypeEnum.APP.name().equals(clientType)) {
            return operationShortcutNavService.listBuyerNav(ClientTypeEnum.H5.name()).stream()
                    .map(item -> {
                        PlatformHomeShortcutNavVO navVO = new PlatformHomeShortcutNavVO();
                        navVO.setTitle(item.getTitle());
                        navVO.setSubtitle(item.getSubtitle());
                        navVO.setImage(item.getImage());
                        navVO.setLinkType(item.getLinkType());
                        navVO.setLinkValue(item.getLinkValue());
                        return navVO;
                    })
                    .collect(Collectors.toList());
        }
        return result;
    }

    private List<PlatformHomeFloorModuleVO> resolveFloorModules(PageDataVO pageDataVO) {
        if (pageDataVO == null || CharSequenceUtil.isBlank(pageDataVO.getPageData())) {
            return Collections.emptyList();
        }
        try {
            JSONObject root = JSON.parseObject(pageDataVO.getPageData());
            JSONArray sectionList = root.getJSONArray("list");
            if (sectionList == null || sectionList.isEmpty()) {
                return Collections.emptyList();
            }
            List<PlatformHomeFloorModuleVO> floorModules = new ArrayList<>();
            for (int i = 0; i < sectionList.size(); i++) {
                JSONObject section = sectionList.getJSONObject(i);
                if (section == null || !SECTION_TYPE_WHOLESALE_FLOOR.equals(section.getString("type"))) {
                    continue;
                }
                JSONObject options = section.getJSONObject("options");
                if (options == null) {
                    continue;
                }
                String displayStatus = CharSequenceUtil.blankToDefault(options.getString("displayStatus"), "OPEN");
                if (!"OPEN".equalsIgnoreCase(displayStatus)) {
                    continue;
                }
                PlatformHomeFloorModuleVO floorModuleVO = new PlatformHomeFloorModuleVO();
                floorModuleVO.setTitle(firstNotBlank(options.getString("title"), section.getString("title"), section.getString("name")));
                floorModuleVO.setSubtitle(firstNotBlank(options.getString("subtitle"), section.getString("subtitle")));
                floorModuleVO.setImage(firstNotBlank(options.getString("image"), section.getString("img")));
                floorModuleVO.setModuleType(options.getString("moduleType"));
                floorModuleVO.setSourceType(options.getString("sourceType"));
                floorModuleVO.setLinkType(options.getString("linkType"));
                floorModuleVO.setLinkValue(options.getString("linkValue"));
                floorModuleVO.setGoodsLimit(options.getInteger("goodsLimit"));
                floorModuleVO.setSortOrder(options.getInteger("sortOrder"));
                floorModuleVO.setDisplayStatus(displayStatus);
                floorModuleVO.setSpecialId(options.getString("specialId"));
                floorModuleVO.setSpecialName(options.getString("specialName"));
                floorModuleVO.setRemark(options.getString("remark"));
                if (CharSequenceUtil.isNotBlank(floorModuleVO.getTitle())) {
                    floorModules.add(floorModuleVO);
                }
            }
            floorModules.sort(Comparator.comparing(item -> item.getSortOrder() == null ? Integer.MAX_VALUE : item.getSortOrder()));
            return floorModules;
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    private RecommendationConfigBundle resolveRecommendationConfig(PageDataVO pageDataVO) {
        RecommendationConfigBundle bundle = new RecommendationConfigBundle();
        if (pageDataVO == null || CharSequenceUtil.isBlank(pageDataVO.getPageData())) {
            return bundle;
        }
        try {
            JSONObject root = JSON.parseObject(pageDataVO.getPageData());
            JSONArray sectionList = root == null ? null : root.getJSONArray("list");
            if (sectionList == null) {
                return bundle;
            }
            for (int i = 0; i < sectionList.size(); i++) {
                JSONObject section = sectionList.getJSONObject(i);
                if (section == null || !SECTION_TYPE_RECOMMENDATION_CONFIG.equals(section.getString("type"))) {
                    continue;
                }
                JSONObject options = section.getJSONObject("options");
                if (options == null) {
                    continue;
                }
                bundle.setFrequentStoresConfig(parseRecommendationConfig(
                        options.getJSONObject("frequentStoresConfig"),
                        defaultRecommendationConfig("FREQUENT_STORES")
                ));
                bundle.setGuessYouLikeConfig(parseRecommendationConfig(
                        options.getJSONObject("guessYouLikeConfig"),
                        defaultRecommendationConfig("GUESS_YOU_LIKE")
                ));
                bundle.setLowPriceZoneConfig(parseRecommendationConfig(
                        options.getJSONObject("lowPriceZoneConfig"),
                        defaultRecommendationConfig("LOW_PRICE")
                ));
                bundle.setTodaySpecialConfig(parseRecommendationConfig(
                        options.getJSONObject("todaySpecialConfig"),
                        defaultRecommendationConfig("TODAY_SPECIAL")
                ));
                break;
            }
        } catch (Exception ignored) {
            return bundle;
        }
        return bundle;
    }

    private PlatformHomeRecommendationConfigVO parseRecommendationConfig(JSONObject configObject,
                                                                        PlatformHomeRecommendationConfigVO defaults) {
        PlatformHomeRecommendationConfigVO config = copyRecommendationConfig(defaults);
        if (configObject == null) {
            return config;
        }
        if (configObject.containsKey("enabled")) {
            config.setEnabled(configObject.getBoolean("enabled"));
        }
        config.setTitle(CharSequenceUtil.blankToDefault(configObject.getString("title"), defaults.getTitle()));
        config.setSortOrder(configObject.getInteger("sortOrder") == null ? defaults.getSortOrder() : configObject.getInteger("sortOrder"));
        config.setLimit(configObject.getInteger("limit") == null ? defaults.getLimit() : configObject.getInteger("limit"));
        List<String> categoryRange = readStringList(configObject, "categoryRange");
        config.setCategoryRange(categoryRange.isEmpty() ? defaults.getCategoryRange() : categoryRange);
        if (configObject.containsKey("preferRecommendFlag")) {
            config.setPreferRecommendFlag(configObject.getBoolean("preferRecommendFlag"));
        }
        config.setColdStartStrategy(CharSequenceUtil.blankToDefault(configObject.getString("coldStartStrategy"), defaults.getColdStartStrategy()));
        config.setPriceUpperBound(configObject.getDouble("priceUpperBound") == null ? defaults.getPriceUpperBound() : configObject.getDouble("priceUpperBound"));
        List<String> promotionTypes = readStringList(configObject, "promotionTypes");
        config.setPromotionTypes(promotionTypes.isEmpty() ? defaults.getPromotionTypes() : promotionTypes);
        return config;
    }

    private List<String> readStringList(JSONObject object, String key) {
        JSONArray array = object == null ? null : object.getJSONArray(key);
        if (array == null || array.isEmpty()) {
            return Collections.emptyList();
        }
        return array.stream()
                .map(item -> item == null ? null : String.valueOf(item).trim())
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private PlatformHomeRecommendationConfigVO copyRecommendationConfig(PlatformHomeRecommendationConfigVO source) {
        PlatformHomeRecommendationConfigVO target = new PlatformHomeRecommendationConfigVO();
        if (source == null) {
            return target;
        }
        target.setEnabled(source.getEnabled());
        target.setTitle(source.getTitle());
        target.setSortOrder(source.getSortOrder());
        target.setLimit(source.getLimit());
        target.setCategoryRange(source.getCategoryRange() == null ? Collections.emptyList() : new ArrayList<>(source.getCategoryRange()));
        target.setPreferRecommendFlag(source.getPreferRecommendFlag());
        target.setColdStartStrategy(source.getColdStartStrategy());
        target.setPriceUpperBound(source.getPriceUpperBound());
        target.setPromotionTypes(source.getPromotionTypes() == null ? Collections.emptyList() : new ArrayList<>(source.getPromotionTypes()));
        return target;
    }

    private PlatformHomeRecommendationConfigVO defaultRecommendationConfig(String code) {
        PlatformHomeRecommendationConfigVO config = new PlatformHomeRecommendationConfigVO();
        config.setEnabled(Boolean.TRUE);
        config.setSortOrder(0);
        config.setCategoryRange(Collections.emptyList());
        config.setPromotionTypes(Collections.emptyList());
        switch (code) {
            case "FREQUENT_STORES":
                config.setTitle("常买店铺");
                config.setLimit(DEFAULT_FREQUENT_STORE_LIMIT);
                break;
            case "GUESS_YOU_LIKE":
                config.setTitle("猜你喜欢");
                config.setLimit(12);
                config.setPreferRecommendFlag(Boolean.TRUE);
                config.setColdStartStrategy("HOT_AND_NEW");
                break;
            case "LOW_PRICE":
                config.setTitle("低价专区");
                config.setLimit(12);
                config.setPriceUpperBound(99D);
                break;
            case "TODAY_SPECIAL":
                config.setTitle("今日特惠");
                config.setLimit(12);
                config.setPromotionTypes(new ArrayList<>(List.of(
                        PromotionTypeEnum.SECKILL.name(),
                        PromotionTypeEnum.PINTUAN.name(),
                        PromotionTypeEnum.KANJIA.name()
                )));
                break;
            default:
                break;
        }
        return config;
    }

    private PlatformHomeSeckillVO resolveSeckill(List<String> visibleStoreIds) {
        List<SeckillTimelineVO> timeLineList = seckillApplyService.getSeckillTimeline();
        if (timeLineList == null || timeLineList.isEmpty()) {
            return null;
        }
        SeckillTimelineVO active = null;
        List<SeckillGoodsVO> visibleGoodsList = Collections.emptyList();
        for (SeckillTimelineVO timeline : timeLineList) {
            List<SeckillGoodsVO> filteredGoods = timeline.getSeckillGoodsList() == null
                    ? Collections.emptyList()
                    : timeline.getSeckillGoodsList().stream()
                    .filter(item -> isVisibleStore(item.getStoreId(), visibleStoreIds))
                    .limit(DEFAULT_SECKILL_GOODS_LIMIT)
                    .collect(Collectors.toList());
            if (!filteredGoods.isEmpty()) {
                active = timeline;
                visibleGoodsList = filteredGoods;
                break;
            }
        }
        if (active == null) {
            return null;
        }
        PlatformHomeSeckillVO seckillVO = new PlatformHomeSeckillVO();
        seckillVO.setTimeLine(active.getTimeLine());
        seckillVO.setStartTime(active.getStartTime());
        seckillVO.setDistanceStartTime(active.getDistanceStartTime());
        seckillVO.setGoodsList(visibleGoodsList.stream().map(this::buildSeckillGoodsCard).collect(Collectors.toList()));
        return seckillVO;
    }

    private List<PlatformHomeGoodsCardVO> resolveMonthlyHotGoods(int limit, List<String> visibleStoreIds) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        GoodsStatisticsQueryParam queryParam = new GoodsStatisticsQueryParam();
        DateTime dateTime = new DateTime();
        queryParam.setYear(dateTime.year());
        queryParam.setMonth(dateTime.monthBaseOne());
        List<GoodsStatisticsDataVO> statistics = indexStatisticsService.goodsStatistics(queryParam);
        if (statistics == null || statistics.isEmpty()) {
            return loadHotGoodsFallback(limit, visibleStoreIds).stream()
                    .map(item -> buildGoodsCard(item, item.getBuyCount()))
                    .collect(Collectors.toList());
        }
        List<String> goodsIds = statistics.stream()
                .map(GoodsStatisticsDataVO::getGoodsId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
        Map<String, Goods> goodsMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(Goods::getId, Function.identity(), (left, right) -> left));
        List<PlatformHomeGoodsCardVO> result = new ArrayList<>();
        for (GoodsStatisticsDataVO statistic : statistics) {
            Goods goods = goodsMap.get(statistic.getGoodsId());
            if (!isActiveGoods(goods) || !isVisibleStore(goods.getStoreId(), visibleStoreIds)) {
                continue;
            }
            result.add(buildGoodsCard(goods, parseInteger(statistic.getNum())));
            if (result.size() >= limit) {
                break;
            }
        }
        if (result.size() < limit) {
            appendGoodsCards(
                    result,
                    loadHotGoodsFallback(limit * GOODS_QUERY_BUFFER, visibleStoreIds),
                    result.stream().map(PlatformHomeGoodsCardVO::getGoodsId).collect(Collectors.toSet()),
                    limit
            );
        }
        return result;
    }

    private List<Goods> loadHotGoods(int limit, List<String> visibleStoreIds) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        GoodsStatisticsQueryParam queryParam = new GoodsStatisticsQueryParam();
        DateTime dateTime = new DateTime();
        queryParam.setYear(dateTime.year());
        queryParam.setMonth(dateTime.monthBaseOne());
        List<GoodsStatisticsDataVO> statistics = indexStatisticsService.goodsStatistics(queryParam);
        if (statistics == null || statistics.isEmpty()) {
            return loadHotGoodsFallback(limit, visibleStoreIds);
        }
        List<String> goodsIds = statistics.stream()
                .map(GoodsStatisticsDataVO::getGoodsId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
        return reorderGoodsByIds(goodsIds).stream()
                .filter(goods -> isVisibleStore(goods.getStoreId(), visibleStoreIds))
                .collect(Collectors.toList());
    }

    private List<Goods> loadHotGoodsFallback(int limit, List<String> visibleStoreIds) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Goods> queryWrapper = buildActiveGoodsQuery();
        applyGoodsStoreFilter(queryWrapper, visibleStoreIds);
        queryWrapper.orderByDesc(Goods::getBuyCount)
                .orderByDesc(Goods::getCreateTime)
                .last("limit " + Math.min(limit, MAX_GOODS_QUERY_SIZE));
        return goodsService.list(queryWrapper);
    }

    private List<PlatformHomeGoodsCardVO> resolveNewGoods(int limit, List<String> visibleStoreIds) {
        return loadNewGoods(limit, null, visibleStoreIds).stream()
                .map(item -> buildGoodsCard(item, item.getBuyCount()))
                .collect(Collectors.toList());
    }

    private List<Goods> loadNewGoods(int limit, Date createdAfter, List<String> visibleStoreIds) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Goods> queryWrapper = buildActiveGoodsQuery();
        applyGoodsStoreFilter(queryWrapper, visibleStoreIds);
        if (createdAfter != null) {
            queryWrapper.ge(Goods::getCreateTime, createdAfter);
        }
        queryWrapper.orderByDesc(Goods::getCreateTime)
                .orderByDesc(Goods::getBuyCount)
                .last("limit " + limit);
        return goodsService.list(queryWrapper);
    }

    private List<PlatformHomeStoreCardVO> resolveFrequentStores(PlatformHomeRecommendationConfigVO config, List<String> visibleStoreIds) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return Collections.emptyList();
        }
        int limit = normalizeLimit(config.getLimit(), DEFAULT_FREQUENT_STORE_LIMIT);
        AuthUser currentUser = UserContext.getCurrentUser();
        Map<String, String> sourceMap = new LinkedHashMap<>();
        Set<String> storeIds = new LinkedHashSet<>();

        if (currentUser != null && UserEnums.MEMBER.equals(currentUser.getRole())) {
            appendOrderedStores(storeIds, sourceMap, filterStoreIds(loadMemberOrderStoreIds(currentUser.getId()), visibleStoreIds), "ORDER", limit);
            appendOrderedStores(storeIds, sourceMap, filterStoreIds(loadMemberCollectionStoreIds(currentUser.getId()), visibleStoreIds), "COLLECTION", limit);
            appendOrderedStores(storeIds, sourceMap, filterStoreIds(loadMemberFootprintStoreIds(currentUser.getId()), visibleStoreIds), "FOOTPRINT", limit);
        }

        appendOrderedStores(storeIds, sourceMap, filterStoreIds(loadHotStoreIds(), visibleStoreIds), "HOT", limit);
        appendOrderedStores(storeIds, sourceMap, loadFallbackStoreIdsByGoods(limit * GOODS_QUERY_BUFFER, visibleStoreIds), "HOT", limit);
        appendOrderedStores(storeIds, sourceMap, loadOpenStoreIds(limit * GOODS_QUERY_BUFFER, visibleStoreIds), "HOT", limit);
        if (storeIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> candidateStoreIds = new ArrayList<>(storeIds);
        Map<String, Store> storeMap = storeService.listByIds(candidateStoreIds).stream()
                .filter(item -> StoreStatusEnum.OPEN.name().equals(item.getStoreDisable()))
                .collect(Collectors.toMap(Store::getId, Function.identity(), (left, right) -> left));

        List<PlatformHomeStoreCardVO> result = new ArrayList<>();
        for (String storeId : candidateStoreIds) {
            Store store = storeMap.get(storeId);
            if (store == null) {
                continue;
            }
            PlatformHomeStoreCardVO cardVO = new PlatformHomeStoreCardVO();
            cardVO.setStoreId(store.getId());
            cardVO.setStoreName(store.getStoreName());
            cardVO.setStoreLogo(store.getStoreLogo());
            cardVO.setStoreDesc(store.getStoreDesc());
            cardVO.setGoodsNum(store.getGoodsNum());
            cardVO.setCollectionNum(store.getCollectionNum());
            cardVO.setSource(sourceMap.get(storeId));
            result.add(cardVO);
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    private List<String> loadFallbackStoreIdsByGoods(int limit, List<String> visibleStoreIds) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return loadHotGoodsFallback(Math.min(limit, MAX_GOODS_QUERY_SIZE), visibleStoreIds).stream()
                .map(Goods::getStoreId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> loadOpenStoreIds(int limit, List<String> visibleStoreIds) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Store> queryWrapper = Wrappers.<Store>lambdaQuery()
                .eq(Store::getStoreDisable, StoreStatusEnum.OPEN.name())
                .eq(Store::getDeleteFlag, false)
                .orderByDesc(Store::getGoodsNum)
                .orderByDesc(Store::getCollectionNum)
                .orderByDesc(Store::getCreateTime)
                .last("limit " + Math.min(limit, MAX_GOODS_QUERY_SIZE));
        if (hasStoreScope(visibleStoreIds)) {
            queryWrapper.in(Store::getId, visibleStoreIds);
        }
        return storeService.list(queryWrapper).stream()
                .map(Store::getId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    private List<PlatformHomeGoodsCardVO> resolveGuessYouLike(PlatformHomeRecommendationConfigVO config,
                                                              Set<String> usedGoodsIds,
                                                              List<String> visibleStoreIds) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return Collections.emptyList();
        }
        int limit = normalizeLimit(config.getLimit(), 12);
        AuthUser currentUser = UserContext.getCurrentUser();
        MemberBehaviorContext behaviorContext = loadMemberBehaviorContext(currentUser);
        Date sevenDaysAgo = DateUtil.offsetDay(new Date(), -7);

        LinkedHashMap<String, Goods> candidateMap = new LinkedHashMap<>();
        if (!behaviorContext.getCategoryScoreMap().isEmpty()) {
            appendCandidateGoods(candidateMap, queryGoodsByCategoryIds(topCategoryIds(behaviorContext.getCategoryScoreMap(), 10), MAX_GOODS_QUERY_SIZE, visibleStoreIds));
        }
        if (!behaviorContext.getPreferredStoreIds().isEmpty()) {
            appendCandidateGoods(candidateMap, queryGoodsByStoreIds(behaviorContext.getPreferredStoreIds(), MAX_GOODS_QUERY_SIZE / 2, visibleStoreIds));
        }

        List<Goods> hotGoods = loadHotGoods(MAX_GOODS_QUERY_SIZE / 2, visibleStoreIds);
        List<Goods> newGoods = loadNewGoods(MAX_GOODS_QUERY_SIZE / 2, sevenDaysAgo, visibleStoreIds);
        appendCandidateGoods(candidateMap, hotGoods);
        appendCandidateGoods(candidateMap, newGoods);
        if (Boolean.TRUE.equals(config.getPreferRecommendFlag())) {
            appendCandidateGoods(candidateMap, queryRecommendGoods(MAX_GOODS_QUERY_SIZE / 2, visibleStoreIds));
        }

        Set<String> hotGoodsIds = hotGoods.stream().map(Goods::getId).collect(Collectors.toSet());
        boolean coldStart = !behaviorContext.hasBehavior();

        List<GoodsRecommendationCandidate> rankedCandidates = candidateMap.values().stream()
                .filter(this::isActiveGoods)
                .filter(goods -> matchesCategoryRange(goods.getCategoryPath(), config.getCategoryRange()))
                .map(goods -> {
                    int score = calculateCategoryScore(goods.getCategoryPath(), behaviorContext.getCategoryScoreMap());
                    if (behaviorContext.getPreferredStoreIds().contains(goods.getStoreId())) {
                        score += 4;
                    }
                    if (hotGoodsIds.contains(goods.getId())) {
                        score += 3;
                    }
                    if (Boolean.TRUE.equals(config.getPreferRecommendFlag()) && Boolean.TRUE.equals(goods.getRecommend())) {
                        score += 2;
                    }
                    if (goods.getCreateTime() != null && goods.getCreateTime().after(sevenDaysAgo)) {
                        score += 1;
                    }
                    return new GoodsRecommendationCandidate(goods, score);
                })
                .filter(candidate -> coldStart || candidate.getScore() > 0)
                .sorted(Comparator.comparingInt(GoodsRecommendationCandidate::getScore).reversed()
                        .thenComparing(candidate -> safeInteger(candidate.getGoods().getBuyCount()), Comparator.reverseOrder())
                        .thenComparing(candidate -> candidate.getGoods().getCreateTime(), Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        List<PlatformHomeGoodsCardVO> result = new ArrayList<>();
        for (GoodsRecommendationCandidate candidate : rankedCandidates) {
            if (usedGoodsIds.contains(candidate.getGoods().getId())) {
                continue;
            }
            result.add(buildGoodsCard(candidate.getGoods(), candidate.getGoods().getBuyCount()));
            usedGoodsIds.add(candidate.getGoods().getId());
            if (result.size() >= limit) {
                return result;
            }
        }

        if ("HOT_ONLY".equalsIgnoreCase(config.getColdStartStrategy())) {
            appendGoodsCards(result, hotGoods, usedGoodsIds, limit);
        } else {
            appendGoodsCards(result, hotGoods, usedGoodsIds, limit);
            appendGoodsCards(result, newGoods, usedGoodsIds, limit);
        }
        return result;
    }

    private List<PlatformHomeGoodsCardVO> resolveLowPriceZone(PlatformHomeRecommendationConfigVO config,
                                                              Set<String> usedGoodsIds,
                                                              List<String> visibleStoreIds) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return Collections.emptyList();
        }
        int limit = normalizeLimit(config.getLimit(), 12);
        double priceUpperBound = config.getPriceUpperBound() == null || config.getPriceUpperBound() <= 0
                ? 99D
                : config.getPriceUpperBound();

        List<LowPriceCandidate> candidates = queryLowPriceCandidates(
                config.getCategoryRange(),
                priceUpperBound,
                limit * GOODS_QUERY_BUFFER,
                visibleStoreIds
        );
        if (candidates.size() < limit && config.getCategoryRange() != null && !config.getCategoryRange().isEmpty()) {
            LinkedHashMap<String, LowPriceCandidate> merged = new LinkedHashMap<>();
            appendLowPriceCandidates(merged, candidates);
            appendLowPriceCandidates(
                    merged,
                    queryLowPriceCandidates(Collections.emptyList(), priceUpperBound, limit * GOODS_QUERY_BUFFER, visibleStoreIds)
            );
            candidates = new ArrayList<>(merged.values());
        }

        List<PlatformHomeGoodsCardVO> result = new ArrayList<>();
        appendLowPriceGoodsCards(result, candidates, usedGoodsIds, limit);
        return result;
    }

    private List<PlatformHomeGoodsCardVO> resolveTodaySpecial(PlatformHomeRecommendationConfigVO config,
                                                              Set<String> usedGoodsIds,
                                                              List<String> visibleStoreIds) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return Collections.emptyList();
        }
        List<String> promotionTypes = config.getPromotionTypes() == null || config.getPromotionTypes().isEmpty()
                ? defaultRecommendationConfig("TODAY_SPECIAL").getPromotionTypes()
                : config.getPromotionTypes();
        if (promotionTypes.isEmpty()) {
            return Collections.emptyList();
        }
        int limit = normalizeLimit(config.getLimit(), 12);
        int queryLimit = Math.min(limit * GOODS_QUERY_BUFFER, MAX_GOODS_QUERY_SIZE);

        QueryWrapper<PromotionGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_flag", false);
        applyPromotionStoreFilter(queryWrapper, visibleStoreIds);
        queryWrapper.in("promotion_type", promotionTypes);
        queryWrapper.gt("quantity", 0);
        queryWrapper.gt("original_price", 0);
        queryWrapper.isNotNull("goods_id");
        queryWrapper.isNotNull("price");
        queryWrapper.apply("price <= original_price");
        queryWrapper.and(PromotionTools.queryPromotionStatus(PromotionsStatusEnum.START));
        queryWrapper.last("order by (original_price - price) / original_price desc, end_time asc, num desc limit " + queryLimit);
        List<PromotionGoods> promotionGoodsList = promotionGoodsService.list(queryWrapper);
        if (promotionGoodsList == null || promotionGoodsList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Goods> goodsMap = goodsService.listByIds(
                        promotionGoodsList.stream()
                                .map(PromotionGoods::getGoodsId)
                                .filter(CharSequenceUtil::isNotBlank)
                                .distinct()
                                .collect(Collectors.toList()))
                .stream()
                .filter(this::isActiveGoods)
                .collect(Collectors.toMap(Goods::getId, Function.identity(), (left, right) -> left));

        Map<String, PromotionGoods> bestPromotionByGoodsId = new LinkedHashMap<>();
        for (PromotionGoods promotionGoods : promotionGoodsList) {
            if (promotionGoods == null
                    || CharSequenceUtil.isBlank(promotionGoods.getGoodsId())
                    || promotionGoods.getPrice() == null
                    || promotionGoods.getOriginalPrice() == null
                    || promotionGoods.getOriginalPrice() <= 0
                    || promotionGoods.getPrice() > promotionGoods.getOriginalPrice()
                    || promotionGoods.getQuantity() == null
                    || promotionGoods.getQuantity() <= 0) {
                continue;
            }
            Goods goods = goodsMap.get(promotionGoods.getGoodsId());
            if (goods == null || !matchesCategoryRange(goods.getCategoryPath(), config.getCategoryRange())) {
                continue;
            }
            PromotionGoods previous = bestPromotionByGoodsId.get(promotionGoods.getGoodsId());
            if (previous == null || comparePromotionGoods(promotionGoods, previous) < 0) {
                bestPromotionByGoodsId.put(promotionGoods.getGoodsId(), promotionGoods);
            }
        }

        List<PromotionGoods> rankedPromotions = new ArrayList<>(bestPromotionByGoodsId.values());
        rankedPromotions.sort((left, right) -> comparePromotionGoods(left, right));

        List<PlatformHomeGoodsCardVO> result = new ArrayList<>();
        for (PromotionGoods promotionGoods : rankedPromotions) {
            if (usedGoodsIds.contains(promotionGoods.getGoodsId())) {
                continue;
            }
            Goods goods = goodsMap.get(promotionGoods.getGoodsId());
            PlatformHomeGoodsCardVO cardVO = buildPromotionGoodsCard(promotionGoods, goods);
            result.add(cardVO);
            usedGoodsIds.add(promotionGoods.getGoodsId());
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    private MemberBehaviorContext loadMemberBehaviorContext(AuthUser currentUser) {
        MemberBehaviorContext context = new MemberBehaviorContext();
        if (currentUser == null || !UserEnums.MEMBER.equals(currentUser.getRole())) {
            return context;
        }
        String memberId = currentUser.getId();

        List<Order> recentOrders = orderService.list(Wrappers.<Order>lambdaQuery()
                .eq(Order::getMemberId, memberId)
                .in(Order::getOrderStatus,
                        OrderStatusEnum.PAID.name(),
                        OrderStatusEnum.UNDELIVERED.name(),
                        OrderStatusEnum.PARTS_DELIVERED.name(),
                        OrderStatusEnum.DELIVERED.name(),
                        OrderStatusEnum.COMPLETED.name(),
                        OrderStatusEnum.STAY_PICKED_UP.name(),
                        OrderStatusEnum.TAKE.name())
                .orderByDesc(Order::getCreateTime)
                .last("limit " + BEHAVIOR_ORDER_LIMIT));
        for (Order order : recentOrders) {
            List<OrderItem> orderItems = orderItemService.getByOrderSn(order.getSn());
            for (OrderItem orderItem : orderItems) {
                addCategoryScore(context.getCategoryScoreMap(), orderItem.getCategoryId(), 8);
            }
        }

        List<FootPrint> footprints = footprintService.list(Wrappers.<FootPrint>lambdaQuery()
                .eq(FootPrint::getMemberId, memberId)
                .isNotNull(FootPrint::getGoodsId)
                .orderByDesc(FootPrint::getCreateTime)
                .last("limit " + BEHAVIOR_FOOTPRINT_LIMIT));
        if (!footprints.isEmpty()) {
            Map<String, Goods> goodsMap = goodsService.listByIds(footprints.stream()
                            .map(FootPrint::getGoodsId)
                            .filter(CharSequenceUtil::isNotBlank)
                            .distinct()
                            .collect(Collectors.toList()))
                    .stream()
                    .collect(Collectors.toMap(Goods::getId, Function.identity(), (left, right) -> left));
            for (FootPrint footprint : footprints) {
                Goods goods = goodsMap.get(footprint.getGoodsId());
                if (goods != null) {
                    addCategoryScoreFromPath(context.getCategoryScoreMap(), goods.getCategoryPath(), 5);
                }
            }
        }

        List<GoodsCollection> collections = goodsCollectionService.list(Wrappers.<GoodsCollection>lambdaQuery()
                .eq(GoodsCollection::getMemberId, memberId)
                .orderByDesc(GoodsCollection::getCreateTime)
                .last("limit " + BEHAVIOR_COLLECTION_LIMIT));
        if (!collections.isEmpty()) {
            Map<String, GoodsSku> goodsSkuMap = goodsSkuService.listByIds(collections.stream()
                            .map(GoodsCollection::getSkuId)
                            .filter(CharSequenceUtil::isNotBlank)
                            .distinct()
                            .collect(Collectors.toList()))
                    .stream()
                    .collect(Collectors.toMap(GoodsSku::getId, Function.identity(), (left, right) -> left));
            for (GoodsCollection collection : collections) {
                GoodsSku goodsSku = goodsSkuMap.get(collection.getSkuId());
                if (goodsSku == null) {
                    continue;
                }
                addCategoryScoreFromPath(context.getCategoryScoreMap(), goodsSku.getCategoryPath(), 4);
                if (CharSequenceUtil.isNotBlank(goodsSku.getStoreId())) {
                    context.getPreferredStoreIds().add(goodsSku.getStoreId());
                }
            }
        }

        List<StoreCollection> storeCollections = storeCollectionService.list(Wrappers.<StoreCollection>lambdaQuery()
                .eq(StoreCollection::getMemberId, memberId)
                .orderByDesc(StoreCollection::getCreateTime)
                .last("limit " + BEHAVIOR_COLLECTION_LIMIT));
        storeCollections.stream()
                .map(StoreCollection::getStoreId)
                .filter(CharSequenceUtil::isNotBlank)
                .forEach(context.getPreferredStoreIds()::add);
        return context;
    }

    private List<String> topCategoryIds(Map<String, Integer> categoryScoreMap, int limit) {
        return categoryScoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<Goods> queryGoodsByCategoryIds(List<String> categoryIds, int limit, List<String> visibleStoreIds) {
        if (categoryIds == null || categoryIds.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Goods> queryWrapper = buildActiveGoodsQuery();
        applyGoodsStoreFilter(queryWrapper, visibleStoreIds);
        applyGoodsCategoryFilter(queryWrapper, categoryIds);
        queryWrapper.orderByDesc(Goods::getBuyCount)
                .orderByDesc(Goods::getCreateTime)
                .last("limit " + Math.min(limit, MAX_GOODS_QUERY_SIZE));
        return goodsService.list(queryWrapper).stream()
                .filter(goods -> matchesCategoryRange(goods.getCategoryPath(), categoryIds))
                .collect(Collectors.toList());
    }

    private List<Goods> queryGoodsByStoreIds(Set<String> storeIds, int limit, List<String> visibleStoreIds) {
        if (storeIds == null || storeIds.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }
        List<String> filteredStoreIds = filterStoreIds(new ArrayList<>(storeIds), visibleStoreIds);
        if (filteredStoreIds.isEmpty() && hasStoreScope(visibleStoreIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Goods> queryWrapper = buildActiveGoodsQuery();
        queryWrapper.in(Goods::getStoreId, filteredStoreIds.isEmpty() ? storeIds : filteredStoreIds)
                .orderByDesc(Goods::getBuyCount)
                .orderByDesc(Goods::getCreateTime)
                .last("limit " + Math.min(limit, MAX_GOODS_QUERY_SIZE));
        return goodsService.list(queryWrapper);
    }

    private List<Goods> queryRecommendGoods(int limit, List<String> visibleStoreIds) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Goods> queryWrapper = buildActiveGoodsQuery();
        applyGoodsStoreFilter(queryWrapper, visibleStoreIds);
        queryWrapper.eq(Goods::getRecommend, true)
                .orderByDesc(Goods::getBuyCount)
                .orderByDesc(Goods::getCreateTime)
                .last("limit " + Math.min(limit, MAX_GOODS_QUERY_SIZE));
        return goodsService.list(queryWrapper);
    }

    private List<LowPriceCandidate> queryLowPriceCandidates(List<String> categoryRange,
                                                            double priceUpperBound,
                                                            int limit,
                                                            List<String> visibleStoreIds) {
        LinkedHashMap<String, LowPriceCandidate> candidateMap = new LinkedHashMap<>();

        LambdaQueryWrapper<Goods> goodsQueryWrapper = buildActiveGoodsQuery();
        applyGoodsStoreFilter(goodsQueryWrapper, visibleStoreIds);
        goodsQueryWrapper.le(Goods::getPrice, priceUpperBound);
        applyGoodsCategoryFilter(goodsQueryWrapper, categoryRange);
        goodsQueryWrapper.orderByAsc(Goods::getPrice)
                .orderByDesc(Goods::getBuyCount)
                .orderByDesc(Goods::getCreateTime)
                .last("limit " + Math.min(limit, MAX_GOODS_QUERY_SIZE));
        goodsService.list(goodsQueryWrapper).stream()
                .filter(goods -> matchesCategoryRange(goods.getCategoryPath(), categoryRange))
                .forEach(goods -> candidateMap.put(goods.getId(), LowPriceCandidate.ofGoods(goods)));

        QueryWrapper<PromotionGoods> promotionQueryWrapper = new QueryWrapper<>();
        promotionQueryWrapper.eq("delete_flag", false)
                .gt("quantity", 0)
                .le("price", priceUpperBound)
                .and(PromotionTools.queryPromotionStatus(PromotionsStatusEnum.START))
                .orderByAsc("price")
                .orderByDesc("num")
                .orderByDesc("end_time")
                .last("limit " + Math.min(limit, MAX_GOODS_QUERY_SIZE));
        applyPromotionStoreFilter(promotionQueryWrapper, visibleStoreIds);
        applyCategoryPathFilter(promotionQueryWrapper, "category_path", categoryRange);

        List<PromotionGoods> promotionGoodsList = promotionGoodsService.list(promotionQueryWrapper);
        if (promotionGoodsList.isEmpty()) {
            return sortLowPriceCandidates(candidateMap.values());
        }

        Map<String, Goods> goodsMap = goodsService.listByIds(promotionGoodsList.stream()
                        .map(PromotionGoods::getGoodsId)
                        .filter(CharSequenceUtil::isNotBlank)
                        .distinct()
                        .collect(Collectors.toList()))
                .stream()
                .filter(this::isActiveGoods)
                .collect(Collectors.toMap(Goods::getId, Function.identity(), (left, right) -> left));
        for (PromotionGoods promotionGoods : promotionGoodsList) {
            if (promotionGoods == null
                    || CharSequenceUtil.isBlank(promotionGoods.getGoodsId())
                    || promotionGoods.getPrice() == null) {
                continue;
            }
            Goods goods = goodsMap.get(promotionGoods.getGoodsId());
            if (goods == null || !matchesCategoryRange(goods.getCategoryPath(), categoryRange)) {
                continue;
            }
            LowPriceCandidate current = candidateMap.get(goods.getId());
            if (current == null) {
                candidateMap.put(goods.getId(), LowPriceCandidate.ofPromotion(goods, promotionGoods));
                continue;
            }
            if (current.getDisplayPrice() == null || promotionGoods.getPrice() < current.getDisplayPrice()) {
                current.setDisplayPrice(promotionGoods.getPrice());
                current.setOriginalPrice(resolvePromotionOriginalPrice(promotionGoods, goods));
            }
        }
        return sortLowPriceCandidates(candidateMap.values());
    }

    private LambdaQueryWrapper<Goods> buildActiveGoodsQuery() {
        return Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getAuthFlag, GoodsAuthEnum.PASS.name())
                .eq(Goods::getMarketEnable, GoodsStatusEnum.UPPER.name())
                .gt(Goods::getQuantity, 0)
                .eq(Goods::getDeleteFlag, false);
    }

    private void applyGoodsStoreFilter(LambdaQueryWrapper<Goods> queryWrapper, List<String> visibleStoreIds) {
        if (queryWrapper != null && hasStoreScope(visibleStoreIds)) {
            queryWrapper.in(Goods::getStoreId, visibleStoreIds);
        }
    }

    private void applyPromotionStoreFilter(QueryWrapper<PromotionGoods> queryWrapper, List<String> visibleStoreIds) {
        if (queryWrapper != null && hasStoreScope(visibleStoreIds)) {
            queryWrapper.in("store_id", visibleStoreIds);
        }
    }

    private boolean isVisibleStore(String storeId, List<String> visibleStoreIds) {
        return !hasStoreScope(visibleStoreIds)
                || (CharSequenceUtil.isNotBlank(storeId) && visibleStoreIds.contains(storeId));
    }

    private boolean hasStoreScope(List<String> visibleStoreIds) {
        return visibleStoreIds != null && !visibleStoreIds.isEmpty();
    }

    private List<String> filterStoreIds(List<String> storeIds, List<String> visibleStoreIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return storeIds.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .filter(storeId -> isVisibleStore(storeId, visibleStoreIds))
                .distinct()
                .collect(Collectors.toList());
    }

    private void appendCandidateGoods(Map<String, Goods> bucket, List<Goods> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        for (Goods goods : goodsList) {
            if (goods != null && CharSequenceUtil.isNotBlank(goods.getId())) {
                bucket.putIfAbsent(goods.getId(), goods);
            }
        }
    }

    private void appendGoodsCards(List<PlatformHomeGoodsCardVO> result,
                                  List<Goods> goodsList,
                                  Set<String> usedGoodsIds,
                                  int limit) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        for (Goods goods : goodsList) {
            if (!isActiveGoods(goods) || usedGoodsIds.contains(goods.getId())) {
                continue;
            }
            result.add(buildGoodsCard(goods, goods.getBuyCount()));
            usedGoodsIds.add(goods.getId());
            if (result.size() >= limit) {
                return;
            }
        }
    }

    private void appendLowPriceCandidates(Map<String, LowPriceCandidate> bucket,
                                          List<LowPriceCandidate> candidateList) {
        if (candidateList == null || candidateList.isEmpty()) {
            return;
        }
        for (LowPriceCandidate candidate : candidateList) {
            if (candidate == null || candidate.getGoods() == null || CharSequenceUtil.isBlank(candidate.getGoods().getId())) {
                continue;
            }
            bucket.putIfAbsent(candidate.getGoods().getId(), candidate);
        }
    }

    private void appendLowPriceGoodsCards(List<PlatformHomeGoodsCardVO> result,
                                          List<LowPriceCandidate> candidateList,
                                          Set<String> usedGoodsIds,
                                          int limit) {
        if (candidateList == null || candidateList.isEmpty()) {
            return;
        }
        for (LowPriceCandidate candidate : candidateList) {
            Goods goods = candidate == null ? null : candidate.getGoods();
            if (!isActiveGoods(goods) || usedGoodsIds.contains(goods.getId())) {
                continue;
            }
            result.add(buildGoodsCard(
                    goods,
                    goods.getBuyCount(),
                    candidate.getDisplayPrice(),
                    candidate.getOriginalPrice()
            ));
            usedGoodsIds.add(goods.getId());
            if (result.size() >= limit) {
                return;
            }
        }
    }

    private int calculateCategoryScore(String categoryPath, Map<String, Integer> categoryScoreMap) {
        if (CharSequenceUtil.isBlank(categoryPath) || categoryScoreMap == null || categoryScoreMap.isEmpty()) {
            return 0;
        }
        int score = 0;
        for (String categoryId : categoryPath.split(",")) {
            score += categoryScoreMap.getOrDefault(categoryId, 0);
        }
        return score;
    }

    private void addCategoryScore(Map<String, Integer> categoryScoreMap, String categoryId, int weight) {
        if (categoryScoreMap == null || CharSequenceUtil.isBlank(categoryId)) {
            return;
        }
        categoryScoreMap.merge(categoryId.trim(), weight, Integer::sum);
    }

    private void addCategoryScoreFromPath(Map<String, Integer> categoryScoreMap, String categoryPath, int weight) {
        if (CharSequenceUtil.isBlank(categoryPath)) {
            return;
        }
        for (String categoryId : categoryPath.split(",")) {
            addCategoryScore(categoryScoreMap, categoryId, weight);
        }
    }

    private boolean matchesCategoryRange(String categoryPath, List<String> categoryRange) {
        if (categoryRange == null || categoryRange.isEmpty()) {
            return true;
        }
        if (CharSequenceUtil.isBlank(categoryPath)) {
            return false;
        }
        for (String categoryId : categoryRange) {
            if (categoryPathContains(categoryPath, categoryId)) {
                return true;
            }
        }
        return false;
    }

    private boolean categoryPathContains(String categoryPath, String categoryId) {
        if (CharSequenceUtil.isBlank(categoryPath) || CharSequenceUtil.isBlank(categoryId)) {
            return false;
        }
        String target = categoryId.trim();
        for (String item : categoryPath.split(",")) {
            if (target.equals(item.trim())) {
                return true;
            }
        }
        return false;
    }

    private void applyGoodsCategoryFilter(LambdaQueryWrapper<Goods> queryWrapper, List<String> categoryIds) {
        List<String> normalizedCategoryIds = normalizeCategoryIds(categoryIds);
        if (normalizedCategoryIds.isEmpty()) {
            return;
        }
        queryWrapper.and(wrapper -> {
            boolean first = true;
            for (String categoryId : normalizedCategoryIds) {
                if (first) {
                    wrapper.apply("concat(',', category_path, ',') like {0}", "%," + categoryId + ",%");
                    first = false;
                } else {
                    wrapper.or().apply("concat(',', category_path, ',') like {0}", "%," + categoryId + ",%");
                }
            }
        });
    }

    private void applyCategoryPathFilter(QueryWrapper<PromotionGoods> queryWrapper,
                                         String columnName,
                                         List<String> categoryIds) {
        List<String> normalizedCategoryIds = normalizeCategoryIds(categoryIds);
        if (normalizedCategoryIds.isEmpty()) {
            return;
        }
        queryWrapper.and(wrapper -> {
            boolean first = true;
            for (String categoryId : normalizedCategoryIds) {
                if (first) {
                    wrapper.apply("concat(',', " + columnName + ", ',') like {0}", "%," + categoryId + ",%");
                    first = false;
                } else {
                    wrapper.or().apply("concat(',', " + columnName + ", ',') like {0}", "%," + categoryId + ",%");
                }
            }
        });
    }

    private List<String> normalizeCategoryIds(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        return categoryIds.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isActiveGoods(Goods goods) {
        return goods != null
                && !Boolean.TRUE.equals(goods.getDeleteFlag())
                && GoodsAuthEnum.PASS.name().equals(goods.getAuthFlag())
                && GoodsStatusEnum.UPPER.name().equals(goods.getMarketEnable())
                && safeInteger(goods.getQuantity()) > 0;
    }

    private int comparePromotionGoods(PromotionGoods left, PromotionGoods right) {
        int rateCompare = Double.compare(discountRate(right), discountRate(left));
        if (rateCompare != 0) {
            return rateCompare;
        }
        int endTimeCompare = Comparator.nullsLast(Comparator.<Date>naturalOrder())
                .compare(left.getEndTime(), right.getEndTime());
        if (endTimeCompare != 0) {
            return endTimeCompare;
        }
        return Integer.compare(safeInteger(right.getNum()), safeInteger(left.getNum()));
    }

    private double discountRate(PromotionGoods promotionGoods) {
        if (promotionGoods == null
                || promotionGoods.getOriginalPrice() == null
                || promotionGoods.getOriginalPrice() <= 0
                || promotionGoods.getPrice() == null) {
            return 0D;
        }
        return (promotionGoods.getOriginalPrice() - promotionGoods.getPrice()) / promotionGoods.getOriginalPrice();
    }

    private List<Goods> reorderGoodsByIds(List<String> goodsIds) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Goods> goodsMap = goodsService.listByIds(goodsIds).stream()
                .filter(this::isActiveGoods)
                .collect(Collectors.toMap(Goods::getId, Function.identity(), (left, right) -> left));
        List<Goods> result = new ArrayList<>();
        for (String goodsId : goodsIds) {
            Goods goods = goodsMap.get(goodsId);
            if (goods != null) {
                result.add(goods);
            }
        }
        return result;
    }

    private int normalizeLimit(Integer configuredLimit, int defaultLimit) {
        if (configuredLimit == null || configuredLimit <= 0) {
            return defaultLimit;
        }
        return Math.min(configuredLimit, 50);
    }

    private List<LowPriceCandidate> sortLowPriceCandidates(java.util.Collection<LowPriceCandidate> candidates) {
        return candidates.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                                LowPriceCandidate::getDisplayPrice,
                                Comparator.nullsLast(Double::compareTo))
                        .thenComparing(
                                candidate -> safeInteger(candidate.getGoods().getBuyCount()),
                                Comparator.reverseOrder())
                        .thenComparing(
                                candidate -> candidate.getGoods().getCreateTime(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private List<PlatformHomeRecommendationModuleVO> buildRecommendationModules(
            RecommendationConfigBundle configBundle,
            List<PlatformHomeStoreCardVO> frequentStores,
            List<PlatformHomeGoodsCardVO> guessYouLikeGoods,
            List<PlatformHomeGoodsCardVO> lowPriceZoneGoods,
            List<PlatformHomeGoodsCardVO> todaySpecialGoods) {
        List<PlatformHomeRecommendationModuleVO> modules = new ArrayList<>();
        appendRecommendationStoreModule(
                modules,
                "FREQUENT_STORES",
                configBundle.getFrequentStoresConfig(),
                frequentStores
        );
        appendRecommendationGoodsModule(
                modules,
                "GUESS_YOU_LIKE",
                configBundle.getGuessYouLikeConfig(),
                guessYouLikeGoods
        );
        appendRecommendationGoodsModule(
                modules,
                "LOW_PRICE",
                configBundle.getLowPriceZoneConfig(),
                lowPriceZoneGoods
        );
        appendRecommendationGoodsModule(
                modules,
                "TODAY_SPECIAL",
                configBundle.getTodaySpecialConfig(),
                todaySpecialGoods
        );
        modules.sort(Comparator.comparing(
                        PlatformHomeRecommendationModuleVO::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparingInt(item -> recommendationModuleRank(item.getCode())));
        return modules;
    }

    private void appendRecommendationStoreModule(List<PlatformHomeRecommendationModuleVO> modules,
                                                 String code,
                                                 PlatformHomeRecommendationConfigVO config,
                                                 List<PlatformHomeStoreCardVO> storeList) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled()) || storeList == null || storeList.isEmpty()) {
            return;
        }
        PlatformHomeRecommendationModuleVO moduleVO = new PlatformHomeRecommendationModuleVO();
        moduleVO.setCode(code);
        moduleVO.setTitle(config.getTitle());
        moduleVO.setSortOrder(config.getSortOrder());
        moduleVO.setModuleType(RECOMMENDATION_MODULE_TYPE_STORE);
        moduleVO.setStoreList(storeList);
        moduleVO.setGoodsList(Collections.emptyList());
        modules.add(moduleVO);
    }

    private void appendRecommendationGoodsModule(List<PlatformHomeRecommendationModuleVO> modules,
                                                 String code,
                                                 PlatformHomeRecommendationConfigVO config,
                                                 List<PlatformHomeGoodsCardVO> goodsList) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return;
        }
        PlatformHomeRecommendationModuleVO moduleVO = new PlatformHomeRecommendationModuleVO();
        moduleVO.setCode(code);
        moduleVO.setTitle(config.getTitle());
        moduleVO.setSortOrder(config.getSortOrder());
        moduleVO.setModuleType(RECOMMENDATION_MODULE_TYPE_GOODS);
        moduleVO.setStoreList(Collections.emptyList());
        moduleVO.setGoodsList(goodsList == null ? Collections.emptyList() : goodsList);
        modules.add(moduleVO);
    }

    private int recommendationModuleRank(String code) {
        if ("FREQUENT_STORES".equals(code)) {
            return 1;
        }
        if ("GUESS_YOU_LIKE".equals(code)) {
            return 2;
        }
        if ("LOW_PRICE".equals(code)) {
            return 3;
        }
        if ("TODAY_SPECIAL".equals(code)) {
            return 4;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * 常用店铺规则：
     * 1. 近 100 笔有效订单按出现次数和最近下单时间排序。
     * 2. 不足时用关注店铺补位。
     * 3. 仍不足时用浏览足迹补位。
     * 4. 最终不足时再退化到平台热卖店铺。
     */
    private List<String> loadMemberOrderStoreIds(String memberId) {
        List<Order> recentOrders = orderService.list(Wrappers.<Order>lambdaQuery()
                .eq(Order::getMemberId, memberId)
                .in(Order::getOrderStatus,
                        OrderStatusEnum.PAID.name(),
                        OrderStatusEnum.UNDELIVERED.name(),
                        OrderStatusEnum.PARTS_DELIVERED.name(),
                        OrderStatusEnum.DELIVERED.name(),
                        OrderStatusEnum.COMPLETED.name(),
                        OrderStatusEnum.STAY_PICKED_UP.name(),
                        OrderStatusEnum.TAKE.name())
                .orderByDesc(Order::getCreateTime)
                .last("limit 100"));
        Map<String, StoreScore> scoreMap = new HashMap<>();
        for (Order order : recentOrders) {
            if (CharSequenceUtil.isBlank(order.getStoreId())) {
                continue;
            }
            StoreScore score = scoreMap.computeIfAbsent(order.getStoreId(), key -> new StoreScore());
            score.storeId = order.getStoreId();
            score.orderCount++;
            if (order.getCreateTime() != null && (score.latestOrderTime == null || order.getCreateTime().after(score.latestOrderTime))) {
                score.latestOrderTime = order.getCreateTime();
            }
        }
        return scoreMap.values().stream()
                .sorted(Comparator.comparingInt(StoreScore::getOrderCount).reversed()
                        .thenComparing(StoreScore::getLatestOrderTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(StoreScore::getStoreId)
                .collect(Collectors.toList());
    }

    private List<String> loadMemberCollectionStoreIds(String memberId) {
        return storeCollectionService.list(Wrappers.<StoreCollection>lambdaQuery()
                        .eq(StoreCollection::getMemberId, memberId)
                        .orderByDesc(StoreCollection::getCreateTime)
                        .last("limit 20"))
                .stream()
                .map(StoreCollection::getStoreId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    private List<String> loadMemberFootprintStoreIds(String memberId) {
        return footprintService.list(Wrappers.<FootPrint>lambdaQuery()
                        .eq(FootPrint::getMemberId, memberId)
                        .isNotNull(FootPrint::getStoreId)
                        .orderByDesc(FootPrint::getCreateTime)
                        .last("limit 50"))
                .stream()
                .map(FootPrint::getStoreId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    private List<String> loadHotStoreIds() {
        StatisticsQueryParam queryParam = new StatisticsQueryParam();
        DateTime dateTime = new DateTime();
        queryParam.setYear(dateTime.year());
        queryParam.setMonth(dateTime.monthBaseOne());
        List<StoreStatisticsDataVO> hotStores = indexStatisticsService.storeStatistics(queryParam);
        if (hotStores == null) {
            return Collections.emptyList();
        }
        return hotStores.stream()
                .map(StoreStatisticsDataVO::getStoreId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    private void appendOrderedStores(Set<String> storeIds,
                                     Map<String, String> sourceMap,
                                     List<String> candidates,
                                     String source,
                                     int limit) {
        if (candidates == null || candidates.isEmpty() || storeIds.size() >= limit) {
            return;
        }
        for (String storeId : candidates) {
            if (CharSequenceUtil.isBlank(storeId)) {
                continue;
            }
            if (storeIds.add(storeId)) {
                sourceMap.put(storeId, source);
            }
            if (storeIds.size() >= limit) {
                return;
            }
        }
    }

    private PlatformHomeGoodsCardVO buildSeckillGoodsCard(SeckillGoodsVO item) {
        PlatformHomeGoodsCardVO cardVO = new PlatformHomeGoodsCardVO();
        cardVO.setGoodsId(item.getGoodsId());
        cardVO.setSkuId(item.getSkuId());
        cardVO.setGoodsName(item.getGoodsName());
        cardVO.setGoodsImage(item.getGoodsImage());
        cardVO.setPrice(item.getPrice());
        cardVO.setOriginalPrice(item.getOriginalPrice());
        cardVO.setStoreId(item.getStoreId());
        cardVO.setSalesNum(item.getSalesNum());
        return cardVO;
    }

    private PlatformHomeGoodsCardVO buildGoodsCard(Goods goods, Integer salesNum) {
        return buildGoodsCard(goods, salesNum, goods.getPrice(), goods.getPrice());
    }

    private PlatformHomeGoodsCardVO buildGoodsCard(Goods goods,
                                                   Integer salesNum,
                                                   Double displayPrice,
                                                   Double originalPrice) {
        PlatformHomeGoodsCardVO cardVO = new PlatformHomeGoodsCardVO();
        cardVO.setGoodsId(goods.getId());
        cardVO.setGoodsName(goods.getGoodsName());
        cardVO.setGoodsImage(firstNotBlank(goods.getThumbnail(), goods.getSmall(), goods.getOriginal()));
        cardVO.setPrice(displayPrice == null ? goods.getPrice() : displayPrice);
        cardVO.setOriginalPrice(originalPrice == null ? goods.getPrice() : originalPrice);
        cardVO.setStoreId(goods.getStoreId());
        cardVO.setStoreName(goods.getStoreName());
        cardVO.setSalesNum(salesNum == null ? 0 : salesNum);
        return cardVO;
    }

    private PlatformHomeGoodsCardVO buildPromotionGoodsCard(PromotionGoods promotionGoods, Goods goods) {
        PlatformHomeGoodsCardVO cardVO = new PlatformHomeGoodsCardVO();
        cardVO.setGoodsId(promotionGoods.getGoodsId());
        cardVO.setSkuId(promotionGoods.getSkuId());
        cardVO.setGoodsName(firstNotBlank(promotionGoods.getGoodsName(), goods == null ? null : goods.getGoodsName()));
        cardVO.setGoodsImage(firstNotBlank(promotionGoods.getThumbnail(), goods == null ? null : goods.getThumbnail()));
        cardVO.setPrice(promotionGoods.getPrice());
        cardVO.setOriginalPrice(promotionGoods.getOriginalPrice());
        cardVO.setStoreId(firstNotBlank(promotionGoods.getStoreId(), goods == null ? null : goods.getStoreId()));
        cardVO.setStoreName(firstNotBlank(promotionGoods.getStoreName(), goods == null ? null : goods.getStoreName()));
        cardVO.setSalesNum(safeInteger(promotionGoods.getNum()));
        return cardVO;
    }

    @SafeVarargs
    private final void normalizeGoodsCardStoreNames(List<PlatformHomeGoodsCardVO>... goodsGroups) {
        if (goodsGroups == null || goodsGroups.length == 0) {
            return;
        }
        Set<String> storeIds = new LinkedHashSet<>();
        for (List<PlatformHomeGoodsCardVO> goodsGroup : goodsGroups) {
            if (goodsGroup == null || goodsGroup.isEmpty()) {
                continue;
            }
            goodsGroup.stream()
                    .map(PlatformHomeGoodsCardVO::getStoreId)
                    .filter(CharSequenceUtil::isNotBlank)
                    .forEach(storeIds::add);
        }
        if (storeIds.isEmpty()) {
            return;
        }
        Map<String, Store> storeMap = storeService.listByIds(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, Function.identity(), (left, right) -> left));
        for (List<PlatformHomeGoodsCardVO> goodsGroup : goodsGroups) {
            if (goodsGroup == null || goodsGroup.isEmpty()) {
                continue;
            }
            for (PlatformHomeGoodsCardVO goodsCard : goodsGroup) {
                if (goodsCard == null || CharSequenceUtil.isBlank(goodsCard.getStoreId())) {
                    continue;
                }
                Store store = storeMap.get(goodsCard.getStoreId());
                if (store != null && CharSequenceUtil.isNotBlank(store.getStoreName())) {
                    goodsCard.setStoreName(store.getStoreName());
                }
            }
        }
    }

    private Integer safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private Double resolvePromotionOriginalPrice(PromotionGoods promotionGoods, Goods goods) {
        if (promotionGoods != null
                && promotionGoods.getOriginalPrice() != null
                && promotionGoods.getOriginalPrice() > 0) {
            return promotionGoods.getOriginalPrice();
        }
        return goods == null ? null : goods.getPrice();
    }

    private Integer parseInteger(String value) {
        if (CharSequenceUtil.isBlank(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (CharSequenceUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static class StoreScore {
        private String storeId;
        private int orderCount;
        private Date latestOrderTime;

        public String getStoreId() {
            return storeId;
        }

        public int getOrderCount() {
            return orderCount;
        }

        public Date getLatestOrderTime() {
            return latestOrderTime;
        }
    }

    private static class RecommendationConfigBundle {
        private PlatformHomeRecommendationConfigVO frequentStoresConfig =
                buildDefaultBundleConfig("FREQUENT_STORES");
        private PlatformHomeRecommendationConfigVO guessYouLikeConfig =
                buildDefaultBundleConfig("GUESS_YOU_LIKE");
        private PlatformHomeRecommendationConfigVO lowPriceZoneConfig =
                buildDefaultBundleConfig("LOW_PRICE");
        private PlatformHomeRecommendationConfigVO todaySpecialConfig =
                buildDefaultBundleConfig("TODAY_SPECIAL");

        public PlatformHomeRecommendationConfigVO getFrequentStoresConfig() {
            return frequentStoresConfig;
        }

        public void setFrequentStoresConfig(PlatformHomeRecommendationConfigVO frequentStoresConfig) {
            this.frequentStoresConfig = frequentStoresConfig;
        }

        public PlatformHomeRecommendationConfigVO getGuessYouLikeConfig() {
            return guessYouLikeConfig;
        }

        public void setGuessYouLikeConfig(PlatformHomeRecommendationConfigVO guessYouLikeConfig) {
            this.guessYouLikeConfig = guessYouLikeConfig;
        }

        public PlatformHomeRecommendationConfigVO getLowPriceZoneConfig() {
            return lowPriceZoneConfig;
        }

        public void setLowPriceZoneConfig(PlatformHomeRecommendationConfigVO lowPriceZoneConfig) {
            this.lowPriceZoneConfig = lowPriceZoneConfig;
        }

        public PlatformHomeRecommendationConfigVO getTodaySpecialConfig() {
            return todaySpecialConfig;
        }

        public void setTodaySpecialConfig(PlatformHomeRecommendationConfigVO todaySpecialConfig) {
            this.todaySpecialConfig = todaySpecialConfig;
        }

        private static PlatformHomeRecommendationConfigVO buildDefaultBundleConfig(String code) {
            PlatformHomeRecommendationConfigVO config = new PlatformHomeRecommendationConfigVO();
            config.setEnabled(Boolean.TRUE);
            config.setSortOrder(0);
            config.setCategoryRange(Collections.emptyList());
            config.setPromotionTypes(Collections.emptyList());
            switch (code) {
                case "FREQUENT_STORES":
                    config.setTitle("常买店铺");
                    config.setLimit(DEFAULT_FREQUENT_STORE_LIMIT);
                    break;
                case "GUESS_YOU_LIKE":
                    config.setTitle("猜你喜欢");
                    config.setLimit(12);
                    config.setPreferRecommendFlag(Boolean.TRUE);
                    config.setColdStartStrategy("HOT_AND_NEW");
                    break;
                case "LOW_PRICE":
                    config.setTitle("低价专区");
                    config.setLimit(12);
                    config.setPriceUpperBound(99D);
                    break;
                case "TODAY_SPECIAL":
                    config.setTitle("今日特惠");
                    config.setLimit(12);
                    config.setPromotionTypes(new ArrayList<>(List.of(
                            PromotionTypeEnum.SECKILL.name(),
                            PromotionTypeEnum.PINTUAN.name(),
                            PromotionTypeEnum.KANJIA.name()
                    )));
                    break;
                default:
                    break;
            }
            return config;
        }
    }

    private static class MemberBehaviorContext {
        private final Map<String, Integer> categoryScoreMap = new HashMap<>();
        private final Set<String> preferredStoreIds = new LinkedHashSet<>();

        public Map<String, Integer> getCategoryScoreMap() {
            return categoryScoreMap;
        }

        public Set<String> getPreferredStoreIds() {
            return preferredStoreIds;
        }

        public boolean hasBehavior() {
            return !categoryScoreMap.isEmpty() || !preferredStoreIds.isEmpty();
        }
    }

    private static class GoodsRecommendationCandidate {
        private final Goods goods;
        private final int score;

        private GoodsRecommendationCandidate(Goods goods, int score) {
            this.goods = goods;
            this.score = score;
        }

        public Goods getGoods() {
            return goods;
        }

        public int getScore() {
            return score;
        }
    }

    private static class LowPriceCandidate {
        private final Goods goods;
        private Double displayPrice;
        private Double originalPrice;

        private LowPriceCandidate(Goods goods, Double displayPrice, Double originalPrice) {
            this.goods = goods;
            this.displayPrice = displayPrice;
            this.originalPrice = originalPrice;
        }

        public static LowPriceCandidate ofGoods(Goods goods) {
            return new LowPriceCandidate(goods, goods.getPrice(), goods.getPrice());
        }

        public static LowPriceCandidate ofPromotion(Goods goods, PromotionGoods promotionGoods) {
            Double promotionPrice = promotionGoods == null ? null : promotionGoods.getPrice();
            Double originPrice = promotionGoods == null ? null : promotionGoods.getOriginalPrice();
            return new LowPriceCandidate(
                    goods,
                    promotionPrice == null ? goods.getPrice() : promotionPrice,
                    originPrice == null ? goods.getPrice() : originPrice
            );
        }

        public Goods getGoods() {
            return goods;
        }

        public Double getDisplayPrice() {
            return displayPrice;
        }

        public void setDisplayPrice(Double displayPrice) {
            this.displayPrice = displayPrice;
        }

        public Double getOriginalPrice() {
            return originalPrice;
        }

        public void setOriginalPrice(Double originalPrice) {
            this.originalPrice = originalPrice;
        }
    }
}
