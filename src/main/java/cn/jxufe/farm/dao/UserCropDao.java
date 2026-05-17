package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserCrop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCropDao extends JpaRepository<UserCrop, Long> {
    List<UserCrop> findByUserIdAndIsDeletedFalseOrderByIdAsc(Long userId);

    Optional<UserCrop> findByPlotIdAndIsDeletedFalse(Long plotId);

    Optional<UserCrop> findByUserIdAndPlotIdAndIsDeletedFalse(Long userId, Long plotId);

    Optional<UserCrop> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);
}
