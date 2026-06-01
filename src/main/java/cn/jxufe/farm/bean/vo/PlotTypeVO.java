package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlotTypeVO implements Serializable {

    private Long id;
    private String name;
    private String iconUrl;
    private String coverImageUrl;
    private Long soilTypeId;
    private String soilTypeName;
    private Boolean unlockRequired;
    private Boolean defaultUsable;
    private Long defaultPlotUnlockExperienceConfig;
    private Integer sortOrder;
    private String description;
}
