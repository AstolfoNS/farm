package cn.jxufe.farm.entity;

import cn.jxufe.farm.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_plots", schema = "farm")
public class UserPlot extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "soil_type_id", nullable = false)
    private Long soilTypeId;

    @Column(name = "plot_index", nullable = false)
    private Short plotIndex;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "unlocked_at")
    private OffsetDateTime unlockedAt;

    @Column(name = "lock_reason")
    private String lockReason;
}
