# Controller API 文档

## 控制器清单 (7 个，49 个端点)

| Controller                    | @RequestMapping | 端点数 | Swagger Tag  |
| ----------------------------- | --------------- | ------ | ------------ |
| `UserController`              | `/user`         | 10     | 用户模块     |
| `FileController`              | `/file`         | 4      | 文件模块     |
| `SeedController`              | `/seed`         | 17     | 种子与商店   |
| `CropGameplayController`      | `/gameplay`     | 5      | 作物生命周期 |
| `PlotGameplayController`      | `/gameplay`     | 6      | 地块经营     |
| `FarmGameplayQueryController` | `/gameplay`     | 3      | 农场聚合查询 |
| `PlotController`              | `/plot`         | 7      | 地块配置     |

## WebSocket

| 端点                                | 说明                                  |
| ----------------------------------- | ------------------------------------- |
| `ws://host:8080/ws/server?userId=N` | 作物状态实时推送（服务端→客户端单向） |

推送事件类型：`FARM_OVERVIEW`，消息体 `FarmRealtimeMessageVO {event, userId, serverTime, cropStatusChanged, overview}`

## 完整端点索引

### `/user` — 用户模块

| 方法 | 路径                           | 说明          |
| ---- | ------------------------------ | ------------- |
| GET  | `/user/list`                   | 用户列表      |
| POST | `/user/gridDataFilterSortPage` | 用户分页搜索  |
| POST | `/user/addOrUpdate`            | 新增/更新用户 |
| POST | `/user/delete`                 | 删除用户      |
| POST | `/user/updateAvatar`           | 更新头像      |
| GET  | `/user/loginOptions`           | 登录选项      |
| POST | `/user/setCurUser`             | 设置当前用户  |
| GET  | `/user/getCurUser`             | 获取当前用户  |
| GET  | `/user/settings/get`           | 获取设置      |
| POST | `/user/settings/save`          | 保存设置      |

### `/file` — 文件模块

| 方法 | 路径                | 说明                 |
| ---- | ------------------- | -------------------- |
| POST | `/file/upload`      | 上传文件 (multipart) |
| POST | `/file/saveHeadImg` | 上传头像 (multipart) |
| POST | `/file/delete`      | 删除文件             |
| POST | `/file/url`         | 获取文件 URL         |

### `/seed` — 种子与商店

| 方法 | 路径                         | 说明            |
| ---- | ---------------------------- | --------------- |
| POST | `/seed/type/page`            | 种子分页        |
| POST | `/seed/type/save`            | 保存种子        |
| POST | `/seed/type/delete`          | 删除种子        |
| GET  | `/seed/quality/options`      | 品质选项        |
| GET  | `/seed/soil/options`         | 土壤选项        |
| GET  | `/seed/growth-stage/options` | 阶段选项        |
| POST | `/seed/stage/page`           | 阶段分页        |
| POST | `/seed/stage/save`           | 保存阶段        |
| POST | `/seed/stage/delete`         | 删除阶段        |
| POST | `/seed/shop/page`            | 商店分页        |
| POST | `/seed/shop/buy`             | 购买种子 (幂等) |
| POST | `/seed/shop/sell-fruit`      | 出售果实 (幂等) |
| POST | `/seed/shop/trade/page`      | 交易记录        |
| POST | `/seed/shop/fruit/page`      | 果实库存        |
| POST | `/seed/shop/seed/page`       | 种子库存        |
| POST | `/seed/shop/overview`        | 商店概览        |
| POST | `/seed/shop/home`            | 商店首页        |

### `/gameplay` — 作物生命周期

| 方法 | 路径                         | 说明         |
| ---- | ---------------------------- | ------------ |
| POST | `/gameplay/plant`            | 种植 (幂等)  |
| POST | `/gameplay/harvest`          | 收获 (幂等)  |
| POST | `/gameplay/clear`            | 铲除 (幂等)  |
| POST | `/gameplay/care`             | 养护杀虫     |
| POST | `/gameplay/crop/action/page` | 行为日志分页 |

### `/gameplay` — 地块经营

| 方法 | 路径                                   | 说明         |
| ---- | -------------------------------------- | ------------ |
| POST | `/gameplay/plot/unlock`                | 解锁地块     |
| POST | `/gameplay/plot/expand`                | 扩地         |
| POST | `/gameplay/plot/expand/options`        | 扩地选项     |
| POST | `/gameplay/plot/status`                | 地块状态     |
| POST | `/gameplay/plot/trade/page`            | 经营流水     |
| GET  | `/gameplay/plot/trade/bizType/options` | 业务类型字典 |

### `/gameplay` — 农场查询

| 方法 | 路径                           | 说明     |
| ---- | ------------------------------ | -------- |
| POST | `/gameplay/myFarmOverview`     | 农场概览 |
| POST | `/gameplay/myPlantingPanel`    | 种植面板 |
| POST | `/gameplay/seedPlantablePlots` | 可种地块 |

### `/plot` — 地块配置

| 方法 | 路径                    | 说明     |
| ---- | ----------------------- | -------- |
| POST | `/plot/soil/page`       | 土壤分页 |
| POST | `/plot/soil/get`        | 土壤详情 |
| POST | `/plot/soil/save`       | 保存土壤 |
| POST | `/plot/soil/delete`     | 删除土壤 |
| POST | `/plot/policy/current`  | 当前策略 |
| POST | `/plot/policy/save`     | 保存策略 |
| POST | `/plot/policy/activate` | 激活策略 |
