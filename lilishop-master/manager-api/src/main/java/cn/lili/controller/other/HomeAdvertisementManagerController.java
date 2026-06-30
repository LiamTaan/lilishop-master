package cn.lili.controller.other;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.page.entity.dto.PlatformHomeConfigBannerDTO;
import cn.lili.modules.page.entity.dto.PlatformHomeConfigSaveDTO;
import cn.lili.modules.page.entity.dto.PlatformHomeRecommendationConfigDTO;
import cn.lili.modules.page.entity.vos.PlatformHomeBannerVO;
import cn.lili.modules.page.entity.vos.PlatformHomeConfigVO;
import cn.lili.modules.page.entity.vos.PlatformHomeRecommendationConfigVO;
import cn.lili.modules.page.service.PlatformHomeConfigService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 管理端,首页广告位业务接口
 *
 * @author codex
 * @since 2026/6/26
 */
@RestController
@RequestMapping("/manager/other/homeAdvertisement")
public class HomeAdvertisementManagerController {

    @Autowired
    private PlatformHomeConfigService platformHomeConfigService;

    @GetMapping
    public ResultMessage<PlatformHomeConfigVO> get(@RequestParam String clientType) {
        return ResultUtil.data(platformHomeConfigService.getConfig(clientType));
    }

    @DemoSite
    @PutMapping
    public ResultMessage<PlatformHomeConfigVO> save(@Valid @RequestBody PlatformHomeConfigSaveDTO saveDTO) {
        PlatformHomeConfigVO current = platformHomeConfigService.getConfig(saveDTO.getClientType());
        PlatformHomeConfigSaveDTO merged = buildBaseSaveDTO(current, saveDTO);
        merged.setBanners(saveDTO.getBanners() == null ? Collections.emptyList() : saveDTO.getBanners());
        merged.setTopAdvert(saveDTO.getTopAdvert());
        return ResultUtil.data(platformHomeConfigService.saveConfig(merged));
    }

    private PlatformHomeConfigSaveDTO buildBaseSaveDTO(PlatformHomeConfigVO current,
                                                       PlatformHomeConfigSaveDTO incoming) {
        PlatformHomeConfigSaveDTO saveDTO = new PlatformHomeConfigSaveDTO();
        saveDTO.setClientType(incoming.getClientType());
        saveDTO.setPageName(current.getPageName());
        saveDTO.setPublishNow(incoming.getPublishNow());
        saveDTO.setBanners(copyBannerList(current.getBanners()));
        saveDTO.setTopAdvert(copyBanner(current.getTopAdvert()));
        saveDTO.setFloorModules(Collections.emptyList());
        saveDTO.setShortcutNavList(Collections.emptyList());
        saveDTO.setFrequentStoresConfig(copyRecommendationConfig(current.getFrequentStoresConfig()));
        saveDTO.setGuessYouLikeConfig(copyRecommendationConfig(current.getGuessYouLikeConfig()));
        saveDTO.setLowPriceZoneConfig(copyRecommendationConfig(current.getLowPriceZoneConfig()));
        saveDTO.setTodaySpecialConfig(copyRecommendationConfig(current.getTodaySpecialConfig()));
        return saveDTO;
    }

    private List<PlatformHomeConfigBannerDTO> copyBannerList(List<PlatformHomeBannerVO> banners) {
        if (banners == null || banners.isEmpty()) {
            return Collections.emptyList();
        }
        return banners.stream().map(this::copyBanner).toList();
    }

    private PlatformHomeConfigBannerDTO copyBanner(PlatformHomeBannerVO source) {
        if (source == null) {
            return null;
        }
        PlatformHomeConfigBannerDTO target = new PlatformHomeConfigBannerDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    private PlatformHomeRecommendationConfigDTO copyRecommendationConfig(PlatformHomeRecommendationConfigVO source) {
        if (source == null) {
            return null;
        }
        PlatformHomeRecommendationConfigDTO target = new PlatformHomeRecommendationConfigDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
