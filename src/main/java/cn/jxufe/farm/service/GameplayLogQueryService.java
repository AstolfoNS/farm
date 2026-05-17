package cn.jxufe.farm.service;

import cn.jxufe.farm.bean.dto.CropActionLogQueryDTO;
import cn.jxufe.farm.bean.vo.CropActionLogRecordVO;
import cn.jxufe.farm.common.pages.PageResult;

public interface GameplayLogQueryService {

    PageResult<CropActionLogRecordVO> pageCropActionLogs(CropActionLogQueryDTO params);
}
