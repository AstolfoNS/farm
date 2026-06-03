package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.CareCropDTO;
import cn.jxufe.farm.bean.dto.ClearCropDTO;
import cn.jxufe.farm.bean.dto.CropActionLogQueryDTO;
import cn.jxufe.farm.bean.dto.HarvestCropDTO;
import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.MyPlantingPanelDTO;
import cn.jxufe.farm.bean.dto.PlantCropDTO;
import cn.jxufe.farm.bean.dto.PlotExpandDTO;
import cn.jxufe.farm.bean.dto.PlotExpandOptionsQueryDTO;
import cn.jxufe.farm.bean.dto.PlotStatusQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTradeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotUnlockDTO;
import cn.jxufe.farm.bean.dto.SeedPlantablePlotsDTO;
import cn.jxufe.farm.bean.vo.CareResultVO;
import cn.jxufe.farm.bean.vo.ClearResultVO;
import cn.jxufe.farm.bean.vo.CropActionLogRecordVO;
import cn.jxufe.farm.bean.vo.HarvestResultVO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.MyPlantingPanelVO;
import cn.jxufe.farm.bean.vo.PlantResultVO;
import cn.jxufe.farm.bean.vo.PlotExpandOptionsVO;
import cn.jxufe.farm.bean.vo.PlotExpandResultVO;
import cn.jxufe.farm.bean.vo.PlotStatusVO;
import cn.jxufe.farm.bean.vo.PlotTradeBizTypeOptionVO;
import cn.jxufe.farm.bean.vo.PlotTradeRecordVO;
import cn.jxufe.farm.bean.vo.PlotUnlockResultVO;
import cn.jxufe.farm.bean.vo.SeedPlantablePlotsVO;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.entity.RequestIdempotency;
import cn.jxufe.farm.service.CropLifecycleService;
import cn.jxufe.farm.service.GameplayLogQueryService;
import cn.jxufe.farm.service.GameplayService;
import cn.jxufe.farm.service.PlotManagementService;
import cn.jxufe.farm.service.RequestIdempotencyService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameplayServiceImp implements GameplayService {

    private final CropLifecycleService cropLifecycleService;

    private final PlotManagementService plotManagementService;

    private final GameplayLogQueryService gameplayLogQueryService;

    private final RequestIdempotencyService requestIdempotencyService;

    public GameplayServiceImp(
            CropLifecycleService cropLifecycleService,
            PlotManagementService plotManagementService,
            GameplayLogQueryService gameplayLogQueryService,
            RequestIdempotencyService requestIdempotencyService
    ) {
        this.cropLifecycleService = cropLifecycleService;
        this.plotManagementService = plotManagementService;
        this.gameplayLogQueryService = gameplayLogQueryService;
        this.requestIdempotencyService = requestIdempotencyService;
    }

    @Override
    @Transactional
    public PlantResultVO plant(PlantCropDTO params) {
        PlantResultVO cached = requestIdempotencyService.getCachedSuccessResult(
                params == null ? null : params.getUserId(),
                "PLANT",
                params == null ? null : params.getRequestId(),
                PlantResultVO.class
        );
        if (cached != null) {
            return cached;
        }
        RequestIdempotency idempotency = requestIdempotencyService.claimProcessing(
                params == null ? null : params.getUserId(),
                "PLANT",
                params == null ? null : params.getRequestId()
        );
        try {
            PlantResultVO result = cropLifecycleService.plant(params);
            requestIdempotencyService.markSuccess(idempotency.getId(), result);
            return result;
        } catch (RuntimeException ex) {
            requestIdempotencyService.markFailed(idempotency.getId(), ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public HarvestResultVO harvest(HarvestCropDTO params) {
        HarvestResultVO cached = requestIdempotencyService.getCachedSuccessResult(
                params == null ? null : params.getUserId(),
                "HARVEST",
                params == null ? null : params.getRequestId(),
                HarvestResultVO.class
        );
        if (cached != null) {
            return cached;
        }
        RequestIdempotency idempotency = requestIdempotencyService.claimProcessing(
                params == null ? null : params.getUserId(),
                "HARVEST",
                params == null ? null : params.getRequestId()
        );
        try {
            HarvestResultVO result = cropLifecycleService.harvest(params);
            requestIdempotencyService.markSuccess(idempotency.getId(), result);
            return result;
        } catch (RuntimeException ex) {
            requestIdempotencyService.markFailed(idempotency.getId(), ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public ClearResultVO clear(ClearCropDTO params) {
        ClearResultVO cached = requestIdempotencyService.getCachedSuccessResult(
                params == null ? null : params.getUserId(),
                "CLEAR",
                params == null ? null : params.getRequestId(),
                ClearResultVO.class
        );
        if (cached != null) {
            return cached;
        }
        RequestIdempotency idempotency = requestIdempotencyService.claimProcessing(
                params == null ? null : params.getUserId(),
                "CLEAR",
                params == null ? null : params.getRequestId()
        );
        try {
            ClearResultVO result = cropLifecycleService.clear(params);
            requestIdempotencyService.markSuccess(idempotency.getId(), result);
            return result;
        } catch (RuntimeException ex) {
            requestIdempotencyService.markFailed(idempotency.getId(), ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public CareResultVO care(CareCropDTO params) {
        return cropLifecycleService.care(params);
    }

    @Override
    public MyFarmOverviewVO myFarmOverview(MyFarmOverviewDTO params) {
        return cropLifecycleService.myFarmOverview(params);
    }

    @Override
    public MyPlantingPanelVO myPlantingPanel(MyPlantingPanelDTO params) {
        return cropLifecycleService.myPlantingPanel(params);
    }

    @Override
    public SeedPlantablePlotsVO seedPlantablePlots(SeedPlantablePlotsDTO params) {
        return cropLifecycleService.seedPlantablePlots(params);
    }

    @Override
    @Transactional
    public PlotUnlockResultVO unlockPlot(PlotUnlockDTO params) {
        return plotManagementService.unlockPlot(params);
    }

    @Override
    @Transactional
    public PlotExpandResultVO expandPlot(PlotExpandDTO params) {
        return plotManagementService.expandPlot(params);
    }

    @Override
    public PlotExpandOptionsVO listPlotExpandOptions(PlotExpandOptionsQueryDTO params) {
        return plotManagementService.listPlotExpandOptions(params);
    }

    @Override
    public PlotStatusVO plotStatus(PlotStatusQueryDTO params) {
        return plotManagementService.plotStatus(params);
    }

    @Override
    public PageResult<PlotTradeRecordVO> pagePlotTrades(PlotTradeQueryDTO params) {
        return plotManagementService.pagePlotTrades(params);
    }

    @Override
    public List<PlotTradeBizTypeOptionVO> listPlotTradeBizTypeOptions() {
        return plotManagementService.listPlotTradeBizTypeOptions();
    }

    @Override
    public PageResult<CropActionLogRecordVO> pageCropActionLogs(CropActionLogQueryDTO params) {
        return gameplayLogQueryService.pageCropActionLogs(params);
    }
}
