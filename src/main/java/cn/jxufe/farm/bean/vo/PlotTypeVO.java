package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlotTypeVO implements Serializable {

    private Long id;
    private String name;
    private String iconUrl;
    private Long soilTypeId;
    private String soilTypeName;
    private Boolean unlockRequired;
    private Boolean defaultUsable;
    private Long defaultUnlockExperienceRequired;
    private Integer sortOrder;
    private String description;
}

