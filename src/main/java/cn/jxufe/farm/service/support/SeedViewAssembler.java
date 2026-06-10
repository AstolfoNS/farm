package cn.jxufe.farm.service.support;

import cn.jxufe.farm.bean.vo.OptionVO;
import cn.jxufe.farm.bean.vo.SeedGridVO;
import cn.jxufe.farm.bean.vo.SeedShopItemVO;
import cn.jxufe.farm.bean.vo.SoilOptionVO;
import cn.jxufe.farm.entity.SeedQuality;
import cn.jxufe.farm.entity.SeedType;
import cn.jxufe.farm.service.GameplayCoreService;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SeedViewAssembler {

  private final GameplayCoreService gameplayCoreService;

  public SeedViewAssembler(GameplayCoreService gameplayCoreService) {
    this.gameplayCoreService = gameplayCoreService;
  }

  public OptionVO option(Long id, String text) {
    OptionVO vo = new OptionVO();
    vo.setId(id);
    vo.setText(gameplayCoreService.safeString(text));
    return vo;
  }

  public SoilOptionVO soilOption(Long id, String text, Integer bitCode) {
    SoilOptionVO vo = new SoilOptionVO();
    vo.setId(id);
    vo.setText(gameplayCoreService.safeString(text));
    vo.setBitCode(bitCode);
    return vo;
  }

  public SeedGridVO seedGrid(
      SeedType seedType,
      Map<Long, SeedQuality> qualityMap,
      Map<Integer, String> soilNameByBitCode,
      Map<Long, Integer> totalGrowSecondsMap) {
    SeedGridVO vo = new SeedGridVO();
    vo.setId(seedType.getId());
    vo.setName(gameplayCoreService.safeString(seedType.getName()));
    vo.setCoverImageUrl(gameplayCoreService.safeString(seedType.getCoverImageUrl()));
    vo.setSeedQualityId(seedType.getSeedQualityId());
    vo.setSeedQualityName(resolveSeedQualityName(qualityMap, seedType.getSeedQualityId()));
    vo.setEnableSoilTypeBits(gameplayCoreService.defaultLong(seedType.getEnableSoilTypeBits(), 0L));
    vo.setEnableSoilTypeNames(resolveSoilName(seedType.getEnableSoilTypeBits(), soilNameByBitCode));
    vo.setLevel(seedType.getLevel());
    vo.setDescription(gameplayCoreService.safeString(seedType.getDescription()));
    vo.setMaxBugLimit(seedType.getMaxBugLimit());
    vo.setMaxHarvestCount(seedType.getMaxHarvestCount());
    vo.setRegrowStageIndex(seedType.getRegrowStageIndex());
    vo.setPrice(gameplayCoreService.defaultLong(seedType.getPrice(), 0L));
    vo.setHarvestExperience(gameplayCoreService.defaultLong(seedType.getHarvestExperience(), 0L));
    vo.setHarvestFruitNumber(defaultInteger(seedType.getHarvestFruitNumber(), 0));
    vo.setFruitLossPerBug(defaultInteger(seedType.getFruitLossPerBug(), 0));
    vo.setBugKillCoinReward(gameplayCoreService.defaultLong(seedType.getBugKillCoinReward(), 0L));
    vo.setBugKillExperienceReward(
        gameplayCoreService.defaultLong(seedType.getBugKillExperienceReward(), 0L));
    vo.setBugKillScoreReward(gameplayCoreService.defaultLong(seedType.getBugKillScoreReward(), 0L));
    vo.setFruitPrice(gameplayCoreService.defaultLong(seedType.getFruitPrice(), 0L));
    vo.setHarvestScore(gameplayCoreService.defaultLong(seedType.getHarvestScore(), 0L));
    vo.setTotalGrowSeconds(totalGrowSecondsMap.getOrDefault(seedType.getId(), 0));
    return vo;
  }

  public SeedShopItemVO seedShopItem(
      SeedType seedType,
      Map<Long, SeedQuality> qualityMap,
      Map<Integer, String> soilNameByBitCode,
      Map<Long, Integer> totalGrowSecondsMap) {
    SeedShopItemVO vo = new SeedShopItemVO();
    vo.setId(seedType.getId());
    vo.setName(gameplayCoreService.safeString(seedType.getName()));
    vo.setCoverImageUrl(gameplayCoreService.safeString(seedType.getCoverImageUrl()));
    vo.setSeedQualityId(seedType.getSeedQualityId());
    vo.setSeedQualityName(resolveSeedQualityName(qualityMap, seedType.getSeedQualityId()));
    vo.setLevel(seedType.getLevel());
    vo.setEnableSoilTypeBits(gameplayCoreService.defaultLong(seedType.getEnableSoilTypeBits(), 0L));
    vo.setEnableSoilTypeNames(resolveSoilName(seedType.getEnableSoilTypeBits(), soilNameByBitCode));
    vo.setDescription(gameplayCoreService.safeString(seedType.getDescription()));
    vo.setPrice(gameplayCoreService.defaultLong(seedType.getPrice(), 0L));
    vo.setHarvestFruitNumber(defaultInteger(seedType.getHarvestFruitNumber(), 0));
    vo.setFruitLossPerBug(defaultInteger(seedType.getFruitLossPerBug(), 0));
    vo.setBugKillCoinReward(gameplayCoreService.defaultLong(seedType.getBugKillCoinReward(), 0L));
    vo.setBugKillExperienceReward(
        gameplayCoreService.defaultLong(seedType.getBugKillExperienceReward(), 0L));
    vo.setBugKillScoreReward(gameplayCoreService.defaultLong(seedType.getBugKillScoreReward(), 0L));
    vo.setFruitPrice(gameplayCoreService.defaultLong(seedType.getFruitPrice(), 0L));
    vo.setHarvestExperience(gameplayCoreService.defaultLong(seedType.getHarvestExperience(), 0L));
    vo.setHarvestScore(gameplayCoreService.defaultLong(seedType.getHarvestScore(), 0L));
    vo.setMaxHarvestCount(
        seedType.getMaxHarvestCount() == null ? 1 : seedType.getMaxHarvestCount());
    vo.setTotalGrowSeconds(totalGrowSecondsMap.getOrDefault(seedType.getId(), 0));
    long singleHarvestFruitValue =
        gameplayCoreService.safeMultiply(vo.getHarvestFruitNumber(), vo.getFruitPrice());
    long totalHarvestFruitValue =
        gameplayCoreService.safeMultiply(singleHarvestFruitValue, vo.getMaxHarvestCount());
    vo.setSingleHarvestFruitValue(singleHarvestFruitValue);
    vo.setTotalHarvestFruitValue(totalHarvestFruitValue);
    vo.setEstimatedNetValue(totalHarvestFruitValue - vo.getPrice());
    return vo;
  }

  private String resolveSeedQualityName(Map<Long, SeedQuality> qualityMap, Long seedQualityId) {
    return qualityMap.containsKey(seedQualityId)
        ? gameplayCoreService.safeString(qualityMap.get(seedQualityId).getName())
        : "";
  }

  private String resolveSoilName(Long soilBits, Map<Integer, String> soilNameByBitCode) {
    long bits = gameplayCoreService.defaultLong(soilBits, 0L);
    if (bits <= 0) {
      return "";
    }
    return soilNameByBitCode.entrySet().stream()
        .filter(e -> e.getKey() != null && e.getKey() > 0 && (bits & e.getKey()) == e.getKey())
        .map(Map.Entry::getValue)
        .collect(Collectors.joining("/"));
  }

  private Integer defaultInteger(Integer value, int fallback) {
    return value == null ? fallback : value;
  }
}
