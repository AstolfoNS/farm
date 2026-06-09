package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.User;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
  List<User> findByIsDeletedFalseOrderByIdAsc();

  Page<User> findByIsDeletedFalseAndUsernameContainingIgnoreCase(
      String username, Pageable pageable);

  Optional<User> findByUsernameAndIsDeletedFalse(String username);

  Optional<User> findByIdAndIsDeletedFalse(Long id);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
            update User u
               set u.coin = u.coin - :amount,
                   u.updatedAt = :updatedAt,
                   u.updatedBy = :updatedBy
             where u.id = :userId
               and u.isDeleted = false
               and u.coin >= :amount
            """)
  int decreaseCoinIfEnough(
      @Param("userId") Long userId,
      @Param("amount") Long amount,
      @Param("updatedBy") Long updatedBy,
      @Param("updatedAt") OffsetDateTime updatedAt);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
            update User u
               set u.coin = u.coin + :amount,
                   u.updatedAt = :updatedAt,
                   u.updatedBy = :updatedBy
             where u.id = :userId
               and u.isDeleted = false
            """)
  int increaseCoin(
      @Param("userId") Long userId,
      @Param("amount") Long amount,
      @Param("updatedBy") Long updatedBy,
      @Param("updatedAt") OffsetDateTime updatedAt);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
            update User u
               set u.experience = u.experience + :expDelta,
                   u.score = u.score + :scoreDelta,
                   u.updatedAt = :updatedAt,
                   u.updatedBy = :updatedBy
             where u.id = :userId
               and u.isDeleted = false
            """)
  int increaseExperienceAndScore(
      @Param("userId") Long userId,
      @Param("expDelta") Long expDelta,
      @Param("scoreDelta") Long scoreDelta,
      @Param("updatedBy") Long updatedBy,
      @Param("updatedAt") OffsetDateTime updatedAt);
}
