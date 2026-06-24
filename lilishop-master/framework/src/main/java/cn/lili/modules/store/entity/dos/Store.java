package cn.lili.modules.store.entity.dos;

import cn.lili.common.utils.BeanUtil;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.store.entity.dto.AdminStoreApplyDTO;
import cn.lili.modules.store.entity.enums.StoreAuditStatusEnum;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 店铺
 *
 * @author pikachu
 * @since 2020-02-18 15:18:56
 */
@Data
@TableName("li_store")
@Schema(description = "店铺")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Store extends BaseEntity {

    private static final long serialVersionUID = -5861767726387892272L;

    @Schema(description = "客户Id")
    private String memberId;

    @Schema(description = "客户名称")
    private String memberName;

    @Schema(description = "店铺名称")
    private String storeName;

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

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "店铺关闭时间")
    private Date storeEndTime;

    /**
     * @see StoreStatusEnum
     */
    @Schema(description = "店铺状态")
    private String storeDisable;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "是否自营")
    private Boolean selfOperated;

    @Schema(description = "店铺logo")
    private String storeLogo;

    @Schema(description = "经纬度")
    @NotEmpty
    private String storeCenter;

    @Size(min = 6, max = 200, message = "店铺简介需在6-200字符之间")
    @NotBlank(message = "店铺简介不能为空")
    @Schema(description = "店铺简介")
    private String storeDesc;

    @Schema(description = "地址名称， '，'分割")
    private String storeAddressPath;

    @Schema(description = "地址id，'，'分割 ")
    private String storeAddressIdPath;

    @Schema(description = "详细地址")
    private String storeAddressDetail;

    @Schema(description = "描述评分")
    private Double descriptionScore;

    @Schema(description = "服务评分")
    private Double serviceScore;

    @Schema(description = "物流评分")
    private Double deliveryScore;

    @Schema(description = "商品数量")
    private Integer goodsNum;

    @Schema(description = "收藏数量")
    private Integer collectionNum;

    @Schema(description = "腾讯云智服唯一标识")
    private String yzfSign;

    @Schema(description = "腾讯云智服小程序唯一标识")
    private String yzfMpSign;

    @Schema(description = "udesk IM标识")
    private String merchantEuid;

    public Boolean getPageShow() {
        if (pageShow == null) {
            return false;
        }
        return pageShow;
    }

    public Boolean getSelfPickFlag() {
        if (selfPickFlag == null) {
            return false;
        }
        return selfPickFlag;
    }

    @Schema(description = "默认页面是否开启")
    private Boolean pageShow;

    @Schema(description = "是否开启自提")
    private Boolean selfPickFlag;

    public Store(Member member) {
        this.memberId = member.getId();
        this.memberName = member.getUsername();
        storeDisable = StoreStatusEnum.APPLY.value();
        auditStatus = StoreAuditStatusEnum.DRAFT.name();
        selfOperated = false;
        deliveryScore = 5.0;
        serviceScore = 5.0;
        descriptionScore = 5.0;
        goodsNum = 0;
        collectionNum = 0;
        this.selfPickFlag = false;
        this.pageShow = false;
    }

    public Store(Member member, AdminStoreApplyDTO adminStoreApplyDTO) {
        BeanUtil.copyProperties(adminStoreApplyDTO, this);

        this.memberId = member.getId();
        this.memberName = member.getUsername();
        storeDisable = StoreStatusEnum.APPLYING.value();
        auditStatus = StoreAuditStatusEnum.SUBMITTED.name();
        selfOperated = false;
        deliveryScore = 5.0;
        serviceScore = 5.0;
        descriptionScore = 5.0;
        goodsNum = 0;
        collectionNum = 0;
        this.selfPickFlag = false;
        this.pageShow = false;
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
