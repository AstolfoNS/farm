package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class PlotExpandOptionVO implements Serializable {

  private Long soilTypeId;
  private String soilName;
  private Integer soilBitCode;
  private Short soilLevel;
  private String coverImageUrl;
  private String description;
  private Long unlockExperienceRequired;
  private Long expandCostCoin;
  private Boolean unlockableByExperience;
  private Boolean unlockableByCoin;
  private Boolean expandable;
}
