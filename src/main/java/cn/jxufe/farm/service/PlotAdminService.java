package cn.jxufe.farm.service;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PlotPolicySaveDTO;
import cn.jxufe.farm.bean.dto.PlotTypeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTypeSaveDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationApplyDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationQueryDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationSaveDTO;
import cn.jxufe.farm.bean.vo.OptionVO;
import cn.jxufe.farm.bean.vo.PlotPolicyVO;
import cn.jxufe.farm.bean.vo.PlotTypeVO;
import cn.jxufe.farm.bean.vo.UserPlotAllocationApplyResultVO;
import cn.jxufe.farm.bean.vo.UserPlotAllocationVO;
import cn.jxufe.farm.common.pages.PageResult;

import java.util.List;

public interface PlotAdminService {

    List<OptionVO> listPlotTypeOptions();

    List<OptionVO> listUserOptions();

    PageResult<PlotTypeVO> pagePlotTypes(PlotTypeQueryDTO query);

    Long savePlotType(PlotTypeSaveDTO params);

    void removePlotType(IdDTO params);

    PlotPolicyVO getPlotPolicy();

    Long savePlotPolicy(PlotPolicySaveDTO params);

    PageResult<UserPlotAllocationVO> pageUserPlotAllocations(UserPlotAllocationQueryDTO query);

    Long saveUserPlotAllocation(UserPlotAllocationSaveDTO params);

    UserPlotAllocationApplyResultVO applyUserPlotAllocation(UserPlotAllocationApplyDTO params);
}
