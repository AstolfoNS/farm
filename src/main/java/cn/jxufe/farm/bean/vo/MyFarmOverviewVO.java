package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
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
}
