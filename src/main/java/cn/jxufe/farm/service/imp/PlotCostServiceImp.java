package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.config.properties.GameplayPolicyProperties;
import cn.jxufe.farm.service.PlotCostService;
import org.springframework.stereotype.Service;

@Service
public class PlotCostServiceImp implements PlotCostService {

    private final GameplayPolicyProperties  gameplayPolicyProperties;

    public PlotCostServiceImp(
            GameplayPolicyProperties gameplayPolicyProperties
    ) {
        this.gameplayPolicyProperties = gameplayPolicyProperties;
    }

    @Override
    public long calculateUnlockCostCoin(Short plotIndex) {
        int freeLimit = gameplayPolicyProperties.getPlot().getUnlock().getFreePlotIndexLimit();
        short safePlotIndex = plotIndex == null || plotIndex <= 0 ? 1 : plotIndex;

        if (safePlotIndex <= freeLimit) {
            return 0L;
        }

        return gameplayPolicyProperties.getPlot().getUnlock().getBaseCostCoin() + (safePlotIndex - freeLimit - 1L) * gameplayPolicyProperties.getPlot().getUnlock().getCostStepCoin();
    }


}
