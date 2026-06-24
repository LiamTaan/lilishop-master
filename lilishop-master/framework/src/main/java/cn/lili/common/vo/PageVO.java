package cn.lili.common.vo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询参数
 *
 * @author Chopper
 */
@Data
public class PageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码，从 1 开始。", example = "1")
    private Integer pageNumber = 1;

    @Schema(description = "每页条数。", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "排序字段。默认按驼峰字段自动转为下划线字段名。", example = "createTime")
    private String sort;

    @Schema(description = "排序方向，传 asc 或 desc。", example = "desc")
    private String order;

    @Schema(description = "是否关闭排序字段的驼峰转下划线。true 表示按原字段名直接排序。", example = "false")
    private Boolean notConvert;

    public String getSort() {
        if (CharSequenceUtil.isNotEmpty(sort)) {
            if (notConvert == null || Boolean.FALSE.equals(notConvert)) {
                return StringUtils.camel2Underline(sort);
            } else {
                return sort;
            }
        }
        return sort;
    }

}
