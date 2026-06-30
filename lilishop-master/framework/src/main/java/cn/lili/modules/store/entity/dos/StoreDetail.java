package cn.lili.modules.store.entity.dos;

import cn.lili.mybatis.BaseIdEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 店铺详细
 *
 * @author pikachu
 * @since 2020-02-18 15:18:56
 */
@Data
@TableName("li_store_detail")
@Schema(description = "店铺详细")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StoreDetail extends BaseIdEntity {

    private static final long serialVersionUID = 4949782642253898816L;

    @NotBlank(message = "店铺不能为空")
    @Schema(description = "店铺id")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "店铺logo")
    private String storeLogo;

    @Schema(description = "店铺简介")
    private String storeDesc;

    @Schema(description = "经纬度")
    private String storeCenter;

    @Schema(description = "地址名称， '，'分割")
    private String storeAddressPath;

    @Schema(description = "地址id，'，'分割")
    private String storeAddressIdPath;

    @Schema(description = "详细地址")
    private String storeAddressDetail;

    @Schema(description = "主体类型")
    private String subjectType;

    @Schema(description = "企业身份类型")
    private String companyIdentityType;

    @Schema(description = "店铺类型")
    private String storeType;

    @Schema(description = "代理等级")
    private String agentLevel;

    @Schema(description = "代理区域ID")
    private String agentRegionId;

    @Schema(description = "代理区域名称")
    private String agentRegionName;

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

    @Schema(description = "法人姓名")
    private String legalName;

    @Schema(description = "法人身份证")
    private String legalId;

    @Schema(description = "个人主体姓名")
    private String realName;

    @Schema(description = "个人身份证号")
    private String idCardNo;

    @Schema(description = "法人手机号")
    private String legalMobile;

    @Schema(description = "企业非法人授权书")
    private String authorizationUrl;

    @Schema(description = "店铺经营类目")
    private String goodsManagementCategory;

    @Schema(description = "库存预警数量")
    private Integer stockWarning;

    /**
     * 同城配送达达店铺编码
     */
    @Schema(description = "同城配送达达店铺编码")
    @TableField(value = "dd_code")
    private String ddCode;

    //店铺退货收件地址

    @Schema(description = "收货人姓名")
    private String salesConsigneeName;

    @Schema(description = "收件人手机")
    private String salesConsigneeMobile;

    @Schema(description = "地址Id， '，'分割")
    private String salesConsigneeAddressId;

    @Schema(description = "地址名称， '，'分割")
    private String salesConsigneeAddressPath;

    @Schema(description = "详细地址")
    private String salesConsigneeDetail;


    //店铺发货地址
    @Schema(description = "发货人姓名")
    private String salesConsignorName;

    @Schema(description = "发件人手机")
    private String salesConsignorMobile;

    @Schema(description = "发件人地址Id， '，'分割")
    private String salesConsignorAddressId;

    @Schema(description = "发件人地址名称， '，'分割")
    private String salesConsignorAddressPath;

    @Schema(description = "发件人详细地址")
    private String salesConsignorDetail;

    @Schema(description = "入驻业务类型")
    public String getBizType() {
        return this.storeType;
    }

    public void setBizType(String bizType) {
        this.storeType = bizType;
    }

    @Schema(description = "代理区域ID")
    public String getRegionId() {
        return this.agentRegionId;
    }

    public void setRegionId(String regionId) {
        this.agentRegionId = regionId;
    }

    @Schema(description = "代理区域名称")
    public String getRegionName() {
        return this.agentRegionName;
    }

    public void setRegionName(String regionName) {
        this.agentRegionName = regionName;
    }
}
