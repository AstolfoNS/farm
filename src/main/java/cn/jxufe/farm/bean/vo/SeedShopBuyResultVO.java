package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

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
