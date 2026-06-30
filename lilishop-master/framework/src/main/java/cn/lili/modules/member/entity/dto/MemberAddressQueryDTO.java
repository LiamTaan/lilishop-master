package cn.lili.modules.member.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端客户地址查询参数
 *
 * @author OpenAI
 * @since 2026/6/29
 */
@Data
public class MemberAddressQueryDTO {

    @Schema(description = "地址关键字，匹配收货人/手机号/别名/详细地址")
    private String keyword;

    @Schema(description = "所属用户关键字，匹配昵称/用户名/手机号")
    private String memberKeyword;

    @Schema(description = "客户ID")
    private String memberId;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;
}
