package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import lombok.Data;

@Data
public class PlotExpandDTO implements Serializable {

  @NotNull(message = "用户ID不能为空")
  @Positive(message = "用户ID必须大于0")
  private Long userId;

  @NotNull(message = "soilTypeId不能为空")
  @Positive(message = "soilTypeId必须大于0")
  private Long soilTypeId;
}
