package cn.jxufe.farm.entity;

import cn.jxufe.farm.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_plot_allocations", schema = "farm")
public class UserPlotAllocation extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "total_plot_count", nullable = false)
    private Short totalPlotCount;

    @Column(name = "unlocked_plot_count", nullable = false)
    private Short unlockedPlotCount;

    @Column(name = "locked_plot_count", nullable = false)
    private Short lockedPlotCount;

    @Column(name = "default_plot_type_id")
    private Long defaultPlotTypeId;

    @Column(name = "lock_rule_code", nullable = false, length = 64)
    private String lockRuleCode;

    @Column(name = "lock_reason", nullable = false, length = 255)
    private String lockReason;

    @Column(name = "allocation_rule_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String allocationRuleJson;

    @Column(name = "applied_at")
    private OffsetDateTime appliedAt;
}

