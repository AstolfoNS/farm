# PlotManagementService — 地块经营核心服务

**文件**: `service/PlotManagementService.java` | **实现**: `PlotManagementServiceImp`

## 方法

### `PlotUnlockResultVO unlockPlot(PlotUnlockDTO params)`
- **事务**: Y
- **流程**:
  1. 校验用户、地块存在且已锁定
  2. 校验经验门槛 (`experience >= unlockExperienceRequired`)
  3. 校验顺序解锁（必须解锁最小的锁定地块）
  4. 按公式计算金币成本并扣款
  5. 解锁地块 (`isLocked=false, unlockedAt=now`)
  6. 记录 UNLOCK_PLOT 资产流水

### `PlotExpandResultVO expandPlot(PlotExpandDTO params)`
- **事务**: Y
- **流程**:
  1. 选择土壤类型（不传则取最低级土壤）
  2. 校验经验门槛
  3. 按土壤 `expandCostCoin` 扣金币
  4. 创建新地块（**默认锁定** `isLocked=true`）
  5. 记录 EXPAND_PLOT 资产流水

### `PlotExpandOptionsVO listPlotExpandOptions(PlotExpandOptionsQueryDTO params)`
- 返回所有可用土壤的扩地选项，每个含成本和可行性校验

### `PlotStatusVO plotStatus(PlotStatusQueryDTO params)`
- 委托 `CropLifecycleService.myFarmOverview()` 获取状态

### `PageResult<PlotTradeRecordVO> pagePlotTrades(PlotTradeQueryDTO params)`
- 地块经营流水分页（UNLOCK_PLOT / EXPAND_PLOT）

### `List<PlotTradeBizTypeOptionVO> listPlotTradeBizTypeOptions()`
- 经营业务类型字典

## 关键变更（v2.0）

- 扩地成本由**公式计算**改为**土壤独立配置**（`SoilType.expandCostCoin`）
- 扩地后地块由**自动解锁**改为**默认锁定**
- 新的 `UserActionContext` 内部类统一处理金币和经验校验
