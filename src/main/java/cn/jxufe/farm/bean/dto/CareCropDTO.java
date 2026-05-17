package cn.jxufe.farm.bean.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

@Data
public class CareCropDTO implements Serializable {

    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须大于0")
    private Long userId;

    @NotNull(message = "地块ID不能为空")
    @Positive(message = "地块ID必须大于0")
    private Long plotId;
}
