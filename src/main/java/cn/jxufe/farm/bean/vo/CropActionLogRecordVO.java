package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class CropActionLogRecordVO implements Serializable {

  private Long id;
  private Long plotId;
  private Long cropId;
  private Long seedTypeId;
  private String seedTypeName;
  private String actionType;
  private String actionResult;
  private OffsetDateTime actionAt;
  private String actionSnapshot;
}
