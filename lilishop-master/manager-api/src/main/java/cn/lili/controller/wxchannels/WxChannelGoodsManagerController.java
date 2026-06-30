package cn.lili.controller.wxchannels;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.wxchannels.entity.dos.WxChannelGoods;
import cn.lili.modules.wxchannels.entity.dto.WxChannelGoodsSearchParams;
import cn.lili.modules.wxchannels.service.WxChannelGoodsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager/wxchannels/goods")
public class WxChannelGoodsManagerController {

    @Autowired
    private WxChannelGoodsService wxChannelGoodsService;

    @GetMapping
    public ResultMessage<IPage<WxChannelGoods>> page(WxChannelGoodsSearchParams params) {
        return ResultUtil.data(wxChannelGoodsService.page(params));
    }

    @PutMapping("/{id}")
 public ResultMessage<WxChannelGoods> update(@PathVariable String id,
                                                @RequestBody WxChannelGoods body) {
        body.setId(id);
        wxChannelGoodsService.updateById(body);
        return ResultUtil.data(body);
    }

    @DeleteMapping("/{id}")
 public ResultMessage<Object> delete(@PathVariable String id) {
        wxChannelGoodsService.removeById(id);
        return ResultUtil.success();
    }
}
