package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SeedShopOverviewVO implements Serializable {

  private Long userId;
  private Long currentCoin;
  private Long sellableTotalValue;
  private Long sellableFruitTotalCount;
  private Integer purchasableSeedTypeCount;
}
