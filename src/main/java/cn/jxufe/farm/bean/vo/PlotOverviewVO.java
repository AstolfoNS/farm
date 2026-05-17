package cn.jxufe.farm.bean.vo;

import java.io.Serializable;

public class PlotOverviewVO implements Serializable {

    private Long plotId;
    private Long userId;
    private Short plotIndex;
    private Boolean locked;
    private String lockReason;
    private Long soilTypeId;
    private Integer soilBitCode;
    private String soilName;
    private Boolean hasCrop;
    private Boolean occupied;
    private Boolean plantable;
    private Long unlockCostCoin;
    private Boolean canUnlock;
    private CropOverviewVO crop;

    public Long getPlotId() {
        return plotId;
    }

    public void setPlotId(Long plotId) {
        this.plotId = plotId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Short getPlotIndex() {
        return plotIndex;
    }

    public void setPlotIndex(Short plotIndex) {
        this.plotIndex = plotIndex;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public Long getSoilTypeId() {
        return soilTypeId;
    }

    public void setSoilTypeId(Long soilTypeId) {
        this.soilTypeId = soilTypeId;
    }

    public Integer getSoilBitCode() {
        return soilBitCode;
    }

    public void setSoilBitCode(Integer soilBitCode) {
        this.soilBitCode = soilBitCode;
    }

    public String getSoilName() {
        return soilName;
    }

    public void setSoilName(String soilName) {
        this.soilName = soilName;
    }

    public Boolean getHasCrop() {
        return hasCrop;
    }

    public void setHasCrop(Boolean hasCrop) {
        this.hasCrop = hasCrop;
    }

    public Boolean getOccupied() {
        return occupied;
    }

    public void setOccupied(Boolean occupied) {
        this.occupied = occupied;
    }

    public Boolean getPlantable() {
        return plantable;
    }

    public void setPlantable(Boolean plantable) {
        this.plantable = plantable;
    }

    public Long getUnlockCostCoin() {
        return unlockCostCoin;
    }

    public void setUnlockCostCoin(Long unlockCostCoin) {
        this.unlockCostCoin = unlockCostCoin;
    }

    public Boolean getCanUnlock() {
        return canUnlock;
    }

    public void setCanUnlock(Boolean canUnlock) {
        this.canUnlock = canUnlock;
    }

    public CropOverviewVO getCrop() {
        return crop;
    }

    public void setCrop(CropOverviewVO crop) {
        this.crop = crop;
    }
}
