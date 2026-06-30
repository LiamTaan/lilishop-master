package cn.lili.controller.goods;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.Parameters;
import cn.lili.modules.goods.entity.dto.GoodsParamsDTO;
import cn.lili.modules.goods.entity.dto.GoodsParamsSearchDTO;
import cn.lili.modules.goods.service.CategoryParameterService;
import cn.lili.modules.goods.service.ParametersService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端,商品参数管理接口
 *
 * @author Bulbasaur
 * @since 2020/11/26 16:15
 */
@RestController
@RequestMapping("/manager/goods/parameters")
public class ParameterManagerController {

    @Autowired
    private ParametersService parametersService;

    @Autowired
    private CategoryParameterService categoryParameterService;

    @GetMapping
    public ResultMessage<IPage<Parameters>> page(GoodsParamsSearchDTO goodsParamsSearchDTO) {
        return ResultUtil.data(parametersService.parametersPage(goodsParamsSearchDTO));
    }

    @PostMapping
    public ResultMessage<Parameters> save(@Valid @RequestBody GoodsParamsDTO parameters) {
        if (parametersService.addParameter(parameters)) {
            return ResultUtil.data(parameters);
        }
        throw new ServiceException(ResultCode.PARAMETER_SAVE_ERROR);

    }

    @PutMapping
    public ResultMessage<Parameters> update(@Valid @RequestBody GoodsParamsDTO parameters) {
        if (parametersService.updateParameter(parameters)) {
            return ResultUtil.data(parameters);
        }
        throw new ServiceException(ResultCode.PARAMETER_UPDATE_ERROR);
    }

    @DeleteMapping("/{id}")
    public ResultMessage<Object> delById(@PathVariable String id) {
        categoryParameterService.deleteByParameterId(id);
        parametersService.removeById(id);
        return ResultUtil.success();

    }

    @GetMapping("/{id}")
    public ResultMessage<GoodsParamsDTO> page(@PathVariable String id) {
        return ResultUtil.data(parametersService.getGoodsParamsDTO(id));
    }

}
