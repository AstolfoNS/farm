package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.GrowthStage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrowthStageDao extends CrudRepository<GrowthStage, Long> {
    List<GrowthStage> findByIsDeletedFalseOrderByIdAsc();
}
