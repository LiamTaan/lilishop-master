package cn.lili.controller.goods;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.ManagerGoodsOperationDTO;
import cn.lili.modules.goods.entity.dto.GoodsAuthUpdateDTO;
import cn.lili.modules.goods.entity.dto.GoodsBatchOperationDTO;
import cn.lili.modules.goods.entity.dto.GoodsBatchUnderDTO;
import cn.lili.modules.goods.entity.dto.GoodsSearchParams;
import cn.lili.modules.goods.entity.dto.GoodsSkuSearchParams;
import cn.lili.modules.goods.entity.dto.GoodsVirtualSalesSingleDTO;
import cn.lili.modules.goods.entity.dto.GoodsVirtualSalesDTO;
import cn.lili.modules.goods.entity.enums.GoodsAuthEnum;
import cn.lili.modules.goods.entity.enums.GoodsSalesModeEnum;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import cn.lili.modules.goods.entity.vos.GoodsNumVO;
import cn.lili.modules.goods.entity.vos.GoodsVO;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.goods.service.GoodsSkuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 管理端,商品管理接口
 *
 * @author pikachu
 * @since 2020-02-23 15:18:56
 */
@RestController
@Validated
@RequestMapping("/manager/goods/goods")
public class GoodsManagerController {
    /**
     * 商品
     */
    @Autowired
    private GoodsService goodsService;
    /**
     * 规格商品
     */
    @Autowired
    private GoodsSkuService goodsSkuService;

    @GetMapping("/list")
    public ResultMessage<IPage<Goods>> getByPage(GoodsSearchParams goodsSearchParams) {
        if (goodsSearchParams.getSalesModel() == null || goodsSearchParams.getSalesModel().isBlank()) {
            goodsSearchParams.setSalesModel(GoodsSalesModeEnum.RETAIL.name());
        }
        return ResultUtil.data(goodsService.queryByParams(goodsSearchParams));
    }

    @GetMapping("/goodsNumber")
    public ResultMessage<GoodsNumVO> getGoodsNumVO(GoodsSearchParams goodsSearchParams) {
        if (goodsSearchParams.getSalesModel() == null || goodsSearchParams.getSalesModel().isBlank()) {
            goodsSearchParams.setSalesModel(GoodsSalesModeEnum.RETAIL.name());
        }
        return ResultUtil.data(goodsService.getGoodsNumVO(goodsSearchParams));
    }

    @GetMapping("/sku/list")
    public ResultMessage<IPage<GoodsSku>> getSkuByPage(GoodsSearchParams goodsSearchParams) {
        goodsSearchParams.setSort("create_time");
        goodsSearchParams.setOrder("desc");
        if (goodsSearchParams.getSalesModel() == null || goodsSearchParams.getSalesModel().isBlank()) {
            goodsSearchParams.setSalesModel(GoodsSalesModeEnum.RETAIL.name());
        }
        return ResultUtil.data(goodsSkuService.getGoodsSkuByPage(goodsSearchParams));
    }

    @GetMapping("/sku/promotion/list")
    public ResultMessage<IPage<GoodsSku>> getPromotionSkuByPage(GoodsSkuSearchParams goodsSkuSearchParams) {
        goodsSkuSearchParams.setSalesModel(GoodsSalesModeEnum.RETAIL.name());
        goodsSkuSearchParams.setAuthFlag(GoodsAuthEnum.PASS.name());
        goodsSkuSearchParams.setMarketEnable(GoodsStatusEnum.UPPER.name());
        goodsSkuSearchParams.setSort("create_time");
        goodsSkuSearchParams.setOrder("desc");
        return ResultUtil.data(goodsSkuService.getGoodsSkuByPage(goodsSkuSearchParams));
    }

    @GetMapping("/auth/list")
    public ResultMessage<IPage<Goods>> getAuthPage(GoodsSearchParams goodsSearchParams) {
        goodsSearchParams.setAuthFlag(GoodsAuthEnum.TOBEAUDITED.name());
        return ResultUtil.data(goodsService.queryByParams(goodsSearchParams));
    }

