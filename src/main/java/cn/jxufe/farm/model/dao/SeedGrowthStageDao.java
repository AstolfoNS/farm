package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.SeedGrowthStage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeedGrowthStageDao extends CrudRepository<SeedGrowthStage, Long> {
    List<SeedGrowthStage> findBySeedTypeIdAndIsDeletedFalseOrderByStageIndexAsc(Long seedTypeId);

    Optional<SeedGrowthStage> findByIdAndIsDeletedFalse(Long id);
}
