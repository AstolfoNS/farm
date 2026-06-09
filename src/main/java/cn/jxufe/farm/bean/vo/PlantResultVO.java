package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class PlantResultVO implements Serializable {

    private Long userId;
    private Long plotId;
    private Long cropId;
    private Long seedTypeId;
    private Long remainSeedQuantity;
    private Short growStatus;
    private Short currentStageIndex;
    private OffsetDateTime expectedRipeAt;
    private OffsetDateTime expectedWitheredAt;
}
