package cn.lili.modules.goods.entity.dto;

import cn.lili.common.validation.EnumValue;
import cn.lili.modules.goods.entity.enums.GoodsSalesModeEnum;
import cn.lili.modules.goods.entity.enums.GoodsTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品操作DTO
 *
 * @author pikachu
 * @since 2020-02-24 19:27:20
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsOperationDTO implements Serializable {

    private static final long serialVersionUID = -509667581371776913L;

    @Schema(description = "商品id", hidden = true)
    private String goodsId;

    @Schema(description = "商品默认销售价。单规格商品通常与 skuList 中 price 一致；多规格商品场景下用于商品主价格展示。", required = true)
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格不能为负数")
    @Max(value = 99999999, message = "商品价格不能超过99999999")
    private Double price;

    @Schema(description = "平台分类路径，使用分类 ID 逗号拼接，例如：一级ID,二级ID,三级ID。")
    private String categoryPath;

    @Schema(description = "店铺商品分类路径，使用店铺自定义分类 ID 逗号拼接。", required = true)
    @Size(max = 200, message = "选择了太多店铺分类")
    private String storeCategoryPath;

    @Schema(description = "品牌ID")
    @Min(value = 0, message = "品牌值不正确")
    private String brandId;

    @Schema(description = "商品名称", required = true)
    @NotEmpty(message = "商品名称不能为空")
    @Length(max = 50, message = "商品名称不能超过50个字符")
    private String goodsName;

    @Schema(description = "商品主条码")
    @Length(max = 64, message = "商品主条码不能超过64个字符")
    private String barcode;

    @Schema(description = "商品 PC 端详情 HTML")
    private String intro;

    @Schema(description = "商品移动端详情 HTML")
    private String mobileIntro;

    @Schema(description = "商品总库存展示值。实际库存以 skuList 中各 SKU 的 quantity 为准。")
    @Min(value = 0, message = "库存不能为负数")
    @Max(value = 99999999, message = "库存不能超过99999999")
    private Integer quantity;

    @Schema(description = "是否提交后立即发布/上架，具体生效还受审核状态与库存影响。")
    private Boolean release;

    @Schema(description = "是否推荐商品")
    private Boolean recommend;

    @Schema(description = "商品参数列表，用于详情页参数展示。")
    private List<GoodsParamsItemDTO> goodsParamsDTOList;

    @Schema(description = "商品公共相册图片列表。SKU 未单独传 images 时，会优先使用这里的图片。")
    private List<String> goodsGalleryList;

    @Schema(description = "运费模板ID。实物商品必须传有效模板 ID；虚拟商品传 0。", required = true)
    @NotNull(message = "运费模板不能为空，没有运费模板时，传值0")
    @Min(value = 0, message = "运费模板值不正确")
    private String templateId;

    @Schema(description = "商品卖点")
    private String sellingPoint;

    /**
     * @see cn.lili.modules.goods.entity.enums.GoodsSalesModeEnum
     */
    @Schema(description = "销售模式，例如 RETAIL 零售、WHOLESALE 批发。", required = true)
    private String salesModel;

    @Schema(description = "是否有规格", hidden = true)
    private String haveSpec;

    @Schema(description = "商品单位，例如 件、箱、盒、袋。", required = true)
    @NotEmpty(message = "商品单位不能为空")
    private String goodsUnit;

    @Schema(description = "商品补充说明/描述")
    private String info;

    @Schema(description = "修改商品时是否按当前 skuList 重建 SKU。true 表示重建并替换旧 SKU；false 表示按 skuList 中带回的 id 定向更新。")
    @Builder.Default
    private Boolean regeneratorSkuFlag = true;

    /**
     * @see cn.lili.modules.goods.entity.enums.GoodsTypeEnum
     */
    @Schema(description = "商品类型：PHYSICAL_GOODS 实物商品，VIRTUAL_GOODS 虚拟商品，E_COUPON 电子券商品。")
    @EnumValue(strValues = {"PHYSICAL_GOODS", "VIRTUAL_GOODS", "E_COUPON"}, message = "商品类型参数值错误")
    private String goodsType;

    /**
     * 商品视频
     */
    @Schema(description = "商品视频地址")
    private String goodsVideo;


    @Schema(description = "SKU 列表。每个对象代表一个 SKU；保留字段包括 id、sn、barcode、cost、price、quantity、weight、alertQuantity、images，其余字段都会被当作规格项（如 颜色、尺寸、包装规格）保存。")
    @Valid
    private List<Map<String, Object>> skuList;

    @Schema(description = "是否为商品模板")
    @Builder.Default
    private Boolean goodsTemplateFlag = false;
    /**
     * 批发商品规则
     */
    @Schema(description = "批发阶梯价规则列表。销售模式为 WHOLESALE 时使用。")
    private List<WholesaleDTO> wholesaleList;

    @Schema(description = "商品注意事项")
    private String needingAttention;


    @Schema(description = "是否为年度客户专属商品")
    private Boolean annualFeeExclusive;

    @Schema(description = "浏览权限标识")
    private String browsePermissions;

    public String getGoodsName() {
        //对商品对名称做一个极限处理。这里没有用xss过滤是因为xss过滤为全局过滤，影响很大。
        // 业务中，全局代码中只有商品名称不能拥有英文逗号，是由于商品名称存在一个数据库联合查询，结果要根据逗号分组
        return goodsName.replace(",", "");
    }

    public GoodsOperationDTO(GoodsImportDTO goodsImportDTO) {
        this.price = goodsImportDTO.getPrice();
        this.goodsName = goodsImportDTO.getGoodsName();
        this.intro = goodsImportDTO.getIntro();
        this.mobileIntro = goodsImportDTO.getIntro();
        this.quantity = goodsImportDTO.getQuantity();
        this.goodsGalleryList = goodsImportDTO.getGoodsGalleryList();
        this.templateId = goodsImportDTO.getTemplate();
        this.sellingPoint = goodsImportDTO.getSellingPoint();
        this.salesModel = GoodsSalesModeEnum.RETAIL.name();
        this.goodsUnit = goodsImportDTO.getGoodsUnit();
        this.goodsType = GoodsTypeEnum.PHYSICAL_GOODS.name();
        this.release = goodsImportDTO.getRelease();
        this.recommend = false;

        Map<String, Object> map = new HashMap<>();
        map.put("sn", goodsImportDTO.getSn());
        map.put("price", goodsImportDTO.getPrice());
        map.put("cost", goodsImportDTO.getCost());
        map.put("weight", goodsImportDTO.getWeight());
        map.put("quantity", goodsImportDTO.getQuantity());
        map.put(goodsImportDTO.getSkuKey(), goodsImportDTO.getSkuValue());
        map.put("images", goodsImportDTO.getImages());

        List<Map<String, Object>> skuList = new ArrayList<>();
        skuList.add(map);
        this.skuList = skuList;

    }
}
