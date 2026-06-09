package cn.jxufe.farm.common.utils;

import cn.jxufe.farm.common.enums.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import java.util.Optional;

public final class ServiceGuardUtils {

  private ServiceGuardUtils() {}

  public static void requireNotNull(Object value, String message) {
    requireNotNull(value, BizErrorCode.PARAM_INVALID, message);
  }

  public static void requireNotNull(Object value, BizErrorCode bizCode, String message) {
    if (value == null) {
      throw new ServiceException(bizCode, message);
    }
  }

  public static void requireTrue(boolean condition, String message) {
    requireTrue(condition, BizErrorCode.SERVICE_ERROR, message);
  }

  public static void requireTrue(boolean condition, BizErrorCode bizCode, String message) {
    if (!condition) {
      throw new ServiceException(bizCode, message);
    }
  }

  public static Long requirePositive(Long value, String message) {
    return requirePositive(value, BizErrorCode.PARAM_INVALID, message);
  }

  public static Long requirePositive(Long value, BizErrorCode bizCode, String message) {
    if (value == null || value <= 0) {
      throw new ServiceException(bizCode, message);
    }
    return value;
  }

  public static Integer requirePositive(Integer value, String message) {
    return requirePositive(value, BizErrorCode.PARAM_INVALID, message);
  }

  public static Integer requirePositive(Integer value, BizErrorCode bizCode, String message) {
    if (value == null || value <= 0) {
      throw new ServiceException(bizCode, message);
    }
    return value;
  }

  public static <T> T requirePresent(Optional<T> optional, String message) {
    return requirePresent(optional, BizErrorCode.SERVICE_ERROR, message);
  }

  public static <T> T requirePresent(Optional<T> optional, BizErrorCode bizCode, String message) {
    if (optional == null || optional.isEmpty()) {
      throw new ServiceException(bizCode, message);
    }
    return optional.get();
  }
}
