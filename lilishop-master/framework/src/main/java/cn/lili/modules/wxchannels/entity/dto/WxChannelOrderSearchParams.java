package cn.lili.modules.wxchannels.entity.dto;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.wxchannels.entity.dos.WxChannelOrder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "视频号订单分页查询参数")
public class WxChannelOrderSearchParams extends PageVO {

    @Schema(description = "视频号订单编号，精确匹配", example = "CH202406220001")
    private String channelOrderSn;

    @Schema(description = "客户昵称，精确匹配", example = "小王同学")
    private String memberNickName;

    @Schema(description = "商品名称，支持模糊查询", example = "吊干杏")
    private String goodsName;

    @Schema(description = "微信视频号订单状态，按微信侧原始状态值筛选", example = "PAID")
    private String status;

    @Schema(
            description = "下单场景：LIVE=直播间下单，WINDOW=橱窗下单",
            allowableValues = {"LIVE", "WINDOW"},
            example = "LIVE"
    )
    private String scene;

    @Schema(description = "开始时间，13 位时间戳（毫秒）", example = "1718985600000")
    private Long startTime;

    @Schema(description = "结束时间，13 位时间戳（毫秒）", example = "1719071999000")
    private Long endTime;

    public QueryWrapper<WxChannelOrder> queryWrapper() {
        QueryWrapper<WxChannelOrder> wrapper = new QueryWrapper<>();
        wrapper.eq(CharSequenceUtil.isNotEmpty(channelOrderSn), "channel_order_sn", channelOrderSn);
        wrapper.eq(CharSequenceUtil.isNotEmpty(memberNickName), "member_nick_name", memberNickName);
        wrapper.like(CharSequenceUtil.isNotEmpty(goodsName), "goods_name", goodsName);
        wrapper.eq(CharSequenceUtil.isNotEmpty(status), "status", status);
        wrapper.eq(CharSequenceUtil.isNotEmpty(scene), "scene", scene);
        if (startTime != null) {
            wrapper.ge("create_time", new java.util.Date(startTime));
        }
        if (endTime != null) {
            wrapper.le("create_time", new java.util.Date(endTime));
        }
        wrapper.eq("delete_flag", false);
        return wrapper;
    }
}
