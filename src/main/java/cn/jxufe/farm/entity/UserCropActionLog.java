package cn.jxufe.farm.entity;

import cn.jxufe.farm.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_crop_action_logs", schema = "farm")
public class UserCropActionLog extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plot_id", nullable = false)
    private Long plotId;

    @Column(name = "crop_id")
    private Long cropId;

    @Column(name = "seed_type_id")
    private Long seedTypeId;

    @Column(name = "action_type", nullable = false, length = 32)
    private String actionType;

    @Column(name = "action_result", nullable = false, length = 32)
    private String actionResult;

    @Column(name = "action_at", nullable = false)
    private OffsetDateTime actionAt;

    @Column(name = "action_snapshot", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String actionSnapshot;
}
