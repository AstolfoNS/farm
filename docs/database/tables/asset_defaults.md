# asset_defaults - 默认资源配置表

**Schema**: `farm` | **引擎**: PostgreSQL

## 概述

存储系统中各类默认资源的路径配置，如默认头像、默认种子封面、默认背景音乐等。前端通过 `AssetDefaultProvider` 读取这些配置。

## 字段定义

| 字段名              | 类型            | 非空 | 默认值                         | 说明                                        |
| ------------------- | --------------- | ---- | ------------------------------ | ------------------------------------------- |
| `id`                | `BIGINT`        | Y    | `GENERATED ALWAYS AS IDENTITY` | 主键                                        |
| `asset_key`         | `VARCHAR(128)`  | Y    | —                              | 资源键名，唯一（如 avatar, seedCover, bgm） |
| `asset_url`         | `VARCHAR(1024)` | Y    | —                              | 资源访问 URL 或路径                         |
| `description`       | `TEXT`          | N    | —                              | 资源描述                                    |
| _(BaseEntity 字段)_ |                 |      |                                |                                             |

## 唯一约束

| 索引名                         | 字段        | 条件                       |
| ------------------------------ | ----------- | -------------------------- |
| `uk_asset_defaults_key_active` | `asset_key` | `WHERE is_deleted = false` |

## 种子数据

| asset_key           | asset_url                                            | description        |
| ------------------- | ---------------------------------------------------- | ------------------ |
| `avatar`            | `/oss/.defaults/avatar/default-avatar.png`            | 用户头像默认图     |
| `seedCover`         | `/oss/.defaults/seed/seed-cover-default.png`          | 种子封面默认图     |
| `seedStage`         | `/oss/.defaults/seed/seed-stage-default.png`          | 种子阶段默认图     |
| `soilCover`         | `/oss/.defaults/soil/soil-default.png`                | 土壤默认图         |
| `plotCover`         | `/oss/.defaults/plot/plot-cover-default.png`          | 地块封面默认图     |
| `plotIcon`          | `/oss/.defaults/plot/plot-icon-default.png`           | 地块图标默认图     |
| `bgm`               | `/resources/sounds/bgm/Must Work to Eat.wav`         | 默认背景音乐       |
| `seedStageWithered` | `/oss/.defaults/seed/seed-stage-withered-default.png` | 种子枯萎阶段默认图 |

## 业务说明

- 前端通过 `AssetDefaultProvider.get(key)` 获取默认资源路径
- 当用户未自定义某资源时，使用此表中的默认值
- 插入使用 `ON CONFLICT (asset_key) WHERE is_deleted = false DO UPDATE` 实现幂等
