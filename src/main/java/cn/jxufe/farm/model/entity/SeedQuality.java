package cn.jxufe.farm.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "seed_qualities", schema = "farm")
public class SeedQuality extends BaseEntity {

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "description")
    private String description;
}
