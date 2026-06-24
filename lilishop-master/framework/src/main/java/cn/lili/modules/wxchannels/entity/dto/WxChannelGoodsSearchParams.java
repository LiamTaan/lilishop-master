package cn.lili.modules.wxchannels.entity.dto;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.lili.modules.wxchannels.entity.dos.WxChannelGoods;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "视频号商品分页查询参数")
public class WxChannelGoodsSearchParams extends cn.lili.common.vo.PageVO {

    @Schema(description = "商品名称，支持模糊查询", example = "吊干杏")
    private String goodsName;

    @Schema(
            description = "审核状态：APPROVED=审核通过，PENDING=待审核，REJECTED=审核驳回",
            allowableValues = {"APPROVED", "PENDING", "REJECTED"},
            example = "APPROVED"
    )
    private String status;

    public QueryWrapper<WxChannelGoods> queryWrapper() {
        QueryWrapper<WxChannelGoods> wrapper = new QueryWrapper<>();
        wrapper.like(CharSequenceUtil.isNotEmpty(goodsName), "goods_name", goodsName);
        wrapper.eq(CharSequenceUtil.isNotEmpty(status), "status", status);
        wrapper.eq("delete_flag", false);
        return wrapper;
    }
}
