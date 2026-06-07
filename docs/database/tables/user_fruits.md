# user_fruits - 用户果实仓库表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名 | 类型 | 非空 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | `BIGINT` | Y | `GENERATED ALWAYS AS IDENTITY` | 主键 |
| `user_id` | `BIGINT` | Y | — | 用户 ID |
| `seed_type_id` | `BIGINT` | Y | — | 种子类型 ID（表示该种子产出的果实） |
| `quantity` | `BIGINT` | Y | `0` | 持有数量 |
| `frozen_quantity` | `BIGINT` | Y | `0` | 冻结数量（预留） |
| *(BaseEntity 字段)* | | | | |

## 唯一约束

| 索引名 | 字段 | 条件 |
|--------|------|------|
| `uk_user_fruits_active` | `(user_id, seed_type_id)` | `WHERE is_deleted = false` |

## 业务说明

- 收获作物时增加果实库存
- 出售果实时扣减可用数量 = `quantity - frozen_quantity`
- 每种种子对应一种果实类型（`seed_type_id` 即代表果实类型）
- `frozen_quantity` 为预留字段

## 关联

- N:1 → `users`
- N:1 → `seed_types`
