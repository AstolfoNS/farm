# GameplayCoreService — 通用核心工具服务

**文件**: `service/GameplayCoreService.java` | **实现**: `GameplayCoreServiceImp`

## 类型校验与规范化

| 方法 | 说明 |
|------|------|
| `isPlotBizType(String)` | 校验是否为合法地块经营类型 |
| `isCropActionType(String)` | 校验是否为合法作物操作类型 |
| `normalizePlotBizType(String)` | 规范化地块业务类型 |
| `normalizeCropActionType(String)` | 规范化作物操作类型 |

## 安全类型转换

| 方法 | 说明 |
|------|------|
| `safeString(String)` | null → "" |
| `safeLong(Long)` | null → 0L |
| `safeInteger(Integer)` | null → 0 |
| `safeShort(Short)` | null → 0 |
| `defaultLong(Long, Long)` | null → 默认值 |
| `defaultInt(Integer, Integer)` | null → 默认值 |
| `safeMultiply(long, long)` | 安全乘法（溢出检测） |
| `safeAdd(long, long)` | 安全加法（溢出检测） |
| `extractLongFromExtData(String, String)` | 从 JSON 扩展数据提取 Long 值 |

## 分页工具

| 方法 | 说明 |
|------|------|
| `normalizePageNo(Integer)` | 规范化页码（≥1） |
| `normalizePageSize(Integer)` | 规范化页大小（1~100） |

## 实体工具

| 方法 | 说明 |
|------|------|
| `initNewEntity(BaseEntity, Long, OffsetDateTime)` | 初始化新实体公共字段 |
| `touchForUpdate(BaseEntity, Long, OffsetDateTime)` | 更新实体的 updatedAt/updatedBy |
| `createUserFruit(Long, Long, OffsetDateTime)` | 创建用户果实库存记录 |

## 流水构建器

### `UserInventoryFlow buildInventoryFlow(...)`
- 参数: userId, itemType, seedTypeId, operationType, changeAmount, beforeAmount, afterAmount, beforeFrozenAmount, afterFrozenAmount, bizType, bizId, now, extData
- 构建库存流水实体

### `UserAssetFlow buildAssetFlow(...)`
- 参数: userId, assetType, operationType, changeAmount, beforeAmount, afterAmount, bizType, bizId, now, extData
- 构建资产流水实体

### `UserCropActionLog buildCropActionLog(...)`
- 参数: userId, plotId, cropId, seedTypeId, actionType, actionResult, now, snapshot
- 构建作物行为日志实体
