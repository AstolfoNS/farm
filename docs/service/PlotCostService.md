# PlotCostService — 地块成本计算服务

**文件**: `service/PlotCostService.java` | **实现**: `PlotCostServiceImp`

## 方法

### `long calculateUnlockCostCoin(Short plotIndex)`
- 计算解锁指定序号地块所需的金币

**公式**:
```
freeLimit = farm.gameplay.policy.plot.unlock.free-plot-index-limit (默认 3)
if (plotIndex <= freeLimit) return 0L
return baseCostCoin(80) + (plotIndex - freeLimit - 1) × costStepCoin(40)
```

**示例**: 地块1-3免费, 地块4=80金币, 地块5=120金币, 地块6=160金币...
