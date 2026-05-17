package cn.jxufe.farm.controller;

import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.dto.MyPlantingPanelDTO;
import cn.jxufe.farm.bean.dto.SeedPlantablePlotsDTO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.bean.vo.MyPlantingPanelVO;
import cn.jxufe.farm.bean.vo.SeedPlantablePlotsVO;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.service.FarmGameplayQueryService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gameplay")
@Validated
public class FarmGameplayQueryController {

    private final FarmGameplayQueryService farmGameplayQueryService;

    public FarmGameplayQueryController(FarmGameplayQueryService farmGameplayQueryService) {
        this.farmGameplayQueryService = farmGameplayQueryService;
    }

    @PostMapping(value = "/myFarmOverview", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<MyFarmOverviewVO> myFarmOverview(@Valid @RequestBody MyFarmOverviewDTO params) {
        return R.ok(farmGameplayQueryService.myFarmOverview(params));
    }

    @PostMapping(value = "/myPlantingPanel", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<MyPlantingPanelVO> myPlantingPanel(@Valid @RequestBody MyPlantingPanelDTO params) {
        return R.ok(farmGameplayQueryService.myPlantingPanel(params));
    }

    @PostMapping(value = "/seedPlantablePlots", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<SeedPlantablePlotsVO> seedPlantablePlots(@Valid @RequestBody SeedPlantablePlotsDTO params) {
        return R.ok(farmGameplayQueryService.seedPlantablePlots(params));
    }
}
