package cn.jxufe.farm.entity;

import cn.jxufe.farm.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "seed_growth_stages", schema = "farm")
public class SeedGrowthStage extends BaseEntity {

    @Column(name = "seed_type_id", nullable = false)
    private Long seedTypeId;

    @Column(name = "growth_stage_id", nullable = false)
    private Long growthStageId;

    @Column(name = "stage_index", nullable = false)
    private Short stageIndex;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "asset_url", length = 1024)
    private String assetUrl;

    @Column(name = "bug_probability", nullable = false, precision = 5, scale = 4)
    private BigDecimal bugProbability;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "offset_x", nullable = false)
    private Integer offsetX;

    @Column(name = "offset_y", nullable = false)
    private Integer offsetY;
}
