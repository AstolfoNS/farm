package cn.jxufe.farm.bean.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

@Data
public class SeedStageQueryDTO implements Serializable {

    @NotNull(message = "seedTypeId不能为空")
    @Positive(message = "seedTypeId必须大于0")
    private Long seedTypeId;
}
