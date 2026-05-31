package cn.jxufe.farm.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class SoilTypeSaveDTO implements Serializable {

    private Long id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "bitCode is required")
    @Positive(message = "bitCode must be > 0")
    private Integer bitCode;

    private String coverImageUrl;

    private Short level;

    private Long unlockExperienceRequired;

    private String growSpeedMultiplier;

    private String description;
}
