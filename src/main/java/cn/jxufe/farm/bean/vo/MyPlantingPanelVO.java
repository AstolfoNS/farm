package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

public class MyPlantingPanelVO implements Serializable {

    private Long userId;
    private OffsetDateTime serverTime;
    private Integer totalPlotCount;
    private Integer unlockedPlotCount;
    private Integer plantablePlotCount;
    private Integer backpackSeedTypeCount;
    private Integer selectableSeedTypeCount;
    private List<SeedBackpackItemVO> seeds;

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

    public Integer getTotalPlotCount() {
        return totalPlotCount;
    }

    public void setTotalPlotCount(Integer totalPlotCount) {
        this.totalPlotCount = totalPlotCount;
    }

    public Integer getUnlockedPlotCount() {
        return unlockedPlotCount;
    }

    public void setUnlockedPlotCount(Integer unlockedPlotCount) {
        this.unlockedPlotCount = unlockedPlotCount;
    }

    public Integer getPlantablePlotCount() {
        return plantablePlotCount;
    }

    public void setPlantablePlotCount(Integer plantablePlotCount) {
        this.plantablePlotCount = plantablePlotCount;
    }

    public Integer getBackpackSeedTypeCount() {
        return backpackSeedTypeCount;
    }

    public void setBackpackSeedTypeCount(Integer backpackSeedTypeCount) {
        this.backpackSeedTypeCount = backpackSeedTypeCount;
    }

    public Integer getSelectableSeedTypeCount() {
        return selectableSeedTypeCount;
    }

    public void setSelectableSeedTypeCount(Integer selectableSeedTypeCount) {
        this.selectableSeedTypeCount = selectableSeedTypeCount;
    }

    public List<SeedBackpackItemVO> getSeeds() {
        return seeds;
    }

    public void setSeeds(List<SeedBackpackItemVO> seeds) {
        this.seeds = seeds;
    }
}
