package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantablePlotVO implements Serializable {

  private Long plotId;
  private Short plotIndex;
  private Long soilTypeId;
  private Integer soilBitCode;
  private String soilName;
}
