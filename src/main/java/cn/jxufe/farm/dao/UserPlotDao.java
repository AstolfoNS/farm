package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserPlot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPlotDao extends JpaRepository<UserPlot, Long> {
  List<UserPlot> findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(Long userId);

  Optional<UserPlot> findByUserIdAndPlotIndexAndIsDeletedFalse(Long userId, Short plotIndex);

  Optional<UserPlot> findByIdAndIsDeletedFalse(Long id);

  Optional<UserPlot> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

  Optional<UserPlot> findTopByUserIdAndIsDeletedFalseOrderByPlotIndexDesc(Long userId);

  List<UserPlot> findByIdInAndIsDeletedFalse(List<Long> ids);

  boolean existsBySoilTypeIdAndIsDeletedFalse(Long soilTypeId);

  long countByUserIdAndIsDeletedFalse(Long userId);

  long countByUserIdAndIsLockedFalseAndIsDeletedFalse(Long userId);
}
