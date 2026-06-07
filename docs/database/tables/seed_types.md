# seed_types - 种子类型配置表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名                       | 类型            | 非空 | 默认值                         | 说明                             |
| ---------------------------- | --------------- | ---- | ------------------------------ | -------------------------------- |
| `id`                         | `BIGINT`        | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                             |
| `name`                       | `VARCHAR(500)`  | Y    | —                              | 种子名称（唯一）                 |
| `cover_image_url`            | `VARCHAR(1024)` | Y    | `''`                           | 封面图片 URL                     |
| `seed_quality_id`            | `BIGINT`        | Y    | —                              | 关联种子品质 ID                  |
| `enable_soil_type_bits`      | `BIGINT`        | Y    | —                              | 可种植土壤位掩码                 |
| `level`                      | `SMALLINT`      | Y    | —                              | 种子等级                         |
| `unlock_experience_required` | `BIGINT`        | Y    | `0`                            | 解锁所需经验                     |
| `description`                | `TEXT`          | N    | —                              | 描述                             |
| **机制与事件**               |                 |      |                                |                                  |
| `max_bug_limit`              | `SMALLINT`      | Y    | `0`                            | 虫子上限（达到后直接枯萎）       |
| `max_harvest_count`          | `SMALLINT`      | Y    | `1`                            | 最大收获次数                     |
| `regrow_stage_index`         | `SMALLINT`      | N    | —                              | 多次收获作物收获后回退的阶段索引 |
| `harvest_stage_index`        | `SMALLINT`      | N    | —                              | 成熟可收获的阶段索引             |
| **经济数值**                 |                 |      |                                |                                  |
| `price`                      | `BIGINT`        | Y    | `0`                            | 种子购买价格（金币）             |
| `harvest_experience`         | `BIGINT`        | Y    | `0`                            | 收获获得经验                     |
| `harvest_fruit_number`       | `INT`           | Y    | `0`                            | 每次收获基础果实数               |
| `fruit_loss_per_bug`         | `INT`           | Y    | `1`                            | 每只虫造成的果实损失             |
| `bug_kill_coin_reward`       | `BIGINT`        | Y    | `0`                            | 杀虫金币奖励                     |
| `bug_kill_experience_reward` | `BIGINT`        | Y    | `0`                            | 杀虫经验奖励                     |
| `bug_kill_score_reward`      | `BIGINT`        | Y    | `0`                            | 杀虫积分奖励                     |
| `fruit_price`                | `BIGINT`        | Y    | `0`                            | 果实出售单价（金币）             |
| `harvest_score`              | `BIGINT`        | Y    | `0`                            | 收获获得积分                     |
| _(BaseEntity 字段)_          |                 |      |                                |                                  |

## 唯一约束

| 索引名                      | 字段   | 条件                       |
| --------------------------- | ------ | -------------------------- |
| `uk_seed_types_name_active` | `name` | `WHERE is_deleted = false` |

## 业务规则

- **虫害机制**: `bug_count >= max_bug_limit` → 作物直接枯萎
- **收获机制**: `harvest_count < max_harvest_count` 时可收获；`harvest_count >= max_harvest_count` 后作物清除
- **再生机制**: 多次收获作物(`max_harvest_count > 1`)收获后退回 `regrow_stage_index` 阶段
- **土壤兼容**: `(enable_soil_type_bits & soil_bit_code) == soil_bit_code`
- **虫害减产**: `实际果实 = max(0, harvest_fruit_number - bug_count × fruit_loss_per_bug)`

## 关联

- N:1 → `seed_qualities` (种子品质)
- 1:N → `seed_growth_stages` (生长阶段配置)
- 被 `user_crops` 引用 (作物实例)
- 被 `user_seeds` / `user_fruits` 引用 (背包/仓库)
