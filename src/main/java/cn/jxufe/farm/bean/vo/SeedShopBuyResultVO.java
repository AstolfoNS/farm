package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeedShopBuyResultVO implements Serializable {

    private Long userId;
    private Long seedTypeId;
    private String seedName;
    private Long buyQuantity;
    private Long unitPrice;
    private Long totalCostCoin;
    private Long beforeCoin;
    private Long afterCoin;
    private Long beforeSeedQuantity;
    private Long afterSeedQuantity;
}
