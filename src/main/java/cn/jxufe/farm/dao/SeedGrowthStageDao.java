package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.SeedGrowthStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeedGrowthStageDao extends JpaRepository<SeedGrowthStage, Long> {
    List<SeedGrowthStage> findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(Long seedTypeId);

    Optional<SeedGrowthStage> findByIdAndIsDeletedFalse(Long id);

    Optional<SeedGrowthStage> findBySeedTypeIdAndStageIndexAndIsDeletedFalse(Long seedTypeId, Short stageIndex);
}
