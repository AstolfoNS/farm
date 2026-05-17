package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.entity.UserAssetFlow;
import cn.jxufe.farm.entity.UserCropActionLog;
import cn.jxufe.farm.entity.UserFruit;
import cn.jxufe.farm.entity.UserInventoryFlow;
import cn.jxufe.farm.entity.base.BaseEntity;
import cn.jxufe.farm.service.GameplayCoreService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class GameplayCoreServiceImp implements GameplayCoreService {

    private static final int MAX_PAGE_SIZE = 100;

    @Override
    public boolean isPlotBizType(String bizType) {
        return "UNLOCK_PLOT".equalsIgnoreCase(bizType) || "EXPAND_PLOT".equalsIgnoreCase(bizType);
    }

    @Override
    public boolean isCropActionType(String actionType) {
        return "CARE".equalsIgnoreCase(actionType)
                || "HARVEST".equalsIgnoreCase(actionType)
                || "PLANT".equalsIgnoreCase(actionType);
    }

    @Override
    public String normalizePlotBizType(String bizType) {
        String value = safeString(bizType).trim().toUpperCase();
        if (value.isBlank()) {
            return "";
        }
        if ("UNLOCK".equals(value) || "UNLOCK_PLOT".equals(value)) {
            return "UNLOCK_PLOT";
        }
        if ("EXPAND".equals(value) || "EXPAND_PLOT".equals(value)) {
            return "EXPAND_PLOT";
        }
        return "";
    }

    @Override
    public String normalizeCropActionType(String actionType) {
        String value = safeString(actionType).trim().toUpperCase();
        if (value.isBlank()) {
            return "";
        }
        if ("CARE".equals(value)) {
            return "CARE";
        }
        if ("HARVEST".equals(value)) {
            return "HARVEST";
        }
        if ("PLANT".equals(value)) {
            return "PLANT";
        }
        return "";
    }

    @Override
    public int normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo <= 0) {
            return 1;
        }
        return pageNo;
    }

    @Override
    public int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    @Override
    public Long extractLongFromExtData(String extData, String key) {
        if (extData == null || extData.isBlank() || key == null || key.isBlank()) {
            return null;
        }
        String pattern = "\"" + key + "\":";
        int start = extData.indexOf(pattern);
        if (start < 0) {
            return null;
        }
        int valueStart = start + pattern.length();
        int valueEnd = valueStart;
        while (valueEnd < extData.length() && Character.isWhitespace(extData.charAt(valueEnd))) {
            valueEnd++;
        }
        int numberStart = valueEnd;
        while (valueEnd < extData.length() && Character.isDigit(extData.charAt(valueEnd))) {
            valueEnd++;
        }
        if (numberStart == valueEnd) {
            return null;
        }
        try {
            return Long.parseLong(extData.substring(numberStart, valueEnd));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

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
    public UserInventoryFlow buildInventoryFlow(Long userId,
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
    public UserAssetFlow buildAssetFlow(Long userId,
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
    public UserCropActionLog buildCropActionLog(Long userId,
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

