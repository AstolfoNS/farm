# Service API 文档

## 服务架构层次

```
Controller → GameplayService (门面) → 具体 Service → DAO
           ↘ CropGameplayService / PlotGameplayService / PlotPhase1Service / SeedService / UserService / FileService
```

## 服务接口清单

| 接口                         | 实现类                                           | 职责                                       |
| ---------------------------- | ------------------------------------------------ | ------------------------------------------ |
| `UserService`                | `UserServiceImp`                                 | 用户 CRUD、会话管理、设置、新用户初始化    |
| `FileService`                | `FileServiceImp`                                 | 本地文件上传/删除、路径 URL 转换           |
| `SeedService`                | `SeedServiceImp` → `SeedServiceGuardedDecorator` | 种子类型/阶段管理、商店购买/出售、库存查询 |
| `CropGameplayService`        | `CropGameplayServiceImp`                         | 种植/收获/养护/铲除的门面层（含幂等）      |
| `CropLifecycleService`       | `CropLifecycleServiceImp`                        | 作物生命周期核心逻辑                       |
| `PlotGameplayService`        | `PlotGameplayServiceImp`                         | 地块解锁/扩地的门面层                      |
| `PlotManagementService`      | `PlotManagementServiceImp`                       | 地块解锁/扩地核心逻辑                      |
| `PlotPhase1Service`          | `PlotPhase1ServiceImp`                           | 土壤类型 CRUD、全局策略管理                |
| `PlotCostService`            | `PlotCostServiceImp`                             | 地块解锁金币成本计算                       |
| `FarmGameplayQueryService`   | `FarmGameplayQueryServiceImp`                    | 农场概览/种植面板/可种地块查询             |
| `GameplayService`            | `GameplayServiceImp`                             | 顶级门面：聚合作物+地块所有玩法            |
| `GameplayCoreService`        | `GameplayCoreServiceImp`                         | 通用工具：流水构建、类型转换、安全运算     |
| `GameplayLogQueryService`    | `GameplayLogQueryServiceImp`                     | 作物操作日志查询                           |
| `FarmRealtimePushService`    | `FarmRealtimePushServiceImp`                     | WebSocket 实时推送                         |
| `RequestIdempotencyService`  | `RequestIdempotencyServiceImp`                   | 请求幂等处理                               |
| `CropStatusSchedulerService` | `CropStatusSchedulerServiceImp`                  | 定时任务：扫描并更新作物状态               |

## 调用链

```
PlantCropDTO → GameplayService.plant()
                → RequestIdempotencyService (幂等)
                → CropLifecycleService.plant()
                    → SeedStageRuleSupport (阶段解析)
                    → isSoilIncompatible() (土壤兼容)
                    → 扣种子库存 → 创建作物 → 写日志

PlotExpandDTO → GameplayService.expandPlot()
                → PlotManagementService.expandPlot()
                    → 选土壤 → 算成本(soil.expandCostCoin) → 扣金币
                    → 创建地块(is_locked=true) → 写流水
```
