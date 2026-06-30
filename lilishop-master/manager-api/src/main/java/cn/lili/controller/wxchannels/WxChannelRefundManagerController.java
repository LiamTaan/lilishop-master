package cn.lili.controller.wxchannels;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.wxchannels.entity.dos.WxChannelRefund;
import cn.lili.modules.wxchannels.entity.dto.WxChannelRefundSearchParams;
import cn.lili.modules.wxchannels.service.WxChannelRefundService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/wxchannels/refund")
public class WxChannelRefundManagerController {

    @Autowired
    private WxChannelRefundService wxChannelRefundService;

    @GetMapping
    public ResultMessage<IPage<WxChannelRefund>> page(WxChannelRefundSearchParams params) {
        return ResultUtil.data(wxChannelRefundService.page(params));
    }

    @GetMapping("/{id}")
 public ResultMessage<WxChannelRefund> get(@PathVariable String id) {
        return ResultUtil.data(wxChannelRefundService.getById(id));
    }
}
