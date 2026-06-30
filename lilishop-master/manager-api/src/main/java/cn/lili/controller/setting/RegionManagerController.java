package cn.lili.controller.setting;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.system.entity.dos.Region;
import cn.lili.modules.system.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;


/**
 * 管理端,行政地区管理接口
 *
 * @author Chopper
 * @since 2020/12/2 10:40
 */
@RestController
@RequestMapping("/manager/setting/region")
public class RegionManagerController {
    @Autowired
    private RegionService regionService;

    @DemoSite
    @PostMapping( "/sync")
    public void synchronizationData(String url) {
        regionService.synchronizationData(url);
    }

    @GetMapping( "/{id}")
    public ResultMessage<Region> get(@PathVariable String id) {
        return ResultUtil.data(regionService.getById(id));
    }

    @GetMapping( "/item/{id}")
    public ResultMessage<List<Region>> getItem(@PathVariable String id) {
        return ResultUtil.data(regionService.getItem(id));
    }

    @DemoSite
    @PutMapping( "/{id}")
    public ResultMessage<Region> update(@PathVariable String id, @Valid Region region) {
        region.setId(id);
        regionService.updateById(region);
        return ResultUtil.data(region);
    }


    @DemoSite
    @PostMapping( "/save")
    public ResultMessage<Region> save(@Valid Region region) {
        regionService.save(region);
        return ResultUtil.data(region);
    }

    @DemoSite
    @DeleteMapping( "/{ids}")
    public ResultMessage<Object> delAllByIds(@PathVariable List<String> ids) {
        regionService.removeByIds(ids);
        return ResultUtil.success();
    }
}
