package cn.lili.modules.store.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺审核历史
 *
 * @author dawn
 * @since 2026/6/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("li_store_audit_log")
@Schema(description = "店铺审核历史")
public class StoreAuditLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

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
}
