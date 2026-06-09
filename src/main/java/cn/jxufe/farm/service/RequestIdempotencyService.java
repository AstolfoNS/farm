package cn.jxufe.farm.service;

import cn.jxufe.farm.entity.RequestIdempotency;

public interface RequestIdempotencyService {

  <T> T getCachedSuccessResult(
      Long userId, String bizType, String requestId, Class<T> responseType);

  RequestIdempotency claimProcessing(Long userId, String bizType, String requestId);

  void markSuccess(Long idempotencyId, Object responseBody);

  void markFailed(Long idempotencyId, String errorMessage);
}
