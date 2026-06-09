package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SeedShopItemVO implements Serializable {

  private Long id;
  private String name;
  private String coverImageUrl;
  private Long seedQualityId;
  private String seedQualityName;
  private Short level;
  private Long unlockExperienceRequired;
  private Long currentUserExperience;
  private Boolean unlockedByExperience;
  private Integer unlockProgressPercent;
  private Long enableSoilTypeBits;
  private String enableSoilTypeNames;
  private String description;
  private Long price;
  private Integer harvestFruitNumber;
  private Integer fruitLossPerBug;
  private Long bugKillCoinReward;
  private Long bugKillExperienceReward;
  private Long bugKillScoreReward;
  private Long fruitPrice;
  private Long harvestExperience;
  private Long harvestScore;
  private Short maxHarvestCount;
  private Integer totalGrowSeconds;
  private Long singleHarvestFruitValue;
  private Long totalHarvestFruitValue;
  private Long estimatedNetValue;
}
