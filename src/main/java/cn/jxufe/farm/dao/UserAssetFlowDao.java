package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserAssetFlow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAssetFlowDao extends JpaRepository<UserAssetFlow, Long> {
  List<UserAssetFlow> findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(Long userId);
}
