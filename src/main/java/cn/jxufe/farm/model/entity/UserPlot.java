package cn.jxufe.farm.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
}
