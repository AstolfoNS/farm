package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PageQueryDTO implements Serializable {

  @Min(value = 1, message = "page最小为1")
  private Integer page = 1;

  @Min(value = 1, message = "rows最小为1")
  @Max(value = 100, message = "rows最大为100")
  private Integer rows = 10;

  private String sort = "id";

  private String order = "asc";
}
