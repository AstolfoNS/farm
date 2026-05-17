package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

public class MyFarmOverviewVO implements Serializable {

    private Long userId;
    private OffsetDateTime serverTime;
    private Integer totalPlots;
    private Integer unlockedPlots;
    private Integer lockedPlots;
    private Integer occupiedPlots;
    private Integer emptyUnlockedPlots;
    private Integer harvestableCount;
    private Long nextExpandCostCoin;
    private List<PlotOverviewVO> plots;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OffsetDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(OffsetDateTime serverTime) {
        this.serverTime = serverTime;
    }

    public Integer getTotalPlots() {
        return totalPlots;
    }

    public void setTotalPlots(Integer totalPlots) {
        this.totalPlots = totalPlots;
    }

    public Integer getHarvestableCount() {
        return harvestableCount;
    }

    public void setHarvestableCount(Integer harvestableCount) {
        this.harvestableCount = harvestableCount;
    }

    public Integer getUnlockedPlots() {
        return unlockedPlots;
    }

    public void setUnlockedPlots(Integer unlockedPlots) {
        this.unlockedPlots = unlockedPlots;
    }

    public Integer getLockedPlots() {
        return lockedPlots;
    }

    public void setLockedPlots(Integer lockedPlots) {
        this.lockedPlots = lockedPlots;
    }

    public Integer getOccupiedPlots() {
        return occupiedPlots;
    }

    public void setOccupiedPlots(Integer occupiedPlots) {
        this.occupiedPlots = occupiedPlots;
    }

    public Integer getEmptyUnlockedPlots() {
        return emptyUnlockedPlots;
    }

    public void setEmptyUnlockedPlots(Integer emptyUnlockedPlots) {
        this.emptyUnlockedPlots = emptyUnlockedPlots;
    }

    public Long getNextExpandCostCoin() {
        return nextExpandCostCoin;
    }

    public void setNextExpandCostCoin(Long nextExpandCostCoin) {
        this.nextExpandCostCoin = nextExpandCostCoin;
    }

    public List<PlotOverviewVO> getPlots() {
        return plots;
    }

    public void setPlots(List<PlotOverviewVO> plots) {
        this.plots = plots;
    }
}
