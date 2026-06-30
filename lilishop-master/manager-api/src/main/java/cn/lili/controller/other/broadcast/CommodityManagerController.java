package cn.lili.controller.other.broadcast;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.vos.CommodityVO;
import cn.lili.modules.goods.service.CommodityService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端,直播间管理接口
 *
 * @author Bulbasaur
 * @since 2021/5/28 11:56 上午
 */
@RestController
@RequestMapping("/manager/broadcast/commodity")
public class CommodityManagerController {

    @Autowired
    private CommodityService commodityService;

    @GetMapping
    public ResultMessage<IPage<CommodityVO>> page(String auditStatus, String name, PageVO pageVO) {
        return ResultUtil.data(commodityService.commodityList(pageVO, name, auditStatus));
    }
}
