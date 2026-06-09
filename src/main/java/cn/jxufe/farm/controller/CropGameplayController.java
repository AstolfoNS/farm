package cn.jxufe.farm.controller;

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
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.service.CropGameplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "作物生命周期", description = "种植、收获、养护(杀虫)、铲除、作物行为日志")
@RestController
@RequestMapping("/gameplay")
@Validated
public class CropGameplayController {

    private final CropGameplayService cropGameplayService;

    public CropGameplayController(CropGameplayService cropGameplayService) {
        this.cropGameplayService = cropGameplayService;
    }

    @Operation(summary = "种植作物", description = "在指定地块种植种子。校验土壤兼容性、种子库存，扣减种子并创建作物实例，设置成熟/枯萎时间轴。需传幂等 requestId")
    @PostMapping(value = "/plant", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<PlantResultVO> plant(@Valid @RequestBody PlantCropDTO params) {
        return R.ok(cropGameplayService.plant(params));
    }

    @Operation(summary = "收获作物", description = "收获成熟作物。计算虫害减产，增加果实库存、经验、积分。多次收获作物回退到 regrow 阶段。需传幂等 requestId")
    @PostMapping(value = "/harvest", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<HarvestResultVO> harvest(@Valid @RequestBody HarvestCropDTO params) {
        return R.ok(cropGameplayService.harvest(params));
    }

    @Operation(summary = "铲除作物", description = "强制清除地块上的作物。记录铲除前阶段和虫害数量。需传幂等 requestId")
    @PostMapping(value = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<ClearResultVO> clear(@Valid @RequestBody ClearCropDTO params) {
        return R.ok(cropGameplayService.clear(params));
    }

    @Operation(summary = "养护杀虫", description = "清除作物上的一只虫害。枯萎作物不可养护。每清除一只虫发放金币/经验/积分奖励。不消费 requestId")
    @PostMapping(value = "/care", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<CareResultVO> care(@Valid @RequestBody CareCropDTO params) {
        return R.ok(cropGameplayService.care(params));
    }

    @Operation(summary = "作物操作日志分页", description = "查询指定用户的种植/收获/养护/铲除等行为日志，支持按操作类型和地块筛选")
    @PostMapping(value = "/crop/action/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<CropActionLogRecordVO>> pageCropActionLogs(@Valid @RequestBody CropActionLogQueryDTO params) {
        return R.ok(cropGameplayService.pageCropActionLogs(params));
    }
}
