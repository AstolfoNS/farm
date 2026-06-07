# user_crops - 用户种植作物表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名 | 类型 | 非空 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | `BIGINT` | Y | `GENERATED ALWAYS AS IDENTITY` | 主键 |
| `user_id` | `BIGINT` | Y | — | 用户 ID |
| `plot_id` | `BIGINT` | Y | — | 地块 ID（唯一） |
| `seed_type_id` | `BIGINT` | Y | — | 种子类型 ID |
| `planted_at` | `TIMESTAMPTZ` | Y | `CURRENT_TIMESTAMP` | 种植时间 |
| `stage_started_at` | `TIMESTAMPTZ` | Y | `CURRENT_TIMESTAMP` | 当前阶段开始时间 |
| `last_harvest_at` | `TIMESTAMPTZ` | N | — | 上次收获时间 |
| `matured_at` | `TIMESTAMPTZ` | N | — | 成熟时间 |
| `withered_at` | `TIMESTAMPTZ` | N | — | 枯萎时间 |
| `expected_ripe_at` | `TIMESTAMPTZ` | N | — | 预计成熟时间（用于倒计时） |
| `expected_withered_at` | `TIMESTAMPTZ` | N | — | 预计枯萎时间（用于倒计时） |
| `harvest_count` | `SMALLINT` | Y | `0` | 已收获次数 |
| `current_stage_index` | `SMALLINT` | Y | `1` | 当前阶段序号 |
| `grow_status` | `SMALLINT` | Y | `1` | 生长状态：1=生长中, 2=成熟待收, 3=已枯萎 |
| `bug_count` | `SMALLINT` | Y | `0` | 当前虫害数量 |
| `last_bug_at` | `TIMESTAMPTZ` | N | — | 上次虫害发生时间 |
| `last_care_at` | `TIMESTAMPTZ` | N | — | 上次养护时间 |
| *(BaseEntity 字段)* | | | | |

## 唯一约束

| 索引名 | 字段 | 条件 |
|--------|------|------|
| `uk_plot_active_crop` | `plot_id` | `WHERE is_deleted = false` |

## 状态机

```
生长中(1) ──[时间到达 expected_ripe_at]──→ 成熟待收(2)
    │                                            │
    └──[时间到达 expected_withered_at]──→ 已枯萎(3)
                                             ↑
    [bug_count >= max_bug_limit] ────────────┘
```

## 业务说明

- 每个地块同时只能有一个活跃作物
- 收获次数达到 `max_harvest_count` 后作物被清除
- 多次收获作物收获后回退到 `regrow_stage_index` 阶段
- 虫害减产公式：`实际果实 = max(0, harvest_fruit_number - bug_count × fruit_loss_per_bug)`
- 每次养护（care）清除 1 只虫
- 铲除操作直接软删除作物，不产生收获

## 关联

- N:1 → `users`
- 1:1 → `user_plots` (通过 `plot_id`)
- N:1 → `seed_types`
