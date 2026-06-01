package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class PlotOverviewVO implements Serializable {

    private Long plotId;
    private Long userId;
    private Short plotIndex;
    private Boolean locked;
    private String lockReason;
    private String lockSource;
    private String lockRuleCode;
    private Long plotTypeId;
    private String plotTypeName;
    private Long soilTypeId;
    private Integer soilBitCode;
    private String soilName;
    private String soilCoverImageUrl;
    private Boolean hasCrop;
    private Boolean occupied;
    private Boolean plantable;
    private Long unlockCostCoin;
    private Long unlockRequiredExperience;
    private Boolean unlockableByExperience;
    private Boolean unlockableByCoin;
    private Boolean canUnlock;
    private CropOverviewVO crop;
}
