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
@Table(name = "request_idempotencies", schema = "farm")
public class RequestIdempotency extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "biz_type", nullable = false, length = 64)
  private String bizType;

  @Column(name = "request_id", nullable = false, length = 128)
  private String requestId;

  @Column(name = "process_status", nullable = false, length = 16)
  private String processStatus;

  @Column(name = "response_payload", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String responsePayload;

  @Column(name = "error_message", length = 500)
  private String errorMessage;

  @Column(name = "finished_at")
  private OffsetDateTime finishedAt;
}
