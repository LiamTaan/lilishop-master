package cn.lili.modules.wxchannels.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "微信视频号小店配置")
@Data
public class WxChannelsSetting {
    @Schema(description = "视频号小店 AppId", example = "wx1234567890abcdef")
    private String appId;
    @Schema(description = "视频号小店 AppSecret", example = "8f0b1c2d3e4f5a6b7c8d9e0f11223344")
    private String appSecret;
    @Schema(description = "视频号小店接口基础地址", example = "https://api.weixin.qq.com/minishop")
    private String apiBase;
    @Schema(description = "获取 access_token 地址", example = "https://api.weixin.qq.com/cgi-bin/token")
    private String tokenUrl;
}
