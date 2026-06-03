package cn.jxufe.farm.service;

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
import cn.jxufe.farm.common.pages.PageResult;

import java.util.List;

public interface PlotGameplayService {

    PlotUnlockResultVO unlockPlot(PlotUnlockDTO params);

    PlotExpandResultVO expandPlot(PlotExpandDTO params);

    PlotExpandOptionsVO listPlotExpandOptions(PlotExpandOptionsQueryDTO params);

    PlotStatusVO plotStatus(PlotStatusQueryDTO params);

    PageResult<PlotTradeRecordVO> pagePlotTrades(PlotTradeQueryDTO params);

    List<PlotTradeBizTypeOptionVO> listPlotTradeBizTypeOptions();

}
