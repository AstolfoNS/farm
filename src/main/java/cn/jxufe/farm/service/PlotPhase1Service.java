package cn.jxufe.farm.service;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.PlotPolicyActivateDTO;
import cn.jxufe.farm.bean.dto.PlotPolicySaveDTO;
import cn.jxufe.farm.bean.dto.SoilTypeQueryDTO;
import cn.jxufe.farm.bean.dto.SoilTypeSaveDTO;
import cn.jxufe.farm.bean.vo.PlotPolicyVO;
import cn.jxufe.farm.bean.vo.SoilTypeGridVO;
import cn.jxufe.farm.common.pages.PageResult;

public interface PlotPhase1Service {

    PageResult<SoilTypeGridVO> pageSoilTypes(SoilTypeQueryDTO query);

    SoilTypeGridVO getSoilType(IdDTO params);

    Long saveSoilType(SoilTypeSaveDTO params);

    void removeSoilType(IdDTO params);

    PlotPolicyVO currentPolicy();

    Long savePolicy(PlotPolicySaveDTO params);

    Long activatePolicy(PlotPolicyActivateDTO params);
}
