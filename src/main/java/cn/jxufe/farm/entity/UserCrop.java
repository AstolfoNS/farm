package cn.jxufe.farm.entity;

import cn.jxufe.farm.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

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

  @Column(name = "stage_started_at", nullable = false)
  private OffsetDateTime stageStartedAt;

  @Column(name = "last_harvest_at")
  private OffsetDateTime lastHarvestAt;

  @Column(name = "matured_at")
  private OffsetDateTime maturedAt;

  @Column(name = "withered_at")
  private OffsetDateTime witheredAt;

  @Column(name = "expected_ripe_at")
  private OffsetDateTime expectedRipeAt;

  @Column(name = "expected_withered_at")
  private OffsetDateTime expectedWitheredAt;

  @Column(name = "harvest_count", nullable = false)
  private Short harvestCount;

  @Column(name = "current_stage_index", nullable = false)
  private Short currentStageIndex;

  @Column(name = "grow_status", nullable = false)
  private Short growStatus;

  @Column(name = "bug_count", nullable = false)
  private Short bugCount;

  @Column(name = "last_bug_at")
  private OffsetDateTime lastBugAt;

  @Column(name = "last_care_at")
  private OffsetDateTime lastCareAt;
}
