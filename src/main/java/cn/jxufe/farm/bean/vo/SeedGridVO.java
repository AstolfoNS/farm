package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SeedGridVO implements Serializable {

  private Long id;
  private String name;
  private String coverImageUrl;
  private Long seedQualityId;
  private String seedQualityName;
  private Long enableSoilTypeBits;
  private String enableSoilTypeNames;
  private Short level;
  private Long unlockExperienceRequired;
  private String description;
  private Short maxBugLimit;
  private Short maxHarvestCount;
  private Short regrowStageIndex;
  private Short harvestStageIndex;
  private Long price;
  private Long harvestExperience;
  private Integer harvestFruitNumber;
  private Integer fruitLossPerBug;
  private Long bugKillCoinReward;
  private Long bugKillExperienceReward;
  private Long bugKillScoreReward;
  private Long fruitPrice;
  private Long harvestScore;
  private Integer totalGrowSeconds;
}
