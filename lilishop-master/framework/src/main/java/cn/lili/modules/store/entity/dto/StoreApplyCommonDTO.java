package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺入驻公共字段
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
public abstract class StoreApplyCommonDTO {

    @Schema(description = "店铺类型")
    private String storeType;

    @Schema(description = "代理等级")
    private String agentLevel;

    @Schema(description = "代理区域ID")
    private String agentRegionId;

    @Schema(description = "代理区域名称")
    private String agentRegionName;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "店铺logo")
    private String storeLogo;

    @Schema(description = "店铺简介")
    private String storeDesc;

    @Schema(description = "经纬度")
    private String storeCenter;

    @Schema(description = "店铺经营类目")
    private String goodsManagementCategory;

    @Schema(description = "地址名称， '，'分割")
    private String storeAddressPath;

    @Schema(description = "地址id，'，'分割")
    private String storeAddressIdPath;

    @Schema(description = "详细地址")
    private String storeAddressDetail;

    @Schema(description = "营业执照图片")
    private String businessLicenseUrl;

    @Schema(description = "统一社会信用代码")
    private String creditCode;

    @Schema(description = "营业执照地区ID")
    private String businessLicenseRegionId;

    @Schema(description = "营业执照有效期类型")
    private String businessLicenseExpireType;

    @Schema(description = "营业执照有效期截止时间")
    private String businessLicenseExpireDate;

    @Schema(description = "门头照")
    private String facadeImageUrl;

    @Schema(description = "店内照，英文逗号分隔")
    private String indoorImageUrls;
}
