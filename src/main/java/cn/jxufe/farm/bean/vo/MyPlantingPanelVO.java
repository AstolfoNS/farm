package cn.jxufe.farm.bean.vo;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPlantingPanelVO implements Serializable {

  private Long userId;
  private OffsetDateTime serverTime;
  private Integer totalPlotCount;
  private Integer unlockedPlotCount;
  private Integer plantablePlotCount;
  private Integer backpackSeedTypeCount;
  private Integer selectableSeedTypeCount;
  private List<SeedBackpackItemVO> seeds;
}
