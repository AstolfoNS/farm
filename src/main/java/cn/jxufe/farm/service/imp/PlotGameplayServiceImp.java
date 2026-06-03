package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.PlotExpandDTO;
import cn.jxufe.farm.bean.dto.PlotExpandOptionsQueryDTO;
import cn.jxufe.farm.bean.dto.PlotStatusQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTradeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotUnlockDTO;
import cn.jxufe.farm.bean.vo.PlotExpandOptionsVO;
import cn.jxufe.farm.bean.vo.PlotExpandResultVO;
import cn.jxufe.farm.bean.vo.PlotStatusVO;
import cn.jxufe.farm.bean.vo.PlotTradeBizTypeOptionVO;
import cn.jxufe.farm.bean.vo.PlotTradeRecordVO;
import cn.jxufe.farm.bean.vo.PlotUnlockResultVO;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.service.GameplayService;
import cn.jxufe.farm.service.PlotGameplayService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlotGameplayServiceImp implements PlotGameplayService {

    private final GameplayService gameplayService;

    public PlotGameplayServiceImp(
            GameplayService gameplayService
    ) {
        this.gameplayService = gameplayService;
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
    public PlotExpandOptionsVO listPlotExpandOptions(PlotExpandOptionsQueryDTO params) {
        return gameplayService.listPlotExpandOptions(params);
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

}
