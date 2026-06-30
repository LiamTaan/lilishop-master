package cn.lili.controller.goods;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.Category;
import cn.lili.modules.goods.service.CategoryBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端,分类品牌接口
 *
 * @author pikachu
 * @since 2020-02-27 15:18:56
 */
@RestController
@RequestMapping("/manager/goods/categoryBrand")
public class CategoryBrandManagerController {

    /**
     * 规格品牌管理
     */
    @Autowired
    private CategoryBrandService categoryBrandService;

    @GetMapping("/{brandId}")
    public ResultMessage<List<Category>> getBrandCategory(@PathVariable String brandId) {
        return ResultUtil.data(categoryBrandService.getBrandCategoryList(brandId));
    }

    @PostMapping("/{brandId}")
    public ResultMessage<Object> saveBrandCategory(@PathVariable String brandId, @RequestBody List<String> categoryIds) {
        categoryBrandService.saveBrandCategoryList(brandId, categoryIds);
        return ResultUtil.success();
    }

}
