# Phase 0 字段与接口契约（规则冻结版）

## 1. 目标与范围

本文档用于冻结以下能力的统一口径，作为 Phase 1~Phase 6 的唯一实现依据：

1. 地块规则体系（`plot_type` / `plot_policy` / `user_plot_allocation`）
2. 种子类型与种子阶段的关键字段语义
3. 后端管理接口契约（请求/响应/错误）
4. `myFarmOverview` 扩展结构（地块规则来源可追溯）

---

## 2. 核心术语冻结

1. `soil_type`：土壤类型，定义“是否可种”的兼容基线（由 `seed.enableSoilTypeBits` 决定）。
2. `plot_type`：地块类型，定义“地块表现与规则策略维度”（图标、是否需解锁、默认可用等）。
3. `seed.enableSoilTypeBits`：种子可种土壤位图；`soil_type.bit_code` 参与位运算匹配。
4. `user_plot`：用户地块实例，最终以实例状态生效（锁定/解锁/土壤类型/地块序号）。
5. `plot_policy`：全局默认策略，新用户初始化地块时采用。
6. `user_plot_allocation`：用户级覆盖策略，优先级高于全局默认策略。

---

## 3. 规则优先级冻结

地块锁定来源与策略优先级固定如下（不可反转）：

1. `USER_ALLOCATION`
2. `GLOBAL_POLICY`
3. `SYSTEM`

同一用户同一地块冲突时，按上述顺序决定 `lockSource` 与 `lockRuleCode`。

---

## 4. 字段字典（冻结）

## 4.1 种子类型（`seed_type`）

1. `id`：主键。
2. `name`：种子名称（唯一，逻辑删除维度内唯一）。
3. `seedQualityId`：品质字典 ID。
4. `level`：等级/季节等级（正整数）。
5. `price`：购买单价（金币）。
6. `harvestFruitNumber`：单次收获果实数。
7. `fruitPrice`：果实单价（金币）。
8. `harvestExperience`：收获经验。
9. `harvestScore`：收获积分。
10. `enableSoilTypeBits`：可种土壤位图（至少命中一种土壤）。
11. `maxBugLimit`：虫害上限。
12. `maxHarvestCount`：最大可收获次数。
13. `regrowStageIndex`：重复收获作物回退阶段索引（可空）。

## 4.2 种子阶段（`seed_growth_stage`）

1. `id`：主键。
2. `seedTypeId`：所属种子类型。
3. `growthStageId`：阶段字典 ID（如发芽/生长期/成熟）。
4. `stageIndex`：阶段序号（建议连续 1..N）。
5. `durationSeconds`：阶段持续时长（秒）。
6. `bugProbability`：虫害概率（0~1）。
7. `assetUrl`：阶段资源路径（静态资源路径或上传资源路径）。
8. `width`：舞台渲染宽度（像素）。
9. `height`：舞台渲染高度（像素）。
10. `offsetX`：舞台偏移 X（像素）。
11. `offsetY`：舞台偏移 Y（像素）。

## 4.3 地块类型（`plot_type`）

1. `id`：主键。
2. `name`：地块类型名称（逻辑删除维度内唯一）。
3. `iconUrl`：地块类型图标路径。
4. `soilTypeId`：默认土壤类型 ID（用于实例初始化映射）。
5. `unlockRequired`：是否需要解锁。
6. `defaultUsable`：默认可用（策略初始化时使用）。
7. `defaultPlotUnlockExperienceConfig`：地块解锁经验默认配置值。
8. `sortOrder`：排序权重（小到大）。
9. `description`：描述。

## 4.4 全局策略（`plot_policy`）

1. `id`：主键。
2. `policyName`：策略名称。
3. `active`：是否启用。
4. `defaultTotalPlotCount`：默认总地块数。
5. `defaultUnlockedPlotCount`：默认解锁数。
6. `defaultLockedPlotCount`：默认锁定数（建议与总数/解锁数一致）。
7. `defaultPlotTypeId`：默认地块类型。
8. `defaultLockRuleCode`：默认锁定规则码。
9. `defaultLockReason`：默认锁定原因文本。
10. `allocationRuleJson`：类型分配规则（JSON，按地块类型定义总数/锁定数）。

## 4.5 用户覆盖策略（`user_plot_allocation`）

