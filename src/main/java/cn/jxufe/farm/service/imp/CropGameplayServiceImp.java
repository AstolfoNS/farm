package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.CareCropDTO;
import cn.jxufe.farm.bean.dto.ClearCropDTO;
import cn.jxufe.farm.bean.dto.CropActionLogQueryDTO;
import cn.jxufe.farm.bean.dto.HarvestCropDTO;
import cn.jxufe.farm.bean.dto.PlantCropDTO;
import cn.jxufe.farm.bean.vo.CareResultVO;
import cn.jxufe.farm.bean.vo.ClearResultVO;
import cn.jxufe.farm.bean.vo.CropActionLogRecordVO;
import cn.jxufe.farm.bean.vo.HarvestResultVO;
import cn.jxufe.farm.bean.vo.PlantResultVO;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.service.CropGameplayService;
import cn.jxufe.farm.service.GameplayService;
import org.springframework.stereotype.Service;

@Service
public class CropGameplayServiceImp implements CropGameplayService {

    private final GameplayService gameplayService;

    public CropGameplayServiceImp(GameplayService gameplayService) {
        this.gameplayService = gameplayService;
    }

    @Override
    public PlantResultVO plant(PlantCropDTO params) {
        return gameplayService.plant(params);
    }

    @Override
    public HarvestResultVO harvest(HarvestCropDTO params) {
        return gameplayService.harvest(params);
    }

    @Override
    public ClearResultVO clear(ClearCropDTO params) {
        return gameplayService.clear(params);
    }

    @Override
    public CareResultVO care(CareCropDTO params) {
        return gameplayService.care(params);
    }

    @Override
    public PageResult<CropActionLogRecordVO> pageCropActionLogs(CropActionLogQueryDTO params) {
        return gameplayService.pageCropActionLogs(params);
    }
}
