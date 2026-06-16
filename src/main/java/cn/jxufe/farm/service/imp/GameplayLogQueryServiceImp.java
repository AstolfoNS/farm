package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.CropActionLogQueryDTO;
import cn.jxufe.farm.bean.vo.CropActionLogRecordVO;
import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exceptions.ServiceException;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.dao.SeedTypeDao;
import cn.jxufe.farm.dao.UserCropActionLogDao;
import cn.jxufe.farm.dao.UserDao;
import cn.jxufe.farm.entity.SeedType;
import cn.jxufe.farm.entity.UserCropActionLog;
import cn.jxufe.farm.service.GameplayCoreService;
import cn.jxufe.farm.service.GameplayLogQueryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GameplayLogQueryServiceImp implements GameplayLogQueryService {

  private final UserDao userDao;
  private final SeedTypeDao seedTypeDao;
  private final UserCropActionLogDao userCropActionLogDao;
  private final GameplayCoreService gameplayCoreService;

  public GameplayLogQueryServiceImp(
      UserDao userDao,
      SeedTypeDao seedTypeDao,
      UserCropActionLogDao userCropActionLogDao,
      GameplayCoreService gameplayCoreService) {
    this.userDao = userDao;
    this.seedTypeDao = seedTypeDao;
    this.userCropActionLogDao = userCropActionLogDao;
    this.gameplayCoreService = gameplayCoreService;
  }

  @Override
  public PageResult<CropActionLogRecordVO> pageCropActionLogs(CropActionLogQueryDTO params) {
    CropActionLogQueryDTO request = params == null ? new CropActionLogQueryDTO() : params;
    if (request.getUserId() == null || request.getUserId() <= 0) {
      throw new ServiceException(BizErrorCode.PARAM_INVALID, "用户ID无效");
    }
    if (userDao.findByIdAndIsDeletedFalse(request.getUserId()).isEmpty()) {
      throw new ServiceException(BizErrorCode.USER_NOT_FOUND, "用户不存在");
    }

    int pageNo = gameplayCoreService.normalizePageNo(request.getPage());
    int pageSize = gameplayCoreService.normalizePageSize(request.getRows());
    String actionTypeFilter = gameplayCoreService.normalizeCropActionType(request.getActionType());
    if (request.getActionType() != null
        && !request.getActionType().isBlank()
        && actionTypeFilter.isBlank()) {
      throw new ServiceException(
          BizErrorCode.CROP_ACTION_TYPE_UNSUPPORTED,
          "actionType 仅支持 CARE / CLEAR / HARVEST / PLANT / BUG_SPAWN");
    }
    Long plotIdFilter = request.getPlotId();

    Map<Long, String> seedNameById = new HashMap<>();
    List<SeedType> seedTypes = seedTypeDao.findByIsDeletedFalseOrderByIdAsc();
    for (SeedType seedType : seedTypes) {
      seedNameById.put(seedType.getId(), gameplayCoreService.safeString(seedType.getName()));
    }

    List<UserCropActionLog> actionLogs =
        userCropActionLogDao.findByUserIdAndIsDeletedFalseOrderByActionAtDesc(request.getUserId());
    List<CropActionLogRecordVO> records = new ArrayList<>();
    for (UserCropActionLog actionLog : actionLogs) {
      String actionType =
          gameplayCoreService.safeString(actionLog.getActionType()).trim().toUpperCase();
      if (!gameplayCoreService.isCropActionType(actionType)) {
        continue;
      }
      if (!actionTypeFilter.isBlank() && !actionTypeFilter.equals(actionType)) {
        continue;
      }
      if (plotIdFilter != null && plotIdFilter > 0 && !plotIdFilter.equals(actionLog.getPlotId())) {
        continue;
      }

      CropActionLogRecordVO record = new CropActionLogRecordVO();
      record.setId(actionLog.getId());
      record.setPlotId(actionLog.getPlotId());
      record.setCropId(actionLog.getCropId());
      record.setSeedTypeId(actionLog.getSeedTypeId());
      record.setSeedTypeName(seedNameById.getOrDefault(actionLog.getSeedTypeId(), ""));
      record.setActionType(actionType);
      record.setActionResult(gameplayCoreService.safeString(actionLog.getActionResult()));
      record.setActionAt(actionLog.getActionAt());
      record.setActionSnapshot(gameplayCoreService.safeString(actionLog.getActionSnapshot()));
      records.add(record);
    }

    return PageResult.of(records, pageNo, pageSize);
  }
}
