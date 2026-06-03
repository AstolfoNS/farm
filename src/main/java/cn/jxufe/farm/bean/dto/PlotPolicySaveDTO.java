package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class PlotPolicySaveDTO implements Serializable {

    private Long id;

    @NotBlank(message = "policyName is required")
    private String policyName;

    private String policyVersion;

    private Boolean active;

    private String effectiveScope;

    private String publishStatus;

    @Min(value = 1, message = "defaultTotalPlotCount must be > 0")
    private Integer defaultTotalPlotCount;

    @Min(value = 0, message = "defaultUnlockedPlotCount must be >= 0")
    private Integer defaultUnlockedPlotCount;

    @Min(value = 0, message = "defaultLockedPlotCount must be >= 0")
    private Integer defaultLockedPlotCount;

    private String defaultLockRuleCode;

    private String defaultLockReason;
}
