package cn.lili.modules.store.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 店铺结算资料
 *
 * @author OpenAI
 * @since 2026/6/26
 */
@Data
@TableName("li_store_settlement_profile")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "店铺结算资料")
public class StoreSettlementProfile extends BaseEntity {

    @Schema(description = "店铺ID")
    private String storeId;

    @Schema(description = "结算银行开户名")
    private String bankAccountName;

    @Schema(description = "结算银行账号")
    private String bankAccountNumber;

    @Schema(description = "开户支行")
    private String bankBranchName;

    @Schema(description = "联行号")
    private String bankJointCode;

    @Schema(description = "结算周期")
    private String settlementCycle;

    @Schema(description = "结算日")
    private Date settlementDay;
}
