package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.SoilType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoilTypeDao extends JpaRepository<SoilType, Long> {
  Page<SoilType> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);

  List<SoilType> findByIsDeletedFalseOrderByIdAsc();

  Optional<SoilType> findByIdAndIsDeletedFalse(Long id);

  Optional<SoilType> findByNameAndIsDeletedFalse(String name);

  Optional<SoilType> findByBitCodeAndIsDeletedFalse(Integer bitCode);

  Optional<SoilType> findFirstByIsDeletedFalseOrderByLevelAscIdAsc();

  List<SoilType> findByIdInAndIsDeletedFalse(List<Long> ids);
}
