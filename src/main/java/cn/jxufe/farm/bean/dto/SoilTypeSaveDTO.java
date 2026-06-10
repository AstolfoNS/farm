package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import lombok.Data;

@Data
public class SoilTypeSaveDTO implements Serializable {

  @Positive(message = "id must be > 0")
  private Long id;

  @NotBlank(message = "name is required")
  private String name;

  @Positive(message = "bitCode must be > 0")
  private Integer bitCode;

  private String coverImageUrl;

  @Positive(message = "level must be > 0")
  private Short level;

  @PositiveOrZero(message = "unlockExperienceRequired must be >= 0")
  private Long unlockExperienceRequired;

  @PositiveOrZero(message = "expandCostCoin must be >= 0")
  private Long expandCostCoin;

  private String growSpeedMultiplier;

  private String description;
}
