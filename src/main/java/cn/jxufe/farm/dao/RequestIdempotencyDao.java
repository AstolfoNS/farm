package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.RequestIdempotency;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestIdempotencyDao extends JpaRepository<RequestIdempotency, Long> {

  Optional<RequestIdempotency> findByUserIdAndBizTypeAndRequestIdAndIsDeletedFalse(
      Long userId, String bizType, String requestId);
}
