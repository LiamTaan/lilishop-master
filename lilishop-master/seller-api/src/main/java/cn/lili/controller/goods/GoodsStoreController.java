package cn.lili.controller.goods;

import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.context.ThreadContextHolder;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.OperationalJudgment;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.GoodsOperationDTO;
import cn.lili.modules.goods.entity.dto.GoodsBatchOperationDTO;
import cn.lili.modules.goods.entity.dto.GoodsFreightUpdateDTO;
import cn.lili.modules.goods.entity.dto.GoodsMarketScheduleDTO;
import cn.lili.modules.goods.entity.dto.GoodsSearchParams;
import cn.lili.modules.goods.entity.dto.GoodsSkuStockDTO;
import cn.lili.modules.goods.entity.enums.GoodsStatusEnum;
import cn.lili.modules.goods.entity.vos.GoodsNumVO;
import cn.lili.modules.goods.entity.vos.GoodsSkuVO;
import cn.lili.modules.goods.entity.vos.GoodsVO;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.statistics.aop.PageViewPoint;
import cn.lili.modules.statistics.aop.enums.PageViewEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 店铺端,商品接口
 *
 * @author pikachu
 * @since 2020-02-23 15:18:56
 */
@RestController
@Slf4j
@Tag(name = "店铺端,商品接口")
@RequestMapping("/store/goods/goods")
public class GoodsStoreController {

    /**
     * 商品
     */
    @Autowired
    private GoodsService goodsService;
    /**
     * 商品sku
     */
    @Autowired
    private GoodsSkuService goodsSkuService;

    @Operation(summary = "分页查询当前店铺商品列表", description = "用于商品管理列表页，返回当前登录店铺下的商品分页数据。")
    @GetMapping("/list")
    public ResultMessage<IPage<Goods>> getByPage(GoodsSearchParams goodsSearchParams) {
        //获取当前登录商家账号
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        goodsSearchParams.setStoreId(storeId);
        return ResultUtil.data(goodsService.queryByParams(goodsSearchParams));
    }

    @Operation(summary = "获取当前店铺商品数量统计", description = "返回当前店铺商品在售、下架、待审核、审核拒绝等数量统计。")
    @GetMapping("/goodsNumber")
    public ResultMessage<GoodsNumVO> getGoodsNumVO(GoodsSearchParams goodsSearchParams) {
        //获取当前登录商家账号
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        goodsSearchParams.setStoreId(storeId);
        return ResultUtil.data(goodsService.getGoodsNumVO(goodsSearchParams));
    }

    @Operation(summary = "分页查询当前店铺SKU列表", description = "按 SKU 维度查询当前登录店铺的商品规格分页数据。")
    @GetMapping("/sku/list")
    public ResultMessage<IPage<GoodsSku>> getSkuByPage(GoodsSearchParams goodsSearchParams) {
        //获取当前登录商家账号
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        goodsSearchParams.setStoreId(storeId);
        return ResultUtil.data(goodsSkuService.getGoodsSkuByPage(goodsSearchParams));
    }

    @Operation(summary = "分页查询库存告警商品列表", description = "仅返回当前店铺已上架且触发库存预警的 SKU 分页数据。")
    @GetMapping("/list/stock")
    public ResultMessage<IPage<GoodsSku>> getWarningStockByPage(GoodsSearchParams goodsSearchParams) {
        //获取当前登录商家账号
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        goodsSearchParams.setStoreId(storeId);
        goodsSearchParams.setAlertQuantity(true);
        goodsSearchParams.setMarketEnable(GoodsStatusEnum.UPPER.name());
        IPage<GoodsSku> goodsSkuPage = goodsSkuService.getGoodsSkuByPage(goodsSearchParams);
        return ResultUtil.data(goodsSkuPage);
    }

