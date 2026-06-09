package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.SeedQuality;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeedQualityDao extends JpaRepository<SeedQuality, Long> {
  List<SeedQuality> findByIsDeletedFalseOrderByIdAsc();

  Optional<SeedQuality> findByIdAndIsDeletedFalse(Long id);
}
