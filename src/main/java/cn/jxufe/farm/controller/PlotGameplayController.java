package cn.jxufe.farm.controller;

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
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.service.PlotGameplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "地块经营", description = "地块解锁、扩地、状态查询、经营流水分页")
@RestController
@RequestMapping("/gameplay")
@Validated
public class PlotGameplayController {

  private final PlotGameplayService plotGameplayService;

  public PlotGameplayController(PlotGameplayService plotGameplayService) {
    this.plotGameplayService = plotGameplayService;
  }

  @Operation(summary = "解锁地块", description = "按要求顺序解锁地块。校验经验门槛、顺序约束，扣金币，记录 UNLOCK_PLOT 流水")
  @PostMapping(value = "/plot/unlock", produces = MediaType.APPLICATION_JSON_VALUE)
  public R<PlotUnlockResultVO> unlockPlot(@Valid @RequestBody PlotUnlockDTO params) {
    return R.ok(plotGameplayService.unlockPlot(params));
  }

  @Operation(
      summary = "扩地",
      description = "扩展新地块。选择土壤类型(不传则默认最低级土壤)，按土壤 expandCostCoin 扣金币，新地块初始为锁定状态")
  @PostMapping(value = "/plot/expand", produces = MediaType.APPLICATION_JSON_VALUE)
  public R<PlotExpandResultVO> expandPlot(@Valid @RequestBody PlotExpandDTO params) {
    return R.ok(plotGameplayService.expandPlot(params));
  }

  @Operation(summary = "扩地选项", description = "查询可用的扩地土壤选项列表。每个选项含土壤信息、扩地成本、经验/金币是否满足")
  @PostMapping(value = "/plot/expand/options", produces = MediaType.APPLICATION_JSON_VALUE)
  public R<PlotExpandOptionsVO> listExpandOptions(
      @Valid @RequestBody PlotExpandOptionsQueryDTO params) {
    return R.ok(plotGameplayService.listPlotExpandOptions(params));
  }

  @Operation(summary = "地块状态", description = "查询用户全部地块状态，包含下一个可解锁地块的详情、扩地成本等")
  @PostMapping(value = "/plot/status", produces = MediaType.APPLICATION_JSON_VALUE)
  public R<PlotStatusVO> plotStatus(@Valid @RequestBody PlotStatusQueryDTO params) {
    return R.ok(plotGameplayService.plotStatus(params));
  }

  @Operation(summary = "地块经营流水分页", description = "查询 UNLOCK_PLOT / EXPAND_PLOT 等地块经营流水记录，支持按业务类型筛选")
  @PostMapping(value = "/plot/trade/page", produces = MediaType.APPLICATION_JSON_VALUE)
  public R<PageResult<PlotTradeRecordVO>> pagePlotTrades(
      @Valid @RequestBody PlotTradeQueryDTO params) {
    return R.ok(plotGameplayService.pagePlotTrades(params));
  }

  @Operation(summary = "经营业务类型选项", description = "返回地块经营流水支持的全部业务类型字典(UNLOCK_PLOT / EXPAND_PLOT)")
  @GetMapping(value = "/plot/trade/bizType/options", produces = MediaType.APPLICATION_JSON_VALUE)
  public R<List<PlotTradeBizTypeOptionVO>> listPlotTradeBizTypeOptions() {
    return R.ok(plotGameplayService.listPlotTradeBizTypeOptions());
  }
}
