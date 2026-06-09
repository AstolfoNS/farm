package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SoilTypeGridVO implements Serializable {

  private Long id;
  private String name;
  private Integer bitCode;
  private String coverImageUrl;
  private Short level;
  private Long unlockExperienceRequired;
  private Long expandCostCoin;
  private String growSpeedMultiplier;
  private String description;
}
