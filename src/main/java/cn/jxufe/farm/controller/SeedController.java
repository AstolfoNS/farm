package cn.jxufe.farm.controller;

import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.SeedAddOrUpdateDTO;
import cn.jxufe.farm.bean.dto.SeedFruitInventoryQueryDTO;
import cn.jxufe.farm.bean.dto.SeedInventoryQueryDTO;
import cn.jxufe.farm.bean.dto.SeedShopBuyDTO;
import cn.jxufe.farm.bean.dto.SeedShopHomeQueryDTO;
import cn.jxufe.farm.bean.dto.SeedShopOverviewDTO;
import cn.jxufe.farm.bean.dto.SeedShopQueryDTO;
import cn.jxufe.farm.bean.dto.SeedShopSellFruitDTO;
import cn.jxufe.farm.bean.dto.SeedShopTradeQueryDTO;
import cn.jxufe.farm.bean.dto.SeedStageAddOrUpdateDTO;
import cn.jxufe.farm.bean.dto.SeedStageQueryDTO;
import cn.jxufe.farm.bean.dto.SeedTypeQueryDTO;
import cn.jxufe.farm.bean.vo.OptionVO;
import cn.jxufe.farm.bean.vo.SeedFruitInventoryItemVO;
import cn.jxufe.farm.bean.vo.SeedGridVO;
import cn.jxufe.farm.bean.vo.SeedInventoryItemVO;
import cn.jxufe.farm.bean.vo.SeedShopBuyResultVO;
import cn.jxufe.farm.bean.vo.SeedShopHomeVO;
import cn.jxufe.farm.bean.vo.SeedShopItemVO;
import cn.jxufe.farm.bean.vo.SeedShopOverviewVO;
import cn.jxufe.farm.bean.vo.SeedShopSellFruitResultVO;
import cn.jxufe.farm.bean.vo.SeedShopTradeRecordVO;
import cn.jxufe.farm.bean.vo.SeedStageGridVO;
import cn.jxufe.farm.bean.vo.SoilOptionVO;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.service.SeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "种子与商店", description = "种子类型与阶段管理、商店购买/出售、交易记录、库存查询")
@RestController
@RequestMapping(value = "/seed", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class SeedController {

  private final SeedService seedService;

  public SeedController(SeedService seedService) {
    this.seedService = seedService;
  }

  // ======================== 种子类型管理 ========================

  @Operation(summary = "种子类型分页", description = "按种子名称模糊搜索，支持分页排序")
  @PostMapping(value = "/type/page", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<PageResult<SeedGridVO>> pageSeedTypes(
      @Valid @RequestBody(required = false) SeedTypeQueryDTO query) {
    return R.ok(seedService.pageSeedTypes(query));
  }

  @Operation(summary = "保存种子类型", description = "新增或更新种子类型。含经济属性、虫害参数、收获机制、可种土壤(bits)等完整配置")
  @PostMapping(value = "/type/save", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Long> saveSeedType(@Valid @RequestBody SeedAddOrUpdateDTO params) {
    return R.ok(seedService.saveSeedType(params));
  }

  @Operation(summary = "删除种子类型", description = "按 ID 软删除种子类型。被用户种子库存引用时禁止删除")
  @PostMapping(value = "/type/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Void> removeSeedType(@Valid @RequestBody IdDTO params) {
    seedService.removeSeedType(params);
    return R.ok();
  }

  // ======================== 基础选项 ========================

  @Operation(summary = "种子品质选项", description = "返回所有种子品质字典(普通/优质/稀有)，供下拉框使用")
  @GetMapping("/quality/options")
  public R<List<OptionVO>> listSeedQualityOptions() {
    return R.ok(seedService.listSeedQualityOptions());
  }

  @Operation(summary = "土壤选项", description = "返回所有土壤类型列表(含 bitCode)，供种子编辑时选择可种土壤")
  @GetMapping("/soil/options")
  public R<List<SoilOptionVO>> listSoilOptions() {
    return R.ok(seedService.listSoilOptions());
  }

  @Operation(summary = "生长阶段选项", description = "返回所有生长阶段字典(种子/发芽/幼苗/生长期/开花/结果/成熟/枯萎)，供阶段编辑使用")
  @GetMapping("/growth-stage/options")
  public R<List<OptionVO>> listGrowthStageOptions() {
    return R.ok(seedService.listGrowthStageOptions());
  }

  // ======================== 种子阶段配置 ========================

  @Operation(summary = "种子阶段分页", description = "按 seedTypeId 查询某一类种子配置的所有生长阶段")
  @PostMapping(value = "/stage/page", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<PageResult<SeedStageGridVO>> pageSeedStages(
      @Valid @RequestBody SeedStageQueryDTO query) {
    return R.ok(seedService.pageSeedStages(query));
  }

  @Operation(summary = "保存种子阶段", description = "新增或更新种子生长阶段。含阶段序号、时长、虫害概率、展示图宽高偏移等")
  @PostMapping(value = "/stage/save", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Void> saveSeedStage(@Valid @RequestBody SeedStageAddOrUpdateDTO params) {
    seedService.saveSeedStage(params);
    return R.ok();
  }

  @Operation(summary = "删除种子阶段", description = "按 ID 软删除种子阶段配置")
  @PostMapping(value = "/stage/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Void> removeSeedStage(@Valid @RequestBody IdDTO params) {
    seedService.removeSeedStage(params);
    return R.ok();
  }

  @Operation(summary = "校验种子阶段清单", description = "保存阶段清单后触发完整阶段规则校验，检查必要阶段、阶段序号、收获/再生阶段等配置")
  @PostMapping(value = "/stage/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<Void> validateSeedStages(@Valid @RequestBody IdDTO params) {
    seedService.validateSeedStages(params);
    return R.ok();
  }

  // ======================== 商店 ========================

  @Operation(summary = "商店分页", description = "查询可购买的种子列表。可按名称、品质、等级筛选，返回种子详情及用户是否满足购买条件")
  @PostMapping(value = "/shop/page", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<PageResult<SeedShopItemVO>> pageSeedShop(
      @Valid @RequestBody(required = false) SeedShopQueryDTO query) {
    return R.ok(seedService.pageSeedShop(query));
  }

  @Operation(summary = "购买种子", description = "扣金币、增加种子库存，写资产流水和库存流水。幂等接口，需传 requestId")
  @PostMapping(value = "/shop/buy", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<SeedShopBuyResultVO> buySeed(@Valid @RequestBody SeedShopBuyDTO params) {
    return R.ok(seedService.buySeed(params));
  }

  @Operation(summary = "出售果实", description = "扣果实库存、增加金币，写库存流水和资产流水。幂等接口，需传 requestId")
  @PostMapping(value = "/shop/sell-fruit", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<SeedShopSellFruitResultVO> sellFruit(@Valid @RequestBody SeedShopSellFruitDTO params) {
    return R.ok(seedService.sellFruit(params));
  }

  @Operation(summary = "交易记录分页", description = "查询用户的商店交易流水(BUY_SEED / SELL_FRUIT)，支持按交易类型筛选")
  @PostMapping(value = "/shop/trade/page", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<PageResult<SeedShopTradeRecordVO>> pageShopTrades(
      @Valid @RequestBody SeedShopTradeQueryDTO query) {
    return R.ok(seedService.pageShopTrades(query));
  }

  @Operation(summary = "果实库存分页", description = "查询用户的果实库存列表，含可出售估值")
  @PostMapping(value = "/shop/fruit/page", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<PageResult<SeedFruitInventoryItemVO>> pageFruitInventory(
      @Valid @RequestBody SeedFruitInventoryQueryDTO query) {
    return R.ok(seedService.pageFruitInventory(query));
  }

  @Operation(summary = "种子库存分页", description = "查询用户的种子库存列表")
  @PostMapping(value = "/shop/seed/page", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<PageResult<SeedInventoryItemVO>> pageSeedInventory(
      @Valid @RequestBody SeedInventoryQueryDTO query) {
    return R.ok(seedService.pageSeedInventory(query));
  }

  @Operation(summary = "商店概览", description = "返回用户金币、可售果实总值/总数、可购种子种类数等汇总信息")
  @PostMapping(value = "/shop/overview", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<SeedShopOverviewVO> shopOverview(@Valid @RequestBody SeedShopOverviewDTO query) {
    return R.ok(seedService.shopOverview(query));
  }

  @Operation(summary = "商店首页", description = "返回商店概览 + 种子商店分页的组合数据，供前端首页一次性加载")
  @PostMapping(value = "/shop/home", consumes = MediaType.APPLICATION_JSON_VALUE)
  public R<SeedShopHomeVO> shopHome(
      @Valid @RequestBody(required = false) SeedShopHomeQueryDTO query) {
    return R.ok(seedService.shopHome(query));
  }
}
