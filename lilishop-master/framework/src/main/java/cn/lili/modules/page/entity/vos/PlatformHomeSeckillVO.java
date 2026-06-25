package cn.lili.modules.page.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 平台首页秒杀模块
 *
 * @author codex
 * @since 2026/6/25
 */
@Data
public class PlatformHomeSeckillVO {

    @Schema(description = "秒杀时段")
    private Integer timeLine;

    @Schema(description = "开始时间戳")
    private Long startTime;

    @Schema(description = "距离开始秒数")
    private Long distanceStartTime;

    @Schema(description = "秒杀商品列表")
    private List<PlatformHomeGoodsCardVO> goodsList;
}