1. `id`：主键。
2. `userId`：用户 ID。
3. `active`：是否启用覆盖策略。
4. `totalPlotCount`：目标总地块数。
5. `unlockedPlotCount`：目标解锁地块数。
6. `lockedPlotCount`：目标锁定地块数。
7. `defaultPlotTypeId`：默认地块类型（当规则未命中时兜底）。
8. `lockRuleCode`：锁定规则码。
9. `lockReason`：锁定原因文本。
10. `allocationRuleJson`：用户级分配规则（JSON）。
11. `appliedAt`：最后应用时间。

---

## 5. 枚举清单（冻结）

## 5.1 `lockSource`

1. `GLOBAL_POLICY`：来自全局策略。
2. `USER_ALLOCATION`：来自用户覆盖策略。
3. `SYSTEM`：系统兼容兜底（旧数据或策略缺失）。

## 5.2 `lockRuleCode`

1. `DEFAULT_LOCKED`：默认初始化锁定。
2. `EXP_REQUIRED`：经验不足。
3. `COIN_REQUIRED`：金币不足。
4. `MANUAL_LOCK`：手动锁定。
5. `SYSTEM_COMPAT`：系统兼容策略。

---

## 6. 接口契约（Phase 1）

## 6.1 地块类型管理

### 6.1.1 `POST /plot-type/page`

请求：

```json
{
  "name": "",
  "page": 1,
  "rows": 10,
  "sort": "id",
  "order": "asc"
}
```

响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "pageNo": 1,
    "pageSize": 10,
    "total": 2,
    "records": [
      {
        "id": 1,
        "name": "基础地块",
        "iconUrl": "/resources/imgs/ui/plot/plot-type-basic.png",
        "soilTypeId": 1,
        "soilTypeName": "黄土地",
        "unlockRequired": false,
        "defaultUsable": true,
        "defaultPlotUnlockExperienceConfig": 0,
        "sortOrder": 10,
        "description": "默认地块类型"
      }
    ]
  }
}
```

### 6.1.2 `POST /plot-type/save`

请求：

```json
{
  "id": 0,
  "name": "黑土地地块",
  "iconUrl": "/resources/imgs/ui/plot/plot-type-black.png",
  "soilTypeId": 2,
  "unlockRequired": true,
  "defaultUsable": false,
  "defaultPlotUnlockExperienceConfig": 800,
  "sortOrder": 20,
  "description": "高阶地块类型"
}
```

响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": 12
}
```

### 6.1.3 `POST /plot-type/delete`

请求：

```json
{
  "id": 12
}
```

响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": null
}
```

删除前校验：存在策略引用或用户覆盖引用时拒绝删除。

---

## 6.2 全局策略管理

### 6.2.1 `GET /plot-policy/get`

响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "id": 1,
    "policyName": "default-policy",
    "active": true,
    "defaultTotalPlotCount": 6,
    "defaultUnlockedPlotCount": 1,
    "defaultLockedPlotCount": 5,
    "defaultPlotTypeId": 1,
    "defaultLockRuleCode": "DEFAULT_LOCKED",
    "defaultLockReason": "待解锁",
    "allocationRuleJson": "{\"1\":{\"total\":6,\"locked\":5}}"
  }
}
```

### 6.2.2 `POST /plot-policy/save`

请求：

```json
{
  "id": 1,
  "policyName": "default-policy-v2",
  "active": true,
  "defaultTotalPlotCount": 8,
  "defaultUnlockedPlotCount": 2,
  "defaultLockedPlotCount": 6,
  "defaultPlotTypeId": 1,
  "defaultLockRuleCode": "DEFAULT_LOCKED",
  "defaultLockReason": "待解锁",
  "allocationRuleJson": "{\"1\":{\"total\":5,\"locked\":3},\"2\":{\"total\":3,\"locked\":3}}"
}
```

