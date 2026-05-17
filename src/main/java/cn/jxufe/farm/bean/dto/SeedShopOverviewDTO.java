package cn.jxufe.farm.bean.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

@Data
public class SeedShopOverviewDTO implements Serializable {

    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须大于0")
    private Long userId;
}
