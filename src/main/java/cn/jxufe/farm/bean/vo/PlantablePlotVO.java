package cn.jxufe.farm.bean.vo;

import java.io.Serializable;

public class PlantablePlotVO implements Serializable {

    private Long plotId;
    private Short plotIndex;
    private Long soilTypeId;
    private Integer soilBitCode;
    private String soilName;

    public Long getPlotId() {
        return plotId;
    }

    public void setPlotId(Long plotId) {
        this.plotId = plotId;
    }

    public Short getPlotIndex() {
        return plotIndex;
    }

    public void setPlotIndex(Short plotIndex) {
        this.plotIndex = plotIndex;
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
}
