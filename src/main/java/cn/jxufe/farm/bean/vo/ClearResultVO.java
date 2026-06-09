package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class ClearResultVO implements Serializable {

  private Long userId;
  private Long plotId;
  private Long cropId;
  private Long seedTypeId;
  private Short growStatusBefore;
  private Short stageIndexBefore;
  private Short bugCountBefore;
  private Boolean cleared;
  private OffsetDateTime clearedAt;
}
