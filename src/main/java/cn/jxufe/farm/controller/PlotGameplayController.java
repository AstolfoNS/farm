package cn.jxufe.farm.controller;

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
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.service.PlotGameplayService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gameplay")
@Validated
public class PlotGameplayController {

    private final PlotGameplayService plotGameplayService;

    public PlotGameplayController(PlotGameplayService plotGameplayService) {
        this.plotGameplayService = plotGameplayService;
    }

    @PostMapping(value = "/plot/unlock", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<PlotUnlockResultVO> unlockPlot(@Valid @RequestBody PlotUnlockDTO params) {
        return R.ok(plotGameplayService.unlockPlot(params));
    }

    @PostMapping(value = "/plot/expand", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<PlotExpandResultVO> expandPlot(@Valid @RequestBody PlotExpandDTO params) {
        return R.ok(plotGameplayService.expandPlot(params));
    }

    @PostMapping(value = "/plot/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<PlotStatusVO> plotStatus(@Valid @RequestBody PlotStatusQueryDTO params) {
        return R.ok(plotGameplayService.plotStatus(params));
    }

    @PostMapping(value = "/plot/trade/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<PlotTradeRecordVO>> pagePlotTrades(@Valid @RequestBody PlotTradeQueryDTO params) {
        return R.ok(plotGameplayService.pagePlotTrades(params));
    }

    @GetMapping(value = "/plot/trade/bizType/options", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<List<PlotTradeBizTypeOptionVO>> listPlotTradeBizTypeOptions() {
        return R.ok(plotGameplayService.listPlotTradeBizTypeOptions());
    }
}
