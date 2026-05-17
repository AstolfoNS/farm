package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.PlotExpandDTO;
import cn.jxufe.farm.bean.dto.PlotStatusQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTradeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotUnlockDTO;
import cn.jxufe.farm.bean.vo.PlotExpandResultVO;
import cn.jxufe.farm.bean.vo.PlotStatusVO;
import cn.jxufe.farm.bean.vo.PlotTradeBizTypeOptionVO;
import cn.jxufe.farm.bean.vo.PlotTradeRecordVO;
import cn.jxufe.farm.bean.vo.PlotUnlockResultVO;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.config.properties.GameplayPolicyProperties;
import cn.jxufe.farm.service.GameplayService;
import cn.jxufe.farm.service.PlotGameplayService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlotGameplayServiceImp implements PlotGameplayService {

    private final GameplayService gameplayService;

    private final GameplayPolicyProperties gameplayPolicyProperties;

    public PlotGameplayServiceImp(
            GameplayService gameplayService,
            GameplayPolicyProperties gameplayPolicyProperties
    ) {
        this.gameplayService = gameplayService;
        this.gameplayPolicyProperties = gameplayPolicyProperties;
    }

    @Override
    public PlotUnlockResultVO unlockPlot(PlotUnlockDTO params) {
        return gameplayService.unlockPlot(params);
    }

    @Override
    public PlotExpandResultVO expandPlot(PlotExpandDTO params) {
        return gameplayService.expandPlot(params);
    }

    @Override
    public PlotStatusVO plotStatus(PlotStatusQueryDTO params) {
        return gameplayService.plotStatus(params);
    }

    @Override
    public PageResult<PlotTradeRecordVO> pagePlotTrades(PlotTradeQueryDTO params) {
        return gameplayService.pagePlotTrades(params);
    }

    @Override
    public List<PlotTradeBizTypeOptionVO> listPlotTradeBizTypeOptions() {
        return gameplayService.listPlotTradeBizTypeOptions();
    }

    @Override
    public long calculateUnlockCostCoin(Short plotIndex) {
        int freeLimit = gameplayPolicyProperties.getPlot().getUnlock().getFreePlotIndexLimit();
        short safePlotIndex = plotIndex == null || plotIndex <= 0 ? 1 : plotIndex;
        if (safePlotIndex <= freeLimit) return 0L;
        return gameplayPolicyProperties.getPlot().getUnlock().getBaseCostCoin()
                + (safePlotIndex - freeLimit - 1L) * gameplayPolicyProperties.getPlot().getUnlock().getCostStepCoin();
    }

}
