# plot_policies - 地块全局策略表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名                        | 类型           | 非空 | 默认值                         | 说明                                       |
| ----------------------------- | -------------- | ---- | ------------------------------ | ------------------------------------------ |
| `id`                          | `BIGINT`       | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                                       |
| `policy_name`                 | `VARCHAR(128)` | Y    | —                              | 策略名称                                   |
| `policy_version`              | `VARCHAR(64)`  | N    | —                              | 策略版本号                                 |
| `active`                      | `BOOLEAN`      | Y    | `true`                         | 是否激活（同时只有一个激活）               |
| `effective_scope`             | `VARCHAR(32)`  | N    | —                              | 生效范围：NEW_USER_ONLY / MANUAL_APPLY     |
| `publish_status`              | `VARCHAR(32)`  | N    | —                              | 发布状态：DRAFT / ACTIVE / ARCHIVED        |
| `default_total_plot_count`    | `SMALLINT`     | Y    | —                              | 新用户默认总地块数                         |
| `default_unlocked_plot_count` | `SMALLINT`     | Y    | —                              | 新用户默认已解锁地块数                     |
| `default_locked_plot_count`   | `SMALLINT`     | Y    | —                              | 新用户默认锁定地块数（= total - unlocked） |
| `default_lock_rule_code`      | `VARCHAR(64)`  | Y    | `'DEFAULT_LOCKED'`             | 默认锁定规则代码                           |
| `default_lock_reason`         | `VARCHAR(255)` | Y    | `'待解锁'`                     | 默认锁定原因文本                           |
| _(BaseEntity 字段)_           |                |      |                                |                                            |

## 索引

| 索引名                     | 字段     | 条件                       |
| -------------------------- | -------- | -------------------------- |
| `idx_plot_policies_active` | `active` | `WHERE is_deleted = false` |

## 业务说明

- 全局策略控制新用户的初始地块配置
- 同时只能有一个 `active = true` 的策略
- 激活新策略时自动失活其他策略
- 策略激活仅对新创建的用户生效，已存在用户不受影响
- 新用户初始化流程：PlotPolicy → GameplayPolicyProperties 默认值（fallback）
- 锁定规则代码：`DEFAULT_LOCKED`（默认锁定）, `EXP_REQUIRED`（需要经验）, `COIN_REQUIRED`（需要金币）, `MANUAL_LOCK`（手动锁定）, `SYSTEM_COMPAT`（系统兼容）
