package cn.jxufe.farm.service;

import cn.jxufe.farm.bean.dto.CareCropDTO;
import cn.jxufe.farm.bean.dto.HarvestCropDTO;
import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.MyPlantingPanelDTO;
import cn.jxufe.farm.bean.dto.PlantCropDTO;
import cn.jxufe.farm.bean.dto.SeedPlantablePlotsDTO;
import cn.jxufe.farm.bean.vo.CareResultVO;
import cn.jxufe.farm.bean.vo.HarvestResultVO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.MyPlantingPanelVO;
import cn.jxufe.farm.bean.vo.PlantResultVO;
import cn.jxufe.farm.bean.vo.SeedPlantablePlotsVO;

public interface CropLifecycleService {

    PlantResultVO plant(PlantCropDTO params);

    HarvestResultVO harvest(HarvestCropDTO params);

    CareResultVO care(CareCropDTO params);

    MyFarmOverviewVO myFarmOverview(MyFarmOverviewDTO params);

    MyPlantingPanelVO myPlantingPanel(MyPlantingPanelDTO params);

    SeedPlantablePlotsVO seedPlantablePlots(SeedPlantablePlotsDTO params);
}
