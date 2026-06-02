package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.io.Serializable;

@Data
public class SeedAddOrUpdateDTO implements Serializable {

    private Long id;

    @NotBlank(message = "种子名称不能为空")
    private String name;

    private String coverImageUrl;
    private Long seedQualityId;
    private String soilTypeIds;
    private String soilTypeId;
    private Long enableSoilTypeBits;
    private Short level;
    @PositiveOrZero(message = "unlockExperienceRequired必须大于等于0")
    private Long unlockExperienceRequired;
    private String season;
    private String description;
    private String tips;
    private Short maxBugLimit;
    private Short maxHarvestCount;
    private Short regrowStageIndex;
    private Short harvestStageIndex;
    private Long price;
    private Long buyPrice;
    private Long harvestExperience;
    private Long exp;
    private Integer harvestFruitNumber;
    private Integer fruitLossPerBug;
    private Long bugKillCoinReward;
    private Long bugKillExperienceReward;
    private Long bugKillScoreReward;
    private Integer harvestCount;
    private Long fruitPrice;
    private Long harvestScore;
    private Long score;
}
