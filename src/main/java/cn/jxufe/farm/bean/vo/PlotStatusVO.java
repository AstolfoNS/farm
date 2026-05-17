package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PlotStatusVO implements Serializable {

    private Long userId;
    private OffsetDateTime serverTime;
    private Integer totalPlots;
    private Integer unlockedPlots;
    private Integer lockedPlots;
    private Integer occupiedPlots;
    private Integer emptyUnlockedPlots;
    private Integer harvestablePlots;
    private Long nextExpandCostCoin;
    private Long nextUnlockPlotId;
    private Short nextUnlockPlotIndex;
    private List<PlotOverviewVO> plots;
}
