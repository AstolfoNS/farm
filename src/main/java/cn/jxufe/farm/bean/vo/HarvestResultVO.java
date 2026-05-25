package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class HarvestResultVO implements Serializable {

    private Long userId;
    private Long plotId;
    private Long cropId;
    private Long seedTypeId;
    private Long baseHarvestFruitNumber;
    private Long bugPenaltyPerBug;
    private Long totalBugPenaltyFruit;
    private Long harvestFruitNumber;
    private Long totalFruitQuantity;
    private Long experienceGain;
    private Long scoreGain;
    private Long currentExperience;
    private Long currentScore;
    private Short bugCountBefore;
    private Short bugCountAfter;
    private Boolean cropCleared;
    private Short nextGrowStatus;
    private Short nextStageIndex;
    private OffsetDateTime nextExpectedRipeAt;
    private OffsetDateTime nextExpectedWitheredAt;
}
