package cn.jxufe.farm.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "地块经营流水分页查询参数")
public class PlotTradeQueryDTO implements Serializable {

    @Schema(description = "用户ID", example = "1")
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须大于0")
    private Long userId;

    @Schema(
            description = "业务类型筛选，支持 UNLOCK_PLOT / EXPAND_PLOT，也支持别名 UNLOCK / EXPAND",
            example = "UNLOCK"
    )
    private String bizType;

    @Schema(description = "页码，从1开始", example = "1")
    @Min(value = 1, message = "page最小为1")
    private Integer page = 1;

    @Schema(description = "每页条数，最大100", example = "10")
    @Min(value = 1, message = "rows最小为1")
    @Max(value = 100, message = "rows最大为100")
    private Integer rows = 10;
}
