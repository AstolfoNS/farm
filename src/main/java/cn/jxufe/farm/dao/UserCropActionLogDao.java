package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserCropActionLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCropActionLogDao extends JpaRepository<UserCropActionLog, Long> {
  List<UserCropActionLog> findByUserIdAndIsDeletedFalseOrderByActionAtDesc(Long userId);
}
