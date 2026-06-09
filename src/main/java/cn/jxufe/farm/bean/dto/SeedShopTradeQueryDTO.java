package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import lombok.Data;

@Data
public class SeedShopTradeQueryDTO implements Serializable {

  @NotNull(message = "用户ID不能为空")
  @Positive(message = "用户ID必须大于0")
  private Long userId;

  private String tradeType;

  @Min(value = 1, message = "page最小为1")
  private Integer page = 1;

  @Min(value = 1, message = "rows最小为1")
  @Max(value = 100, message = "rows最大为100")
  private Integer rows = 10;
}
