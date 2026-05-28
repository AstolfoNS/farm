package cn.jxufe.farm.controller;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PlotPolicySaveDTO;
import cn.jxufe.farm.bean.dto.PlotTypeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTypeSaveDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationApplyDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationQueryDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationSaveDTO;
import cn.jxufe.farm.bean.vo.PlotPolicyVO;
import cn.jxufe.farm.bean.vo.PlotTypeVO;
import cn.jxufe.farm.bean.vo.UserPlotAllocationApplyResultVO;
import cn.jxufe.farm.bean.vo.UserPlotAllocationVO;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.service.PlotAdminService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class PlotAdminController {

    private final PlotAdminService plotAdminService;

    public PlotAdminController(PlotAdminService plotAdminService) {
        this.plotAdminService = plotAdminService;
    }

    @PostMapping(value = "/plot-type/page", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<PlotTypeVO>> pagePlotTypes(@Valid @RequestBody(required = false) PlotTypeQueryDTO query) {
        return R.ok(plotAdminService.pagePlotTypes(query));
    }

    @PostMapping(value = "/plot-type/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Long> savePlotType(@Valid @RequestBody PlotTypeSaveDTO params) {
        return R.ok(plotAdminService.savePlotType(params));
    }

    @PostMapping(value = "/plot-type/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Void> removePlotType(@Valid @RequestBody IdDTO params) {
        plotAdminService.removePlotType(params);
        return R.ok();
    }

    @GetMapping("/plot-policy/get")
    public R<PlotPolicyVO> getPlotPolicy() {
        return R.ok(plotAdminService.getPlotPolicy());
    }

    @PostMapping(value = "/plot-policy/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Long> savePlotPolicy(@Valid @RequestBody PlotPolicySaveDTO params) {
        return R.ok(plotAdminService.savePlotPolicy(params));
    }

    @PostMapping(value = "/user-plot-allocation/page", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<UserPlotAllocationVO>> pageUserPlotAllocations(@Valid @RequestBody(required = false) UserPlotAllocationQueryDTO query) {
        return R.ok(plotAdminService.pageUserPlotAllocations(query));
    }

    @PostMapping(value = "/user-plot-allocation/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Long> saveUserPlotAllocation(@Valid @RequestBody UserPlotAllocationSaveDTO params) {
        return R.ok(plotAdminService.saveUserPlotAllocation(params));
    }

    @PostMapping(value = "/user-plot-allocation/apply", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<UserPlotAllocationApplyResultVO> applyUserPlotAllocation(@Valid @RequestBody UserPlotAllocationApplyDTO params) {
        return R.ok(plotAdminService.applyUserPlotAllocation(params));
    }
}

