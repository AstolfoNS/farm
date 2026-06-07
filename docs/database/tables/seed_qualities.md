# seed_qualities - 种子品质字典

**Schema**: `farm` | **引擎**: PostgreSQL

## 字段定义

| 字段名              | 类型           | 非空 | 默认值                         | 说明             |
| ------------------- | -------------- | ---- | ------------------------------ | ---------------- |
| `id`                | `BIGINT`       | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键             |
| `name`              | `VARCHAR(500)` | Y    | —                              | 品质名称（唯一） |
| `description`       | `TEXT`         | N    | —                              | 品质描述         |
| _(BaseEntity 字段)_ |                |      |                                |                  |

## 唯一约束

| 索引名                          | 字段   | 条件                       |
| ------------------------------- | ------ | -------------------------- |
| `uk_seed_qualities_name_active` | `name` | `WHERE is_deleted = false` |

## 种子数据

| name | description  |
| ---- | ------------ |
| 普通 | 普通品质种子 |
| 优质 | 优质品质种子 |
| 稀有 | 稀有品质种子 |

## 关联

- 被 `seed_types.seed_quality_id` 引用
