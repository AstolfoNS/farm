package cn.jxufe.farm.controller;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PlotPolicyActivateDTO;
import cn.jxufe.farm.bean.dto.PlotPolicySaveDTO;
import cn.jxufe.farm.bean.dto.SoilTypeQueryDTO;
import cn.jxufe.farm.bean.dto.SoilTypeSaveDTO;
import cn.jxufe.farm.bean.vo.PlotPolicyVO;
import cn.jxufe.farm.bean.vo.SoilTypeGridVO;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.service.PlotPhase1Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "地块配置", description = "土壤类型 CRUD、全局策略管理")
@RestController
@Validated
@RequestMapping(value = "/plot", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlotController {

  private final PlotPhase1Service plotPhase1Service;

  public PlotController(PlotPhase1Service plotPhase1Service) {
    this.plotPhase1Service = plotPhase1Service;
  }

  @Operation(summary = "土壤分页查询", description = "按土壤名称模糊搜索，支持分页排序")
  @PostMapping(value = "/soil/page", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<PageResult<SoilTypeGridVO>> pageSoilTypes(
      @Valid @RequestBody(required = false) SoilTypeQueryDTO query) {
    return R.ok(plotPhase1Service.pageSoilTypes(query));
  }

  @Operation(summary = "获取土壤详情", description = "按 ID 获取单个土壤类型的完整信息")
  @PostMapping(value = "/soil/get", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<SoilTypeGridVO> getSoilType(@Valid @RequestBody IdDTO params) {
    return R.ok(plotPhase1Service.getSoilType(params));
  }

  @Operation(summary = "保存土壤类型", description = "新增或更新土壤类型。id 为空时新增，bitCode 自动分配(2的幂)；id > 0 时更新")
  @PostMapping(value = "/soil/save", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Long> saveSoilType(@Valid @RequestBody SoilTypeSaveDTO params) {
    return R.ok(plotPhase1Service.saveSoilType(params));
  }

  @Operation(summary = "删除土壤类型", description = "按 ID 软删除土壤类型。被用户地块引用时禁止删除")
  @PostMapping(value = "/soil/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Void> removeSoilType(@Valid @RequestBody IdDTO params) {
    plotPhase1Service.removeSoilType(params);
    return R.ok();
  }

  @Operation(summary = "获取当前策略", description = "返回当前激活的全局策略；无策略时返回 fallback 默认值(6总/1解锁)")
  @PostMapping(value = "/policy/current", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<PlotPolicyVO> currentPolicy(@RequestBody(required = false) Object ignored) {
    return R.ok(plotPhase1Service.currentPolicy());
  }

  @Operation(summary = "保存全局策略", description = "新增或更新全局策略。active=true 时会自动将其他策略设为 inactive")
  @PostMapping(value = "/policy/save", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Long> savePolicy(@Valid @RequestBody PlotPolicySaveDTO params) {
    return R.ok(plotPhase1Service.savePolicy(params));
  }

  @Operation(summary = "激活全局策略", description = "将指定策略设为 active，其他策略自动失活。仅对新用户生效")
  @PostMapping(value = "/policy/activate", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Long> activatePolicy(@Valid @RequestBody PlotPolicyActivateDTO params) {
    return R.ok(plotPhase1Service.activatePolicy(params));
  }
}
