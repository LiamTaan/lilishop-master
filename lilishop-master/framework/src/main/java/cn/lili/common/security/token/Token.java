package cn.lili.common.security.token;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Token 实体类
 *
 * @author Chopper
 * @version v1.0
 * 2020-11-13 10:02
 */
@Data
@Schema(description = "登录成功后返回的 token 结构。")
public class Token {
    /**
     * 访问token
     */
    @Schema(description = "访问令牌。后续业务请求通常放在请求头 Authorization 中。")
    private String accessToken;

    /**
     * 刷新token
     */
    @Schema(description = "刷新令牌。accessToken 过期后可用来换取新的 token。")
    private String refreshToken;

}
