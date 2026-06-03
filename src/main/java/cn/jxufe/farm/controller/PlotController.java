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
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(value = "/plot", produces = MediaType.APPLICATION_JSON_VALUE)
public class PlotController {

    private final PlotPhase1Service plotPhase1Service;

    public PlotController(PlotPhase1Service plotPhase1Service) {
        this.plotPhase1Service = plotPhase1Service;
    }

    @PostMapping(value = "/soil/page", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<SoilTypeGridVO>> pageSoilTypes(@Valid @RequestBody(required = false) SoilTypeQueryDTO query) {
        return R.ok(plotPhase1Service.pageSoilTypes(query));
    }

    @PostMapping(value = "/soil/get", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<SoilTypeGridVO> getSoilType(@Valid @RequestBody IdDTO params) {
        return R.ok(plotPhase1Service.getSoilType(params));
    }

    @PostMapping(value = "/soil/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Long> saveSoilType(@Valid @RequestBody SoilTypeSaveDTO params) {
        return R.ok(plotPhase1Service.saveSoilType(params));
    }

    @PostMapping(value = "/soil/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Void> removeSoilType(@Valid @RequestBody IdDTO params) {
        plotPhase1Service.removeSoilType(params);
        return R.ok();
    }

    @PostMapping(value = "/policy/current", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PlotPolicyVO> currentPolicy(@RequestBody(required = false) Object ignored) {
        return R.ok(plotPhase1Service.currentPolicy());
    }

    @PostMapping(value = "/policy/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Long> savePolicy(@Valid @RequestBody PlotPolicySaveDTO params) {
        return R.ok(plotPhase1Service.savePolicy(params));
    }

    @PostMapping(value = "/policy/activate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Long> activatePolicy(@Valid @RequestBody PlotPolicyActivateDTO params) {
        return R.ok(plotPhase1Service.activatePolicy(params));
    }
}
