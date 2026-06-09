package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.SeedGrowthStage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeedGrowthStageDao extends JpaRepository<SeedGrowthStage, Long> {
  List<SeedGrowthStage> findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(Long seedTypeId);

  Optional<SeedGrowthStage> findByIdAndIsDeletedFalse(Long id);

  Optional<SeedGrowthStage> findBySeedTypeIdAndStageIndexAndIsDeletedFalse(
      Long seedTypeId, Short stageIndex);

  List<SeedGrowthStage> findBySeedTypeIdInAndIsDeletedFalseOrderBySeedTypeIdAscStageIndexAsc(
      List<Long> seedTypeIds);
}
