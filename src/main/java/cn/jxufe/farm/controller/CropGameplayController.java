package cn.jxufe.farm.controller;

import cn.jxufe.farm.bean.dto.CareCropDTO;
import cn.jxufe.farm.bean.dto.CropActionLogQueryDTO;
import cn.jxufe.farm.bean.dto.HarvestCropDTO;
import cn.jxufe.farm.bean.dto.PlantCropDTO;
import cn.jxufe.farm.bean.vo.CareResultVO;
import cn.jxufe.farm.bean.vo.CropActionLogRecordVO;
import cn.jxufe.farm.bean.vo.HarvestResultVO;
import cn.jxufe.farm.bean.vo.PlantResultVO;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.service.CropGameplayService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gameplay")
@Validated
public class CropGameplayController {

    private final CropGameplayService cropGameplayService;

    public CropGameplayController(CropGameplayService cropGameplayService) {
        this.cropGameplayService = cropGameplayService;
    }

    @PostMapping(value = "/plant", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<PlantResultVO> plant(@Valid @RequestBody PlantCropDTO params) {
        return R.ok(cropGameplayService.plant(params));
    }

    @PostMapping(value = "/harvest", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<HarvestResultVO> harvest(@Valid @RequestBody HarvestCropDTO params) {
        return R.ok(cropGameplayService.harvest(params));
    }

    @PostMapping(value = "/care", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<CareResultVO> care(@Valid @RequestBody CareCropDTO params) {
        return R.ok(cropGameplayService.care(params));
    }

    @PostMapping(value = "/crop/action/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<CropActionLogRecordVO>> pageCropActionLogs(@Valid @RequestBody CropActionLogQueryDTO params) {
        return R.ok(cropGameplayService.pageCropActionLogs(params));
    }
}
