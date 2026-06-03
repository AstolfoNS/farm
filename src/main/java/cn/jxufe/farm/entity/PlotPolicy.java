package cn.jxufe.farm.entity;

import cn.jxufe.farm.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "plot_policies", schema = "farm")
public class PlotPolicy extends BaseEntity {

    @Column(name = "policy_name", nullable = false, length = 128)
    private String policyName;

    @Column(name = "policy_version", length = 64)
    private String policyVersion;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "effective_scope", length = 32)
    private String effectiveScope;

    @Column(name = "publish_status", length = 32)
    private String publishStatus;

    @Column(name = "default_total_plot_count", nullable = false)
    private Short defaultTotalPlotCount;

    @Column(name = "default_unlocked_plot_count", nullable = false)
    private Short defaultUnlockedPlotCount;

    @Column(name = "default_locked_plot_count", nullable = false)
    private Short defaultLockedPlotCount;

    @Column(name = "default_lock_rule_code", nullable = false, length = 64)
    private String defaultLockRuleCode;

    @Column(name = "default_lock_reason", nullable = false, length = 255)
    private String defaultLockReason;
}
