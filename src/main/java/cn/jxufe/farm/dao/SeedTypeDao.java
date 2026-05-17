package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.SeedType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeedTypeDao extends JpaRepository<SeedType, Long> {
    Page<SeedType> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);

    List<SeedType> findByIsDeletedFalseOrderByIdAsc();

    Optional<SeedType> findByIdAndIsDeletedFalse(Long id);
}
