# DTO 请求参数类文档 (38 个)

## 通用

| 类 | 字段 | 说明 |
|----|------|------|
| `PageQueryDTO` | `page, rows, sort, order` | 分页排序公共参数 |
| `IdDTO` | `id` (Long, @Positive) | 通用 ID 参数 |

## 用户模块 (5 个)

| 类 | 字段 | 说明 |
|----|------|------|
| `UserGridQueryDTO` | `name, page, rows, sort, order` | 用户分页搜索 |
| `UserAddOrUpdateDTO` | `id, username, nickname, experience, exp, score, coin, avatarPath` | 增/改用户 |
| `SetCurUserDTO` | `id` (Long) | 设置当前用户 |
| `UserAvatarUpdateDTO` | `id, avatarPath` | 更新头像 |
| `UserSettingsUpdateDTO` | `effectEnabled, effectVolume, bgmEnabled, bgmVolume` | 保存设置 |

## 文件模块 (2 个)

| 类 | 字段 | 说明 |
|----|------|------|
| `FileUploadResultDTO` | `relativePath, accessUrl, originalName, size, contentType` | 上传结果 |
| `FileRelativePathDTO` | `relativePath` | 文件路径参数 |

## 种子模块 (13 个)

| 类 | 字段 | 说明 |
|----|------|------|
| `SeedTypeQueryDTO` | `name, page, rows, sort, order` | 种子分页搜索 |
| `SeedAddOrUpdateDTO` | `id, name, coverImageUrl, seedQualityId, soilTypeIds, enableSoilTypeBits, level, price, harvestExperience, harvestFruitNumber, fruitLossPerBug, bugKillCoinReward, ...` (约30字段) | 种子完整配置 |
| `SeedStageQueryDTO` | `seedTypeId` | 阶段查询 |
| `SeedStageAddOrUpdateDTO` | `id, seedTypeId, growthStageId, stageIndex, durationSeconds, bugProbability, pestProbability, width, height, offsetX, offsetY, assetUrl` | 阶段配置 |
| `SeedShopQueryDTO` | `userId, name, seedQualityId, level, page, rows, sort, order` | 商店分页搜索 |
| `SeedShopHomeQueryDTO` | 同上 | 商店首页查询 |
| `SeedShopBuyDTO` | `requestId` (@NotBlank), `userId, seedTypeId, quantity` | 购买种子 |
| `SeedShopSellFruitDTO` | `requestId`, `userId, seedTypeId, quantity` | 出售果实 |
| `SeedShopTradeQueryDTO` | `userId, tradeType, page, rows` | 交易记录查询 |
| `SeedShopOverviewDTO` | `userId` | 商店概览 |
| `SeedInventoryQueryDTO` | `userId, name, page, rows` | 种子库存查询 |
| `SeedFruitInventoryQueryDTO` | `userId, name, page, rows` | 果实库存查询 |

## 作物模块 (7 个)

| 类 | 字段 | 说明 |
|----|------|------|
| `PlantCropDTO` | `requestId, userId, plotId, seedTypeId` | 种植 |
| `HarvestCropDTO` | `requestId, userId, plotId` | 收获 |
| `ClearCropDTO` | `requestId, userId, plotId` | 铲除 |
| `CareCropDTO` | `userId, plotId` | 养护杀虫 |
| `CropActionLogQueryDTO` | `userId, actionType, plotId, page, rows` | 行为日志查询 |
| `MyFarmOverviewDTO` | `userId` | 农场概览 |
| `MyPlantingPanelDTO` | `userId` | 种植面板 |
| `SeedPlantablePlotsDTO` | `userId, seedTypeId` | 可种地块查询 |

## 地块模块 (8 个)

| 类 | 字段 | 说明 |
|----|------|------|
| `PlotUnlockDTO` | `userId, plotId` | 解锁地块 |
| `PlotExpandDTO` | `userId, soilTypeId` | 扩地 |
| `PlotExpandOptionsQueryDTO` | `userId` | 扩地选项查询 |
| `PlotStatusQueryDTO` | `userId` | 地块状态 |
| `PlotTradeQueryDTO` | `userId, bizType, page, rows` | 经营流水查询 |
| `SoilTypeQueryDTO` | `name, page, rows, sort, order` | 土壤搜索 |
| `SoilTypeSaveDTO` | `id, name, bitCode, coverImageUrl, level, unlockExperienceRequired, expandCostCoin, growSpeedMultiplier, description` | 土壤保存 |
| `PlotPolicySaveDTO` | `id, policyName, policyVersion, active, effectiveScope, publishStatus, defaultTotalPlotCount, defaultUnlockedPlotCount, defaultLockedPlotCount, defaultLockRuleCode, defaultLockReason` | 策略保存 |
| `PlotPolicyActivateDTO` | `id` | 激活策略 |
