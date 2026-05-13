package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends CrudRepository<User, Long> {
    List<User> findByIsDeletedFalseOrderByIdAsc();

    Page<User> findByIsDeletedFalseAndUsernameContainingIgnoreCase(String username, Pageable pageable);

    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    Optional<User> findByIdAndIsDeletedFalse(Long id);
}
