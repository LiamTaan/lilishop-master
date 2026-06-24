package cn.lili.modules.store.entity.dto;

import cn.lili.common.validation.Phone;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 店铺修改DTO
 *
 * @author pikachu
 * @since 2020-08-22 15:10:51
 */
@Data
public class StoreEditDTO {


    @Schema(description = "唯一标识", hidden = true)
    private String id;

    @NotBlank(message = "店铺不能为空")
    @Schema(description = "店铺id")
    private String storeId;

    @Schema(description = "申请主体类型")
    private String applyType;

    @Schema(description = "店铺类型")
    private String storeType;

    @Schema(description = "代理等级")
    private String agentLevel;

    @Schema(description = "代理区域ID")
    private String agentRegionId;

    @Schema(description = "代理区域名称")
    private String agentRegionName;

    @Size(min = 2, max = 200, message = "店铺名称长度为2-200位")
    @Schema(description = "店铺名称")
    private String storeName;

    @Size(min = 2, max = 100, message = "公司名称错误")
    @Schema(description = "公司名称")
    private String companyName;

    @NotBlank(message = "公司地址不能为空")
    @Size(min = 1, max = 200, message = "公司地址,长度为1-200字符")
    @Schema(description = "公司地址")
    private String companyAddress;

    @Schema(description = "公司地址地区Id")
    private String companyAddressIdPath;

    @Schema(description = "公司地址地区")
    private String companyAddressPath;

    @Schema(description = "公司电话")
    private String companyPhone;

    @Schema(description = "电子邮箱")
    private String companyEmail;

    @Min(value = 1, message = "员工总数,至少一位")
    @Schema(description = "员工总数")
    private Integer employeeNum;

    @Min(value = 1, message = "注册资金,至少一位")
    @Schema(description = "注册资金")
    private Double registeredCapital;

    @Length(min = 2, max = 20, message = "联系人长度为：2-20位字符")
    @Schema(description = "联系人姓名")
    private String linkName;

    @Phone
    @Schema(description = "联系人电话")
    private String linkPhone;

    @Size(min = 18, max = 18, message = "营业执照长度为18位字符")
    @Schema(description = "营业执照号")
    private String licenseNum;

    @Schema(description = "法定经营范围")
    private String scope;

    @Schema(description = "营业执照电子版")
    private String licencePhoto;

    @Size(min = 2, max = 20, message = "法人姓名长度为2-20位字符")
    @Schema(description = "法人姓名")
    private String legalName;

    @Size(min = 18, max = 18, message = "法人身份证号长度为18位")
    @Schema(description = "法人身份证")
    private String legalId;

    @Schema(description = "法人身份证照片")
    private String legalPhoto;

    @Schema(description = "个人主体姓名")
    private String realName;

    @Schema(description = "个人身份证号")
    private String idCardNo;

    @Schema(description = "身份证正面")
    private String idCardFrontUrl;

    @Schema(description = "身份证反面")
    private String idCardBackUrl;

    @Schema(description = "法人手机号")
    private String legalMobile;

    @Schema(description = "营业执照图片")
    private String businessLicenseUrl;

    @Schema(description = "统一社会信用代码")
    private String creditCode;

    @Schema(description = "营业执照地区ID")
    private String businessLicenseRegionId;

    @Schema(description = "营业执照有效期类型")
    private String businessLicenseExpireType;

    @Schema(description = "营业执照到期时间")
    private String businessLicenseExpireDate;

    @Schema(description = "门头照")
    private String facadeImageUrl;

    @Schema(description = "店内照")
    private String indoorImageUrls;

    @Schema(description = "店铺分类ID")
    private String storeCategoryId;

    @Schema(description = "营业开始时间")
    private String businessHoursStart;

    @Schema(description = "营业结束时间")
    private String businessHoursEnd;

    @Schema(description = "非法人授权书")
    private String authorizationUrl;

    @Size(min = 1, max = 200, message = "结算银行开户行名称长度为1-200位")
    @Schema(description = "结算银行开户行名称")
    private String settlementBankAccountName;

    @Size(min = 1, max = 200, message = "结算银行开户账号长度为1-200位")
    @Schema(description = "结算银行开户账号")
    private String settlementBankAccountNum;

    @Size(min = 1, max = 200, message = "结算银行开户支行名称长度为1-200位")
    @Schema(description = "结算银行开户支行名称")
    private String settlementBankBranchName;

    @Size(min = 1, max = 50, message = "结算银行支行联行号长度为1-200位")
    @Schema(description = "结算银行支行联行号")
    private String settlementBankJointName;

    @NotBlank(message = "店铺经营类目不能为空")
    @Schema(description = "店铺经营类目")
    private String goodsManagementCategory;

    @Schema(description = "结算周期")
    private String settlementCycle;


    @Schema(description = "库存预警数量")
    private Integer stockWarning;

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

    @Schema(description = "店铺状态")
    private String storeDisable;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "是否自营", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean selfOperated;

    @Schema(description = "经纬度")
    private String storeCenter;

    @Schema(description = "店铺logo")
    private String storeLogo;

    @Size(min = 6, max = 200, message = "店铺简介个数需要在6-200位")
    @NotBlank(message = "店铺简介不能为空")
    @Schema(description = "店铺简介")
    private String storeDesc;

    @Schema(description = "地址名称， '，'分割")
    private String storeAddressPath;

    @Schema(description = "地址id，'，'分割 ")
    private String storeAddressIdPath;

    @Schema(description = "详细地址")
    private String storeAddressDetail;

    @Schema(description = "腾讯云智服唯一标识")
    private String yzfSign;

    @Schema(description = "腾讯云智服小程序唯一标识")
    private String yzfMpSign;

    public String getSalesConsigneeName() {
        return getDefaultValue(salesConsigneeName);
    }

    public String getSalesConsigneeMobile() {
        return getDefaultValue(salesConsigneeMobile);
    }

    public String getSalesConsigneeAddressId() {
        return getDefaultValue(salesConsigneeAddressId);
    }

    public String getSalesConsigneeAddressPath() {
        return getDefaultValue(salesConsigneeAddressPath);
    }

    public String getSalesConsigneeDetail() {
        return getDefaultValue(salesConsigneeDetail);
    }

    public String getYzfSign() {
        return getDefaultValue(yzfSign);
    }

    public String getYzfMpSign() {
        return getDefaultValue(yzfMpSign);
    }

    /**
     * JSON转换中的null 会转成 "null"
     * @param value
     * @return
     */
    private String getDefaultValue(String value){
        return (value == null || "null".equals(value)) ? "" : value;
    }

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
