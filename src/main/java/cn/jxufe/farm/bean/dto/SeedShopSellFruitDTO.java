package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class SeedShopSellFruitDTO implements Serializable {

    @NotBlank(message = "请求ID不能为空")
    private String requestId;

    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须大于0")
    private Long userId;

    @NotNull(message = "seedTypeId不能为空")
    @Positive(message = "seedTypeId必须大于0")
    private Long seedTypeId;

    @NotNull(message = "quantity不能为空")
    @Positive(message = "quantity必须大于0")
    private Long quantity;
}
