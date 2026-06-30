package cn.lili.controller.wxchannels;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.wxchannels.entity.dos.WxChannelCategory;
import cn.lili.modules.wxchannels.entity.dto.WxChannelCategorySubmitDTO;
import cn.lili.modules.wxchannels.entity.dto.WxChannelsCategoryDTO;
import cn.lili.modules.wxchannels.service.WxChannelCategoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager/wxchannels/category")
public class WxChannelCategoryManagerController {

    @Autowired
    private WxChannelCategoryService wxChannelCategoryService;

    @GetMapping
    public ResultMessage<IPage<WxChannelCategory>> page(PageVO page, String status) {
        return ResultUtil.data(wxChannelCategoryService.page(page, status));
    }

    @PutMapping("/{id}")
 public ResultMessage<WxChannelCategory> update(@PathVariable String id,
                                                   @RequestBody WxChannelCategory body) {
        body.setId(id);
        wxChannelCategoryService.updateById(body);
        return ResultUtil.data(body);
    }

    @DeleteMapping("/{id}")
 public ResultMessage<Object> delete(@PathVariable String id) {
        wxChannelCategoryService.removeById(id);
        return ResultUtil.success();
    }

    @PostMapping("/submit")
    public ResultMessage<Object> submit(@Valid @RequestBody WxChannelCategorySubmitDTO dto) {
        wxChannelCategoryService.submit(dto);
        return ResultUtil.success();
    }

    @GetMapping("/third")
    public ResultMessage<List<WxChannelsCategoryDTO>> third(@RequestParam(value = "forceRefresh", required = false, defaultValue = "false") Boolean forceRefresh) {
        return ResultUtil.data(wxChannelCategoryService.listAllThirdCategory(Boolean.TRUE.equals(forceRefresh)));
    }
}
