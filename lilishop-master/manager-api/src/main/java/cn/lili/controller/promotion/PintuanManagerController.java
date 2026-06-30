package cn.lili.controller.promotion;

import cn.lili.common.enums.PromotionTypeEnum;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.promotion.entity.dos.Pintuan;
import cn.lili.modules.promotion.entity.dos.PromotionGoods;
import cn.lili.modules.promotion.entity.dto.search.PintuanSearchParams;
import cn.lili.modules.promotion.entity.dto.search.PromotionGoodsSearchParams;
import cn.lili.modules.promotion.entity.vos.PintuanMemberVO;
import cn.lili.modules.promotion.entity.vos.PintuanVO;
import cn.lili.modules.promotion.service.PintuanService;
import cn.lili.modules.promotion.service.PromotionGoodsService;
import cn.lili.modules.promotion.tools.PromotionTools;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 管理端,平台拼团接口
 *
 * @author paulG
 * @since 2020/10/9
 **/
@RestController
@RequestMapping("/manager/promotion/pintuan")
public class PintuanManagerController {
    @Autowired
    private PintuanService pintuanService;
    @Autowired
    private PromotionGoodsService promotionGoodsService;

    @GetMapping("/{id}")
    public ResultMessage<PintuanVO> get(@PathVariable String id) {
        PintuanVO pintuan = pintuanService.getPintuanVO(id);
        return ResultUtil.data(pintuan);
    }

    @GetMapping
    public ResultMessage<IPage<Pintuan>> getPintuanByPage(PintuanSearchParams queryParam, PageVO pageVo) {
        IPage<Pintuan> pintuanIPage = pintuanService.pageFindAll(queryParam, pageVo);
        return ResultUtil.data(pintuanIPage);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResultMessage<PintuanVO> add(@Valid @RequestBody PintuanVO pintuanVO) {
        this.setPlatformStoreInfo(pintuanVO);
        if (pintuanService.savePromotions(pintuanVO)) {
            return ResultUtil.data(pintuanVO);
        }
        throw new ServiceException(ResultCode.PINTUAN_ADD_ERROR);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResultMessage<PintuanVO> update(@PathVariable String id, @Valid @RequestBody PintuanVO pintuanVO) {
        this.setPlatformStoreInfo(pintuanVO);
        pintuanVO.setId(id);
        if (pintuanService.updatePromotions(pintuanVO)) {
            return ResultUtil.data(pintuanVO);
        }
        throw new ServiceException(ResultCode.PINTUAN_EDIT_ERROR);
    }

    @DeleteMapping("/{ids}")
    public ResultMessage<Object> delete(@PathVariable String ids) {
        if (pintuanService.removePromotions(Arrays.asList(ids.split(",")))) {
            return ResultUtil.success(ResultCode.PINTUAN_DELETE_SUCCESS);
        }
        throw new ServiceException(ResultCode.PINTUAN_DELETE_ERROR);
    }

    @GetMapping("/goods/{pintuanId}")
    public ResultMessage<IPage<PromotionGoods>> getPintuanGoodsByPage(@PathVariable String pintuanId, PageVO pageVo) {
        PromotionGoodsSearchParams searchParams = new PromotionGoodsSearchParams();
        searchParams.setPromotionId(pintuanId);
        searchParams.setPromotionType(PromotionTypeEnum.PINTUAN.name());
        return ResultUtil.data(promotionGoodsService.pageFindAll(searchParams, pageVo));
    }

    @GetMapping("/{pintuanId}/members")
    public ResultMessage<List<PintuanMemberVO>> getPintuanMembers(@PathVariable String pintuanId) {
        pintuanService.getPintuanVO(pintuanId);
        return ResultUtil.data(pintuanService.getPintuanMember(pintuanId));
    }

    @PutMapping("/status/{pintuanIds}")
    public ResultMessage<String> openPintuan(@PathVariable String pintuanIds, Long startTime, Long endTime) {
        if (pintuanService.updateStatus(Arrays.asList(pintuanIds.split(",")), startTime, endTime)) {
            return ResultUtil.success(ResultCode.PINTUAN_MANUAL_OPEN_SUCCESS);
        }
        throw new ServiceException(ResultCode.PINTUAN_MANUAL_OPEN_ERROR);

    }

    private void setPlatformStoreInfo(PintuanVO pintuanVO) {
        Objects.requireNonNull(UserContext.getCurrentUser());
        pintuanVO.setStoreId(PromotionTools.PLATFORM_ID);
        pintuanVO.setStoreName(PromotionTools.PLATFORM_NAME);
    }

}
