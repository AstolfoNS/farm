package cn.jxufe.farm.common.exception;

import cn.jxufe.farm.common.enums.BizErrorCode;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final BizErrorCode bizCode;

    public ServiceException(String message) {
        this(BizErrorCode.SERVICE_ERROR, message);
    }

    public ServiceException(BizErrorCode bizCode) {
        this(bizCode, bizCode == null ? null : bizCode.getDefaultMessage());
    }

    public ServiceException(BizErrorCode bizCode, String message) {
        super(message);
        this.bizCode = bizCode == null ? BizErrorCode.SERVICE_ERROR : bizCode;
    }

}
