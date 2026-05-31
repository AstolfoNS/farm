package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.PlotPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlotPolicyDao extends JpaRepository<PlotPolicy, Long> {

    Optional<PlotPolicy> findByIdAndIsDeletedFalse(Long id);

    Optional<PlotPolicy> findFirstByActiveTrueAndIsDeletedFalseOrderByIdAsc();

    List<PlotPolicy> findByIsDeletedFalseOrderByIdAsc();

    boolean existsByDefaultPlotTypeIdAndIsDeletedFalse(Long defaultPlotTypeId);
}