    @Operation(summary = "批量修改SKU预警库存", description = "按 skuId 批量设置预警库存值，仅允许修改当前登录店铺自己的 SKU。")
    @PutMapping(value = "/batch/update/alert/stocks", consumes = "application/json")
    public ResultMessage<Object> batchUpdateAlertQuantity(@RequestBody List<GoodsSkuStockDTO> updateStockList) {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        // 获取商品skuId集合
        List<String> goodsSkuIds = updateStockList.stream().map(GoodsSkuStockDTO::getSkuId).collect(Collectors.toList());
        // 根据skuId集合查询商品信息
        List<GoodsSku> goodsSkuList = goodsSkuService.listByIdsAndStoreId(goodsSkuIds, storeId);
        // 过滤不符合当前店铺的商品
        List<String> filterGoodsSkuIds = goodsSkuList.stream().map(GoodsSku::getId).collect(Collectors.toList());
        List<GoodsSkuStockDTO> collect = updateStockList.stream().filter(i -> filterGoodsSkuIds.contains(i.getSkuId())).collect(Collectors.toList());
        goodsSkuService.batchUpdateAlertQuantity(collect);
        return ResultUtil.success();
    }

    @Operation(summary = "修改单个SKU预警库存", description = "按 skuId 修改单个 SKU 的预警库存值。")
    @PutMapping(value = "/update/alert/stocks", consumes = "application/json")
    public ResultMessage<Object> updateAlertQuantity(@RequestBody GoodsSkuStockDTO goodsSkuStockDTO) {
        goodsSkuService.updateAlertQuantity(goodsSkuStockDTO);
        return ResultUtil.success();
    }


    @Operation(summary = "获取商品编辑回显信息", description = "商品编辑页初始化回显接口。返回商品基础信息、商品参数、商品相册、SKU 列表、批发阶梯价等完整编辑数据。")
    @GetMapping("/get/{id}")
    public ResultMessage<GoodsVO> get(@PathVariable String id) {
        GoodsVO goods = OperationalJudgment.judgment(goodsService.getGoodsVO(id));
        return ResultUtil.data(goods);
    }

    @Operation(summary = "新增商品", description = "提交商品维护表单创建新商品。请求体使用 GoodsOperationDTO，skuList 中每个对象代表一个 SKU；除 sn、price、cost、quantity、weight、barcode、images 等保留字段外，其余字段都会作为规格项保存。")
    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public ResultMessage<GoodsOperationDTO> save(@Valid @RequestBody GoodsOperationDTO goodsOperationDTO) {
        goodsService.addGoods(goodsOperationDTO);
        return ResultUtil.success();
    }

    @Operation(summary = "修改商品", description = "提交商品维护表单保存商品编辑结果。goodsId 取路径参数，商品主体数据和 SKU 数据从 GoodsOperationDTO 中读取。")
    @PutMapping(value = "/update/{goodsId}", consumes = "application/json", produces = "application/json")
    public ResultMessage<GoodsOperationDTO> update(@Valid @RequestBody GoodsOperationDTO goodsOperationDTO, @PathVariable String goodsId) {
        goodsService.editGoods(goodsOperationDTO, goodsId);
        return ResultUtil.success();
    }

    @DemoSite
    @Operation(summary = "下架商品", description = "批量下架商品。请求体传商品 ID 列表。")
    @PutMapping("/under")
    public ResultMessage<Object> underGoods(@RequestBody @Valid GoodsBatchOperationDTO updateDTO) {

        goodsService.updateGoodsMarketAble(updateDTO.getGoodsId(), GoodsStatusEnum.DOWN, "商家下架");
        return ResultUtil.success();
    }

    @Operation(summary = "上架商品", description = "批量上架商品。请求体传商品 ID 列表。")
    @PutMapping("/up")
    public ResultMessage<Object> unpGoods(@RequestBody @Valid GoodsBatchOperationDTO updateDTO) {
        goodsService.updateGoodsMarketAble(updateDTO.getGoodsId(), GoodsStatusEnum.UPPER, "");
        return ResultUtil.success();
    }

    @DemoSite
    @Operation(summary = "删除商品", description = "逻辑删除商品。请求体传商品 ID 列表。")
    @PutMapping("/delete")
    public ResultMessage<Object> deleteGoods(@RequestBody @Valid GoodsBatchOperationDTO updateDTO) {
        goodsService.deleteGoods(updateDTO.getGoodsId());
        return ResultUtil.success();
    }

    @Operation(summary = "批量设置商品运费模板", description = "为指定商品批量设置运费模板。")
    @PutMapping("/freight")
    public ResultMessage<Object> freight(@RequestBody @Valid GoodsFreightUpdateDTO updateDTO) {
        goodsService.freight(updateDTO.getGoodsId(), updateDTO.getTemplateId());
        return ResultUtil.success();
    }

