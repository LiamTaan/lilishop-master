package cn.lili.modules.store.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * 店铺入驻其他信息
 *
 * @author Bulbasaur
 * @since 2020/12/7 16:16
 */
@Data
public class StoreOtherInfoDTO {

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

    @Size(min = 2, max = 200)
    @NotBlank(message = "店铺名称不能为空")
    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "店铺logo")
    private String storeLogo;

    @Size(min = 6, max = 200)
    @NotBlank(message = "店铺简介不能为空")
    @Schema(description = "店铺简介")
    private String storeDesc;

    @Schema(description = "经纬度")
    @NotEmpty
    private String storeCenter;

    @NotBlank(message = "店铺经营类目不能为空")
    @Schema(description = "店铺经营类目")
    private String goodsManagementCategory;

    @NotBlank(message = "地址不能为空")
    @Schema(description = "地址名称， '，'分割")
    private String storeAddressPath;

    @NotBlank(message = "地址ID不能为空")
    @Schema(description = "地址id，'，'分割 ")
    private String storeAddressIdPath;

    @NotBlank(message = "地址详情")
    @Schema(description = "地址详情")
    private String storeAddressDetail;

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
