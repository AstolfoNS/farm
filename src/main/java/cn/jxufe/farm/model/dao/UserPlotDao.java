package cn.jxufe.farm.model.dao;

import cn.jxufe.farm.model.entity.UserPlot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPlotDao extends CrudRepository<UserPlot, Long> {
}
