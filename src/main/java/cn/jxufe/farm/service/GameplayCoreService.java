package cn.jxufe.farm.service;

import cn.jxufe.farm.entity.UserAssetFlow;
import cn.jxufe.farm.entity.UserCropActionLog;
import cn.jxufe.farm.entity.UserFruit;
import cn.jxufe.farm.entity.UserInventoryFlow;
import cn.jxufe.farm.entity.base.BaseEntity;
import java.time.OffsetDateTime;

public interface GameplayCoreService {

  boolean isPlotBizType(String bizType);

  boolean isCropActionType(String actionType);

  String normalizePlotBizType(String bizType);

  String normalizeCropActionType(String actionType);

  int normalizePageNo(Integer pageNo);

  int normalizePageSize(Integer pageSize);

  Long extractLongFromExtData(String extData, String key);

  String safeString(String value);

  long safeLong(Long value);

  int safeInteger(Integer value);

  short safeShort(Short value);

  Long defaultLong(Long value, Long defaultValue);

  Integer defaultInt(Integer value, Integer defaultValue);

  long safeMultiply(long a, long b);

  long safeAdd(long a, long b);

  void initNewEntity(BaseEntity entity, Long operatorId, OffsetDateTime now);

  void touchForUpdate(BaseEntity entity, Long operatorId, OffsetDateTime now);

  UserFruit createUserFruit(Long userId, Long seedTypeId, OffsetDateTime now);

  UserInventoryFlow buildInventoryFlow(
      Long userId,
      String itemType,
      Long seedTypeId,
      String operationType,
      Long changeAmount,
      Long beforeAmount,
      Long afterAmount,
      Long beforeFrozenAmount,
      Long afterFrozenAmount,
      String bizType,
      String bizId,
      OffsetDateTime now,
      String extData);

  UserAssetFlow buildAssetFlow(
      Long userId,
      String assetType,
      String operationType,
      Long changeAmount,
      Long beforeAmount,
      Long afterAmount,
      String bizType,
      String bizId,
      OffsetDateTime now,
      String extData);

  UserCropActionLog buildCropActionLog(
      Long userId,
      Long plotId,
      Long cropId,
      Long seedTypeId,
      String actionType,
      String actionResult,
      OffsetDateTime now,
      String snapshot);
}
