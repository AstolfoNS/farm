package cn.jxufe.farm.common.apis;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Unified API response")
public class R<T> implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @Schema(description = "Status code")
  private int code;

  @Schema(description = "Message")
  private String msg;

  @Schema(description = "Payload data")
  private T data;

  @Schema(description = "Extra details")
  private Object details;

  public static <T> R<T> ok() {
    return ok(null);
  }

  public static <T> R<T> ok(T data) {
    return new R<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data, null);
  }

  public static <T> R<T> ok(T data, String msg) {
    return new R<>(HttpStatus.OK.value(), msg, data, null);
  }

  public static <T> R<T> ok(T data, HttpStatus status) {
    return new R<>(status.value(), status.getReasonPhrase(), data, null);
  }

  public static <T> R<T> okWithMsg(String msg) {
    return ok(null, msg);
  }

  public static <T> R<T> failed() {
    return failed(null, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public static <T> R<T> failed(T data) {
    return failed(data, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public static <T> R<T> failed(String msg) {
    return failed(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, null);
  }

  public static <T> R<T> failed(HttpStatus status) {
    return failed(null, status);
  }

  public static <T> R<T> failed(T data, HttpStatus status) {
    return new R<>(status.value(), status.getReasonPhrase(), data, null);
  }

  public static <T> R<T> failed(int code, String msg, T data) {
    return new R<>(code, msg, data, null);
  }

  public static <T> R<T> failedWithDetails(HttpStatus status, Object details) {
    return new R<>(status.value(), status.getReasonPhrase(), null, details);
  }

  public static <T> R<T> failedWithDetails(String msg, Object details) {
    return new R<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, null, details);
  }
}
