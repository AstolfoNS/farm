# users - 用户信息表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名              | 类型            | 非空 | 默认值                         | 说明                            |
| ------------------- | --------------- | ---- | ------------------------------ | ------------------------------- |
| `id`                | `BIGINT`        | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                            |
| `username`          | `citext`        | Y    | —                              | 用户名（大小写不敏感，唯一）    |
| `nickname`          | `VARCHAR(500)`  | Y    | —                              | 昵称                            |
| `password_hash`     | `VARCHAR(500)`  | Y    | —                              | 密码哈希                        |
| `email`             | `citext`        | Y    | —                              | 邮箱（大小写不敏感，唯一）      |
| `avatar_url`        | `VARCHAR(1024)` | Y    | `''`                           | 头像 URL                        |
| `experience`        | `BIGINT`        | Y    | `0`                            | 经验值                          |
| `score`             | `BIGINT`        | Y    | `0`                            | 积分                            |
| `coin`              | `BIGINT`        | Y    | `0`                            | 金币                            |
| `preferences_json`  | `JSONB`         | Y    | `'{}'`                         | 用户偏好设置（音效/背景音乐等） |
| _(BaseEntity 字段)_ |                 |      |                                |                                 |
| `created_at`        | `TIMESTAMPTZ`   | Y    | `CURRENT_TIMESTAMP`            | 创建时间                        |
| `updated_at`        | `TIMESTAMPTZ`   | Y    | `CURRENT_TIMESTAMP`            | 更新时间                        |
| `created_by`        | `BIGINT`        | N    | —                              | 创建者 ID                       |
| `updated_by`        | `BIGINT`        | N    | —                              | 更新者 ID                       |
| `remark`            | `TEXT`          | N    | —                              | 备注                            |
| `status`            | `SMALLINT`      | Y    | `1`                            | 状态                            |
| `is_deleted`        | `BOOLEAN`       | Y    | `false`                        | 软删除                          |
| `opt_lock_version`  | `INT`           | Y    | `0`                            | 乐观锁版本                      |

## 唯一约束

| 索引名                     | 字段       | 条件                       |
| -------------------------- | ---------- | -------------------------- |
| `uk_users_username_active` | `username` | `WHERE is_deleted = false` |
| `uk_users_email_active`    | `email`    | `WHERE is_deleted = false` |

## 业务说明

- 用户是农场经营的唯一主体，拥有金币、经验、积分三类资产
- 新建用户时会自动初始化默认地块（根据 PlotPolicy 或配置）
- `preferences_json` 存储 JSON 格式的偏好设置，如 `{"audio":{"effectEnabled":true,"effectVolume":0.8,"bgmEnabled":true,"bgmVolume":0.6}}`
- 使用 `citext` 类型实现用户名和邮箱的大小写不敏感唯一约束

## 关联

- 1:N → `user_plots` (用户拥有的地块)
- 1:N → `user_crops` (用户种植的作物)
- 1:N → `user_seeds` (用户种子背包)
- 1:N → `user_fruits` (用户果实仓库)
- 1:N → `user_asset_flows` (用户资产流水)
- 1:N → `user_inventory_flows` (用户库存流水)
- 1:N → `user_crop_action_logs` (用户作物行为日志)
- 1:N → `request_idempotencies` (请求幂等记录)
