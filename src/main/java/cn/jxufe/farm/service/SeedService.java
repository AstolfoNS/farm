package cn.jxufe.farm.service;

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
import cn.jxufe.farm.common.pages.PageResult;
import java.util.List;

public interface SeedService {
  PageResult<SeedGridVO> pageSeedTypes(SeedTypeQueryDTO query);

  PageResult<SeedShopItemVO> pageSeedShop(SeedShopQueryDTO query);

  SeedShopBuyResultVO buySeed(SeedShopBuyDTO params);

  SeedShopSellFruitResultVO sellFruit(SeedShopSellFruitDTO params);

  PageResult<SeedShopTradeRecordVO> pageShopTrades(SeedShopTradeQueryDTO query);

  PageResult<SeedFruitInventoryItemVO> pageFruitInventory(SeedFruitInventoryQueryDTO query);

  PageResult<SeedInventoryItemVO> pageSeedInventory(SeedInventoryQueryDTO query);

  SeedShopOverviewVO shopOverview(SeedShopOverviewDTO query);

  SeedShopHomeVO shopHome(SeedShopHomeQueryDTO query);

  Long saveSeedType(SeedAddOrUpdateDTO params);

  void removeSeedType(IdDTO params);

  List<OptionVO> listSeedQualityOptions();

  List<SoilOptionVO> listSoilOptions();

  List<OptionVO> listGrowthStageOptions();

  PageResult<SeedStageGridVO> pageSeedStages(SeedStageQueryDTO query);

  void saveSeedStage(SeedStageAddOrUpdateDTO params);

  void removeSeedStage(IdDTO params);

  void validateSeedStages(IdDTO params);
}
