package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PlotExpandOptionsVO implements Serializable {

    private Long userId;
    private Long currentExperience;
    private Long currentCoin;
    private Integer currentTotalPlots;
    private Short nextPlotIndex;
    private Long expandCostCoin;
    private List<PlotExpandOptionVO> options;
}
