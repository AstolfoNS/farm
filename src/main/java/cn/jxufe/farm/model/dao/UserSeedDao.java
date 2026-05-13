package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.UserSeed;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSeedDao extends CrudRepository<UserSeed, Long> {
}
