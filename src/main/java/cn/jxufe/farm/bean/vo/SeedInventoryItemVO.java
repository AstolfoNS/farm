package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SeedInventoryItemVO implements Serializable {

  private Long seedTypeId;
  private String seedName;
  private String coverImageUrl;
  private Long quantity;
  private Long frozenQuantity;
  private Long availableQuantity;
  private Long unitBuyPrice;
  private Long unlockExperienceRequired;
}
