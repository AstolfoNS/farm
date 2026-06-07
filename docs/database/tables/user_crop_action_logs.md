# user_crop_action_logs - 作物行为日志表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名 | 类型 | 非空 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `id` | `BIGINT` | Y | `GENERATED ALWAYS AS IDENTITY` | 主键 |
| `user_id` | `BIGINT` | Y | — | 用户 ID |
| `plot_id` | `BIGINT` | Y | — | 地块 ID |
| `crop_id` | `BIGINT` | N | — | 作物 ID |
| `seed_type_id` | `BIGINT` | N | — | 种子类型 ID |
| `action_type` | `VARCHAR(32)` | Y | — | 操作类型：PLANT / HARVEST / REMOVE_BUG / CLEAR / WITHER |
| `action_result` | `VARCHAR(32)` | Y | `'SUCCESS'` | 操作结果：SUCCESS / FAIL |
| `action_at` | `TIMESTAMPTZ` | Y | `CURRENT_TIMESTAMP` | 操作时间 |
| `action_snapshot` | `JSONB` | N | — | 操作快照（记录操作前后状态） |
| *(BaseEntity 字段)* | | | | |

## 操作类型枚举

| action_type | 说明 |
|-------------|------|
| `PLANT` | 种植作物 |
| `HARVEST` | 收获作物 |
| `REMOVE_BUG` | 杀虫养护 |
| `CLEAR` | 铲除作物 |
| `WITHER` | 作物枯萎 |

## 关联

- N:1 → `users`
- N:1 → `user_plots`
- N:1 → `user_crops`
- N:1 → `seed_types`
