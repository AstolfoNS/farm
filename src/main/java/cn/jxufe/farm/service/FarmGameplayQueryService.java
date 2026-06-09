package cn.jxufe.farm.service;

import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.MyPlantingPanelDTO;
import cn.jxufe.farm.bean.dto.SeedPlantablePlotsDTO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.MyPlantingPanelVO;
import cn.jxufe.farm.bean.vo.SeedPlantablePlotsVO;

public interface FarmGameplayQueryService {

  MyFarmOverviewVO myFarmOverview(MyFarmOverviewDTO params);

  MyPlantingPanelVO myPlantingPanel(MyPlantingPanelDTO params);

  SeedPlantablePlotsVO seedPlantablePlots(SeedPlantablePlotsDTO params);
}