    @GetMapping("/wholesale/list")
    public ResultMessage<IPage<Goods>> getWholesalePage(GoodsSearchParams goodsSearchParams) {
        goodsSearchParams.setSalesModel(GoodsSalesModeEnum.WHOLESALE.name());
        return ResultUtil.data(goodsService.queryByParams(goodsSearchParams));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResultMessage<Object> create(@Valid @RequestBody ManagerGoodsOperationDTO goodsOperationDTO) {
        goodsService.addGoodsByManager(goodsOperationDTO, goodsOperationDTO.getStoreId());
        return ResultUtil.success();
    }

    @PutMapping(value = "/{goodsId}", consumes = "application/json", produces = "application/json")
    public ResultMessage<Object> update(@PathVariable String goodsId, @Valid @RequestBody ManagerGoodsOperationDTO goodsOperationDTO) {
        goodsService.editGoodsByManager(goodsOperationDTO, goodsId, goodsOperationDTO.getStoreId());
        return ResultUtil.success();
    }

    @PreventDuplicateSubmissions
    @PutMapping("/auth")
    public ResultMessage<Object> auth(@RequestBody @Valid GoodsAuthUpdateDTO updateDTO) {
        //校验商品是否存在
        if (goodsService.auditGoods(updateDTO.getGoodsIds(), GoodsAuthEnum.valueOf(updateDTO.getAuthFlag()))) {
            return ResultUtil.success();
        }
        throw new ServiceException(ResultCode.GOODS_AUTH_ERROR);
    }


    @PreventDuplicateSubmissions
    @PutMapping("/up")
    public ResultMessage<Object> unpGoods(@RequestBody @Valid GoodsBatchOperationDTO updateDTO) {
        if (Boolean.TRUE.equals(goodsService.updateGoodsMarketAble(updateDTO.getGoodsId(), GoodsStatusEnum.UPPER, ""))) {
            return ResultUtil.success();
        }
        throw new ServiceException(ResultCode.GOODS_UPPER_ERROR);
    }

    @PreventDuplicateSubmissions

    @DemoSite
    @PutMapping("/under")
    public ResultMessage<Object> underGoods(@RequestBody @Valid GoodsBatchUnderDTO updateDTO) {

        if (Boolean.TRUE.equals(goodsService.managerUpdateGoodsMarketAble(updateDTO.getGoodsId(), GoodsStatusEnum.DOWN, updateDTO.getReason()))) {
            return ResultUtil.success();
        }
        throw new ServiceException(ResultCode.GOODS_UNDER_ERROR);
    }


    @GetMapping("/get/{id}")
    public ResultMessage<GoodsVO> get(@PathVariable String id) {
        GoodsVO goods = goodsService.getGoodsVO(id);
        return ResultUtil.data(goods);
    }

    @PreventDuplicateSubmissions
    @PutMapping("/virtualSales/{skuId}")
    public ResultMessage<Object> updateVirtualSales(@PathVariable String skuId,
                                                    @RequestBody @Valid GoodsVirtualSalesSingleDTO updateDTO) {
        goodsSkuService.updateGoodsSkuVirtualSales(skuId, updateDTO.getVirtualSales());
        return ResultUtil.success();
    }

    @PreventDuplicateSubmissions
    @PutMapping("/virtualSales")
    public ResultMessage<Object> batchUpdateVirtualSales(@Valid @RequestBody GoodsVirtualSalesDTO goodsVirtualSalesDTO) {
        goodsSkuService.batchUpdateGoodsSkuVirtualSales(goodsVirtualSalesDTO.getSkuIds(), goodsVirtualSalesDTO.getVirtualSales());
        return ResultUtil.success();
    }

    @DemoSite
    @PreventDuplicateSubmissions
    @DeleteMapping("/{ids}")
    public ResultMessage<Object> delete(@PathVariable String ids) {
        goodsService.deleteGoods(Arrays.asList(ids.split(",")));
        return ResultUtil.success();
    }

}
