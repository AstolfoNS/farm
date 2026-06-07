# GameplayService — 顶级门面服务接口

**文件**: `service/GameplayService.java` | **实现**: `GameplayServiceImp`

## 说明

`GameplayService` 是作物和地块玩法的顶级门面，聚合了 `CropLifecycleService` 和 `PlotManagementService`。Controller 通过此接口调用所有玩法功能，在门面层统一处理幂等。

## 作物生命周期（委托 → CropLifecycleService）

| 方法      | 参数             | 返回              | 幂等 |
| --------- | ---------------- | ----------------- | ---- |
| `plant`   | `PlantCropDTO`   | `PlantResultVO`   | Y    |
| `harvest` | `HarvestCropDTO` | `HarvestResultVO` | Y    |
| `clear`   | `ClearCropDTO`   | `ClearResultVO`   | Y    |
| `care`    | `CareCropDTO`    | `CareResultVO`    | N    |

## 农场查询（委托 → CropLifecycleService）

| 方法                 | 参数                    | 返回                   |
| -------------------- | ----------------------- | ---------------------- |
| `myFarmOverview`     | `MyFarmOverviewDTO`     | `MyFarmOverviewVO`     |
| `myPlantingPanel`    | `MyPlantingPanelDTO`    | `MyPlantingPanelVO`    |
| `seedPlantablePlots` | `SeedPlantablePlotsDTO` | `SeedPlantablePlotsVO` |

## 地块经营（委托 → PlotManagementService）

| 方法                          | 参数                        | 返回                             |
| ----------------------------- | --------------------------- | -------------------------------- |
| `unlockPlot`                  | `PlotUnlockDTO`             | `PlotUnlockResultVO`             |
| `expandPlot`                  | `PlotExpandDTO`             | `PlotExpandResultVO`             |
| `listPlotExpandOptions`       | `PlotExpandOptionsQueryDTO` | `PlotExpandOptionsVO`            |
| `plotStatus`                  | `PlotStatusQueryDTO`        | `PlotStatusVO`                   |
| `pagePlotTrades`              | `PlotTradeQueryDTO`         | `PageResult<PlotTradeRecordVO>`  |
| `listPlotTradeBizTypeOptions` | —                           | `List<PlotTradeBizTypeOptionVO>` |

## 日志查询（委托 → GameplayLogQueryService）

| 方法                 | 参数                    | 返回                                |
| -------------------- | ----------------------- | ----------------------------------- |
| `pageCropActionLogs` | `CropActionLogQueryDTO` | `PageResult<CropActionLogRecordVO>` |
