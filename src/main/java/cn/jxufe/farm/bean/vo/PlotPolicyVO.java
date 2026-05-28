package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlotPolicyVO implements Serializable {

    private Long id;
    private String policyName;
    private Boolean active;
    private Short defaultTotalPlotCount;
    private Short defaultUnlockedPlotCount;
    private Short defaultLockedPlotCount;
    private Long defaultPlotTypeId;
    private String defaultPlotTypeName;
    private String defaultLockRuleCode;
    private String defaultLockReason;
    private String allocationRuleJson;
}

