package cn.jxufe.farm.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_crops", schema = "farm")
public class UserCrop extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plot_id", nullable = false)
    private Long plotId;

    @Column(name = "seed_type_id", nullable = false)
    private Long seedTypeId;

    @Column(name = "planted_at", nullable = false)
    private OffsetDateTime plantedAt;

    @Column(name = "last_harvest_at")
    private OffsetDateTime lastHarvestAt;

    @Column(name = "harvest_count", nullable = false)
    private Short harvestCount;

    @Column(name = "grow_status", nullable = false)
    private Short growStatus;

    @Column(name = "bug_count", nullable = false)
    private Short bugCount;
}
