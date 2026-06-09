package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

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
