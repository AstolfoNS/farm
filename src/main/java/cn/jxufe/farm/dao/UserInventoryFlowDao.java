package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserInventoryFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInventoryFlowDao extends JpaRepository<UserInventoryFlow, Long> {
    List<UserInventoryFlow> findByUserIdAndItemTypeAndIsDeletedFalseOrderByOccurredAtDesc(Long userId, String itemType);
}
