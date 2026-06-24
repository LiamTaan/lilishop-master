package cn.lili.modules.verification.entity.params;

import cn.hutool.core.date.DateUtil;
import cn.lili.common.utils.StringUtils;
import cn.lili.common.vo.PageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 核销记录查询参数
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VerificationRecordSearchParams extends PageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "会员ID")
    private String memberId;

    @Schema(description = "会员名称")
    private String memberName;

    @Schema(description = "核销码")
    private String verificationCode;

    @Schema(description = "核销人ID")
    private String operatorId;

    @Schema(description = "核销人名称")
    private String operatorName;

    @Schema(description = "核销来源")
    private String sourceType;

    @Schema(description = "核销结果")
    private String resultType;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    public Date getStartDateTime() {
        if (StringUtils.isEmpty(startDate)) {
            return null;
        }
        return DateUtil.parse(startDate);
    }

    public Date getEndDateTime() {
        if (StringUtils.isEmpty(endDate)) {
            return null;
        }
        return DateUtil.endOfDay(DateUtil.parse(endDate));
    }
}
