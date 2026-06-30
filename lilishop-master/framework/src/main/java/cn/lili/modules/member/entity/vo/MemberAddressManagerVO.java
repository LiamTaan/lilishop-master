package cn.lili.modules.member.entity.vo;

import cn.lili.modules.member.entity.dos.MemberAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端客户地址列表 VO
 *
 * @author OpenAI
 * @since 2026/6/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberAddressManagerVO extends MemberAddress {

    @Schema(description = "所属用户昵称")
    private String memberNickName;

    @Schema(description = "所属用户名")
    private String memberUsername;
}