响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": 1
}
```

约束：

1. `defaultUnlockedPlotCount <= defaultTotalPlotCount`
2. `defaultLockedPlotCount == defaultTotalPlotCount - defaultUnlockedPlotCount`

---

## 6.3 用户分配管理

### 6.3.1 `POST /user-plot-allocation/page`

请求：

```json
{
  "userId": null,
  "username": "",
  "page": 1,
  "rows": 10,
  "sort": "id",
  "order": "asc"
}
```

响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "pageNo": 1,
    "pageSize": 10,
    "total": 1,
    "records": [
      {
        "id": 1,
        "userId": 1001,
        "username": "liubei",
        "nickname": "刘备",
        "active": true,
        "totalPlotCount": 8,
        "unlockedPlotCount": 2,
        "lockedPlotCount": 6,
        "defaultPlotTypeId": 1,
        "lockRuleCode": "DEFAULT_LOCKED",
        "lockReason": "待解锁",
        "allocationRuleJson": "{\"1\":{\"total\":8,\"locked\":6}}",
        "appliedAt": "2026-05-28T10:00:00+08:00"
      }
    ]
  }
}
```

### 6.3.2 `POST /user-plot-allocation/save`

请求：

```json
{
  "id": 0,
  "userId": 1001,
  "active": true,
  "totalPlotCount": 8,
  "unlockedPlotCount": 2,
  "lockedPlotCount": 6,
  "defaultPlotTypeId": 1,
  "lockRuleCode": "DEFAULT_LOCKED",
  "lockReason": "待解锁",
  "allocationRuleJson": "{\"1\":{\"total\":8,\"locked\":6}}"
}
```

响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": 1
}
```

约束：

1. `unlockedPlotCount <= totalPlotCount`
2. `lockedPlotCount == totalPlotCount - unlockedPlotCount`
3. `totalPlotCount >= 当前用户已有地块数`（禁止缩容到小于已存在实例数）

### 6.3.3 `POST /user-plot-allocation/apply`

请求：

```json
{
  "userId": 1001
}
```

响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "userId": 1001,
    "beforeTotalPlots": 6,
    "afterTotalPlots": 8,
    "createdPlots": 2,
    "updatedPlots": 6,
    "lockSource": "USER_ALLOCATION"
  }
}
```

---

## 7. `myFarmOverview` 扩展结构

在 `plots[]` 每个地块返回中新增以下字段：

1. `plotTypeId`：地块类型 ID。
2. `plotTypeName`：地块类型名称。
3. `lockSource`：锁定来源（`GLOBAL_POLICY` / `USER_ALLOCATION` / `SYSTEM`）。
4. `lockRuleCode`：锁定规则码（`DEFAULT_LOCKED` / `EXP_REQUIRED` / `COIN_REQUIRED` / `MANUAL_LOCK` / `SYSTEM_COMPAT`）。

结构示例：

```json
{
  "userId": 1001,
  "serverTime": "2026-05-28T10:30:00+08:00",
  "totalPlots": 8,
  "unlockedPlots": 2,
  "lockedPlots": 6,
  "occupiedPlots": 1,
  "emptyUnlockedPlots": 1,
  "harvestableCount": 0,
  "nextExpandCostCoin": 250,
  "plots": [
    {
      "plotId": 101,
      "plotIndex": 1,
      "locked": false,
      "lockReason": "",
      "lockSource": "USER_ALLOCATION",
      "lockRuleCode": "DEFAULT_LOCKED",
      "plotTypeId": 1,
      "plotTypeName": "基础地块",
      "soilTypeId": 1,
      "soilName": "黄土地",
      "hasCrop": true,
      "occupied": true,
      "plantable": false
    }
  ]
}
```

---

## 8. 错误码约定（新增建议）

1. `PLOT_TYPE_NOT_FOUND`：地块类型不存在。
2. `PLOT_TYPE_NAME_REQUIRED`：地块类型名称不能为空。
3. `PLOT_TYPE_NAME_DUPLICATE`：地块类型名称重复。
4. `PLOT_TYPE_IN_USE`：地块类型被策略或分配引用，禁止删除。
5. `PLOT_POLICY_INVALID`：全局策略非法。
6. `PLOT_ALLOCATION_INVALID`：用户分配策略非法。
7. `PLOT_ALLOCATION_NOT_FOUND`：用户分配策略不存在。
8. `PLOT_ALLOCATION_APPLY_FAILED`：用户分配应用失败。

---

## 9. 兼容与实施说明

1. Phase 1 实现允许旧数据按 `SYSTEM` 兜底。
2. Phase 2 迁移完成后，优先由 `USER_ALLOCATION/GLOBAL_POLICY` 驱动。
3. 前端模块与后端联调时，严格按本文档字段名与枚举值进行断言。
