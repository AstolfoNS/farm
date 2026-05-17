package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.MyPlantingPanelDTO;
import cn.jxufe.farm.bean.dto.SeedPlantablePlotsDTO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.MyPlantingPanelVO;
import cn.jxufe.farm.bean.vo.SeedPlantablePlotsVO;
import cn.jxufe.farm.service.FarmGameplayQueryService;
import cn.jxufe.farm.service.GameplayService;
import org.springframework.stereotype.Service;

@Service
public class FarmGameplayQueryServiceImp implements FarmGameplayQueryService {

    private final GameplayService gameplayService;

    public FarmGameplayQueryServiceImp(GameplayService gameplayService) {
        this.gameplayService = gameplayService;
    }

    @Override
    public MyFarmOverviewVO myFarmOverview(MyFarmOverviewDTO params) {
        return gameplayService.myFarmOverview(params);
    }

    @Override
    public MyPlantingPanelVO myPlantingPanel(MyPlantingPanelDTO params) {
        return gameplayService.myPlantingPanel(params);
    }

    @Override
    public SeedPlantablePlotsVO seedPlantablePlots(SeedPlantablePlotsDTO params) {
        return gameplayService.seedPlantablePlots(params);
    }
}
