# CropLifecycleService — 作物生命周期核心服务

**文件**: `service/CropLifecycleService.java` | **实现**: `CropLifecycleServiceImp`

## 方法

| 方法                 | 参数                    | 返回                   | 事务 | 说明                                                                            |
| -------------------- | ----------------------- | ---------------------- | ---- | ------------------------------------------------------------------------------- |
| `plant`              | `PlantCropDTO`          | `PlantResultVO`        | Y    | 种植：校验土壤兼容(bits)、种子库存，扣减种子，创建作物实例，设置成熟/枯萎时间轴 |
| `harvest`            | `HarvestCropDTO`        | `HarvestResultVO`      | Y    | 收获：同步状态，计算虫害减产，增加果实/经验/积分，多次收获回退阶段              |
| `clear`              | `ClearCropDTO`          | `ClearResultVO`        | Y    | 铲除：软删除作物，记录铲除前状态                                                |
| `care`               | `CareCropDTO`           | `CareResultVO`         | Y    | 养护杀虫：每次清1只虫，发金币/经验/积分奖励                                     |
| `myFarmOverview`     | `MyFarmOverviewDTO`     | `MyFarmOverviewVO`     | N    | 农场概览：所有地块状态、作物信息、成熟倒计时、可收获标记                        |
| `myPlantingPanel`    | `MyPlantingPanelDTO`    | `MyPlantingPanelVO`    | N    | 种植面板：种子背包列表 + 空闲可种地块数                                         |
| `seedPlantablePlots` | `SeedPlantablePlotsDTO` | `SeedPlantablePlotsVO` | N    | 可种地块：按土壤兼容性返回该种子可种植的地块                                    |

## 核心业务规则

- **状态机**: 生长中(1) → 成熟待收(2) / 已枯萎(3)
- **虫害减产**: `实际=MAX(0, 基础-虫数×每虫损失)`
- **土壤兼容**: `(seed.enableSoilTypeBits & soil.bitCode) == soil.bitCode`
- **收获上限**: `harvestCount >= maxHarvestCount` → 清除作物
- **再生**: 回退到 `regrowStageIndex` 并重置时间轴
