package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MyPlantingPanelDTO implements Serializable {

  @NotNull(message = "用户ID不能为空")
  @Positive(message = "用户ID必须大于0")
  private Long userId;
}
