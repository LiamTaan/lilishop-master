package cn.lili.controller.wxchannels;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.wxchannels.entity.dos.WxChannelOrder;
import cn.lili.modules.wxchannels.entity.dto.WxChannelOrderSearchParams;
import cn.lili.modules.wxchannels.service.WxChannelOrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@Tag(name = "管理端,微信视频号订单接口")
@RequestMapping("/manager/wxchannels/order")
public class WxChannelOrderManagerController {

    @Autowired
    private WxChannelOrderService wxChannelOrderService;

    @GetMapping
    @Operation(summary = "分页获取视频号订单")
    @Parameter(name = "channelOrderSn", description = "订单编号")
    @Parameter(name = "memberNickName", description = "客户昵称")
    @Parameter(name = "goodsName", description = "商品名称")
    @Parameter(name = "status", description = "微信视频号订单状态，当前系统按微信侧原始状态值透传保存")
    @Parameter(name = "scene", description = "下单场景：LIVE=直播间，WINDOW=橱窗")
    @Parameter(name = "startTime", description = "开始时间，13 位时间戳（毫秒）")
    @Parameter(name = "endTime", description = "结束时间，13 位时间戳（毫秒）")
    public ResultMessage<IPage<WxChannelOrder>> page(WxChannelOrderSearchParams params) {
        return ResultUtil.data(wxChannelOrderService.page(params));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查看订单详情")
    public ResultMessage<WxChannelOrder> get(@Parameter(description = "视频号订单记录 ID") @PathVariable String id) {
        return ResultUtil.data(wxChannelOrderService.getById(id));
    }
}
