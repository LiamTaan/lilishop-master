package cn.lili.modules.permission.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 管理员自助资料修改参数
 *
 * @author dawn
 */
@Data
@Schema(description = "管理员自助资料修改参数")
public class AdminUserProfileDTO {

    @Schema(description = "头像")
    @Length(max = 1000, message = "头像地址长度不能超过1000")
    private String avatar;

    @Schema(description = "昵称")
    @Length(max = 10, message = "昵称长度不能超过10个字符")
    private String nickName;

    @Schema(description = "手机")
    @Length(max = 11, message = "手机号长度不能超过11")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Schema(description = "邮件")
    @Length(max = 255, message = "邮箱长度不能超过255")
    @Pattern(
            regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "邮箱格式不正确"
    )
    private String email;

    @Schema(description = "描述/详情/备注")
    @Length(max = 255, message = "简介长度不能超过255")
    private String description;
}
