package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.SoilType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoilTypeDao extends CrudRepository<SoilType, Long> {
    List<SoilType> findByIsDeletedFalseOrderByIdAsc();
}
