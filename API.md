# API 文档

> 编码：UTF-8  
> 返回统一封装：`R<T>`  
> 内容类型：`application/json`（文件上传接口除外）

## 1. 用户模块（`/user`）

- `GET /user/list`  
  - 响应：`R<PageResult<UserInfoVO>>`
- `POST /user/gridDataFilterSortPage`  
  - 请求：`UserGridQueryDTO`  
  - 响应：`R<PageResult<UserInfoVO>>`
- `POST /user/addOrUpdate`  
  - 请求：`UserAddOrUpdateDTO`  
  - 响应：`R<UserInfoVO>`
- `POST /user/delete`  
  - 请求：`IdDTO`  
  - 响应：`R<Void>`
- `POST /user/updateAvatar`  
  - 请求：`UserAvatarUpdateDTO`  
  - 响应：`R<UserAvatarVO>`
- `GET /user/loginOptions`  
  - 响应：`R<List<UserInfoVO>>`
- `POST /user/setCurUser`  
  - 请求：`SetCurUserDTO`  
  - 响应：`R<CurUserVO>`
- `GET /user/getCurUser`  
  - 响应：`R<CurUserVO>`

## 2. 文件模块（`/file`）

- `POST /file/upload`（`multipart/form-data`）  
  - 表单：`file`、`category`（可选）  
  - 响应：`R<FileUploadResultDTO>`
- `POST /file/saveHeadImg`（`multipart/form-data`）  
  - 表单：`file`  
  - 响应：`R<AvatarUploadVO>`
- `POST /file/delete`  
  - 请求：`FileRelativePathDTO`  
  - 响应：`R<Boolean>`
- `POST /file/url`  
  - 请求：`FileRelativePathDTO`  
  - 响应：`R<FileUrlVO>`

## 3. 种子与商店模块（`/seed`）

### 3.1 种子类型管理
- `POST /seed/type/page`  
  - 请求：`SeedTypeQueryDTO`（可空）  
  - 响应：`R<PageResult<SeedGridVO>>`
- `POST /seed/type/save`  
  - 请求：`SeedAddOrUpdateDTO`  
  - 响应：`R<Long>`
- `POST /seed/type/delete`  
  - 请求：`IdDTO`  
  - 响应：`R<Void>`

### 3.2 基础选项
- `GET /seed/quality/options` -> `R<List<OptionVO>>`
- `GET /seed/soil/options` -> `R<List<SoilOptionVO>>`
- `GET /seed/growth-stage/options` -> `R<List<OptionVO>>`

### 3.3 种子阶段配置
- `POST /seed/stage/page`  
  - 请求：`SeedStageQueryDTO`  
  - 响应：`R<PageResult<SeedStageGridVO>>`
- `POST /seed/stage/save`  
  - 请求：`SeedStageAddOrUpdateDTO`  
  - 响应：`R<Void>`
- `POST /seed/stage/delete`  
  - 请求：`IdDTO`  
  - 响应：`R<Void>`

### 3.4 商店能力
- `POST /seed/shop/page`  
  - 请求：`SeedShopQueryDTO`（可空）  
  - 响应：`R<PageResult<SeedShopItemVO>>`
- `POST /seed/shop/buy`  
  - 请求：`SeedShopBuyDTO`  
  - 响应：`R<SeedShopBuyResultVO>`
- `POST /seed/shop/sell-fruit`  
  - 请求：`SeedShopSellFruitDTO`  
  - 响应：`R<SeedShopSellFruitResultVO>`
- `POST /seed/shop/trade/page`  
  - 请求：`SeedShopTradeQueryDTO`  
  - 响应：`R<PageResult<SeedShopTradeRecordVO>>`
- `POST /seed/shop/fruit/page`  
  - 请求：`SeedFruitInventoryQueryDTO`  
  - 响应：`R<PageResult<SeedFruitInventoryItemVO>>`
- `POST /seed/shop/overview`  
  - 请求：`SeedShopOverviewDTO`  
  - 响应：`R<SeedShopOverviewVO>`
- `POST /seed/shop/home`  
  - 请求：`SeedShopHomeQueryDTO`（可空）  
  - 响应：`R<SeedShopHomeVO>`

## 4. 种植/收获/养护（`/gameplay`）

- `POST /gameplay/plant`  
  - 请求：`PlantCropDTO`  
  - 响应：`R<PlantResultVO>`
- `POST /gameplay/harvest`  
  - 请求：`HarvestCropDTO`  
  - 响应：`R<HarvestResultVO>`
- `POST /gameplay/care`  
  - 请求：`CareCropDTO`  
  - 响应：`R<CareResultVO>`
- `POST /gameplay/crop/action/page`  
  - 请求：`CropActionLogQueryDTO`  
  - 响应：`R<PageResult<CropActionLogRecordVO>>`

## 5. 地块经营（`/gameplay`）

- `POST /gameplay/plot/unlock`  
  - 请求：`PlotUnlockDTO`  
  - 响应：`R<PlotUnlockResultVO>`
- `POST /gameplay/plot/expand`  
  - 请求：`PlotExpandDTO`  
  - 响应：`R<PlotExpandResultVO>`
- `POST /gameplay/plot/status`  
  - 请求：`PlotStatusQueryDTO`  
  - 响应：`R<PlotStatusVO>`
- `POST /gameplay/plot/trade/page`  
  - 请求：`PlotTradeQueryDTO`  
  - 响应：`R<PageResult<PlotTradeRecordVO>>`
- `GET /gameplay/plot/trade/bizType/options`  
  - 响应：`R<List<PlotTradeBizTypeOptionVO>>`

## 6. 农场聚合查询（`/gameplay`）

- `POST /gameplay/myFarmOverview`  
  - 请求：`MyFarmOverviewDTO`  
  - 响应：`R<MyFarmOverviewVO>`
- `POST /gameplay/myPlantingPanel`  
  - 请求：`MyPlantingPanelDTO`  
  - 响应：`R<MyPlantingPanelVO>`
- `POST /gameplay/seedPlantablePlots`  
  - 请求：`SeedPlantablePlotsDTO`  
  - 响应：`R<SeedPlantablePlotsVO>`

## 7. 备注

- 业务异常通过全局异常处理统一返回 `R`。
- 校验异常由 `@Validated/@Valid` 触发并返回统一错误结构。
- 接口字段请以后端 DTO/VO 定义为准。