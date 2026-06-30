package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端会员地址新增/编辑入参
 *
 * @author OpenAI
 * @since 2026/6/29
 */
@Data
public class MemberAddressManagerSaveDTO {

    @Schema(description = "地址ID")
    private String id;

    @NotBlank(message = "所属用户不能为空")
    @Schema(description = "客户ID")
    private String memberId;

    @NotBlank(message = "收货人姓名不能为空")
    @Schema(description = "收货人姓名")
    private String name;

    @Schema(description = "手机号码，支持管理端回传脱敏值")
    private String mobile;

    @NotBlank(message = "地址不能为空")
    @Schema(description = "地址名称，','分割")
    private String consigneeAddressPath;

    @NotBlank(message = "地址不能为空")
    @Schema(description = "地址id，','分割")
    private String consigneeAddressIdPath;

    @NotBlank(message = "详细地址不能为空")
    @Schema(description = "详细地址")
    private String detail;

    @Schema(description = "是否为默认收货地址")
    private Boolean isDefault;

    @Schema(description = "地址别名")
    private String alias;

    @Schema(description = "经度")
    private String lon;

    @Schema(description = "纬度")
    private String lat;
}
