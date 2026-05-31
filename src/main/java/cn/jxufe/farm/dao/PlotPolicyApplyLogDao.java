package cn.jxufe.farm.dao;

import cn.jxufe.farm.entity.PlotPolicyApplyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlotPolicyApplyLogDao extends JpaRepository<PlotPolicyApplyLog, Long> {
}
