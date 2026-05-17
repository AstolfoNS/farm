package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeedShopItemVO implements Serializable {

    private Long id;
    private String name;
    private String coverImageUrl;
    private Long seedQualityId;
    private String seedQualityName;
    private Short level;
    private Long enableSoilTypeBits;
    private String enableSoilTypeNames;
    private String description;
    private Long price;
    private Integer harvestFruitNumber;
    private Long fruitPrice;
    private Long harvestExperience;
    private Long harvestScore;
    private Short maxHarvestCount;
    private Integer totalGrowSeconds;
    private Long singleHarvestFruitValue;
    private Long totalHarvestFruitValue;
    private Long estimatedNetValue;
}
