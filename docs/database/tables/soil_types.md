# soil_types - 土壤类型表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名                       | 类型            | 非空 | 默认值                         | 说明                                                |
| ---------------------------- | --------------- | ---- | ------------------------------ | --------------------------------------------------- |
| `id`                         | `BIGINT`        | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                                                |
| `name`                       | `VARCHAR(500)`  | Y    | —                              | 土壤名称（唯一）                                    |
| `bit_code`                   | `INT`           | Y    | —                              | 位掩码编码（2的幂: 1,2,4,8...），用于种子兼容性检查 |
| `cover_image_url`            | `VARCHAR(1024)` | Y    | `''`                           | 土壤封面图片 URL                                    |
| `level`                      | `SMALLINT`      | Y    | —                              | 土壤等级 (1=基础, 2=中级, 3=高级)                   |
| `unlock_experience_required` | `BIGINT`        | Y    | `0`                            | 解锁该土壤所需经验值                                |
| `grow_speed_multiplier`      | `NUMERIC(5,2)`  | Y    | `1.00`                         | 生长速度倍率 (<1.00 表示加速)                       |
| `expand_cost_coin`           | `BIGINT`        | Y    | `0`                            | 扩地为该土壤类型所需金币                            |
| `description`                | `TEXT`          | N    | —                              | 土壤描述                                            |
| _(BaseEntity 字段)_          |                 |      |                                |                                                     |

## 唯一约束

| 索引名                          | 字段       | 条件                       |
| ------------------------------- | ---------- | -------------------------- |
| `uk_soil_types_bit_code_active` | `bit_code` | `WHERE is_deleted = false` |
| `uk_soil_types_name_active`     | `name`     | `WHERE is_deleted = false` |

## 业务说明

- `bit_code` 用于种子的 `enable_soil_type_bits` 位运算兼容性检查: `(seed_bits & soil_bit) == soil_bit`
- `grow_speed_multiplier` 影响作物的生长时长: 实际生长时长 = 阶段时长 × 倍率
- `expand_cost_coin` 为扩地时选择该土壤的金币成本（替代旧版公式计算）
- 新增土壤时 bitCode 自动分配为下一个可用的 2 的幂

## 种子数据

| name   | bit_code | level | unlock_experience_required | grow_speed_multiplier | expand_cost_coin |
| ------ | -------- | ----- | -------------------------- | --------------------- | ---------------- |
| 黄土地 | 1        | 1     | 0                          | 1.00                  | 0                |
| 黑土地 | 2        | 2     | 500                        | 0.90                  | 1500             |
| 金土地 | 4        | 3     | 2000                       | 0.80                  | 5000             |

## 关联

- 被 `user_plots` 引用 (用户地块的实际土壤类型)
- 被 `seed_types.enable_soil_type_bits` 位运算引用（兼容性）
