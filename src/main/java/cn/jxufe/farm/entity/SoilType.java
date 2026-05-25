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
@Table(name = "soil_types", schema = "farm")
public class SoilType extends BaseEntity {

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "bit_code", nullable = false)
    private Integer bitCode;

    @Column(name = "cover_image_url", nullable = false, length = 1024)
    private String coverImageUrl;

    @Column(name = "level", nullable = false)
    private Short level;

    @Column(name = "unlock_experience_required", nullable = false)
    private Long unlockExperienceRequired;

    @Column(name = "grow_speed_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal growSpeedMultiplier;

    @Column(name = "description")
    private String description;
}
