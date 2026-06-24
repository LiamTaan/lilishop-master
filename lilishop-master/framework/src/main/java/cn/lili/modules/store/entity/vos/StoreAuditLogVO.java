package cn.lili.modules.store.entity.vos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 店铺审核历史视图
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
public class StoreAuditLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "审核日志ID")
    private String id;

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "审核前状态")
    private String fromAuditStatus;

    @Schema(description = "审核后状态")
    private String toAuditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "创建时间")
    private Date createTime;
}
