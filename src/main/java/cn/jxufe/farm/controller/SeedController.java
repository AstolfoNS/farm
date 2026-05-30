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
import cn.jxufe.farm.bean.vo.SeedGridVO;
import cn.jxufe.farm.bean.vo.SeedInventoryItemVO;
import cn.jxufe.farm.bean.vo.SeedFruitInventoryItemVO;
import cn.jxufe.farm.bean.vo.SeedShopBuyResultVO;
import cn.jxufe.farm.bean.vo.SeedShopHomeVO;
import cn.jxufe.farm.bean.vo.SeedShopItemVO;
import cn.jxufe.farm.bean.vo.SeedShopOverviewVO;
import cn.jxufe.farm.bean.vo.SeedShopSellFruitResultVO;
import cn.jxufe.farm.bean.vo.SeedShopTradeRecordVO;
import cn.jxufe.farm.bean.vo.SeedStageGridVO;
import cn.jxufe.farm.bean.vo.SoilOptionVO;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.service.SeedService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/seed", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class SeedController {

    private final SeedService seedService;

    public SeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @PostMapping(value = "/type/page", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<SeedGridVO>> pageSeedTypes(@Valid @RequestBody(required = false) SeedTypeQueryDTO query) {
        return R.ok(seedService.pageSeedTypes(query));
    }

    @PostMapping(value = "/shop/page", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<SeedShopItemVO>> pageSeedShop(@Valid @RequestBody(required = false) SeedShopQueryDTO query) {
        return R.ok(seedService.pageSeedShop(query));
    }

    @PostMapping(value = "/shop/buy", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<SeedShopBuyResultVO> buySeed(@Valid @RequestBody SeedShopBuyDTO params) {
        return R.ok(seedService.buySeed(params));
    }

    @PostMapping(value = "/shop/sell-fruit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<SeedShopSellFruitResultVO> sellFruit(@Valid @RequestBody SeedShopSellFruitDTO params) {
        return R.ok(seedService.sellFruit(params));
    }

    @PostMapping(value = "/shop/trade/page", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<SeedShopTradeRecordVO>> pageShopTrades(@Valid @RequestBody SeedShopTradeQueryDTO query) {
        return R.ok(seedService.pageShopTrades(query));
    }

    @PostMapping(value = "/shop/fruit/page", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<SeedFruitInventoryItemVO>> pageFruitInventory(@Valid @RequestBody SeedFruitInventoryQueryDTO query) {
        return R.ok(seedService.pageFruitInventory(query));
    }

    @PostMapping(value = "/shop/seed/page", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<SeedInventoryItemVO>> pageSeedInventory(@Valid @RequestBody SeedInventoryQueryDTO query) {
        return R.ok(seedService.pageSeedInventory(query));
    }

    @PostMapping(value = "/shop/overview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<SeedShopOverviewVO> shopOverview(@Valid @RequestBody SeedShopOverviewDTO query) {
        return R.ok(seedService.shopOverview(query));
    }

    @PostMapping(value = "/shop/home", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<SeedShopHomeVO> shopHome(@Valid @RequestBody(required = false) SeedShopHomeQueryDTO query) {
        return R.ok(seedService.shopHome(query));
    }

    @PostMapping(value = "/type/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Long> saveSeedType(@Valid @RequestBody SeedAddOrUpdateDTO params) {
        return R.ok(seedService.saveSeedType(params));
    }

    @PostMapping(value = "/type/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Void> removeSeedType(@Valid @RequestBody IdDTO params) {
        seedService.removeSeedType(params);
        return R.ok();
    }

    @GetMapping("/quality/options")
    public R<List<OptionVO>> listSeedQualityOptions() {
        return R.ok(seedService.listSeedQualityOptions());
    }

    @GetMapping("/soil/options")
    public R<List<SoilOptionVO>> listSoilOptions() {
        return R.ok(seedService.listSoilOptions());
    }

    @GetMapping("/growth-stage/options")
    public R<List<OptionVO>> listGrowthStageOptions() {
        return R.ok(seedService.listGrowthStageOptions());
    }

    @PostMapping(value = "/stage/page", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<SeedStageGridVO>> pageSeedStages(@Valid @RequestBody SeedStageQueryDTO query) {
        return R.ok(seedService.pageSeedStages(query));
    }

    @PostMapping(value = "/stage/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Void> saveSeedStage(@Valid @RequestBody SeedStageAddOrUpdateDTO params) {
        seedService.saveSeedStage(params);
        return R.ok();
    }

    @PostMapping(value = "/stage/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Void> removeSeedStage(@Valid @RequestBody IdDTO params) {
        seedService.removeSeedStage(params);
        return R.ok();
    }
}
