# PlotPhase1Service — 地块配置管理服务

**文件**: `service/PlotPhase1Service.java` | **实现**: `PlotPhase1ServiceImp`

## 土壤管理

| 方法 | 参数 | 返回 | 事务 | 说明 |
|------|------|------|------|------|
| `pageSoilTypes` | `SoilTypeQueryDTO` | `PageResult<SoilTypeGridVO>` | N | 按名称模糊搜索分页 |
| `getSoilType` | `IdDTO` | `SoilTypeGridVO` | N | 按 ID 获取详情 |
| `saveSoilType` | `SoilTypeSaveDTO` | `Long` | Y | 新增/更新。新增时 bitCode 自动分配(2的幂) |
| `removeSoilType` | `IdDTO` | `void` | Y | 软删除。被 user_plots 引用时禁止 |

## 策略管理

| 方法 | 参数 | 返回 | 事务 | 说明 |
|------|------|------|------|------|
| `currentPolicy` | — | `PlotPolicyVO` | N | 当前激活策略。无策略返回 fallback(6/1) |
| `savePolicy` | `PlotPolicySaveDTO` | `Long` | Y | 新增/更新。active=true 时失活其他策略 |
| `activatePolicy` | `PlotPolicyActivateDTO` | `Long` | Y | 激活指定策略，失活其他。仅新用户生效 |
