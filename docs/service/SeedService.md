# SeedService — 种子与商店服务接口

**文件**: `service/SeedService.java` | **实现**: `SeedServiceImp` → `SeedServiceGuardedDecorator`（校验装饰器）

## 种子类型管理

### `PageResult<SeedGridVO> pageSeedTypes(SeedTypeQueryDTO query)`

- 按名称模糊搜索种子，分页返回

### `Long saveSeedType(SeedAddOrUpdateDTO params)`

- 新增或更新种子类型（完整配置：经济属性、虫害参数、收获机制）
- **事务**: @Transactional

### `void removeSeedType(IdDTO params)`

- 按 ID 软删除。被引用时禁止删除
- **事务**: @Transactional

## 基础选项

### `List<OptionVO> listSeedQualityOptions()`

- 种子品质字典：普通/优质/稀有

### `List<SoilOptionVO> listSoilOptions()`

- 土壤选项（含 bitCode），供种子编辑选择可种土壤

### `List<OptionVO> listGrowthStageOptions()`

- 生长阶段字典：种子/发芽/幼苗/生长期/开花/结果/成熟/枯萎

## 种子阶段配置

### `PageResult<SeedStageGridVO> pageSeedStages(SeedStageQueryDTO query)`

- 按 seedTypeId 查询某类种子的所有阶段

### `void saveSeedStage(SeedStageAddOrUpdateDTO params)`

- 新增或更新阶段：序号、时长、虫害概率、展示图参数
- **事务**: @Transactional

### `void removeSeedStage(IdDTO params)`

- 按 ID 软删除阶段
- **事务**: @Transactional

## 商店

### `PageResult<SeedShopItemVO> pageSeedShop(SeedShopQueryDTO query)`

- 可购买种子列表，含用户购买条件校验

### `SeedShopBuyResultVO buySeed(SeedShopBuyDTO params)`

- 购买种子：扣金币 + 加库存 + 双流水
- **幂等**: 需传 requestId
- **事务**: @Transactional

### `SeedShopSellFruitResultVO sellFruit(SeedShopSellFruitDTO params)`

- 出售果实：扣库存 + 加金币 + 双流水
- **幂等**: 需传 requestId
- **事务**: @Transactional

### `PageResult<SeedShopTradeRecordVO> pageShopTrades(SeedShopTradeQueryDTO query)`

- 交易记录分页（BUY_SEED / SELL_FRUIT）

### `PageResult<SeedFruitInventoryItemVO> pageFruitInventory(SeedFruitInventoryQueryDTO query)`

- 用户果实库存分页

### `PageResult<SeedInventoryItemVO> pageSeedInventory(SeedInventoryQueryDTO query)`

- 用户种子库存分页（默认返回空）

### `SeedShopOverviewVO shopOverview(SeedShopOverviewDTO query)`

- 商店概览：金币、可售总额、果实总数、可购种类

### `SeedShopHomeVO shopHome(SeedShopHomeQueryDTO query)`

- 商店首页 = 概览 + 商店分页的组合数据
