# user_inventory_flows - 用户库存流水表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名                 | 类型           | 非空 | 默认值                         | 说明                                                    |
| ---------------------- | -------------- | ---- | ------------------------------ | ------------------------------------------------------- |
| `id`                   | `BIGINT`       | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                                                    |
| `user_id`              | `BIGINT`       | Y    | —                              | 用户 ID                                                 |
| `item_type`            | `VARCHAR(32)`  | Y    | —                              | 物品类型：SEED / FRUIT                                  |
| `seed_type_id`         | `BIGINT`       | Y    | —                              | 种子类型 ID                                             |
| `operation_type`       | `VARCHAR(32)`  | Y    | —                              | 操作类型：INCOME / EXPENSE / FREEZE / UNFREEZE / ADJUST |
| `change_amount`        | `BIGINT`       | Y    | —                              | 变动数量                                                |
| `before_amount`        | `BIGINT`       | Y    | `0`                            | 变动前库存                                              |
| `after_amount`         | `BIGINT`       | Y    | `0`                            | 变动后库存                                              |
| `before_frozen_amount` | `BIGINT`       | Y    | `0`                            | 变动前冻结量                                            |
| `after_frozen_amount`  | `BIGINT`       | Y    | `0`                            | 变动后冻结量                                            |
| `biz_type`             | `VARCHAR(64)`  | Y    | `''`                           | 业务类型                                                |
| `biz_id`               | `VARCHAR(128)` | N    | —                              | 业务标识                                                |
| `occurred_at`          | `TIMESTAMPTZ`  | Y    | `CURRENT_TIMESTAMP`            | 发生时间                                                |
| `ext_data`             | `JSONB`        | N    | —                              | 扩展数据                                                |
| _(BaseEntity 字段)_    |                |      |                                |                                                         |

## 业务类型枚举

| biz_type     | item_type  | operation_type | 说明           |
| ------------ | ---------- | -------------- | -------------- |
| `BUY_SEED`   | SEED       | INCOME         | 购买种子入库   |
| `SELL_FRUIT` | FRUIT      | EXPENSE        | 出售果实出库   |
| `HARVEST`    | FRUIT      | INCOME         | 收获果实入库   |
| `PLANT`      | SEED       | EXPENSE        | 种植消耗种子   |
| `ADMIN`      | SEED/FRUIT | ADJUST         | 管理员调整库存 |

## 关联

- N:1 → `users`
- N:1 → `seed_types`
