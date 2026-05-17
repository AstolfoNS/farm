package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class HarvestResultVO implements Serializable {

    private Long userId;
    private Long plotId;
    private Long cropId;
    private Long seedTypeId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPlotId() {
        return plotId;
    }

    public void setPlotId(Long plotId) {
        this.plotId = plotId;
    }

    public Long getCropId() {
        return cropId;
    }

    public void setCropId(Long cropId) {
        this.cropId = cropId;
    }

    public Long getSeedTypeId() {
        return seedTypeId;
    }

    public void setSeedTypeId(Long seedTypeId) {
        this.seedTypeId = seedTypeId;
    }

    public Long getHarvestFruitNumber() {
        return harvestFruitNumber;
    }

    public void setHarvestFruitNumber(Long harvestFruitNumber) {
        this.harvestFruitNumber = harvestFruitNumber;
    }

    public Long getTotalFruitQuantity() {
        return totalFruitQuantity;
    }

    public void setTotalFruitQuantity(Long totalFruitQuantity) {
        this.totalFruitQuantity = totalFruitQuantity;
    }

    public Long getExperienceGain() {
        return experienceGain;
    }

    public void setExperienceGain(Long experienceGain) {
        this.experienceGain = experienceGain;
    }

    public Long getScoreGain() {
        return scoreGain;
    }

    public void setScoreGain(Long scoreGain) {
        this.scoreGain = scoreGain;
    }

    public Long getCurrentExperience() {
        return currentExperience;
    }

    public void setCurrentExperience(Long currentExperience) {
        this.currentExperience = currentExperience;
    }

    public Long getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(Long currentScore) {
        this.currentScore = currentScore;
    }

    public Short getBugCountBefore() {
        return bugCountBefore;
    }

    public void setBugCountBefore(Short bugCountBefore) {
        this.bugCountBefore = bugCountBefore;
    }

    public Short getBugCountAfter() {
        return bugCountAfter;
    }

    public void setBugCountAfter(Short bugCountAfter) {
        this.bugCountAfter = bugCountAfter;
    }

    public Boolean getCropCleared() {
        return cropCleared;
    }

    public void setCropCleared(Boolean cropCleared) {
        this.cropCleared = cropCleared;
    }

    public Short getNextGrowStatus() {
        return nextGrowStatus;
    }

    public void setNextGrowStatus(Short nextGrowStatus) {
        this.nextGrowStatus = nextGrowStatus;
    }

    public Short getNextStageIndex() {
        return nextStageIndex;
    }

    public void setNextStageIndex(Short nextStageIndex) {
        this.nextStageIndex = nextStageIndex;
    }

    public OffsetDateTime getNextExpectedRipeAt() {
        return nextExpectedRipeAt;
    }

    public void setNextExpectedRipeAt(OffsetDateTime nextExpectedRipeAt) {
        this.nextExpectedRipeAt = nextExpectedRipeAt;
    }

    public OffsetDateTime getNextExpectedWitheredAt() {
        return nextExpectedWitheredAt;
    }

    public void setNextExpectedWitheredAt(OffsetDateTime nextExpectedWitheredAt) {
        this.nextExpectedWitheredAt = nextExpectedWitheredAt;
    }
}
