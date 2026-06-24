package cn.lili.modules.agent.entity.params;

import cn.lili.common.vo.PageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 代理商采购对账查询参数
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AgentProcurementReconciliationSearchParams extends PageVO {

    @Schema(description = "采购单号")
    private String orderSn;

    @Schema(description = "采购状态")
    private String status;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "创建开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startCreateTime;

    @Schema(description = "创建结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endCreateTime;
}
