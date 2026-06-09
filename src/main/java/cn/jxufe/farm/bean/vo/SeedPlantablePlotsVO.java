package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class SeedPlantablePlotsVO implements Serializable {

    private Long userId;
    private Long seedTypeId;
    private OffsetDateTime serverTime;
    private Integer plantableCount;
    private List<PlantablePlotVO> plots;
}
