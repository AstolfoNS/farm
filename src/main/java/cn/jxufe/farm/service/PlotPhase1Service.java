package cn.jxufe.farm.service;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PlotPolicyActivateDTO;
import cn.jxufe.farm.bean.dto.PlotPolicySaveDTO;
import cn.jxufe.farm.bean.dto.PlotTypeQueryDTO;
import cn.jxufe.farm.bean.dto.PlotTypeSaveDTO;
import cn.jxufe.farm.bean.dto.SoilTypeQueryDTO;
import cn.jxufe.farm.bean.dto.SoilTypeSaveDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationQueryDTO;
import cn.jxufe.farm.bean.dto.UserPlotAllocationUpdateDTO;
import cn.jxufe.farm.bean.vo.PlotPolicyVO;
import cn.jxufe.farm.bean.vo.PlotTypeGridVO;
import cn.jxufe.farm.bean.vo.SoilTypeGridVO;
import cn.jxufe.farm.bean.vo.UserPlotAllocationGridVO;
import cn.jxufe.farm.common.pages.PageResult;

public interface PlotPhase1Service {

    PageResult<SoilTypeGridVO> pageSoilTypes(SoilTypeQueryDTO query);

    SoilTypeGridVO getSoilType(IdDTO params);

    Long saveSoilType(SoilTypeSaveDTO params);

    void removeSoilType(IdDTO params);

    PageResult<PlotTypeGridVO> pagePlotTypes(PlotTypeQueryDTO query);

    PlotTypeGridVO getPlotType(IdDTO params);

    Long savePlotType(PlotTypeSaveDTO params);

    void removePlotType(IdDTO params);

    PlotPolicyVO currentPolicy();

    Long savePolicy(PlotPolicySaveDTO params);

    Long activatePolicy(PlotPolicyActivateDTO params);

    PageResult<UserPlotAllocationGridVO> pageUserAllocations(UserPlotAllocationQueryDTO query);

    Long updateUserAllocation(UserPlotAllocationUpdateDTO params);
}
