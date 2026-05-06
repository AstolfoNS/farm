package cn.jxufe.farm.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_seeds", schema = "farm")
public class UserSeed extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "seed_type_id", nullable = false)
    private Long seedTypeId;

    @Column(name = "quantity", nullable = false)
    private Long quantity;
}
