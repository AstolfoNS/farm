package cn.jxufe.farm.service;

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

public interface CropGameplayService {

    PlantResultVO plant(PlantCropDTO params);

    HarvestResultVO harvest(HarvestCropDTO params);

    ClearResultVO clear(ClearCropDTO params);

    CareResultVO care(CareCropDTO params);

    PageResult<CropActionLogRecordVO> pageCropActionLogs(CropActionLogQueryDTO params);
}
