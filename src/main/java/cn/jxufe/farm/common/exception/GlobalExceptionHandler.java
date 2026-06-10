package cn.jxufe.farm.common.exception;

import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.common.enums.BizErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ServiceException.class)
  @ResponseStatus(HttpStatus.OK)
  public R<Void> handleServiceException(ServiceException ex) {
    BizErrorCode bizCode = ex.getBizCode();
    String message =
        ex.getMessage() == null || ex.getMessage().isBlank()
            ? bizCode.getDefaultMessage()
            : ex.getMessage();
    return R.failed(HttpStatus.BAD_REQUEST.value(), message, null);
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    BindException.class,
    ConstraintViolationException.class
  })
  @ResponseStatus(HttpStatus.OK)
  public R<Void> handleValidationException(Exception ex) {
    return R.failed(HttpStatus.BAD_REQUEST.value(), resolveValidationMessage(ex), null);
  }

  private String resolveValidationMessage(Exception ex) {
    if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException
        && methodArgumentNotValidException.getBindingResult().hasFieldErrors()) {
      return methodArgumentNotValidException
          .getBindingResult()
          .getFieldErrors()
          .getFirst()
          .getDefaultMessage();
    }
    if (ex instanceof BindException bindException
        && bindException.getBindingResult().hasFieldErrors()) {
      return bindException.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
    }
    if (ex instanceof ConstraintViolationException constraintViolationException
        && !constraintViolationException.getConstraintViolations().isEmpty()) {
      return constraintViolationException.getConstraintViolations().iterator().next().getMessage();
    }
    return "请求参数不合法";
  }
}
