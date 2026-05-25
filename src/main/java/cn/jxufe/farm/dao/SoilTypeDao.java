package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.SoilType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoilTypeDao extends JpaRepository<SoilType, Long> {
    List<SoilType> findByIsDeletedFalseOrderByIdAsc();

    Optional<SoilType> findByIdAndIsDeletedFalse(Long id);

    Optional<SoilType> findFirstByIsDeletedFalseOrderByLevelAscIdAsc();

    List<SoilType> findByIdInAndIsDeletedFalse(List<Long> ids);
}
