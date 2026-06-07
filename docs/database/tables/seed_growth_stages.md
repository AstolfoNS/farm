# seed_growth_stages - 种子生长阶段配置表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名              | 类型            | 非空 | 默认值                         | 说明                              |
| ------------------- | --------------- | ---- | ------------------------------ | --------------------------------- |
| `id`                | `BIGINT`        | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                              |
| `seed_type_id`      | `BIGINT`        | Y    | —                              | 关联种子类型 ID                   |
| `growth_stage_id`   | `BIGINT`        | Y    | —                              | 关联生长阶段字典 ID               |
| `stage_index`       | `SMALLINT`      | Y    | —                              | 阶段序号（1-based，同种子内唯一） |
| `duration_seconds`  | `INT`           | Y    | —                              | 该阶段持续时长（秒）              |
| `asset_url`         | `VARCHAR(1024)` | N    | —                              | 阶段展示图 URL                    |
| `bug_probability`   | `NUMERIC(5,4)`  | Y    | `0.0000`                       | 该阶段发生虫害的概率（0~1）       |
| `width`             | `INT`           | Y    | `0`                            | 展示图宽度（px）                  |
| `height`            | `INT`           | Y    | `0`                            | 展示图高度（px）                  |
| `offset_x`          | `INT`           | Y    | `0`                            | 展示图水平偏移（px），默认 110    |
| `offset_y`          | `INT`           | Y    | `0`                            | 展示图垂直偏移（px），默认 280    |
| _(BaseEntity 字段)_ |                 |      |                                |                                   |

## 唯一约束

| 索引名                       | 字段                          | 条件                       |
| ---------------------------- | ----------------------------- | -------------------------- |
| `uk_seed_growth_stage_index` | `(seed_type_id, stage_index)` | `WHERE is_deleted = false` |

## 业务说明

- 每类种子配置多个阶段，按 `stage_index` 顺序推进
- `duration_seconds` 经土壤 `grow_speed_multiplier` 倍率修正后为实际生长时长
- `bug_probability` 为每经过该阶段时触发虫害的概率
- 最后一个阶段通常配置为"枯萎"（`duration_seconds=0`，`bug_probability=0`）
- 超出 `max_harvest_count` 后的阶段通过软删除清理
- 默认 `offset_x=110, offset_y=280` 用于前端等距视图定位

## 关联

- N:1 → `seed_types`
- N:1 → `growth_stages`
