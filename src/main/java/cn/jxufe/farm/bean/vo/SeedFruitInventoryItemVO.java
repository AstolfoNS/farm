package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SeedFruitInventoryItemVO implements Serializable {

  private Long seedTypeId;
  private String seedName;
  private String coverImageUrl;
  private Long fruitQuantity;
  private Long frozenQuantity;
  private Long availableQuantity;
  private Long unitFruitPrice;
  private Long estimatedIncomeCoin;
}
