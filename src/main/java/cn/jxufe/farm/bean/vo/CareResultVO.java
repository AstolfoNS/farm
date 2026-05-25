package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
public class CareResultVO implements Serializable {

    private Long userId;
    private Long plotId;
    private Long cropId;
    private Long seedTypeId;
    private Short bugCountBefore;
    private Short bugCountAfter;
    private Short bugRemovedCount;
    private Long coinGain;
    private Long experienceGain;
    private Long scoreGain;
    private Long currentCoin;
    private Long currentExperience;
    private Long currentScore;
    private Short currentStageIndex;
    private Short growStatus;
    private OffsetDateTime lastCareAt;
}