    @Operation(summary = "根据商品ID获取SKU列表", description = "用于商品编辑页或规格列表弹窗，返回当前商品下全部 SKU 明细。")
    @GetMapping("/sku/{goodsId}/list")
    public ResultMessage<List<GoodsSkuVO>> getSkuByList(@PathVariable String goodsId) {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        return ResultUtil.data(goodsSkuService.getGoodsSkuVOList(goodsSkuService.listByGoodsIdAndStoreId(goodsId, storeId)));
    }

    @Operation(summary = "批量修改SKU库存", description = "按 skuId 批量修改库存，仅允许修改当前登录店铺自己的 SKU。")
    @PutMapping(value = "/update/stocks", consumes = "application/json")
    public ResultMessage<Object> updateStocks(@RequestBody List<GoodsSkuStockDTO> updateStockList) {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        // 获取商品skuId集合
        List<String> goodsSkuIds = updateStockList.stream().map(GoodsSkuStockDTO::getSkuId).collect(Collectors.toList());
        // 根据skuId集合查询商品信息
        List<GoodsSku> goodsSkuList = goodsSkuService.listByIdsAndStoreId(goodsSkuIds, storeId);
        // 过滤不符合当前店铺的商品
        List<String> filterGoodsSkuIds = goodsSkuList.stream().map(GoodsSku::getId).collect(Collectors.toList());
        List<GoodsSkuStockDTO> collect = updateStockList.stream().filter(i -> filterGoodsSkuIds.contains(i.getSkuId())).collect(Collectors.toList());
        goodsSkuService.updateStocks(collect);
        return ResultUtil.success();
    }

    @Operation(summary = "定时上下架商品", description = "按商品 ID 列表设置定时上架或下架任务。触发时间精确到秒，建议格式：yyyy-MM-dd HH:mm:ss。")
    @PostMapping(value = "/schedule/market", consumes = "application/json")
    public ResultMessage<Object> scheduleMarket(@RequestBody @Valid GoodsMarketScheduleDTO dto) {
        GoodsStatusEnum status = GoodsStatusEnum.valueOf(dto.getStatus());
        goodsService.scheduleGoodsMarket(dto.getGoodsIds(), status, dto.getTriggerTime(), dto.getReason());
        return ResultUtil.success();
    }

    @Operation(summary = "获取商品SKU详情", description = "按 goodsId 和 skuId 获取某个 SKU 的详情数据，常用于商品详情预览或规格切换后的明细展示。")
    @Parameter(name = "goodsId", description = "商品ID", required = true)
    @Parameter(name = "skuId", description = "SKU ID", required = true)
    @GetMapping("/sku/{goodsId}/{skuId}")
    @PageViewPoint(type = PageViewEnum.SKU, id = "#id")
    public ResultMessage<Map<String, Object>> getSku(@NotNull(message = "商品ID不能为空") @PathVariable("goodsId") String goodsId,
                                                     @NotNull(message = "SKU ID不能为空") @PathVariable("skuId") String skuId) {
        try {
            // 读取选中的列表
            Map<String, Object> map = goodsSkuService.getGoodsSkuDetail(goodsId, skuId);
            return ResultUtil.data(map);
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.GOODS_ERROR.message(), e);
            return ResultUtil.error(ResultCode.GOODS_ERROR);
        }

    }


    @Operation(summary = "导出商品库存列表", description = "导出当前店铺 SKU 库存清单，返回 Excel 文件流。")
    @GetMapping("/queryExportStock")
    public void queryExportStock(GoodsSearchParams goodsSearchParams) {
        //获取当前登录商家账号
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        goodsSearchParams.setStoreId(storeId);

        HttpServletResponse response = ThreadContextHolder.getHttpResponse();
        goodsSkuService.queryExportStock(response, goodsSearchParams);
    }

    @Operation(summary = "导入商品库存列表", description = "上传 Excel 批量修改当前店铺商品库存。")
    @PostMapping(value = "/importStockExcel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultMessage<Object> importStockExcel(@RequestPart("files") MultipartFile files) {
        //获取当前登录商家账号
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        goodsSkuService.importStock(storeId, files);
        return ResultUtil.success(ResultCode.SUCCESS);
    }


}
