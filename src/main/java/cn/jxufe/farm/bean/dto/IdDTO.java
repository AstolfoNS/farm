package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IdDTO implements Serializable {

  @NotNull(message = "id不能为空")
  @Positive(message = "id必须大于0")
  private Long id;
}
