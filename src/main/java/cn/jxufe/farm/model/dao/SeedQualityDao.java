package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.SeedQuality;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeedQualityDao extends CrudRepository<SeedQuality, Long> {
    List<SeedQuality> findByIsDeletedFalseOrderByIdAsc();
}
