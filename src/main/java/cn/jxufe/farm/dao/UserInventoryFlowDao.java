package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserInventoryFlow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInventoryFlowDao extends JpaRepository<UserInventoryFlow, Long> {
  List<UserInventoryFlow> findByUserIdAndItemTypeAndIsDeletedFalseOrderByOccurredAtDesc(
      Long userId, String itemType);

  boolean existsBySeedTypeIdAndIsDeletedFalse(Long seedTypeId);
}
