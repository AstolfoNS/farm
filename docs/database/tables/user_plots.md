# user_plots - 用户地块表

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名                       | 类型           | 非空 | 默认值                         | 说明                            |
| ---------------------------- | -------------- | ---- | ------------------------------ | ------------------------------- |
| `id`                         | `BIGINT`       | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                            |
| `user_id`                    | `BIGINT`       | Y    | —                              | 用户 ID                         |
| `soil_type_id`               | `BIGINT`       | Y    | —                              | 土壤类型 ID                     |
| `plot_index`                 | `SMALLINT`     | Y    | —                              | 地块序号（1-based，用户内唯一） |
| `unlock_experience_required` | `BIGINT`       | Y    | `0`                            | 解锁该地块所需经验值            |
| `is_locked`                  | `BOOLEAN`      | Y    | `false`                        | 是否锁定                        |
| `unlocked_at`                | `TIMESTAMPTZ`  | N    | —                              | 解锁时间                        |
| `lock_reason`                | `VARCHAR(255)` | N    | —                              | 锁定原因说明                    |
| _(BaseEntity 字段)_          |                |      |                                |                                 |

## 唯一约束

| 索引名               | 字段                    | 条件                       |
| -------------------- | ----------------------- | -------------------------- |
| `uk_user_plot_index` | `(user_id, plot_index)` | `WHERE is_deleted = false` |

## 业务说明

- 新用户创建时根据 PlotPolicy 初始化地块（默认 6 个，1 个解锁）
- 扩地时新增地块默认为**锁定状态**（`is_locked=true`）
- 解锁必须按 `plot_index` 顺序：必须先解锁最小的锁定地块
- 解锁需满足经验门槛（`unlock_experience_required`）和金币要求
- `unlock_experience_required` 通过公式计算：`base + max(0, plotIndex - initialUnlocked - 1) × step`
- 超额地块通过软删除自动清理（`is_deleted=true`）

## 关联

- N:1 → `users`
- N:1 → `soil_types`
- 1:0..1 → `user_crops`（每个地块最多一个作物）
