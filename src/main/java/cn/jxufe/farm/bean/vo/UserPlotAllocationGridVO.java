package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class UserPlotAllocationGridVO implements Serializable {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private Boolean active;
    private Short totalPlotCount;
    private Short unlockedPlotCount;
    private Short lockedPlotCount;
    private Long defaultPlotTypeId;
    private String defaultPlotTypeName;
    private String lockRuleCode;
    private String lockReason;
    private String allocationRuleJson;
    private OffsetDateTime appliedAt;

    private Integer currentTotalPlots;
    private Integer currentUnlockedPlots;
}
