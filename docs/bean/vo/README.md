# VO 响应视图类文档 (40 个)

## 通用

| 类             | 字段                                                 | 说明     |
| -------------- | ---------------------------------------------------- | -------- |
| `OptionVO`     | `id, text`                                           | 下拉选项 |
| `SoilOptionVO` | `id, text, bitCode, level, unlockExperienceRequired` | 土壤选项 |

## 用户模块 (5 个)

| 类               | 字段                                                                                         | 说明                         |
| ---------------- | -------------------------------------------------------------------------------------------- | ---------------------------- |
| `UserInfoVO`     | `id, username, nickname, experience, score, coin, avatarPath, head`                          | 用户列表项                   |
| `CurUserVO`      | `id, username, nickname, experience, score, coin, avatarPath, head, loggedIn, defaultAssets` | 当前用户（含资产和默认资源） |
| `UserSettingsVO` | `userId, loggedIn, effectEnabled, effectVolume, bgmEnabled, bgmVolume, preferencesJson`      | 用户设置                     |
| `UserAvatarVO`   | `id, avatarPath, head`                                                                       | 头像信息                     |
| `AvatarUploadVO` | `relativePath, accessUrl, path`                                                              | 头像上传结果                 |

## 文件模块 (1 个)

| 类          | 字段                              | 说明     |
| ----------- | --------------------------------- | -------- |
| `FileUrlVO` | `relativePath, accessUrl, exists` | 文件 URL |

## 种子模块 (12 个)

| 类                          | 关键字段                                                                                                                                           | 说明       |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ---------- |
| `SeedGridVO`                | `id, name, coverImageUrl, seedQualityId/Name, enableSoilTypeBits/Names, level, price, harvestExperience, harvestFruitNumber, fruitLossPerBug, ...` | 种子列表项 |
| `SeedStageGridVO`           | `id, seedTypeId, seedName, growthStageId, growthStageName, stageIndex, durationSeconds, bugProbability, width, height, offsetX, offsetY, assetUrl` | 阶段列表项 |
| `SeedShopItemVO`            | 含经济分析：`singleHarvestFruitValue, totalHarvestFruitValue, estimatedNetValue`                                                                   | 商店商品   |
| `SeedShopBuyResultVO`       | `userId, seedTypeId, buyQuantity, totalCostCoin, beforeCoin, afterCoin, beforeSeedQuantity, afterSeedQuantity`                                     | 购买结果   |
| `SeedShopSellFruitResultVO` | 对应出售结果的资产+库存快照                                                                                                                        | 出售结果   |
| `SeedShopTradeRecordVO`     | `bizId, tradeType, seedTypeId, seedName, itemQuantity, coinChangeAmount, occurredAt`                                                               | 交易记录   |
| `SeedInventoryItemVO`       | `seedTypeId, seedName, quantity, frozenQuantity, availableQuantity`                                                                                | 种子库存   |
| `SeedFruitInventoryItemVO`  | `seedTypeId, seedName, fruitQuantity, frozenQuantity, availableQuantity, estimatedIncomeCoin`                                                      | 果实库存   |
| `SeedShopOverviewVO`        | `userId, currentCoin, sellableTotalValue, sellableFruitTotalCount, purchasableSeedTypeCount`                                                       | 商店概览   |
| `SeedShopHomeVO`            | `{overview, shopPage}`                                                                                                                             | 商店首页   |
| `SeedBackpackItemVO`        | `userSeedId, seedTypeId, seedTypeName, quantity, frozenQuantity, availableQuantity, selectable`                                                    | 种子背包   |
| `SeedPlantablePlotsVO`      | `userId, seedTypeId, plantableCount, plots(List<PlantablePlotVO>)`                                                                                 | 可种地块   |

## 作物模块 (6 个)

| 类                      | 关键字段                                                                                                                                                                                                                             | 说明     |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------- |
| `PlantResultVO`         | `userId, plotId, cropId, seedTypeId, growStatus, currentStageIndex, expectedRipeAt, expectedWitheredAt`                                                                                                                              | 种植结果 |
| `HarvestResultVO`       | `harvestFruitNumber, totalFruitQuantity, experienceGain, scoreGain, totalBugPenaltyFruit, bugCountBefore/After, cropCleared, nextGrowStatus`                                                                                         | 收获结果 |
| `CareResultVO`          | `bugCountBefore/After, bugRemovedCount, coinGain, experienceGain, scoreGain, currentCoin`                                                                                                                                            | 养护结果 |
| `ClearResultVO`         | `growStatusBefore, stageIndexBefore, bugCountBefore, cleared, clearedAt`                                                                                                                                                             | 铲除结果 |
| `CropOverviewVO`        | `cropId, seedTypeId, seedTypeName, growStatus, currentStageIndex, harvestCount, plantedAt, expectedRipeAt, expectedWitheredAt, remainMatureSeconds, remainWitherSeconds, bugCount, maxBugLimit, canCare, harvestable, stageAssetUrl` | 作物概览 |
| `CropActionLogRecordVO` | `id, plotId, cropId, seedTypeId, seedTypeName, actionType, actionResult, actionAt, actionSnapshot`                                                                                                                                   | 行为日志 |

## 地块模块 (10 个)

| 类                         | 说明                                         |
| -------------------------- | -------------------------------------------- |
| `PlotStatusVO`             | 地块状态总览（含扩展成本、下一可解锁地块）   |
| `PlotOverviewVO`           | 单个地块完整视图（含锁状态、土壤信息、作物） |
| `PlotUnlockResultVO`       | 解锁结果（含成本、资产变化）                 |
| `PlotExpandResultVO`       | 扩地结果（含土壤、成本、资产变化）           |
| `PlotExpandOptionsVO`      | 扩地选项列表                                 |
| `PlotExpandOptionVO`       | 单个扩地选项（土壤信息+可行性）              |
| `PlotTradeRecordVO`        | 地块经营流水记录                             |
| `PlotTradeBizTypeOptionVO` | 经营业务类型 {bizType, text}                 |
| `SoilTypeGridVO`           | 土壤类型列表项（含 expandCostCoin）          |
| `PlotPolicyVO`             | 全局策略视图                                 |

## 实时推送 (1 个)

| 类                      | 字段                                                     | 说明               |
| ----------------------- | -------------------------------------------------------- | ------------------ |
| `FarmRealtimeMessageVO` | `event, userId, serverTime, cropStatusChanged, overview` | WebSocket 推送消息 |

## 其他

| 类                  | 说明                                           |
| ------------------- | ---------------------------------------------- |
| `MyFarmOverviewVO`  | 农场总览（总地块/已解锁/占用/可收获/地块列表） |
| `MyPlantingPanelVO` | 种植面板（背包种子+空闲地块）                  |
| `PlantablePlotVO`   | 可种植地块简化信息                             |
