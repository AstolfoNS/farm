package cn.jxufe.farm.service.support;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.entity.SeedGrowthStage;
import cn.jxufe.farm.entity.SeedType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SeedStageConfigValidatorTest {

  @Test
  void validateCompleteAcceptsValidStageConfig() {
    assertThatCode(
            () ->
                SeedStageConfigValidator.validateComplete(
                    seedType((short) 2, (short) 1, (short) 2),
                    List.of(stage((short) 1, 1L), stage((short) 2, 2L), stage((short) 3, 3L)),
                    Map.of(1L, "发芽", 2L, "成熟", 3L, "枯萎")))
        .doesNotThrowAnyException();
  }

  @Test
  void validateCompleteRequiresWitherStageAtLastPosition() {
    assertThatThrownBy(
            () ->
                SeedStageConfigValidator.validateComplete(
                    seedType((short) 2, null, (short) 1),
                    List.of(stage((short) 1, 1L), stage((short) 2, 2L)),
                    Map.of(1L, "发芽", 2L, "成熟")))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining("枯萎");
  }

  @Test
  void validateDraftAllowsEmptyStageList() {
    assertThatCode(
            () ->
                SeedStageConfigValidator.validateDraft(
                    seedType((short) 1, null, (short) 1), List.of(), Map.of()))
        .doesNotThrowAnyException();
  }

  private static SeedType seedType(Short harvestStageIndex, Short regrowStageIndex, Short count) {
    SeedType seedType = new SeedType();
    seedType.setHarvestStageIndex(harvestStageIndex);
    seedType.setRegrowStageIndex(regrowStageIndex);
    seedType.setMaxHarvestCount(count);
    return seedType;
  }

  private static SeedGrowthStage stage(Short index, Long growthStageId) {
    SeedGrowthStage stage = new SeedGrowthStage();
    stage.setStageIndex(index);
    stage.setGrowthStageId(growthStageId);
    return stage;
  }
}
