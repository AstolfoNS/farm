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
@Table(name = "user_seeds", schema = "farm")
public class UserSeed extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "seed_type_id", nullable = false)
    private Long seedTypeId;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "frozen_quantity", nullable = false)
    private Long frozenQuantity;
}
