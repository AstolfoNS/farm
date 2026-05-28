package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserPlotAllocationQueryDTO implements Serializable {

    private Long userId;

    private String username;

    @Min(value = 1, message = "page最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "rows最小为1")
    @Max(value = 100, message = "rows最大为100")
    private Integer rows = 10;

    private String sort = "id";

    private String order = "asc";
}

