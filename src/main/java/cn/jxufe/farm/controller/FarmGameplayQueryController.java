package cn.jxufe.farm.controller;

import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.MyPlantingPanelDTO;
import cn.jxufe.farm.bean.dto.SeedPlantablePlotsDTO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.MyPlantingPanelVO;
import cn.jxufe.farm.bean.vo.SeedPlantablePlotsVO;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.service.FarmGameplayQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "农场聚合查询", description = "农场概览、种植面板、种子可种地块查询")
@RestController
@RequestMapping("/gameplay")
@Validated
public class FarmGameplayQueryController {

    private final FarmGameplayQueryService farmGameplayQueryService;

    public FarmGameplayQueryController(FarmGameplayQueryService farmGameplayQueryService) {
        this.farmGameplayQueryService = farmGameplayQueryService;
    }

    @Operation(summary = "我的农场概览", description = "返回用户全部地块状态、作物信息、成熟倒计时、可收获标记、下次扩地成本等")
    @PostMapping(value = "/myFarmOverview", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<MyFarmOverviewVO> myFarmOverview(@Valid @RequestBody MyFarmOverviewDTO params) {
        return R.ok(farmGameplayQueryService.myFarmOverview(params));
    }

    @Operation(summary = "种植面板", description = "返回用户种子背包列表、空闲可种地块数，供种植弹窗使用")
    @PostMapping(value = "/myPlantingPanel", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<MyPlantingPanelVO> myPlantingPanel(@Valid @RequestBody MyPlantingPanelDTO params) {
        return R.ok(farmGameplayQueryService.myPlantingPanel(params));
    }

    @Operation(summary = "种子可种地块", description = "根据 seedTypeId 与土壤兼容性(bits)，返回该种子可种植的地块列表")
    @PostMapping(value = "/seedPlantablePlots", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<SeedPlantablePlotsVO> seedPlantablePlots(@Valid @RequestBody SeedPlantablePlotsDTO params) {
        return R.ok(farmGameplayQueryService.seedPlantablePlots(params));
    }
}
