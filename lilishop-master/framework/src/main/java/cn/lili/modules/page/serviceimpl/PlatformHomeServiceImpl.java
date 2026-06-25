package cn.lili.modules.page.serviceimpl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import cn.lili.common.enums.ClientTypeEnum;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dto.GoodsSearchParams;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.member.entity.dos.FootPrint;
import cn.lili.modules.member.entity.dos.StoreCollection;
import cn.lili.modules.member.service.FootprintService;
import cn.lili.modules.member.service.StoreCollectionService;
import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.service.OrderService;
import cn.lili.modules.page.entity.dto.PageDataDTO;
import cn.lili.modules.page.entity.enums.PageEnum;
import cn.lili.modules.page.entity.vos.PageDataVO;
import cn.lili.modules.page.entity.vos.PlatformHomeBannerVO;
import cn.lili.modules.page.entity.vos.PlatformHomeGoodsCardVO;
import cn.lili.modules.page.entity.vos.PlatformHomeFloorModuleVO;
import cn.lili.modules.page.entity.vos.PlatformHomeSeckillVO;
import cn.lili.modules.page.entity.vos.PlatformHomeShortcutNavVO;
import cn.lili.modules.page.entity.vos.PlatformHomeStoreCardVO;
import cn.lili.modules.page.entity.vos.PlatformHomeVO;
import cn.lili.modules.page.service.OperationShortcutNavService;
import cn.lili.modules.page.service.PageDataService;
import cn.lili.modules.page.service.PlatformHomeService;
import cn.lili.modules.promotion.entity.vos.SeckillGoodsVO;
import cn.lili.modules.promotion.entity.vos.SeckillTimelineVO;
import cn.lili.modules.promotion.service.SeckillApplyService;
import cn.lili.modules.statistics.entity.dto.GoodsStatisticsQueryParam;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.vo.GoodsStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.StoreStatisticsDataVO;
import cn.lili.modules.statistics.service.IndexStatisticsService;
import cn.lili.modules.store.entity.dos.Store;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import cn.lili.modules.store.service.StoreService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.BeanUtils;
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
import java.util.stream.Collectors;

/**
 * 平台首页聚合服务实现
 *
 * @author codex
 * @since 2026/6/25
 */
@Service
public class PlatformHomeServiceImpl implements PlatformHomeService {

    private static final int HOT_GOODS_LIMIT = 6;
    private static final int NEW_GOODS_LIMIT = 6;
    private static final int SECKILL_GOODS_LIMIT = 6;
    private static final int FREQUENT_STORE_LIMIT = 4;

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
    private StoreService storeService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private StoreCollectionService storeCollectionService;
    @Autowired
    private FootprintService footprintService;

