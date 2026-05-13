package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.UserCrop;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCropDao extends CrudRepository<UserCrop, Long> {
}
