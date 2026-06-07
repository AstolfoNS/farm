# Farm 数据库文档

## 概述

- **数据库**: PostgreSQL
- **Schema**: `farm`
- **字符集**: UTF-8
- **总表数**: 16 张
- **范式**: 3NF，通过软删除 (`is_deleted`) 保留历史数据
- **乐观锁**: 所有表均有 `opt_lock_version` 字段

## 公共字段（BaseEntity）

所有表均继承以下字段：

| 字段               | 类型          | 说明          |
| ------------------ | ------------- | ------------- |
| `id`               | `BIGINT`      | 主键，自增    |
| `created_at`       | `TIMESTAMPTZ` | 创建时间      |
| `updated_at`       | `TIMESTAMPTZ` | 更新时间      |
| `created_by`       | `BIGINT`      | 创建者 ID     |
| `updated_by`       | `BIGINT`      | 更新者 ID     |
| `remark`           | `TEXT`        | 备注          |
| `status`           | `SMALLINT`    | 状态 (1=正常) |
| `is_deleted`       | `BOOLEAN`     | 软删除标记    |
| `opt_lock_version` | `INT`         | 乐观锁版本号  |

## 表分类

### 基础配置表 (5 张)

| 序号 | 表名             | 说明           |
| ---- | ---------------- | -------------- |
| 1    | `asset_defaults` | 默认资源配置表 |
| 2    | `seed_qualities` | 种子品质字典   |
| 3    | `soil_types`     | 土壤类型表     |
| 4    | `growth_stages`  | 生长阶段字典   |
| 5    | `plot_policies`  | 地块全局策略表 |

### 种子配置表 (2 张)

| 序号 | 表名                 | 说明               |
| ---- | -------------------- | ------------------ |
| 6    | `seed_types`         | 种子类型配置表     |
| 7    | `seed_growth_stages` | 种子生长阶段配置表 |

### 用户经营数据表 (5 张)

| 序号 | 表名          | 说明           |
| ---- | ------------- | -------------- |
| 8    | `users`       | 用户信息表     |
| 9    | `user_plots`  | 用户地块表     |
| 10   | `user_crops`  | 用户种植作物表 |
| 11   | `user_seeds`  | 用户种子背包表 |
| 12   | `user_fruits` | 用户果实仓库表 |

### 流水与日志表 (4 张)

| 序号 | 表名                    | 说明           |
| ---- | ----------------------- | -------------- |
| 13   | `user_asset_flows`      | 用户资产流水表 |
| 14   | `user_inventory_flows`  | 用户库存流水表 |
| 15   | `user_crop_action_logs` | 作物行为日志表 |
| 16   | `request_idempotencies` | 请求幂等记录表 |

## ER 关系

```
seed_qualities ──→ seed_types ──→ seed_growth_stages ──→ growth_stages
                      │
soil_types ───────────┤
                      │
plot_policies ──→ users ──→ user_plots ──→ user_crops
                      │          │
                      │   user_seeds     user_crop_action_logs
                      │   user_fruits    user_asset_flows
                      │                  user_inventory_flows
                      └── request_idempotencies
```

> 注: 所有外键均为逻辑外键（Long 型 ID），未使用数据库级 FK 约束。
