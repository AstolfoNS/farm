package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import lombok.Data;

@Data
public class PlotPolicyActivateDTO implements Serializable {

  @NotNull(message = "id is required")
  @Positive(message = "id must be > 0")
  private Long id;
}
