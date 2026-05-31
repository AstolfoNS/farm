package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SoilTypeGridVO implements Serializable {

    private Long id;
    private String name;
    private Integer bitCode;
    private String coverImageUrl;
    private Short level;
    private Long unlockExperienceRequired;
    private String growSpeedMultiplier;
    private String description;
}
