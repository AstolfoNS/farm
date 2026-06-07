# CropStatusSchedulerService — 作物状态定时调度

**文件**: `service/CropStatusSchedulerService.java` | **实现**: `CropStatusSchedulerServiceImp`

## 方法

| 方法 | 说明 |
|------|------|
| `scheduleTick()` | 扫描所有活跃作物，根据当前时间更新 `growStatus`（生长→成熟→枯萎），标记变化用户，触发实时推送 |

## 配置

```properties
farm.gameplay.realtime.crop-status-refresh-interval-ms=1000
farm.gameplay.realtime.scheduler.parallelism=4
farm.gameplay.realtime.scheduler.partition-size=500
```

- 使用 `@Scheduled` 定时执行
- 支持并行处理 (`parallelism`) 和分区 (`partitionSize`) 配置
