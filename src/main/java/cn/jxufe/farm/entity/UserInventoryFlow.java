package cn.jxufe.farm.entity;

import cn.jxufe.farm.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "user_inventory_flows", schema = "farm")
public class UserInventoryFlow extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "item_type", nullable = false, length = 32)
  private String itemType;

  @Column(name = "seed_type_id", nullable = false)
  private Long seedTypeId;

  @Column(name = "operation_type", nullable = false, length = 32)
  private String operationType;

  @Column(name = "change_amount", nullable = false)
  private Long changeAmount;

  @Column(name = "before_amount", nullable = false)
  private Long beforeAmount;

  @Column(name = "after_amount", nullable = false)
  private Long afterAmount;

  @Column(name = "before_frozen_amount", nullable = false)
  private Long beforeFrozenAmount;

  @Column(name = "after_frozen_amount", nullable = false)
  private Long afterFrozenAmount;

  @Column(name = "biz_type", nullable = false, length = 64)
  private String bizType;

  @Column(name = "biz_id", length = 128)
  private String bizId;

  @Column(name = "occurred_at", nullable = false)
  private OffsetDateTime occurredAt;

  @Column(name = "ext_data", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String extData;
}
