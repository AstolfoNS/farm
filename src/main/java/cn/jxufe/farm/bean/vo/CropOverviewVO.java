package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class CropOverviewVO implements Serializable {

  private Long cropId;
  private Long seedTypeId;
  private String seedTypeName;
  private Short growStatus;
  private Short currentStageIndex;
  private Short harvestCount;
  private OffsetDateTime plantedAt;
  private OffsetDateTime expectedRipeAt;
  private OffsetDateTime expectedWitheredAt;
  private Long remainMatureSeconds;
  private Long remainWitherSeconds;
  private Short bugCount;
  private Short maxBugLimit;
  private Boolean canCare;
  private Boolean harvestable;
  private String stageAssetUrl;
  private Integer stageWidth;
  private Integer stageHeight;
  private Integer stageOffsetX;
  private Integer stageOffsetY;
}
