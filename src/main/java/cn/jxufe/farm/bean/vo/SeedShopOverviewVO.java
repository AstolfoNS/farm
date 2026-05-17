package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeedShopOverviewVO implements Serializable {

    private Long userId;
    private Long currentCoin;
    private Long sellableTotalValue;
    private Long sellableFruitTotalCount;
    private Integer purchasableSeedTypeCount;
}
