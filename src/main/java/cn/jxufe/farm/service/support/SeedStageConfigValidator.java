package cn.jxufe.farm.service.support;

import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exceptions.ServiceException;
import cn.jxufe.farm.entity.SeedGrowthStage;
import cn.jxufe.farm.entity.SeedType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SeedStageConfigValidator {

  private static final String WITHER_STAGE_NAME = "枯萎";

  private SeedStageConfigValidator() {}

  public static void validateComplete(
      SeedType seedType, List<SeedGrowthStage> stages, Map<Long, String> growthStageNameMap) {
    StageSummary summary = validateSequence(stages, growthStageNameMap, true);
    validateRequiredPointers(seedType, summary);
  }

  public static void validateDraft(
      SeedType seedType, List<SeedGrowthStage> stages, Map<Long, String> growthStageNameMap) {
    StageSummary summary = validateSequence(stages, growthStageNameMap, false);
    if (summary.isEmpty()) {
      return;
    }
    validateOptionalPointers(seedType, summary);
  }

  public static Short normalizeStagePointer(Short value) {
    return value == null || value <= 0 ? null : value;
  }

  public static short normalizeMaxHarvestCount(Short value) {
    return value == null || value <= 0 ? (short) 1 : value;
  }

  private static StageSummary validateSequence(
      List<SeedGrowthStage> stages,
      Map<Long, String> growthStageNameMap,
      boolean requireCompleteConfig) {
    if (stages == null || stages.isEmpty()) {
      if (requireCompleteConfig) {
        throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "请至少配置一个成长阶段");
      }
      return StageSummary.empty();
    }
    short expected = 1;
    short lastStageIndex = 0;
    short witherStageCount = 0;
    Short witherStageIndex = null;
    Set<Short> stageIndexSet = new HashSet<>();
    for (SeedGrowthStage stage : stages) {
      short actual = stage == null || stage.getStageIndex() == null ? 0 : stage.getStageIndex();
      if (actual != expected) {
        throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "阶段序号必须连续为 1..N");
      }
      stageIndexSet.add(actual);
      lastStageIndex = actual;
      String growthStageName =
          safeString(
                  growthStageNameMap == null || stage == null
                      ? null
                      : growthStageNameMap.get(stage.getGrowthStageId()))
              .trim();
      if (WITHER_STAGE_NAME.equals(growthStageName)) {
        witherStageCount++;
        witherStageIndex = actual;
      }
      expected++;
    }
    if (witherStageCount > 1) {
      throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "同一种子只能配置一个枯萎阶段");
    }
    if (witherStageIndex != null && witherStageIndex != lastStageIndex) {
      throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "枯萎阶段必须位于最后一阶段");
    }
    if (requireCompleteConfig && witherStageIndex == null) {
      throw new ServiceException(BizErrorCode.SEED_STAGE_SEQUENCE_INVALID, "请为该种子补充最后一个“枯萎”阶段");
    }
    return new StageSummary(stageIndexSet, lastStageIndex, witherStageIndex);
  }

  private static void validateRequiredPointers(SeedType seedType, StageSummary summary) {
    Short harvest =
        normalizeStagePointer(seedType == null ? null : seedType.getHarvestStageIndex());
    Short regrow = normalizeStagePointer(seedType == null ? null : seedType.getRegrowStageIndex());
    if (harvest == null || !summary.contains(harvest)) {
      throw new ServiceException(BizErrorCode.SEED_HARVEST_STAGE_INVALID, "收获阶段必须存在于该种子的阶段集合中");
    }
    if (harvest >= summary.witherStageIndex()) {
      throw new ServiceException(BizErrorCode.SEED_HARVEST_STAGE_INVALID, "收获阶段必须早于最后的枯萎阶段");
    }
    if (normalizeMaxHarvestCount(seedType == null ? null : seedType.getMaxHarvestCount()) > 1) {
      if (regrow == null || !summary.contains(regrow)) {
        throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "多次收获作物必须配置存在的再生阶段");
      }
      if (regrow >= harvest) {
        throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "收获阶段序号必须大于再生阶段序号");
      }
    } else if (regrow != null && summary.contains(regrow) && regrow >= harvest) {
      throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "再生阶段序号必须小于收获阶段序号");
    }
  }

  private static void validateOptionalPointers(SeedType seedType, StageSummary summary) {
    Short harvest =
        normalizeStagePointer(seedType == null ? null : seedType.getHarvestStageIndex());
    Short regrow = normalizeStagePointer(seedType == null ? null : seedType.getRegrowStageIndex());
    if (harvest != null && summary.contains(harvest)) {
      if (summary.witherStageIndex() != null && harvest >= summary.witherStageIndex()) {
        throw new ServiceException(BizErrorCode.SEED_HARVEST_STAGE_INVALID, "收获阶段必须早于枯萎阶段");
      }
      if (harvest >= summary.lastStageIndex()) {
        throw new ServiceException(BizErrorCode.SEED_HARVEST_STAGE_INVALID, "收获阶段不能落在最后一阶段");
      }
    }
    if (regrow != null
        && harvest != null
        && summary.contains(regrow)
        && summary.contains(harvest)
        && regrow >= harvest) {
      throw new ServiceException(BizErrorCode.SEED_REGROW_STAGE_INVALID, "再生阶段序号必须小于收获阶段序号");
    }
  }

  private static String safeString(String value) {
    return value == null ? "" : value;
  }

  private record StageSummary(
      Set<Short> stageIndexSet, short lastStageIndex, Short witherStageIndex) {
    static StageSummary empty() {
      return new StageSummary(Set.of(), (short) 0, null);
    }

    boolean isEmpty() {
      return stageIndexSet.isEmpty();
    }

    boolean contains(Short stageIndex) {
      return stageIndex != null && stageIndexSet.contains(stageIndex);
    }
  }
}
