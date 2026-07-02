package cn.lili.modules.goods.entity.vos;

import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.Wholesale;
import cn.lili.modules.goods.entity.dto.GoodsParamsDTO;
import cn.lili.modules.goods.entity.dto.GoodsParamsItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品VO
 *
 * @author pikachu
 * @since 2020-02-26 23:24:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsVO extends Goods {

    private static final long serialVersionUID = 6377623919990713567L;

    @Schema(description = "平台分类名称路径，按 categoryPath 顺序返回。")
    private List<String> categoryName;

    @Schema(description = "商品参数列表，供编辑回显和详情展示。")
    private List<GoodsParamsItemDTO> goodsParamsDTOList;

    @Schema(description = "商品公共相册图片列表。")
    private List<String> goodsGalleryList;

    @Schema(description = "商品下全部 SKU 列表，供商品编辑页回显。")
    private List<GoodsSkuVO> skuList;

    @Schema(description = "批发阶梯价规则列表。")
    private List<Wholesale> wholesaleList;
}
