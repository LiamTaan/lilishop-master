package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺编辑 DTO
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StoreEditDTO extends StoreApplyCommonDTO {

    @Schema(description = "店铺ID", hidden = true)
    private String storeId;

    @Schema(description = "主体类型：PERSONAL/INDIVIDUAL/COMPANY")
    private String subjectType;

    @Schema(description = "企业身份类型：LEGAL/AUTHORIZED")
    private String companyIdentityType;

    @Schema(description = "个人/个体户姓名")
    private String realName;

    @Schema(description = "个人/个体户身份证号")
    private String idCardNo;

    @Schema(description = "法人姓名")
    private String legalName;

    @Schema(description = "法人身份证号")
    private String legalId;

    @Schema(description = "主体手机号")
    private String legalMobile;

    @Schema(description = "非法人授权书")
    private String authorizationUrl;

    @Schema(description = "库存预警数量")
    private Integer stockWarning;

    @Schema(description = "同城配送达达店铺编码")
    private String ddCode;

    @Schema(description = "退货收件人姓名")
    private String salesConsigneeName;

    @Schema(description = "退货收件人手机")
    private String salesConsigneeMobile;

    @Schema(description = "退货收件地址ID")
    private String salesConsigneeAddressId;

    @Schema(description = "退货收件地址名称")
    private String salesConsigneeAddressPath;

    @Schema(description = "退货收件详细地址")
    private String salesConsigneeDetail;

    @Schema(description = "发货人姓名")
    private String salesConsignorName;

    @Schema(description = "发货人手机")
    private String salesConsignorMobile;

    @Schema(description = "发货地址ID")
    private String salesConsignorAddressId;

    @Schema(description = "发货地址名称")
    private String salesConsignorAddressPath;

    @Schema(description = "发货详细地址")
    private String salesConsignorDetail;
}
