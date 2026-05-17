package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.RequestIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestIdempotencyDao extends JpaRepository<RequestIdempotency, Long> {

    Optional<RequestIdempotency> findByUserIdAndBizTypeAndRequestIdAndIsDeletedFalse(Long userId,
                                                                                       String bizType,
                                                                                       String requestId);
}
