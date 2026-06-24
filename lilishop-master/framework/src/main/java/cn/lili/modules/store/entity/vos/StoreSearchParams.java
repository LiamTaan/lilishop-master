package cn.lili.modules.store.entity.vos;

import cn.hutool.core.date.DateUtil;
import cn.lili.common.utils.StringUtils;
import cn.lili.common.vo.PageVO;
import cn.lili.modules.store.entity.enums.StoreStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 店铺搜索参数VO
 *
 * @author pikachu
 * @since 2020-03-07 17:02:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StoreSearchParams extends PageVO implements Serializable {

    private static final long serialVersionUID = 6916054310764833369L;

    @Schema(description = "客户名称")
    private String memberName;

    @Schema(description = "店铺名称")
    private String storeName;
    /**
     * @see StoreStatusEnum
     */
    @Schema(description = "店铺状态")
    private String storeDisable;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "入驻业务类型")
    private String bizType;

    @Schema(description = "代理等级")
    private String agentLevel;

    @Schema(description = "代理区域ID")
    private String regionId;

    @Schema(description = "是否查询全部入驻记录")
    private Boolean includeAll;

    @Schema(description = "开始时间")
    private String startDate;

    @Schema(description = "结束时间")
    private String endDate;

    public <T> QueryWrapper<T> queryWrapper() {
        return this.queryWrapper(null);
    }

    public <T> QueryWrapper<T> queryWrapper(String tableAlias) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        String prefix = StringUtils.isNotEmpty(tableAlias) ? tableAlias + "." : "";
        if (StringUtils.isNotEmpty(storeName)) {
            queryWrapper.like(prefix + "store_name", storeName);
        }
        if (StringUtils.isNotEmpty(memberName)) {
            queryWrapper.like(prefix + "member_name", memberName);
        }
        if (StringUtils.isNotEmpty(storeDisable)) {
            queryWrapper.eq(prefix + "store_disable", storeDisable);
        } else if (StringUtils.isNotEmpty(auditStatus)) {
            queryWrapper.eq(prefix + "audit_status", auditStatus);
        } else if (!Boolean.TRUE.equals(includeAll)) {
            queryWrapper.and(wrapper -> wrapper.eq(prefix + "store_disable", StoreStatusEnum.OPEN.name()).or().eq(prefix + "store_disable", StoreStatusEnum.CLOSED.name()));
        }
        if (StringUtils.isNotEmpty(bizType)) {
            queryWrapper.eq(prefix + "store_type", bizType);
        }
        if (StringUtils.isNotEmpty(agentLevel)) {
            queryWrapper.eq(prefix + "agent_level", agentLevel);
        }
        if (StringUtils.isNotEmpty(regionId)) {
            queryWrapper.eq(prefix + "agent_region_id", regionId);
        }
        //按时间查询
        if (StringUtils.isNotEmpty(startDate)) {
            queryWrapper.ge(prefix + "create_time", DateUtil.parse(startDate));
        }
        if (StringUtils.isNotEmpty(endDate)) {
            queryWrapper.le(prefix + "create_time", DateUtil.parse(endDate));
        }
        return queryWrapper;
    }
}
