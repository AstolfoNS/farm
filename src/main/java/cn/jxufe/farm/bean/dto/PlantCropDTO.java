package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import lombok.Data;

@Data
public class PlantCropDTO implements Serializable {

  @NotBlank(message = "请求ID不能为空")
  private String requestId;

  @NotNull(message = "用户ID不能为空")
  @Positive(message = "用户ID必须大于0")
  private Long userId;

  @NotNull(message = "地块ID不能为空")
  @Positive(message = "地块ID必须大于0")
  private Long plotId;

  @NotNull(message = "seedTypeId不能为空")
  @Positive(message = "seedTypeId必须大于0")
  private Long seedTypeId;
}
