package cn.jxufe.farm.model.entity;

import cn.jxufe.farm.model.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "seed_types", schema = "farm")
public class SeedType extends BaseEntity {

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "seed_quality_id", nullable = false)
    private Long seedQualityId;

    @Column(name = "enable_soil_type_bits", nullable = false)
    private Long enableSoilTypeBits;

    @Column(name = "level", nullable = false)
    private Short level;

    @Column(name = "description")
    private String description;

    @Column(name = "bug_probability", nullable = false, precision = 5, scale = 4)
    private BigDecimal bugProbability;

    @Column(name = "max_bug_limit", nullable = false)
    private Short maxBugLimit;

    @Column(name = "max_harvest_count", nullable = false)
    private Short maxHarvestCount;

    @Column(name = "regrow_stage_index")
    private Short regrowStageIndex;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "harvest_experience", nullable = false)
    private Long harvestExperience;

    @Column(name = "harvest_fruit_number", nullable = false)
    private Integer harvestFruitNumber;

    @Column(name = "fruit_price", nullable = false)
    private Long fruitPrice;

    @Column(name = "harvest_score", nullable = false)
    private Long harvestScore;
}
