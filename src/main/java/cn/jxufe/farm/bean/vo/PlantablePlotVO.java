package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class PlantablePlotVO implements Serializable {

    private Long plotId;
    private Short plotIndex;
    private Long soilTypeId;
    private Integer soilBitCode;
    private String soilName;
}
