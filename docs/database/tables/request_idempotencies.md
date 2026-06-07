# request_idempotencies - 请求幂等记录表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名              | 类型           | 非空 | 默认值                         | 说明                                              |
| ------------------- | -------------- | ---- | ------------------------------ | ------------------------------------------------- |
| `id`                | `BIGINT`       | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                                              |
| `user_id`           | `BIGINT`       | Y    | —                              | 用户 ID                                           |
| `biz_type`          | `VARCHAR(64)`  | Y    | —                              | 业务类型：BUY_SEED / SELL_FRUIT / PLANT / HARVEST |
| `request_id`        | `VARCHAR(128)` | Y    | —                              | 客户端生成的请求唯一标识                          |
| `process_status`    | `VARCHAR(16)`  | Y    | `'PROCESSING'`                 | 处理状态：PROCESSING / SUCCESS / FAILED           |
| `response_payload`  | `JSONB`        | N    | —                              | 缓存的成功响应结果                                |
| `error_message`     | `VARCHAR(500)` | N    | —                              | 失败时的错误信息                                  |
| `finished_at`       | `TIMESTAMPTZ`  | N    | —                              | 处理完成时间                                      |
| _(BaseEntity 字段)_ |                |      |                                |                                                   |

## 幂等维度

`(user_id, biz_type, request_id)` 组合唯一

## 业务说明

- 关键写操作（购买/出售/种植/收获）必须传 `requestId`
- 重复请求直接返回缓存的成功结果
- PROCESSING 状态的重复请求视为"请求正在进行中"
- FAILED 状态允许重试

## 关联

- N:1 → `users`
