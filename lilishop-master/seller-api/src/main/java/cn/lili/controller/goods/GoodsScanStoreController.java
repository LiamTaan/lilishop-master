package cn.lili.controller.goods;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.vos.StoreGoodsScanVO;
import cn.lili.modules.goods.service.StoreGoodsScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 店铺端,商品扫码接口
 *
 * @author dawn
 * @since 2026/6/22
 */
@RestController
@Tag(name = "店铺端,商品扫码接口")
@RequestMapping("/store/goods")
public class GoodsScanStoreController {

    @Autowired
    private StoreGoodsScanService storeGoodsScanService;

    @Operation(summary = "扫码查询商品预填信息")
    @Parameter(name = "barcode", description = "外部条码", required = true)
    @GetMapping("/scan/barcode/{barcode}")
    public ResultMessage<StoreGoodsScanVO> scanBarcode(@PathVariable String barcode) {
        String storeId = Objects.requireNonNull(UserContext.getCurrentUser()).getStoreId();
        return ResultUtil.data(storeGoodsScanService.scan(storeId, barcode));
    }
}
