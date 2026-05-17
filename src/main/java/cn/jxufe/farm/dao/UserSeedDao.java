package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserSeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSeedDao extends JpaRepository<UserSeed, Long> {
    List<UserSeed> findByUserIdAndIsDeletedFalseOrderByIdAsc(Long userId);

    Optional<UserSeed> findByUserIdAndSeedTypeIdAndIsDeletedFalse(Long userId, Long seedTypeId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update UserSeed s
               set s.quantity = s.quantity + :amount,
                   s.updatedAt = :updatedAt,
                   s.updatedBy = :updatedBy
             where s.id = :id
               and s.isDeleted = false
            """)
    int increaseQuantity(
            @Param("id") Long id,
            @Param("amount") Long amount,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update UserSeed s
               set s.quantity = s.quantity - :amount,
                   s.updatedAt = :updatedAt,
                   s.updatedBy = :updatedBy
             where s.id = :id
               and s.isDeleted = false
               and (s.quantity - s.frozenQuantity) >= :amount
            """)
    int decreaseAvailableQuantityIfEnough(
            @Param("id") Long id,
            @Param("amount") Long amount,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );
}
