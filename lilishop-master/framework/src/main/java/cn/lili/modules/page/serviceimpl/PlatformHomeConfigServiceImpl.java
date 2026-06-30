package cn.lili.modules.page.serviceimpl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import cn.lili.common.enums.ClientTypeEnum;
import cn.lili.common.enums.SwitchEnum;
import cn.lili.modules.page.entity.dos.OperationShortcutNav;
import cn.lili.modules.page.entity.dos.PageData;
import cn.lili.modules.page.entity.dto.PlatformHomeConfigBannerDTO;
import cn.lili.modules.page.entity.dto.PlatformHomeConfigFloorDTO;
import cn.lili.modules.page.entity.dto.PlatformHomeConfigSaveDTO;
import cn.lili.modules.page.entity.dto.PlatformHomeConfigShortcutDTO;
import cn.lili.modules.page.entity.dto.PlatformHomeRecommendationConfigDTO;
import cn.lili.modules.page.entity.enums.PageEnum;
import cn.lili.modules.page.entity.vos.PlatformHomeBannerVO;
import cn.lili.modules.page.entity.vos.PlatformHomeConfigShortcutVO;
import cn.lili.modules.page.entity.vos.PlatformHomeConfigVO;
import cn.lili.modules.page.entity.vos.PlatformHomeFloorModuleVO;
import cn.lili.modules.page.entity.vos.PlatformHomeRecommendationConfigVO;
import cn.lili.modules.page.entity.vos.PlatformHomeRuleBlockVO;
import cn.lili.modules.page.service.OperationShortcutNavService;
import cn.lili.modules.page.service.PageDataService;
import cn.lili.modules.page.service.PlatformHomeConfigService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 首页配置聚合服务实现
 *
 * @author codex
 * @since 2026/6/25
 */
@Service
public class PlatformHomeConfigServiceImpl implements PlatformHomeConfigService {

    private static final String SECTION_TYPE_CAROUSEL = "carousel";
    private static final String SECTION_TYPE_TOP_ADVERT = "topAdvert";
    private static final String SECTION_TYPE_WHOLESALE_FLOOR = "wholesaleFloor";
    private static final String SECTION_TYPE_RECOMMENDATION_CONFIG = "recommendationConfig";

    @Autowired
    private PageDataService pageDataService;
    @Autowired
    private OperationShortcutNavService operationShortcutNavService;

