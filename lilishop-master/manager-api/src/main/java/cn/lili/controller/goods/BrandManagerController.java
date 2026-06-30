package cn.lili.controller.goods;


import cn.lili.common.enums.ResultCode;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.Brand;
import cn.lili.modules.goods.entity.dto.BrandPageDTO;
import cn.lili.modules.goods.entity.vos.BrandVO;
import cn.lili.modules.goods.service.BrandService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;


/**
 * 管理端,品牌接口
 *
 * @author pikachu
 * @since 2020-02-18 15:18:56
 */
@RestController
@RequestMapping("/manager/goods/brand")
public class BrandManagerController {

    /**
     * 品牌
     */
    @Autowired
    private BrandService brandService;

    @GetMapping("/get/{id}")
    public ResultMessage<Brand> get(@NotNull @PathVariable String id) {
        return ResultUtil.data(brandService.getById(id));
    }

    @GetMapping("/all")
    public List<Brand> getAll() {
        return brandService.listNotDeleted();
    }

    @GetMapping("/getByPage")
    public ResultMessage<IPage<Brand>> getByPage(BrandPageDTO page) {
        return ResultUtil.data(brandService.getBrandsByPage(page));
    }

    @PostMapping
    public ResultMessage<BrandVO> save(@Valid @RequestBody BrandVO brand) {
        if (brandService.addBrand(brand)) {
            return ResultUtil.data(brand);
        }
        throw new ServiceException(ResultCode.BRAND_SAVE_ERROR);
    }

    @PutMapping("/{id}")
    public ResultMessage<BrandVO> update(@PathVariable String id, @Valid @RequestBody BrandVO brand) {
        brand.setId(id);
        if (brandService.updateBrand(brand)) {
            return ResultUtil.data(brand);
        }
        throw new ServiceException(ResultCode.BRAND_UPDATE_ERROR);
    }

    @PutMapping("/disable/{brandId}")
    public ResultMessage<Object> disable(@PathVariable String brandId, @RequestParam Boolean disable) {
        if (brandService.brandDisable(brandId, disable)) {
            return ResultUtil.success();
        }
        throw new ServiceException(ResultCode.BRAND_DISABLE_ERROR);
    }

    @DeleteMapping("/delByIds/{ids}")
    public ResultMessage<Object> delAllByIds(@PathVariable List<String> ids) {
        brandService.deleteBrands(ids);
        return ResultUtil.success(ResultCode.SUCCESS);
    }

}
