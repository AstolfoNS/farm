# DAO API 文档

所有 DAO 接口基于 `JpaRepository<T, Long>`，使用 Spring Data JPA 方法命名推导 + `@Query` 自定义查询。统一使用 `schema = "farm"`。

## DAO 清单

| 接口                    | 实体                 | 特殊方法                                                       |
| ----------------------- | -------------------- | -------------------------------------------------------------- |
| `UserDao`               | `User`               | `decreaseCoinIfEnough`(原子扣金币), `increaseCoin`(原子加金币) |
| `UserPlotDao`           | `UserPlot`           | 按 userId+plotIndex 查询, 统计已解锁/总数                      |
| `UserCropDao`           | `UserCrop`           | 按 plotId 查唯一活跃作物                                       |
| `UserSeedDao`           | `UserSeed`           | `increaseQuantity`, `decreaseAvailableQuantityIfEnough`        |
| `UserFruitDao`          | `UserFruit`          | `increaseQuantity`, `decreaseAvailableQuantityIfEnough`        |
| `UserAssetFlowDao`      | `UserAssetFlow`      | 按 userId 按时间倒序                                           |
| `UserInventoryFlowDao`  | `UserInventoryFlow`  | 按 userId+itemType 按时间倒序                                  |
| `UserCropActionLogDao`  | `UserCropActionLog`  | 按 userId 按时间倒序                                           |
| `SeedTypeDao`           | `SeedType`           | 模糊搜索分页                                                   |
| `SeedQualityDao`        | `SeedQuality`        | 按 ID 查                                                       |
| `SeedGrowthStageDao`    | `SeedGrowthStage`    | 按 seedTypeId+stageIndex 查                                    |
| `GrowthStageDao`        | `GrowthStage`        | 按 ID 查                                                       |
| `SoilTypeDao`           | `SoilType`           | 模糊搜索分页, 按 level 取最小, 按 bitCode 查                   |
| `PlotPolicyDao`         | `PlotPolicy`         | 查当前激活策略                                                 |
| `AssetDefaultDao`       | `AssetDefault`       | 按 assetKey 查                                                 |
| `RequestIdempotencyDao` | `RequestIdempotency` | 按 userId+bizType+requestId 查唯一                             |

## 通用查询模式

- 所有查询带 `IsDeletedFalse` 条件实现软删除过滤
- 分页查询使用 `Page<T>` + `Pageable`
- 原子操作使用 `@Modifying` + `@Query` (JPQL)