    @Override
    public PlatformHomeConfigVO getConfig(String clientType) {
        String effectiveClientType = normalizeClientType(clientType);
        PageBundle pageBundle = loadPageBundle(effectiveClientType);
        PageSnapshot pageSnapshot = parsePageSnapshot(pageBundle.getBasePage());

        PlatformHomeConfigVO vo = new PlatformHomeConfigVO();
        vo.setClientType(effectiveClientType);
        vo.setPageId(pageBundle.getBasePage() == null ? null : pageBundle.getBasePage().getId());
        vo.setPageName(pageBundle.getBasePage() == null ? defaultPageName(effectiveClientType) : pageBundle.getBasePage().getName());
        vo.setPublishStatus(pageBundle.getPublishedPage() == null ? SwitchEnum.CLOSE.name() : SwitchEnum.OPEN.name());
        vo.setBanners(pageSnapshot.getBanners());
        vo.setTopAdvert(pageSnapshot.getTopAdvert());
        vo.setFloorModules(pageSnapshot.getFloorModules());
        vo.setLegacySectionCount(pageSnapshot.getLegacySections().size());
        vo.setShortcutNavList(loadShortcutNavList(effectiveClientType));
        vo.setRuleBlocks(buildRuleBlocks());
        vo.setFrequentStoresConfig(copyRecommendationConfig(pageSnapshot.getFrequentStoresConfig()));
        vo.setGuessYouLikeConfig(copyRecommendationConfig(pageSnapshot.getGuessYouLikeConfig()));
        vo.setLowPriceZoneConfig(copyRecommendationConfig(pageSnapshot.getLowPriceZoneConfig()));
        vo.setTodaySpecialConfig(copyRecommendationConfig(pageSnapshot.getTodaySpecialConfig()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformHomeConfigVO saveConfig(PlatformHomeConfigSaveDTO saveDTO) {
        String effectiveClientType = normalizeClientType(saveDTO.getClientType());
        PageBundle pageBundle = loadPageBundle(effectiveClientType);
        PageSnapshot baseSnapshot = parsePageSnapshot(pageBundle.getBasePage());

        PageData targetPage;
        if (Boolean.TRUE.equals(saveDTO.getPublishNow())) {
            targetPage = pageBundle.getDraftPage() != null ? pageBundle.getDraftPage() : pageBundle.getPublishedPage();
        } else {
            targetPage = pageBundle.getDraftPage();
        }

        if (targetPage == null) {
            targetPage = new PageData();
            targetPage.setName(defaultPageName(effectiveClientType));
            targetPage.setPageType(PageEnum.INDEX.name());
            targetPage.setPageClientType(effectiveClientType);
        }

        targetPage.setName(CharSequenceUtil.blankToDefault(saveDTO.getPageName(), defaultPageName(effectiveClientType)));
        targetPage.setPageType(PageEnum.INDEX.name());
        targetPage.setPageClientType(effectiveClientType);
        targetPage.setPageData(buildPageDataJson(saveDTO, baseSnapshot.getLegacySections()));
        targetPage.setPageShow(Boolean.TRUE.equals(saveDTO.getPublishNow()) ? SwitchEnum.OPEN.name() : SwitchEnum.CLOSE.name());

        saveOrUpdatePage(targetPage);
        syncShortcutNav(effectiveClientType, saveDTO.getShortcutNavList());

        return getConfig(effectiveClientType);
    }

    private void saveOrUpdatePage(PageData pageData) {
        if (CharSequenceUtil.isBlank(pageData.getId())) {
            pageDataService.addPageData(pageData);
        } else {
            pageDataService.updatePageData(pageData);
        }
    }

    private void syncShortcutNav(String clientType, List<PlatformHomeConfigShortcutDTO> shortcutList) {
        List<OperationShortcutNav> currentList = operationShortcutNavService.list(Wrappers.<OperationShortcutNav>lambdaQuery()
                .eq(OperationShortcutNav::getClientType, clientType));
        List<PlatformHomeConfigShortcutDTO> safeShortcutList = shortcutList == null ? Collections.emptyList() : shortcutList;

        List<String> keepIds = safeShortcutList.stream()
                .map(PlatformHomeConfigShortcutDTO::getId)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toList());
        List<String> removeIds = currentList.stream()
                .map(OperationShortcutNav::getId)
                .filter(id -> !keepIds.contains(id))
                .collect(Collectors.toList());
        if (!removeIds.isEmpty()) {
            operationShortcutNavService.removeByIds(removeIds);
        }

        for (PlatformHomeConfigShortcutDTO item : safeShortcutList) {
            OperationShortcutNav entity = CharSequenceUtil.isNotBlank(item.getId())
                    ? operationShortcutNavService.getById(item.getId())
                    : null;
            if (entity == null) {
                entity = new OperationShortcutNav();
                entity.setId(IdUtil.simpleUUID());
            }
            entity.setClientType(clientType);
            entity.setTitle(item.getTitle());
            entity.setSubtitle(item.getSubtitle());
            entity.setImage(item.getImage());
            entity.setLinkType(item.getLinkType());
            entity.setLinkValue(item.getLinkValue());
            entity.setSortOrder(item.getSortOrder() == null ? 0 : item.getSortOrder());
            entity.setDisplayStatus(CharSequenceUtil.blankToDefault(item.getDisplayStatus(), SwitchEnum.OPEN.name()));
            entity.setRemark(item.getRemark());
            if (operationShortcutNavService.getById(entity.getId()) == null) {
                operationShortcutNavService.save(entity);
            } else {
                operationShortcutNavService.updateById(entity);
            }
        }
    }

    private List<PlatformHomeConfigShortcutVO> loadShortcutNavList(String clientType) {
        return operationShortcutNavService.list(Wrappers.<OperationShortcutNav>lambdaQuery()
                        .eq(OperationShortcutNav::getClientType, clientType)
                        .orderByAsc(OperationShortcutNav::getSortOrder)
                        .orderByDesc(OperationShortcutNav::getCreateTime))
                .stream()
                .map(item -> {
                    PlatformHomeConfigShortcutVO vo = new PlatformHomeConfigShortcutVO();
                    vo.setId(item.getId());
                    vo.setTitle(item.getTitle());
                    vo.setSubtitle(item.getSubtitle());
                    vo.setImage(item.getImage());
                    vo.setLinkType(item.getLinkType());
                    vo.setLinkValue(item.getLinkValue());
                    vo.setSortOrder(item.getSortOrder());
                    vo.setDisplayStatus(item.getDisplayStatus());
                    vo.setRemark(item.getRemark());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private String buildPageDataJson(PlatformHomeConfigSaveDTO saveDTO, List<JSONObject> legacySections) {
        JSONObject root = new JSONObject();
        JSONArray sectionList = new JSONArray();

        List<PlatformHomeConfigBannerDTO> banners = saveDTO.getBanners() == null ? Collections.emptyList() : saveDTO.getBanners();
        if (!banners.isEmpty()) {
            JSONObject carousel = new JSONObject();
            carousel.put("type", SECTION_TYPE_CAROUSEL);
            JSONObject options = new JSONObject();
            JSONArray list = new JSONArray();
            for (PlatformHomeConfigBannerDTO item : banners) {
                if (CharSequenceUtil.isBlank(item.getImage())) {
                    continue;
                }
                JSONObject banner = new JSONObject();
                banner.put("img", item.getImage());
                String linkValue = firstNotBlank(item.getLinkValue(), item.getUrl());
                banner.put("url", linkValue);
                banner.put("linkType", item.getLinkType());
                banner.put("linkValue", linkValue);
                list.add(banner);
            }
            options.put("list", list);
            carousel.put("options", options);
            sectionList.add(carousel);
        }

        if (saveDTO.getTopAdvert() != null && CharSequenceUtil.isNotBlank(saveDTO.getTopAdvert().getImage())) {
            JSONObject topAdvert = new JSONObject();
            topAdvert.put("type", SECTION_TYPE_TOP_ADVERT);
            topAdvert.put("img", saveDTO.getTopAdvert().getImage());
            String linkValue = firstNotBlank(saveDTO.getTopAdvert().getLinkValue(), saveDTO.getTopAdvert().getUrl());
            topAdvert.put("url", linkValue);
            topAdvert.put("linkType", saveDTO.getTopAdvert().getLinkType());
            topAdvert.put("linkValue", linkValue);
            sectionList.add(topAdvert);
        }

        List<PlatformHomeConfigFloorDTO> floorModules = saveDTO.getFloorModules() == null ? Collections.emptyList() : saveDTO.getFloorModules();
        floorModules.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getTitle()))
                .sorted(Comparator.comparing(item -> item.getSortOrder() == null ? Integer.MAX_VALUE : item.getSortOrder()))
                .forEach(item -> sectionList.add(buildFloorSection(item)));

        sectionList.add(buildRecommendationConfigSection(saveDTO));

        for (JSONObject legacySection : legacySections) {
            sectionList.add(JSON.parseObject(legacySection.toJSONString()));
        }

        root.put("list", sectionList);
        return root.toJSONString();
    }

    private JSONObject buildFloorSection(PlatformHomeConfigFloorDTO item) {
        JSONObject section = new JSONObject();
        section.put("type", SECTION_TYPE_WHOLESALE_FLOOR);
        section.put("name", item.getTitle());
        section.put("title", item.getTitle());
        section.put("subtitle", item.getSubtitle());
        section.put("img", item.getImage());

        JSONObject options = new JSONObject();
        options.put("title", item.getTitle());
        options.put("subtitle", item.getSubtitle());
        options.put("image", item.getImage());
        options.put("moduleType", item.getModuleType());
        options.put("sourceType", item.getSourceType());
        options.put("linkType", item.getLinkType());
        options.put("linkValue", item.getLinkValue());
        options.put("goodsLimit", item.getGoodsLimit());
        options.put("sortOrder", item.getSortOrder());
        options.put("displayStatus", CharSequenceUtil.blankToDefault(item.getDisplayStatus(), SwitchEnum.OPEN.name()));
        options.put("specialId", item.getSpecialId());
        options.put("specialName", item.getSpecialName());
        options.put("remark", item.getRemark());
        section.put("options", options);
        return section;
    }

    private JSONObject buildRecommendationConfigSection(PlatformHomeConfigSaveDTO saveDTO) {
        JSONObject section = new JSONObject();
        section.put("type", SECTION_TYPE_RECOMMENDATION_CONFIG);

        JSONObject options = new JSONObject();
        options.put("frequentStoresConfig", buildRecommendationConfigJson(
                "FREQUENT_STORES",
                saveDTO.getFrequentStoresConfig(),
                defaultRecommendationConfig("FREQUENT_STORES")
        ));
        options.put("guessYouLikeConfig", buildRecommendationConfigJson(
                "GUESS_YOU_LIKE",
                saveDTO.getGuessYouLikeConfig(),
                defaultRecommendationConfig("GUESS_YOU_LIKE")
        ));
        options.put("lowPriceZoneConfig", buildRecommendationConfigJson(
                "LOW_PRICE",
                saveDTO.getLowPriceZoneConfig(),
                defaultRecommendationConfig("LOW_PRICE")
        ));
        options.put("todaySpecialConfig", buildRecommendationConfigJson(
                "TODAY_SPECIAL",
                saveDTO.getTodaySpecialConfig(),
                defaultRecommendationConfig("TODAY_SPECIAL")
        ));
        section.put("options", options);
        return section;
    }

    private JSONObject buildRecommendationConfigJson(String code,
                                                     PlatformHomeRecommendationConfigDTO dto,
                                                     PlatformHomeRecommendationConfigVO defaults) {
        PlatformHomeRecommendationConfigVO merged = mergeRecommendationConfig(dto, defaults);
        JSONObject config = new JSONObject();
        config.put("code", code);
        config.put("enabled", Boolean.TRUE.equals(merged.getEnabled()));
        config.put("title", merged.getTitle());
        config.put("sortOrder", merged.getSortOrder());
        config.put("limit", merged.getLimit());
        config.put("categoryRange", merged.getCategoryRange());
        config.put("preferRecommendFlag", merged.getPreferRecommendFlag());
        config.put("coldStartStrategy", merged.getColdStartStrategy());
        config.put("priceUpperBound", merged.getPriceUpperBound());
        config.put("promotionTypes", merged.getPromotionTypes());
        return config;
    }

    private PageSnapshot parsePageSnapshot(PageData pageData) {
        PageSnapshot snapshot = new PageSnapshot();
        if (pageData == null || CharSequenceUtil.isBlank(pageData.getPageData())) {
            return snapshot;
        }
        try {
            JSONObject root = JSON.parseObject(pageData.getPageData());
            JSONArray sectionList = root == null ? null : root.getJSONArray("list");
            if (sectionList == null) {
                return snapshot;
            }
            for (int i = 0; i < sectionList.size(); i++) {
                JSONObject section = sectionList.getJSONObject(i);
                if (section == null) {
                    continue;
                }
                String type = CharSequenceUtil.blankToDefault(section.getString("type"), "").trim();
                switch (type) {
                    case SECTION_TYPE_CAROUSEL:
                        snapshot.getBanners().addAll(parseBannerSection(section));
                        break;
                    case SECTION_TYPE_TOP_ADVERT:
                        snapshot.setTopAdvert(parseTopAdvert(section));
                        break;
                    case SECTION_TYPE_WHOLESALE_FLOOR:
                        PlatformHomeFloorModuleVO floorModuleVO = parseFloorSection(section);
                        if (floorModuleVO != null) {
                            snapshot.getFloorModules().add(floorModuleVO);
                        }
                        break;
                    case SECTION_TYPE_RECOMMENDATION_CONFIG:
                        parseRecommendationSection(section, snapshot);
                        break;
                    default:
                        snapshot.getLegacySections().add(JSON.parseObject(section.toJSONString()));
                        break;
                }
            }
            snapshot.getFloorModules().sort(Comparator.comparing(item -> item.getSortOrder() == null ? Integer.MAX_VALUE : item.getSortOrder()));
        } catch (Exception ignored) {
            // 旧 JSON 解析失败时按空配置处理，避免阻断管理端进入首页配置。
        }
        return snapshot;
    }

    private List<PlatformHomeBannerVO> parseBannerSection(JSONObject section) {
        JSONObject options = section.getJSONObject("options");
        JSONArray list = options == null ? null : options.getJSONArray("list");
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<PlatformHomeBannerVO> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            JSONObject item = list.getJSONObject(i);
            if (item == null) {
                continue;
            }
            PlatformHomeBannerVO bannerVO = new PlatformHomeBannerVO();
            bannerVO.setImage(item.getString("img"));
            bannerVO.setUrl(item.getString("url"));
            bannerVO.setLinkType(item.getString("linkType"));
            bannerVO.setLinkValue(firstNotBlank(item.getString("linkValue"), item.getString("url")));
            if (CharSequenceUtil.isNotBlank(bannerVO.getImage())) {
                result.add(bannerVO);
            }
        }
        return result;
    }

    private PlatformHomeBannerVO parseTopAdvert(JSONObject section) {
        PlatformHomeBannerVO bannerVO = new PlatformHomeBannerVO();
        bannerVO.setImage(section.getString("img"));
        bannerVO.setUrl(section.getString("url"));
        bannerVO.setLinkType(section.getString("linkType"));
        bannerVO.setLinkValue(firstNotBlank(section.getString("linkValue"), section.getString("url")));
        return CharSequenceUtil.isBlank(bannerVO.getImage()) ? null : bannerVO;
    }

    private PlatformHomeFloorModuleVO parseFloorSection(JSONObject section) {
        JSONObject options = section.getJSONObject("options");
        if (options == null) {
            return null;
        }
        PlatformHomeFloorModuleVO vo = new PlatformHomeFloorModuleVO();
        vo.setTitle(firstNotBlank(options.getString("title"), section.getString("title"), section.getString("name")));
        vo.setSubtitle(firstNotBlank(options.getString("subtitle"), section.getString("subtitle")));
        vo.setImage(firstNotBlank(options.getString("image"), section.getString("img")));
        vo.setModuleType(options.getString("moduleType"));
        vo.setSourceType(options.getString("sourceType"));
        vo.setLinkType(options.getString("linkType"));
        vo.setLinkValue(options.getString("linkValue"));
        vo.setGoodsLimit(options.getInteger("goodsLimit"));
        vo.setSortOrder(options.getInteger("sortOrder"));
        vo.setDisplayStatus(CharSequenceUtil.blankToDefault(options.getString("displayStatus"), SwitchEnum.OPEN.name()));
        vo.setSpecialId(options.getString("specialId"));
        vo.setSpecialName(options.getString("specialName"));
        vo.setRemark(options.getString("remark"));
        return CharSequenceUtil.isBlank(vo.getTitle()) ? null : vo;
    }

    private void parseRecommendationSection(JSONObject section, PageSnapshot snapshot) {
        JSONObject options = section.getJSONObject("options");
        if (options == null) {
            return;
        }
        snapshot.setFrequentStoresConfig(parseRecommendationConfig(
                options.getJSONObject("frequentStoresConfig"),
                defaultRecommendationConfig("FREQUENT_STORES")
        ));
        snapshot.setGuessYouLikeConfig(parseRecommendationConfig(
                options.getJSONObject("guessYouLikeConfig"),
                defaultRecommendationConfig("GUESS_YOU_LIKE")
        ));
        snapshot.setLowPriceZoneConfig(parseRecommendationConfig(
                options.getJSONObject("lowPriceZoneConfig"),
                defaultRecommendationConfig("LOW_PRICE")
        ));
        snapshot.setTodaySpecialConfig(parseRecommendationConfig(
                options.getJSONObject("todaySpecialConfig"),
                defaultRecommendationConfig("TODAY_SPECIAL")
        ));
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
        config.setCategoryRange(readStringList(configObject, "categoryRange"));
        if (config.getCategoryRange().isEmpty()) {
            config.setCategoryRange(defaults.getCategoryRange());
        }
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

    private PlatformHomeRecommendationConfigVO mergeRecommendationConfig(PlatformHomeRecommendationConfigDTO dto,
                                                                        PlatformHomeRecommendationConfigVO defaults) {
        PlatformHomeRecommendationConfigVO merged = copyRecommendationConfig(defaults);
        if (dto == null) {
            return merged;
        }
        if (dto.getEnabled() != null) {
            merged.setEnabled(dto.getEnabled());
        }
        if (CharSequenceUtil.isNotBlank(dto.getTitle())) {
            merged.setTitle(dto.getTitle().trim());
        }
        if (dto.getSortOrder() != null) {
            merged.setSortOrder(dto.getSortOrder());
        }
        if (dto.getLimit() != null) {
            merged.setLimit(dto.getLimit());
        }
        if (dto.getCategoryRange() != null) {
            merged.setCategoryRange(dto.getCategoryRange().stream()
                    .filter(CharSequenceUtil::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList()));
        }
        if (dto.getPreferRecommendFlag() != null) {
            merged.setPreferRecommendFlag(dto.getPreferRecommendFlag());
        }
        if (CharSequenceUtil.isNotBlank(dto.getColdStartStrategy())) {
            merged.setColdStartStrategy(dto.getColdStartStrategy().trim());
        }
        if (dto.getPriceUpperBound() != null) {
            merged.setPriceUpperBound(dto.getPriceUpperBound());
        }
        if (dto.getPromotionTypes() != null) {
            merged.setPromotionTypes(dto.getPromotionTypes().stream()
                    .filter(CharSequenceUtil::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList()));
        }
        return merged;
    }

    private PlatformHomeRecommendationConfigVO copyRecommendationConfig(PlatformHomeRecommendationConfigVO source) {
        PlatformHomeRecommendationConfigVO target = new PlatformHomeRecommendationConfigVO();
        if (source == null) {
            return target;
        }
        BeanUtils.copyProperties(source, target);
        target.setCategoryRange(source.getCategoryRange() == null ? Collections.emptyList() : new ArrayList<>(source.getCategoryRange()));
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
                config.setLimit(4);
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
                config.setPromotionTypes(new ArrayList<>(List.of("SECKILL", "PINTUAN", "KANJIA")));
                break;
            default:
                break;
        }
        return config;
    }

    private List<PlatformHomeRuleBlockVO> buildRuleBlocks() {
        List<PlatformHomeRuleBlockVO> ruleBlocks = new ArrayList<>();
        ruleBlocks.add(buildRuleBlock("SECKILL", "秒杀模块", "前台按当前有效秒杀时段自动展示，不通过首页配置手工维护结果。", "营销中心 / 秒杀活动管理", "/manager/promotion/seckill"));
        ruleBlocks.add(buildRuleBlock("HOT_GOODS", "月度热卖", "由商品统计自动生成，适合做推荐商品来源，不建议手工维护商品清单。", "经营分析 / 商品排行", "/manager/statistics/index/goodsStatistics"));
        ruleBlocks.add(buildRuleBlock("NEW_GOODS", "上新推荐", "由最新上架商品自动生成，建议通过商品上下架与分类治理控制展示。", "商品中心 / 商品列表", "/manager/goods/goods/wholesale/list"));
        ruleBlocks.add(buildRuleBlock("SEASONAL_NEW", "时令上新", "按上新时间、季节标签或专题规则自动计算，不建议在首页配置里手填商品。", "商品中心 / 商品列表", "/manager/goods/goods/wholesale/list"));
        ruleBlocks.add(buildRuleBlock("FREQUENT_STORES", "常买店铺", "由订单、关注、足迹和热店规则综合生成，不通过运营后台手工指定。", "商家与代理治理 / 店铺管理", "/buyer/other/home/platform"));
        ruleBlocks.add(buildRuleBlock("GUESS_YOU_LIKE", "猜你喜欢", "由用户行为和推荐规则动态计算，后台只维护标题、排序、数量与范围，不手工选商品。", "推荐服务 / 首页推荐", "/buyer/other/home/platform"));
        ruleBlocks.add(buildRuleBlock("LOW_PRICE", "低价专区", "由低价商品池和活动规则自动生成，适合做价格带运营，不建议手工编排列表。", "商品中心 / 商品列表", "/manager/goods/goods/wholesale/list"));
        ruleBlocks.add(buildRuleBlock("TODAY_SPECIAL", "今日特惠", "由当日秒杀、拼团、砍价等明确活动价商品自动汇聚，不混入优惠券和满减。", "营销中心 / 秒杀 / 拼团 / 砍价", "/buyer/other/home/platform"));
        return ruleBlocks;
    }

    private PlatformHomeRuleBlockVO buildRuleBlock(String code, String title, String description, String sourceMenu, String sourceApi) {
        PlatformHomeRuleBlockVO vo = new PlatformHomeRuleBlockVO();
        vo.setCode(code);
        vo.setTitle(title);
        vo.setDescription(description);
        vo.setSourceMenu(sourceMenu);
        vo.setSourceApi(sourceApi);
        return vo;
    }

    private PageBundle loadPageBundle(String clientType) {
        List<PageData> pageList = pageDataService.list(Wrappers.<PageData>lambdaQuery()
                .eq(PageData::getPageType, PageEnum.INDEX.name())
                .eq(PageData::getPageClientType, clientType)
                .orderByDesc(PageData::getUpdateTime)
                .orderByDesc(PageData::getCreateTime));

        PageBundle pageBundle = new PageBundle();
        pageBundle.setPageList(pageList);
        for (PageData pageData : pageList) {
            if (pageBundle.getPublishedPage() == null && SwitchEnum.OPEN.name().equalsIgnoreCase(pageData.getPageShow())) {
                pageBundle.setPublishedPage(pageData);
            } else if (pageBundle.getDraftPage() == null) {
                pageBundle.setDraftPage(pageData);
            }
        }
        pageBundle.setBasePage(pageBundle.getDraftPage() != null ? pageBundle.getDraftPage() : pageBundle.getPublishedPage());
        return pageBundle;
    }

    private String normalizeClientType(String clientType) {
        if (CharSequenceUtil.isBlank(clientType)) {
            return ClientTypeEnum.APP.name();
        }
        try {
            return ClientTypeEnum.valueOf(clientType.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException exception) {
            return ClientTypeEnum.APP.name();
        }
    }

    private String defaultPageName(String clientType) {
        return clientType + "首页配置";
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

    private static class PageBundle {
        private List<PageData> pageList = Collections.emptyList();
        private PageData publishedPage;
        private PageData draftPage;
        private PageData basePage;

        public List<PageData> getPageList() {
            return pageList;
        }

        public void setPageList(List<PageData> pageList) {
            this.pageList = pageList;
        }

        public PageData getPublishedPage() {
            return publishedPage;
        }

        public void setPublishedPage(PageData publishedPage) {
            this.publishedPage = publishedPage;
        }

        public PageData getDraftPage() {
            return draftPage;
        }

        public void setDraftPage(PageData draftPage) {
            this.draftPage = draftPage;
        }

        public PageData getBasePage() {
            return basePage;
        }

        public void setBasePage(PageData basePage) {
            this.basePage = basePage;
        }
    }

    private static class PageSnapshot {
        private final List<PlatformHomeBannerVO> banners = new ArrayList<>();
        private PlatformHomeBannerVO topAdvert;
        private final List<PlatformHomeFloorModuleVO> floorModules = new ArrayList<>();
        private final List<JSONObject> legacySections = new ArrayList<>();
        private PlatformHomeRecommendationConfigVO frequentStoresConfig = defaultPageRecommendationConfig("FREQUENT_STORES");
        private PlatformHomeRecommendationConfigVO guessYouLikeConfig = defaultPageRecommendationConfig("GUESS_YOU_LIKE");
        private PlatformHomeRecommendationConfigVO lowPriceZoneConfig = defaultPageRecommendationConfig("LOW_PRICE");
        private PlatformHomeRecommendationConfigVO todaySpecialConfig = defaultPageRecommendationConfig("TODAY_SPECIAL");

        public List<PlatformHomeBannerVO> getBanners() {
            return banners;
        }

        public PlatformHomeBannerVO getTopAdvert() {
            return topAdvert;
        }

        public void setTopAdvert(PlatformHomeBannerVO topAdvert) {
            this.topAdvert = topAdvert;
        }

        public List<PlatformHomeFloorModuleVO> getFloorModules() {
            return floorModules;
        }

        public List<JSONObject> getLegacySections() {
            return legacySections;
        }

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
    }

    private static PlatformHomeRecommendationConfigVO defaultPageRecommendationConfig(String code) {
        PlatformHomeRecommendationConfigVO config = new PlatformHomeRecommendationConfigVO();
        config.setEnabled(Boolean.TRUE);
        config.setSortOrder(0);
        config.setCategoryRange(Collections.emptyList());
        config.setPromotionTypes(Collections.emptyList());
        switch (code) {
            case "FREQUENT_STORES":
                config.setTitle("常买店铺");
                config.setLimit(4);
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
                config.setPromotionTypes(new ArrayList<>(List.of("SECKILL", "PINTUAN", "KANJIA")));
                break;
            default:
                break;
        }
        return config;
    }
}
