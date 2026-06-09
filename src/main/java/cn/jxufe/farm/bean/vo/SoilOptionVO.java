package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SoilOptionVO implements Serializable {

  private Long id;
  private String text;
  private Integer bitCode;
  private Short level;
  private Long unlockExperienceRequired;
}
