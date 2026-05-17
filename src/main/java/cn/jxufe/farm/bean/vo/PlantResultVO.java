package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class PlantResultVO implements Serializable {

    private Long userId;
    private Long plotId;
    private Long cropId;
    private Long seedTypeId;
    private Long remainSeedQuantity;
    private Short growStatus;
    private Short currentStageIndex;
    private OffsetDateTime expectedRipeAt;
    private OffsetDateTime expectedWitheredAt;

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

    public Long getRemainSeedQuantity() {
        return remainSeedQuantity;
    }

    public void setRemainSeedQuantity(Long remainSeedQuantity) {
        this.remainSeedQuantity = remainSeedQuantity;
    }

    public Short getGrowStatus() {
        return growStatus;
    }

    public void setGrowStatus(Short growStatus) {
        this.growStatus = growStatus;
    }

    public Short getCurrentStageIndex() {
        return currentStageIndex;
    }

    public void setCurrentStageIndex(Short currentStageIndex) {
        this.currentStageIndex = currentStageIndex;
    }

    public OffsetDateTime getExpectedRipeAt() {
        return expectedRipeAt;
    }

    public void setExpectedRipeAt(OffsetDateTime expectedRipeAt) {
        this.expectedRipeAt = expectedRipeAt;
    }

    public OffsetDateTime getExpectedWitheredAt() {
        return expectedWitheredAt;
    }

    public void setExpectedWitheredAt(OffsetDateTime expectedWitheredAt) {
        this.expectedWitheredAt = expectedWitheredAt;
    }
}
