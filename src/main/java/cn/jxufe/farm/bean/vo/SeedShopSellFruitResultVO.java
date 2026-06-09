package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SeedShopSellFruitResultVO implements Serializable {

  private Long userId;
  private Long seedTypeId;
  private String seedName;
  private Long sellQuantity;
  private Long unitFruitPrice;
  private Long totalIncomeCoin;
  private Long beforeCoin;
  private Long afterCoin;
  private Long beforeFruitQuantity;
  private Long afterFruitQuantity;
}
