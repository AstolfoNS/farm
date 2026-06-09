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
@Table(name = "seed_types", schema = "farm")
public class SeedType extends BaseEntity {

  @Column(name = "name", nullable = false, length = 500)
  private String name;

  @Column(name = "cover_image_url", nullable = false, length = 1024)
  private String coverImageUrl;

  @Column(name = "seed_quality_id", nullable = false)
  private Long seedQualityId;

  @Column(name = "enable_soil_type_bits", nullable = false)
  private Long enableSoilTypeBits;

  @Column(name = "level", nullable = false)
  private Short level;

  @Column(name = "unlock_experience_required", nullable = false)
  private Long unlockExperienceRequired;

  @Column(name = "description")
  private String description;

  @Column(name = "max_bug_limit", nullable = false)
  private Short maxBugLimit;

  @Column(name = "max_harvest_count", nullable = false)
  private Short maxHarvestCount;

  @Column(name = "regrow_stage_index")
  private Short regrowStageIndex;

  @Column(name = "harvest_stage_index")
  private Short harvestStageIndex;

  @Column(name = "price", nullable = false)
  private Long price;

  @Column(name = "harvest_experience", nullable = false)
  private Long harvestExperience;

  @Column(name = "harvest_fruit_number", nullable = false)
  private Integer harvestFruitNumber;

  @Column(name = "fruit_loss_per_bug", nullable = false)
  private Integer fruitLossPerBug;

  @Column(name = "bug_kill_coin_reward", nullable = false)
  private Long bugKillCoinReward;

  @Column(name = "bug_kill_experience_reward", nullable = false)
  private Long bugKillExperienceReward;

  @Column(name = "bug_kill_score_reward", nullable = false)
  private Long bugKillScoreReward;

  @Column(name = "fruit_price", nullable = false)
  private Long fruitPrice;

  @Column(name = "harvest_score", nullable = false)
  private Long harvestScore;
}
