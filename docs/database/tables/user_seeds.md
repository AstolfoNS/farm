# user_seeds - 用户种子背包表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名              | 类型     | 非空 | 默认值                         | 说明             |
| ------------------- | -------- | ---- | ------------------------------ | ---------------- |
| `id`                | `BIGINT` | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键             |
| `user_id`           | `BIGINT` | Y    | —                              | 用户 ID          |
| `seed_type_id`      | `BIGINT` | Y    | —                              | 种子类型 ID      |
| `quantity`          | `BIGINT` | Y    | `0`                            | 持有数量         |
| `frozen_quantity`   | `BIGINT` | Y    | `0`                            | 冻结数量（预留） |
| _(BaseEntity 字段)_ |          |      |                                |                  |

## 唯一约束

| 索引名                 | 字段                      | 条件                       |
| ---------------------- | ------------------------- | -------------------------- |
| `uk_user_seeds_active` | `(user_id, seed_type_id)` | `WHERE is_deleted = false` |

## 业务说明

- 购买种子时 `quantity` 增加
- 种植时扣减可用数量 = `quantity - frozen_quantity`
- `frozen_quantity` 为预留字段，用于交易/加工场景的库存锁定
- 出售果实不涉及此表，果实库存见 `user_fruits`

## 关联

- N:1 → `users`
- N:1 → `seed_types`
