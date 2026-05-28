package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserPlotAllocationSaveDTO implements Serializable {

    private Long id;

    @NotNull(message = "userId不能为空")
    @Positive(message = "userId必须大于0")
    private Long userId;

    private Boolean active;

    private Integer totalPlotCount;

    private Integer unlockedPlotCount;

    private Integer lockedPlotCount;

    private Long defaultPlotTypeId;

    private String lockRuleCode;

    private String lockReason;

    private String allocationRuleJson;
}