    @Override
    public PlatformHomeVO getPlatformHome(String clientType) {
        String effectiveClientType = normalizeClientType(clientType);
        PlatformHomeVO vo = new PlatformHomeVO();
        vo.setClientType(effectiveClientType);
        vo.setBanners(resolveBanners(effectiveClientType));
        vo.setShortcutNavList(resolveShortcutNav(effectiveClientType));
        vo.setFloorModules(resolveFloorModules(effectiveClientType));
        vo.setSeckill(resolveSeckill());
        vo.setMonthlyHotGoods(resolveMonthlyHotGoods());
        vo.setNewGoods(resolveNewGoods());
        vo.setFrequentStores(resolveFrequentStores());
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

    private List<PlatformHomeBannerVO> resolveBanners(String clientType) {
        PageDataVO pageDataVO = getIndexPageData(clientType);
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
                if ("carousel".equals(type)) {
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
                        if (CharSequenceUtil.isNotBlank(banner.getImage())) {
                            banners.add(banner);
                        }
                    }
                } else if ("topAdvert".equals(type)) {
                    PlatformHomeBannerVO banner = new PlatformHomeBannerVO();
                    banner.setImage(section.getString("img"));
                    banner.setUrl(section.getString("url"));
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

    private List<PlatformHomeFloorModuleVO> resolveFloorModules(String clientType) {
        PageDataVO pageDataVO = getIndexPageData(clientType);
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
                if (section == null || !"wholesaleFloor".equals(section.getString("type"))) {
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

    private PlatformHomeSeckillVO resolveSeckill() {
        List<SeckillTimelineVO> timeLineList = seckillApplyService.getSeckillTimeline();
        if (timeLineList == null || timeLineList.isEmpty()) {
            return null;
        }
        SeckillTimelineVO active = timeLineList.stream()
                .min(Comparator.comparing(item -> item.getDistanceStartTime() == null ? Long.MAX_VALUE : item.getDistanceStartTime()))
                .orElse(timeLineList.get(0));
        PlatformHomeSeckillVO seckillVO = new PlatformHomeSeckillVO();
        seckillVO.setTimeLine(active.getTimeLine());
        seckillVO.setStartTime(active.getStartTime());
        seckillVO.setDistanceStartTime(active.getDistanceStartTime());
        List<SeckillGoodsVO> goodsList = active.getSeckillGoodsList() == null
                ? Collections.emptyList()
                : active.getSeckillGoodsList().stream().limit(SECKILL_GOODS_LIMIT).collect(Collectors.toList());
        seckillVO.setGoodsList(goodsList.stream().map(this::buildSeckillGoodsCard).collect(Collectors.toList()));
        return seckillVO;
    }

    private List<PlatformHomeGoodsCardVO> resolveMonthlyHotGoods() {
        GoodsStatisticsQueryParam queryParam = new GoodsStatisticsQueryParam();
        DateTime dateTime = new DateTime();
        queryParam.setYear(dateTime.year());
        queryParam.setMonth(dateTime.monthBaseOne());
        List<GoodsStatisticsDataVO> statistics = indexStatisticsService.goodsStatistics(queryParam);
        if (statistics == null || statistics.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> goodsIds = statistics.stream()
                .map(GoodsStatisticsDataVO::getGoodsId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
        Map<String, Goods> goodsMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(Goods::getId, item -> item, (left, right) -> left));
        List<PlatformHomeGoodsCardVO> result = new ArrayList<>();
        for (GoodsStatisticsDataVO statistic : statistics) {
            Goods goods = goodsMap.get(statistic.getGoodsId());
            if (goods == null) {
                continue;
            }
            result.add(buildGoodsCard(goods, parseInteger(statistic.getNum())));
            if (result.size() >= HOT_GOODS_LIMIT) {
                break;
            }
        }
        return result;
    }

    private List<PlatformHomeGoodsCardVO> resolveNewGoods() {
        GoodsSearchParams searchParams = new GoodsSearchParams();
        searchParams.setGoodsStatus(GoodsStatusEnum.UPPER.name());
        searchParams.setPageNumber(1);
        searchParams.setPageSize(NEW_GOODS_LIMIT);
        searchParams.setSort("createTime");
        searchParams.setOrder("desc");
        IPage<Goods> page = goodsService.queryByParams(searchParams);
        if (page == null || page.getRecords() == null) {
            return Collections.emptyList();
        }
        return page.getRecords().stream()
                .map(item -> buildGoodsCard(item, item.getBuyCount()))
                .collect(Collectors.toList());
    }

    private List<PlatformHomeStoreCardVO> resolveFrequentStores() {
        AuthUser currentUser = UserContext.getCurrentUser();
        Map<String, String> sourceMap = new LinkedHashMap<>();
        Set<String> storeIds = new LinkedHashSet<>();

        if (currentUser != null && UserEnums.MEMBER.equals(currentUser.getRole())) {
            appendOrderedStores(storeIds, sourceMap, loadMemberOrderStoreIds(currentUser.getId()), "ORDER");
            appendOrderedStores(storeIds, sourceMap, loadMemberCollectionStoreIds(currentUser.getId()), "COLLECTION");
            appendOrderedStores(storeIds, sourceMap, loadMemberFootprintStoreIds(currentUser.getId()), "FOOTPRINT");
        }

        appendOrderedStores(storeIds, sourceMap, loadHotStoreIds(), "HOT");

        if (storeIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> candidateStoreIds = new ArrayList<>(storeIds);
        Map<String, Store> storeMap = storeService.listByIds(candidateStoreIds).stream()
                .filter(item -> StoreStatusEnum.OPEN.name().equals(item.getStoreDisable()))
                .collect(Collectors.toMap(Store::getId, item -> item, (left, right) -> left));

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
            if (result.size() >= FREQUENT_STORE_LIMIT) {
                break;
            }
        }
        return result;
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

    private void appendOrderedStores(Set<String> storeIds, Map<String, String> sourceMap, List<String> candidates, String source) {
        if (candidates == null || candidates.isEmpty() || storeIds.size() >= FREQUENT_STORE_LIMIT) {
            return;
        }
        for (String storeId : candidates) {
            if (CharSequenceUtil.isBlank(storeId)) {
                continue;
            }
            if (storeIds.add(storeId)) {
                sourceMap.put(storeId, source);
            }
            if (storeIds.size() >= FREQUENT_STORE_LIMIT) {
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
        PlatformHomeGoodsCardVO cardVO = new PlatformHomeGoodsCardVO();
        cardVO.setGoodsId(goods.getId());
        cardVO.setGoodsName(goods.getGoodsName());
        cardVO.setGoodsImage(firstNotBlank(goods.getThumbnail(), goods.getSmall(), goods.getOriginal()));
        cardVO.setPrice(goods.getPrice());
        cardVO.setOriginalPrice(goods.getPrice());
        cardVO.setStoreId(goods.getStoreId());
        cardVO.setStoreName(goods.getStoreName());
        cardVO.setSalesNum(salesNum == null ? 0 : salesNum);
        return cardVO;
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
}
