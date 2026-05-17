package cn.jxufe.farm.common.exception;

import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.common.constants.BizErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException ex) {
        BizErrorCode bizCode = ex.getBizCode() == null ? BizErrorCode.SERVICE_ERROR : ex.getBizCode();
        String message = ex.getMessage() == null || ex.getMessage().isBlank() ? bizCode.getDefaultMessage() : ex.getMessage();
        R<Void> response = R.failed(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null);
        response.setDetails(buildDetails(bizCode));
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = "请求参数校验失败";
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            message = fieldError.getDefaultMessage();
        }
        R<Void> response = R.failed(HttpStatus.BAD_REQUEST.value(), message, null);
        response.setDetails(buildDetails(BizErrorCode.PARAM_INVALID));
        return response;
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException ex) {
        String message = "请求参数绑定失败";
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            message = fieldError.getDefaultMessage();
        }
        R<Void> response = R.failed(HttpStatus.BAD_REQUEST.value(), message, null);
        response.setDetails(buildDetails(BizErrorCode.PARAM_INVALID));
        return response;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getMessage() == null || ex.getMessage().isBlank()
                ? BizErrorCode.PARAM_INVALID.getDefaultMessage()
                : ex.getMessage();
        R<Void> response = R.failed(HttpStatus.BAD_REQUEST.value(), message, null);
        response.setDetails(buildDetails(BizErrorCode.PARAM_INVALID));
        return response;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        R<Void> response = R.failed(HttpStatus.BAD_REQUEST.value(), "请求体格式错误", null);
        response.setDetails(buildDetails(BizErrorCode.PARAM_INVALID));
        return response;
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception ex) {
        R<Void> response = R.failed(HttpStatus.INTERNAL_SERVER_ERROR.value(), BizErrorCode.SYSTEM_ERROR.getDefaultMessage(), null);
        response.setDetails(buildDetails(BizErrorCode.SYSTEM_ERROR));
        return response;
    }

    private Map<String, Object> buildDetails(BizErrorCode bizErrorCode) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("bizCode", bizErrorCode.getCode());
        details.put("bizMessage", bizErrorCode.getDefaultMessage());
        return details;
    }
}
