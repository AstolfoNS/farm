package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends CrudRepository<User, Long> {
}
