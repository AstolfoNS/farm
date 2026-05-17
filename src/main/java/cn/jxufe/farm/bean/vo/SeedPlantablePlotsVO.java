package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

public class SeedPlantablePlotsVO implements Serializable {

    private Long userId;
    private Long seedTypeId;
    private OffsetDateTime serverTime;
    private Integer plantableCount;
    private List<PlantablePlotVO> plots;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSeedTypeId() {
        return seedTypeId;
    }

    public void setSeedTypeId(Long seedTypeId) {
        this.seedTypeId = seedTypeId;
    }

    public OffsetDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(OffsetDateTime serverTime) {
        this.serverTime = serverTime;
    }

    public Integer getPlantableCount() {
        return plantableCount;
    }

    public void setPlantableCount(Integer plantableCount) {
        this.plantableCount = plantableCount;
    }

    public List<PlantablePlotVO> getPlots() {
        return plots;
    }

    public void setPlots(List<PlantablePlotVO> plots) {
        this.plots = plots;
    }
}
