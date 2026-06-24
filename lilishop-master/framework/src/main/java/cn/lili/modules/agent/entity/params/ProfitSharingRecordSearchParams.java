package cn.lili.modules.agent.entity.params;

import cn.lili.common.vo.PageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分账记录分页查询参数
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分账记录分页查询参数")
public class ProfitSharingRecordSearchParams extends PageVO {

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "账单状态")
    private String billStatus;

    @Schema(description = "结算状态，支持 PROCESSING / FINISHED")
    private String settlementStatus;

    @Schema(description = "关键字，支持账单号或店铺名称")
    private String keyword;
}
