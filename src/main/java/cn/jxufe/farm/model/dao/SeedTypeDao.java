package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.SeedType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeedTypeDao extends CrudRepository<SeedType, Long>, PagingAndSortingRepository<SeedType, Long> {
    Page<SeedType> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);

    List<SeedType> findByIsDeletedFalseOrderByIdAsc();

    Optional<SeedType> findByIdAndIsDeletedFalse(Long id);
}
