package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import lombok.Data;

@Data
public class SeedStageQueryDTO implements Serializable {

  @NotNull(message = "seedTypeId不能为空")
  @Positive(message = "seedTypeId必须大于0")
  private Long seedTypeId;
}
