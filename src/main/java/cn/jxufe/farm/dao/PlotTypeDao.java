package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.PlotType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlotTypeDao extends JpaRepository<PlotType, Long> {

    Page<PlotType> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<PlotType> findByIdAndIsDeletedFalse(Long id);

    Optional<PlotType> findFirstBySoilTypeIdAndIsDeletedFalseOrderBySortOrderAscIdAsc(Long soilTypeId);

    Optional<PlotType> findByNameAndIsDeletedFalse(String name);

    List<PlotType> findByIsDeletedFalseOrderBySortOrderAscIdAsc();
}

