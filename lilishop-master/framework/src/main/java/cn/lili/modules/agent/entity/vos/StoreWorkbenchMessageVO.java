package cn.lili.modules.agent.entity.vos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 供货商工作台消息横幅
 *
 * @author dawn
 * @since 2026/7/2
 */
@Data
@Schema(description = "供货商工作台消息横幅")
public class StoreWorkbenchMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    private String id;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息业务分类")
    private String bizType;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "消息创建时间")
    private Date createTime;
}
