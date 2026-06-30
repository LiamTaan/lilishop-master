package cn.lili.controller.wxchannels;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.wxchannels.entity.dos.WxChannelOrder;
import cn.lili.modules.wxchannels.entity.dto.WxChannelOrderSearchParams;
import cn.lili.modules.wxchannels.service.WxChannelOrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/wxchannels/order")
public class WxChannelOrderManagerController {

    @Autowired
    private WxChannelOrderService wxChannelOrderService;

    @GetMapping
    public ResultMessage<IPage<WxChannelOrder>> page(WxChannelOrderSearchParams params) {
        return ResultUtil.data(wxChannelOrderService.page(params));
    }

    @GetMapping("/{id}")
 public ResultMessage<WxChannelOrder> get(@PathVariable String id) {
        return ResultUtil.data(wxChannelOrderService.getById(id));
    }
}
