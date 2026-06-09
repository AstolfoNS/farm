package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserCrop;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCropDao extends JpaRepository<UserCrop, Long> {

  List<UserCrop> findByIsDeletedFalseOrderByIdAsc();

  List<UserCrop> findByUserIdAndIsDeletedFalseOrderByIdAsc(Long userId);

  Optional<UserCrop> findByPlotIdAndIsDeletedFalse(Long plotId);

  Optional<UserCrop> findByUserIdAndPlotIdAndIsDeletedFalse(Long userId, Long plotId);

  Optional<UserCrop> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

  boolean existsBySeedTypeIdAndIsDeletedFalse(Long seedTypeId);
}
