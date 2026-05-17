package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserCropActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCropActionLogDao extends JpaRepository<UserCropActionLog, Long> {
    List<UserCropActionLog> findByUserIdAndIsDeletedFalseOrderByActionAtDesc(Long userId);
}
