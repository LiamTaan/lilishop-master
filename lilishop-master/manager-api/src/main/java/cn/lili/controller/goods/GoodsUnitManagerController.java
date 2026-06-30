package cn.lili.controller.goods;


import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.GoodsUnit;
import cn.lili.modules.goods.service.GoodsUnitService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端,商品计量单位接口
 *
 * @author Bulbasaur
 * @since 2020/11/26 16:15
 */
@RestController
@RequestMapping("/manager/goods/goodsUnit")
public class GoodsUnitManagerController {

    @Autowired
    private GoodsUnitService goodsUnitService;


    @GetMapping
    public ResultMessage<IPage<GoodsUnit>> getByPage(PageVO pageVO) {
        return ResultUtil.data(goodsUnitService.page(PageUtil.initPage(pageVO)));
    }

    @GetMapping("/get/{id}")
    public ResultMessage<GoodsUnit> getById(@NotNull @PathVariable String id) {
        return ResultUtil.data(goodsUnitService.getById(id));
    }

    @PostMapping
    public ResultMessage<GoodsUnit> save(@Valid @RequestBody GoodsUnit goodsUnit) {
        goodsUnitService.save(goodsUnit);
        return ResultUtil.data(goodsUnit);
    }

    @PutMapping("/{id}")
    public ResultMessage<GoodsUnit> update(@NotNull @PathVariable String id, @Valid @RequestBody GoodsUnit goodsUnit) {
        goodsUnit.setId(id);
        goodsUnitService.updateById(goodsUnit);
        return ResultUtil.data(goodsUnit);
    }

    @DeleteMapping("/delete/{ids}")
    public ResultMessage<Object> delete(@NotNull @PathVariable List<String> ids) {
        goodsUnitService.removeByIds(ids);
        return ResultUtil.success();
    }


}
