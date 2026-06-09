package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.entity.UserAssetFlow;
import cn.jxufe.farm.entity.UserCropActionLog;
import cn.jxufe.farm.entity.UserFruit;
import cn.jxufe.farm.entity.UserInventoryFlow;
import cn.jxufe.farm.entity.base.BaseEntity;
import cn.jxufe.farm.service.GameplayCoreService;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class GameplayCoreServiceImp implements GameplayCoreService {

  private static final int MAX_PAGE_SIZE = 100;

  private static final int DEFAULT_PAGE_SIZE = 10;

  private static final int DEFAULT_PAGE_NO = 1;

  @Override
  public boolean isPlotBizType(String bizType) {
    return !normalizePlotBizType(bizType).isEmpty();
  }

  @Override
  public boolean isCropActionType(String actionType) {
    return !normalizeCropActionType(actionType).isEmpty();
  }

  @Override
  public String normalizePlotBizType(String bizType) {
    if (bizType == null || bizType.isBlank()) {
      return "";
    }
    return switch (bizType.trim().toUpperCase()) {
      case "UNLOCK", "UNLOCK_PLOT" -> "UNLOCK_PLOT";
      case "EXPAND", "EXPAND_PLOT" -> "EXPAND_PLOT";
      default -> "";
    };
  }

  @Override
  public String normalizeCropActionType(String actionType) {
    if (actionType == null || actionType.isBlank()) {
      return "";
    }
    return switch (actionType.trim().toUpperCase()) {
      case "CARE" -> "CARE";
      case "CLEAR", "CLEAN" -> "CLEAR";
      case "HARVEST" -> "HARVEST";
      case "PLANT" -> "PLANT";
      case "BUG_SPAWN" -> "BUG_SPAWN";
      default -> "";
    };
  }

  @Override
  public int normalizePageNo(Integer pageNo) {
    return (pageNo == null || pageNo <= 0) ? DEFAULT_PAGE_NO : pageNo;
  }

  @Override
  public int normalizePageSize(Integer pageSize) {
    return (pageSize == null || pageSize <= 0)
        ? DEFAULT_PAGE_SIZE
        : Math.min(pageSize, MAX_PAGE_SIZE);
  }

  @Override
  public Long extractLongFromExtData(String extData, String key) {
    if (extData == null || extData.isBlank() || key == null || key.isBlank()) {
      return null;
    }

    String regex = "\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)";
    Matcher matcher = Pattern.compile(regex).matcher(extData);
    if (matcher.find()) {
      try {
        return Long.parseLong(matcher.group(1));
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    return null;
  }

  /* =========================================================
   *  新增 & 强化的安全类型与计算工具
   * ========================================================= */

  @Override
  public String safeString(String value) {
    return value == null ? "" : value;
  }

  @Override
  public long safeLong(Long value) {
    return value == null ? 0L : value;
  }

  @Override
  public int safeInteger(Integer value) {
    return value == null ? 0 : value;
  }

  @Override
  public short safeShort(Short value) {
    return value == null ? 0 : value;
  }

  @Override
  public Long defaultLong(Long value, Long defaultValue) {
    return value == null ? defaultValue : value;
  }

  @Override
  public Integer defaultInt(Integer value, Integer defaultValue) {
    return value == null ? defaultValue : value;
  }

  @Override
  public long safeMultiply(long a, long b) {
    try {
      return Math.multiplyExact(a, b);
    } catch (ArithmeticException ex) {
      return Long.MAX_VALUE;
    }
  }

  @Override
  public long safeAdd(long a, long b) {
    try {
      return Math.addExact(a, b);
    } catch (ArithmeticException ex) {
      return Long.MAX_VALUE;
    }
  }

  /* =========================================================
   *  实体初始化与流水构建
   * ========================================================= */

  @Override
  public void initNewEntity(BaseEntity entity, Long operatorId, OffsetDateTime now) {
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    entity.setCreatedBy(operatorId);
    entity.setUpdatedBy(operatorId);
    entity.setStatus((short) 1);
    entity.setIsDeleted(false);
    entity.setOptLockVersion(0);
  }

  @Override
  public void touchForUpdate(BaseEntity entity, Long operatorId, OffsetDateTime now) {
    entity.setUpdatedAt(now);
    entity.setUpdatedBy(operatorId);
  }

  @Override
  public UserFruit createUserFruit(Long userId, Long seedTypeId, OffsetDateTime now) {
    UserFruit userFruit = new UserFruit();
    initNewEntity(userFruit, userId, now);
    userFruit.setUserId(userId);
    userFruit.setSeedTypeId(seedTypeId);
    userFruit.setQuantity(0L);
    userFruit.setFrozenQuantity(0L);
    return userFruit;
  }

  @Override
  public UserInventoryFlow buildInventoryFlow(
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
      String extData) {
    UserInventoryFlow flow = new UserInventoryFlow();
    initNewEntity(flow, userId, now);
    flow.setUserId(userId);
    flow.setItemType(itemType);
    flow.setSeedTypeId(seedTypeId);
    flow.setOperationType(operationType);
    flow.setChangeAmount(changeAmount);
    flow.setBeforeAmount(beforeAmount);
    flow.setAfterAmount(afterAmount);
    flow.setBeforeFrozenAmount(beforeFrozenAmount);
    flow.setAfterFrozenAmount(afterFrozenAmount);
    flow.setBizType(bizType);
    flow.setBizId(bizId);
    flow.setOccurredAt(now);
    flow.setExtData(extData);
    return flow;
  }

  @Override
  public UserAssetFlow buildAssetFlow(
      Long userId,
      String assetType,
      String operationType,
      Long changeAmount,
      Long beforeAmount,
      Long afterAmount,
      String bizType,
      String bizId,
      OffsetDateTime now,
      String extData) {
    UserAssetFlow flow = new UserAssetFlow();
    initNewEntity(flow, userId, now);
    flow.setUserId(userId);
    flow.setAssetType(assetType);
    flow.setOperationType(operationType);
    flow.setChangeAmount(changeAmount);
    flow.setBeforeAmount(beforeAmount);
    flow.setAfterAmount(afterAmount);
    flow.setBizType(bizType);
    flow.setBizId(bizId);
    flow.setOccurredAt(now);
    flow.setExtData(extData);
    return flow;
  }

  @Override
  public UserCropActionLog buildCropActionLog(
      Long userId,
      Long plotId,
      Long cropId,
      Long seedTypeId,
      String actionType,
      String actionResult,
      OffsetDateTime now,
      String snapshot) {
    UserCropActionLog log = new UserCropActionLog();
    initNewEntity(log, userId, now);
    log.setUserId(userId);
    log.setPlotId(plotId);
    log.setCropId(cropId);
    log.setSeedTypeId(seedTypeId);
    log.setActionType(actionType);
    log.setActionResult(actionResult);
    log.setActionAt(now);
    log.setActionSnapshot(snapshot);
    return log;
  }
}
