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
@Table(name = "plot_types", schema = "farm")
public class PlotType extends BaseEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "icon_url", nullable = false, length = 1024)
    private String iconUrl;

    @Column(name = "soil_type_id", nullable = false)
    private Long soilTypeId;

    @Column(name = "unlock_required", nullable = false)
    private Boolean unlockRequired;

    @Column(name = "default_usable", nullable = false)
    private Boolean defaultUsable;

    @Column(name = "default_unlock_experience_required", nullable = false)
    private Long defaultUnlockExperienceRequired;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "description")
    private String description;
}

