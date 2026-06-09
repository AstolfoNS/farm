package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.Data;

@Data
public class SoilTypeSaveDTO implements Serializable {

  private Long id;

  @NotBlank(message = "name is required")
  private String name;

  private Integer bitCode;

  private String coverImageUrl;

  private Short level;

  private Long unlockExperienceRequired;

  private Long expandCostCoin;

  private String growSpeedMultiplier;

  private String description;
}
