package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.GrowthStage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrowthStageDao extends JpaRepository<GrowthStage, Long> {
  List<GrowthStage> findByIsDeletedFalseOrderByIdAsc();

  Optional<GrowthStage> findByIdAndIsDeletedFalse(Long id);
}
