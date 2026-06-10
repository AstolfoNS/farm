package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import lombok.Data;

@Data
public class SeedAddOrUpdateDTO implements Serializable {

  @Positive(message = "种子ID必须大于0")
  private Long id;

  @NotBlank(message = "种子名称不能为空")
  private String name;

  private String coverImageUrl;
  private Long seedQualityId;
  private String soilTypeIds;
  private String soilTypeId;

  @Positive(message = "土壤类型位必须大于0")
  private Long enableSoilTypeBits;

  @Positive(message = "种子等级必须大于0")
  private Short level;

  @PositiveOrZero(message = "unlockExperienceRequired必须大于等于0")
  private Long unlockExperienceRequired;

  private String season;
  private String description;
  private String tips;

  @PositiveOrZero(message = "maxBugLimit必须大于等于0")
  private Short maxBugLimit;

  @Positive(message = "maxHarvestCount必须大于0")
  private Short maxHarvestCount;

  @Positive(message = "regrowStageIndex必须大于0")
  private Short regrowStageIndex;

  @Positive(message = "harvestStageIndex必须大于0")
  private Short harvestStageIndex;

  @PositiveOrZero(message = "price必须大于等于0")
  private Long price;

  @PositiveOrZero(message = "buyPrice必须大于等于0")
  private Long buyPrice;

  @PositiveOrZero(message = "harvestExperience必须大于等于0")
  private Long harvestExperience;

  @PositiveOrZero(message = "exp必须大于等于0")
  private Long exp;

  @PositiveOrZero(message = "harvestFruitNumber必须大于等于0")
  private Integer harvestFruitNumber;

  @PositiveOrZero(message = "fruitLossPerBug必须大于等于0")
  private Integer fruitLossPerBug;

  @PositiveOrZero(message = "bugKillCoinReward必须大于等于0")
  private Long bugKillCoinReward;

  @PositiveOrZero(message = "bugKillExperienceReward必须大于等于0")
  private Long bugKillExperienceReward;

  @PositiveOrZero(message = "bugKillScoreReward必须大于等于0")
  private Long bugKillScoreReward;

  @PositiveOrZero(message = "harvestCount必须大于等于0")
  private Integer harvestCount;

  @PositiveOrZero(message = "fruitPrice必须大于等于0")
  private Long fruitPrice;

  @PositiveOrZero(message = "harvestScore必须大于等于0")
  private Long harvestScore;

  @PositiveOrZero(message = "score必须大于等于0")
  private Long score;
}
