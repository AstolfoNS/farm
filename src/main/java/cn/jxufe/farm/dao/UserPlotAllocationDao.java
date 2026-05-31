package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserPlotAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPlotAllocationDao extends JpaRepository<UserPlotAllocation, Long> {

    Optional<UserPlotAllocation> findByIdAndIsDeletedFalse(Long id);

    Optional<UserPlotAllocation> findByUserIdAndIsDeletedFalse(Long userId);

    Optional<UserPlotAllocation> findByUserIdAndActiveTrueAndIsDeletedFalse(Long userId);

    Page<UserPlotAllocation> findByIsDeletedFalse(Pageable pageable);

    boolean existsByDefaultPlotTypeIdAndIsDeletedFalse(Long defaultPlotTypeId);
}
