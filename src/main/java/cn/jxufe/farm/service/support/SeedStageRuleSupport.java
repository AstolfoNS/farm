package cn.jxufe.farm.service.support;

import cn.jxufe.farm.entity.SeedGrowthStage;
import cn.jxufe.farm.entity.SeedType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class SeedStageRuleSupport {

  private SeedStageRuleSupport() {}

  public static ResolvedStageRule resolve(SeedType seedType, List<SeedGrowthStage> stages) {
    if (stages == null || stages.isEmpty()) {
      return new ResolvedStageRule((short) 1, (short) 1, (short) 1, (short) 1);
    }
    short firstStageIndex = safeStageIndex(stages.getFirst().getStageIndex(), (short) 1);
    short witherStageIndex = safeStageIndex(stages.getLast().getStageIndex(), firstStageIndex);
    short harvestStageIndex = resolveHarvestStageIndex(seedType, stages, witherStageIndex);
    short regrowStageIndex =
        resolveRegrowStageIndex(seedType, stages, firstStageIndex, harvestStageIndex);
    return new ResolvedStageRule(
        firstStageIndex, harvestStageIndex, witherStageIndex, regrowStageIndex);
  }

  public static int calculateSecondsUntilStage(
      List<SeedGrowthStage> stages,
      Short startStageIndex,
      Short targetStageIndex,
      BigDecimal multiplier) {
    if (stages == null || stages.isEmpty() || targetStageIndex == null) {
      return 0;
    }
    int startPos =
        findStagePos(
            stages, startStageIndex == null ? stages.getFirst().getStageIndex() : startStageIndex);
    int targetPos = findStagePos(stages, targetStageIndex);
    if (startPos < 0 || targetPos < 0 || targetPos <= startPos) {
      return 0;
    }

    int totalRawSeconds = 0;
    for (int i = startPos; i < targetPos; i++) {
      totalRawSeconds += Math.max(0, safeInt(stages.get(i).getDurationSeconds()));
    }
    if (totalRawSeconds <= 0) {
      return 0;
    }
    BigDecimal safeMultiplier =
        multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0
            ? BigDecimal.ONE
            : multiplier;
    BigDecimal adjusted =
        BigDecimal.valueOf(totalRawSeconds).divide(safeMultiplier, 0, RoundingMode.HALF_UP);
    return Math.max(0, adjusted.intValue());
  }

  public static SeedGrowthStage findStageByIndex(List<SeedGrowthStage> stages, Short stageIndex) {
    if (stages == null || stages.isEmpty() || stageIndex == null) {
      return null;
    }
    for (SeedGrowthStage stage : stages) {
      if (stage != null && safeStageIndex(stage.getStageIndex(), (short) 0) == stageIndex) {
        return stage;
      }
    }
    return null;
  }

  public static int findStagePos(List<SeedGrowthStage> stages, Short stageIndex) {
    if (stages == null || stages.isEmpty() || stageIndex == null) {
      return -1;
    }
    for (int i = 0; i < stages.size(); i++) {
      if (safeStageIndex(stages.get(i).getStageIndex(), (short) 0) == stageIndex) {
        return i;
      }
    }
    return -1;
  }

  private static short resolveHarvestStageIndex(
      SeedType seedType, List<SeedGrowthStage> stages, short witherStageIndex) {
    Short configured = seedType == null ? null : seedType.getHarvestStageIndex();
    int configuredPos = findStagePos(stages, configured);
    int witherPos = findStagePos(stages, witherStageIndex);
    if (configuredPos >= 0 && configuredPos < witherPos) {
      return configured;
    }
    if (witherPos > 0) {
      return safeStageIndex(stages.get(witherPos - 1).getStageIndex(), witherStageIndex);
    }
    return witherStageIndex;
  }

  private static short resolveRegrowStageIndex(
      SeedType seedType,
      List<SeedGrowthStage> stages,
      short firstStageIndex,
      short harvestStageIndex) {
    Short configured = seedType == null ? null : seedType.getRegrowStageIndex();
    int configuredPos = findStagePos(stages, configured);
    int harvestPos = findStagePos(stages, harvestStageIndex);
    if (configuredPos >= 0 && configuredPos < harvestPos) {
      return configured;
    }
    return firstStageIndex;
  }

  private static short safeStageIndex(Short value, short fallback) {
    return value == null || value <= 0 ? fallback : value;
  }

  private static int safeInt(Integer value) {
    return value == null ? 0 : value;
  }

  public record ResolvedStageRule(
      short firstStageIndex,
      short harvestStageIndex,
      short witherStageIndex,
      short regrowStageIndex) {}
}
