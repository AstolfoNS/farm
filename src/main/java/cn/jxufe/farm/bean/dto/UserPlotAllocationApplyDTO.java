package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserPlotAllocationApplyDTO implements Serializable {

    @NotNull(message = "userId不能为空")
    @Positive(message = "userId必须大于0")
    private Long userId;
}

