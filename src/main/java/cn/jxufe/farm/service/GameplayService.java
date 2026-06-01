package cn.jxufe.farm.service;

import cn.jxufe.farm.bean.dto.CareCropDTO;
import cn.jxufe.farm.bean.dto.ClearCropDTO;
import cn.jxufe.farm.bean.dto.CropActionLogQueryDTO;
import cn.jxufe.farm.bean.dto.HarvestCropDTO;
import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.MyPlantingPanelDTO;
import cn.jxufe.farm.bean.dto.PlantCropDTO;
import cn.jxufe.farm.bean.dto.PlotExpandDTO;
import cn.jxufe.farm.bean.dto.PlotTradeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotStatusQueryDTO;
import cn.jxufe.farm.bean.dto.PlotUnlockDTO;
import cn.jxufe.farm.bean.dto.SeedPlantablePlotsDTO;
import cn.jxufe.farm.bean.vo.CareResultVO;
import cn.jxufe.farm.bean.vo.ClearResultVO;
import cn.jxufe.farm.bean.vo.CropActionLogRecordVO;
import cn.jxufe.farm.bean.vo.HarvestResultVO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.MyPlantingPanelVO;
import cn.jxufe.farm.bean.vo.PlantResultVO;
import cn.jxufe.farm.bean.vo.PlotExpandResultVO;
import cn.jxufe.farm.bean.vo.PlotTradeBizTypeOptionVO;
import cn.jxufe.farm.bean.vo.PlotStatusVO;
import cn.jxufe.farm.bean.vo.PlotTradeRecordVO;
import cn.jxufe.farm.bean.vo.PlotUnlockResultVO;
import cn.jxufe.farm.bean.vo.SeedPlantablePlotsVO;
import cn.jxufe.farm.common.pages.PageResult;

import java.util.List;

public interface GameplayService {

    PlantResultVO plant(PlantCropDTO params);

    HarvestResultVO harvest(HarvestCropDTO params);

    ClearResultVO clear(ClearCropDTO params);

    CareResultVO care(CareCropDTO params);

    MyFarmOverviewVO myFarmOverview(MyFarmOverviewDTO params);

    MyPlantingPanelVO myPlantingPanel(MyPlantingPanelDTO params);

    SeedPlantablePlotsVO seedPlantablePlots(SeedPlantablePlotsDTO params);

    PlotUnlockResultVO unlockPlot(PlotUnlockDTO params);

    PlotExpandResultVO expandPlot(PlotExpandDTO params);

    PlotStatusVO plotStatus(PlotStatusQueryDTO params);

    PageResult<PlotTradeRecordVO> pagePlotTrades(PlotTradeQueryDTO params);

    List<PlotTradeBizTypeOptionVO> listPlotTradeBizTypeOptions();

    PageResult<CropActionLogRecordVO> pageCropActionLogs(CropActionLogQueryDTO params);
}
