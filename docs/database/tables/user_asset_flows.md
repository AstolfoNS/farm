# user_asset_flows - 用户资产流水表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名              | 类型           | 非空 | 默认值                         | 说明                                |
| ------------------- | -------------- | ---- | ------------------------------ | ----------------------------------- |
| `id`                | `BIGINT`       | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                                |
| `user_id`           | `BIGINT`       | Y    | —                              | 用户 ID                             |
| `asset_type`        | `VARCHAR(32)`  | Y    | —                              | 资产类型：COIN / SCORE / EXPERIENCE |
| `operation_type`    | `VARCHAR(32)`  | Y    | —                              | 操作类型：INCOME / EXPENSE / ADJUST |
| `change_amount`     | `BIGINT`       | Y    | —                              | 变动金额（正数）                    |
| `before_amount`     | `BIGINT`       | Y    | `0`                            | 变动前余额                          |
| `after_amount`      | `BIGINT`       | Y    | `0`                            | 变动后余额                          |
| `biz_type`          | `VARCHAR(64)`  | Y    | `''`                           | 业务类型                            |
| `biz_id`            | `VARCHAR(128)` | N    | —                              | 业务标识（如订单号等）              |
| `occurred_at`       | `TIMESTAMPTZ`  | Y    | `CURRENT_TIMESTAMP`            | 发生时间                            |
| `ext_data`          | `JSONB`        | N    | —                              | 扩展数据                            |
| _(BaseEntity 字段)_ |                |      |                                |                                     |

## 业务类型枚举

| biz_type      | 涉及资产                | 说明            |
| ------------- | ----------------------- | --------------- |
| `BUY_SEED`    | COIN                    | 购买种子扣金币  |
| `SELL_FRUIT`  | COIN                    | 出售果实加金币  |
| `UNLOCK_PLOT` | COIN                    | 解锁地块扣金币  |
| `EXPAND_PLOT` | COIN                    | 扩地扣金币      |
| `HARVEST`     | EXPERIENCE, SCORE       | 收获加经验/积分 |
| `CARE`        | COIN, EXPERIENCE, SCORE | 养护杀虫加奖励  |
| `ADMIN`       | COIN/SCORE/EXPERIENCE   | 管理员调整      |

## 关联

- N:1 → `users`
