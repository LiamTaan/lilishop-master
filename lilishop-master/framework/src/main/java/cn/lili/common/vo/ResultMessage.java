package cn.lili.common.vo;


import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 前后端交互VO
 *
 * @author Chopper
 */
@Data
@Schema(description = "统一响应包装。success/code/message 描述业务结果，result 承载实际业务数据。")
public class ResultMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功标志
     */
    @Schema(description = "业务是否成功。true 表示成功，false 表示失败。")
    private boolean success;

    /**
     * 消息
     */
    @Schema(description = "业务提示文案。失败时请优先结合 code 判断，不要只依赖 message。")
    private String message;

    /**
     * 返回代码
     */
    @Schema(description = "统一业务状态码。200 表示成功，其他值表示具体业务失败原因。", example = "200")
    private Integer code;

    /**
     * 时间戳
     */
    @Schema(description = "服务端生成的响应时间戳，毫秒。")
    private long timestamp = System.currentTimeMillis();

    /**
     * 结果对象
     */
    @Schema(description = "真实业务数据载荷。")
    private T result;
}
