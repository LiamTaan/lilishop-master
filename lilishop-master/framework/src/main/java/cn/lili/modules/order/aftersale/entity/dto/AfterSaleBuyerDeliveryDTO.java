package cn.lili.modules.order.aftersale.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class AfterSaleBuyerDeliveryDTO {

    @NotBlank(message = "发货单号不能为空")
    @Schema(description = "发货单号")
    private String logisticsNo;

    @NotBlank(message = "请选择物流公司")
    @Schema(description = "物流公司ID")
    private String logisticsId;

    @NotNull(message = "请选择发货时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "买家发货时间")
    private Date mDeliverTime;
}
