package cn.jxufe.farm.bean.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SeedStageGridVO implements Serializable {

    private Long id;
    private Long seedTypeId;
    private String seedName;
    private Long growthStageId;
    private String growthStageName;
    private Short stageIndex;
    private Integer durationSeconds;
    private BigDecimal bugProbability;
    private Integer width;
    private Integer height;
    private Integer offsetX;
    private Integer offsetY;
    private String assetUrl;
}
