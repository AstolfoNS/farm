package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class PlotExpandResultVO implements Serializable {

  private Long userId;
  private Long plotId;
  private Short plotIndex;
  private Long soilTypeId;
  private String soilName;
  private Long soilUnlockRequiredExperience;
  private Long currentExperience;
  private Long expandCostCoin;
  private Long beforeCoin;
  private Long afterCoin;
  private Integer totalPlots;
  private Integer unlockedPlots;
  private Integer lockedPlots;
}
