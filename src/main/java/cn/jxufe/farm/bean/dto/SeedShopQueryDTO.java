package cn.jxufe.farm.bean.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

@Data
public class SeedShopQueryDTO implements Serializable {

    private String name;
    @Positive(message = "seedQualityId必须大于0")
    private Long seedQualityId;
    @Positive(message = "level必须大于0")
    private Short level;
    @Min(value = 1, message = "page最小为1")
    private Integer page = 1;
    @Min(value = 1, message = "rows最小为1")
    @Max(value = 100, message = "rows最大为100")
    private Integer rows = 10;
    private String sort = "id";
    private String order = "asc";
}
