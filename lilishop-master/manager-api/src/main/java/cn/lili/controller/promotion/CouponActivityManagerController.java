package cn.lili.controller.promotion;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.promotion.entity.dos.CouponActivity;
import cn.lili.modules.promotion.entity.dos.CouponActivityItem;
import cn.lili.modules.promotion.entity.dto.CouponActivityDTO;
import cn.lili.modules.promotion.entity.vos.CouponActivityVO;
import cn.lili.modules.promotion.service.CouponActivityService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 优惠券活动
 *
 * @author Bulbasaur
 * @since 2021/5/21 7:11 下午
 */
@RestController
@RequestMapping("/manager/promotion/couponActivity")
public class CouponActivityManagerController {

    @Autowired
    private CouponActivityService couponActivityService;

    @GetMapping
    public ResultMessage<IPage<CouponActivity>> getCouponActivityPage(PageVO page, CouponActivity couponActivity) {
        return ResultUtil.data(couponActivityService.getByPage(page, couponActivity));
    }

    @GetMapping("/{couponActivityId}")
    public ResultMessage<CouponActivityVO> getCouponActivity(@PathVariable String couponActivityId) {
        return ResultUtil.data(couponActivityService.getCouponActivityVO(couponActivityId));
    }

    @PostMapping
    public ResultMessage<CouponActivity> addCouponActivity(@RequestBody(required = false) CouponActivityDTO couponActivityDTO) {
        for (CouponActivityItem couponActivityItem : couponActivityDTO.getCouponActivityItems()) {
            if (couponActivityItem.getNum() > 5) {
                throw new ServiceException(ResultCode.COUPON_ACTIVITY_MAX_NUM);
            }
        }
        if (couponActivityService.savePromotions(couponActivityDTO)) {
            return ResultUtil.data(couponActivityDTO);
        }
        return ResultUtil.error(ResultCode.COUPON_ACTIVITY_SAVE_ERROR);
    }

    @DeleteMapping("/{id}")
    public ResultMessage<CouponActivity> updateStatus(@PathVariable String id) {
        if (couponActivityService.updateStatus(Collections.singletonList(id), null, null)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        throw new ServiceException(ResultCode.ERROR);
    }


}
