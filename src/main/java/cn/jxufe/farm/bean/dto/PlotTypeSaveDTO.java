package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class PlotTypeSaveDTO implements Serializable {

    private Long id;

    @NotBlank(message = "地块类型名称不能为空")
    private String name;

    private String iconUrl;

    @NotNull(message = "soilTypeId不能为空")
    @Positive(message = "soilTypeId必须大于0")
    private Long soilTypeId;

    private Boolean unlockRequired;

    private Boolean defaultUsable;

    private Long defaultUnlockExperienceRequired;

    private Integer sortOrder;

    private String description;
}

