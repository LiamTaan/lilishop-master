package cn.lili.modules.wxchannels.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("li_wx_channel_refund")
@Schema(description = "微信视频号退单")
public class WxChannelRefund extends BaseEntity {

    @Schema(description = "微信视频号退单编号", example = "RF202406220001")
    private String channelRefundSn;

    @Schema(description = "关联的视频号订单编号", example = "CH202406220001")
    private String channelOrderSn;

    @Schema(description = "客户 ID", example = "90001")
    private String memberId;

    @Schema(description = "客户昵称", example = "小王同学")
    private String memberNickName;

    @Schema(description = "退款金额，单位：元", example = "18.00")
    private Double amount;

    @Schema(description = "微信视频号退单状态，当前系统按微信侧原始状态值透传保存，联调时请以微信返回值为准", example = "SUCCESS")
    private String status;

    @Schema(
            description = "下单场景：LIVE=直播间下单，WINDOW=橱窗下单",
            allowableValues = {"LIVE", "WINDOW"},
            example = "WINDOW"
    )
    private String scene;
}
