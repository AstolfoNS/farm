package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserPlotAllocationUpdateDTO implements Serializable {

    private Long id;

    @NotNull(message = "userId is required")
    @Positive(message = "userId must be > 0")
    private Long userId;

    private Boolean active;

    private Integer totalPlotCount;

    private Integer unlockedPlotCount;

    private Long defaultPlotTypeId;

    private String lockRuleCode;

    private String lockReason;

    private String allocationRuleJson;
}
