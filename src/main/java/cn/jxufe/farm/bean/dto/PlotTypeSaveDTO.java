package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class PlotTypeSaveDTO implements Serializable {

    private Long id;

    @NotBlank(message = "plot type name is required")
    private String name;

    private String iconUrl;

    private String coverImageUrl;

    @NotNull(message = "soilTypeId is required")
    @Positive(message = "soilTypeId must be > 0")
    private Long soilTypeId;

    private Boolean unlockRequired;

    private Boolean defaultUsable;

    private Long defaultUnlockExperienceRequired;

    private Integer sortOrder;

    private String description;
}
