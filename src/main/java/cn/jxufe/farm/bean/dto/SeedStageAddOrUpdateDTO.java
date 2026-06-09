package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SeedStageAddOrUpdateDTO implements Serializable {

  private Long id;

  @NotNull(message = "seedTypeId不能为空")
  @Positive(message = "seedTypeId必须大于0")
  private Long seedTypeId;

  @NotNull(message = "growthStageId不能为空")
  @Positive(message = "growthStageId必须大于0")
  private Long growthStageId;

  @Positive(message = "stageIndex必须大于0")
  private Short stageIndex;

  @Positive(message = "growthStage必须大于0")
  private Short growthStage;

  @Min(value = 0, message = "durationSeconds不能小于0")
  private Integer durationSeconds;

  private BigDecimal bugProbability;
  private BigDecimal pestProbability;

  @Min(value = 0, message = "width不能小于0")
  private Integer width;

  @Min(value = 0, message = "height不能小于0")
  private Integer height;

  private Integer offsetX;
  private Integer offsetY;
  private String assetUrl;
}
