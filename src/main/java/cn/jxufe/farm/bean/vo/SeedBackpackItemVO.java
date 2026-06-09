package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SeedBackpackItemVO implements Serializable {

  private Long userSeedId;
  private Long seedTypeId;
  private String seedTypeName;
  private Long quantity;
  private Long frozenQuantity;
  private Long availableQuantity;
  private Boolean selectable;
}
