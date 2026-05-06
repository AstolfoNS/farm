package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.UserFruit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFruitDao extends CrudRepository<UserFruit, Long> {
}
