package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.UserFruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserFruitDao extends JpaRepository<UserFruit, Long> {
    List<UserFruit> findByUserIdAndIsDeletedFalseOrderByIdAsc(Long userId);

    Optional<UserFruit> findByUserIdAndSeedTypeIdAndIsDeletedFalse(Long userId, Long seedTypeId);

    boolean existsBySeedTypeIdAndIsDeletedFalse(Long seedTypeId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update UserFruit f
               set f.quantity = f.quantity + :amount,
                   f.updatedAt = :updatedAt,
                   f.updatedBy = :updatedBy
             where f.id = :id
               and f.isDeleted = false
            """)
    int increaseQuantity(
            @Param("id") Long id,
            @Param("amount") Long amount,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update UserFruit f
               set f.quantity = f.quantity - :amount,
                   f.updatedAt = :updatedAt,
                   f.updatedBy = :updatedBy
             where f.id = :id
               and f.isDeleted = false
               and (f.quantity - f.frozenQuantity) >= :amount
            """)
    int decreaseAvailableQuantityIfEnough(
            @Param("id") Long id,
            @Param("amount") Long amount,
            @Param("updatedBy") Long updatedBy,
            @Param("updatedAt") OffsetDateTime updatedAt
    );
}
