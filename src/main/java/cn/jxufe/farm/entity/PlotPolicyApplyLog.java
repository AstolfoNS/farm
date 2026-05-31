package cn.jxufe.farm.entity;

import cn.jxufe.farm.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "plot_policy_apply_logs", schema = "farm")
public class PlotPolicyApplyLog extends BaseEntity {

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "applied_scope", nullable = false, length = 32)
    private String appliedScope;

    @Column(name = "target_user_count", nullable = false)
    private Integer targetUserCount;

    @Column(name = "success_user_count", nullable = false)
    private Integer successUserCount;

    @Column(name = "failed_user_count", nullable = false)
    private Integer failedUserCount;

    @Column(name = "request_payload_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String requestPayloadJson;

    @Column(name = "result_snapshot_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String resultSnapshotJson;

    @Column(name = "applied_by")
    private Long appliedBy;

    @Column(name = "applied_at", nullable = false)
    private OffsetDateTime appliedAt;
}
