package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.common.constants.BizErrorCode;
import cn.jxufe.farm.common.exception.ServiceException;
import cn.jxufe.farm.dao.RequestIdempotencyDao;
import cn.jxufe.farm.entity.RequestIdempotency;
import cn.jxufe.farm.service.RequestIdempotencyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class RequestIdempotencyServiceImp implements RequestIdempotencyService {

    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final RequestIdempotencyDao requestIdempotencyDao;
    private final ObjectMapper objectMapper;

    public RequestIdempotencyServiceImp(RequestIdempotencyDao requestIdempotencyDao, ObjectMapper objectMapper) {
        this.requestIdempotencyDao = requestIdempotencyDao;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(TxType.REQUIRES_NEW)
    public <T> T getCachedSuccessResult(Long userId, String bizType, String requestId, Class<T> responseType) {
        String normalizedRequestId = normalizeRequestId(requestId);
        Optional<RequestIdempotency> optional = requestIdempotencyDao
                .findByUserIdAndBizTypeAndRequestIdAndIsDeletedFalse(userId, bizType, normalizedRequestId);
        if (optional.isEmpty()) {
            return null;
        }
        RequestIdempotency entity = optional.get();
        if (STATUS_PROCESSING.equalsIgnoreCase(entity.getProcessStatus())) {
            throw new ServiceException(BizErrorCode.REQUEST_IN_PROGRESS, "请求正在处理中");
        }
        if (!STATUS_SUCCESS.equalsIgnoreCase(entity.getProcessStatus())) {
            return null;
        }
        String payload = entity.getResponsePayload();
        if (payload == null || payload.isBlank()) {
            throw new ServiceException(BizErrorCode.IDEMPOTENT_RESPONSE_INVALID, "幂等返回数据为空");
        }
        try {
            return objectMapper.readValue(payload, responseType);
        } catch (Exception ex) {
            throw new ServiceException(BizErrorCode.IDEMPOTENT_RESPONSE_INVALID, "幂等返回数据解析失败");
        }
    }

    @Override
    @Transactional(TxType.REQUIRES_NEW)
    public RequestIdempotency claimProcessing(Long userId, String bizType, String requestId) {
        String normalizedRequestId = normalizeRequestId(requestId);
        Optional<RequestIdempotency> optional = requestIdempotencyDao
                .findByUserIdAndBizTypeAndRequestIdAndIsDeletedFalse(userId, bizType, normalizedRequestId);
        OffsetDateTime now = OffsetDateTime.now();
        if (optional.isPresent()) {
            RequestIdempotency entity = optional.get();
            if (STATUS_PROCESSING.equalsIgnoreCase(entity.getProcessStatus())) {
                throw new ServiceException(BizErrorCode.REQUEST_IN_PROGRESS, "请求正在处理中");
            }
            entity.setProcessStatus(STATUS_PROCESSING);
            entity.setResponsePayload(null);
            entity.setErrorMessage(null);
            entity.setFinishedAt(null);
            entity.setUpdatedAt(now);
            entity.setUpdatedBy(userId);
            return requestIdempotencyDao.save(entity);
        }

        RequestIdempotency entity = new RequestIdempotency();
        entity.setUserId(userId);
        entity.setBizType(bizType);
        entity.setRequestId(normalizedRequestId);
        entity.setProcessStatus(STATUS_PROCESSING);
        entity.setResponsePayload(null);
        entity.setErrorMessage(null);
        entity.setFinishedAt(null);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setStatus((short) 1);
        entity.setIsDeleted(false);
        entity.setOptLockVersion(0);
        return requestIdempotencyDao.save(entity);
    }

    @Override
    @Transactional(TxType.REQUIRES_NEW)
    public void markSuccess(Long idempotencyId, Object responseBody) {
        if (idempotencyId == null || idempotencyId <= 0) {
            return;
        }
        RequestIdempotency entity = requestIdempotencyDao.findById(idempotencyId).orElse(null);
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            return;
        }
        String payload;
        try {
            payload = objectMapper.writeValueAsString(responseBody);
        } catch (JsonProcessingException ex) {
            throw new ServiceException(BizErrorCode.IDEMPOTENT_RESPONSE_INVALID, "幂等返回数据序列化失败");
        }
        OffsetDateTime now = OffsetDateTime.now();
        entity.setProcessStatus(STATUS_SUCCESS);
        entity.setResponsePayload(payload);
        entity.setErrorMessage(null);
        entity.setFinishedAt(now);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(entity.getUserId());
        requestIdempotencyDao.save(entity);
    }

    @Override
    @Transactional(TxType.REQUIRES_NEW)
    public void markFailed(Long idempotencyId, String errorMessage) {
        if (idempotencyId == null || idempotencyId <= 0) {
            return;
        }
        RequestIdempotency entity = requestIdempotencyDao.findById(idempotencyId).orElse(null);
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        entity.setProcessStatus(STATUS_FAILED);
        entity.setErrorMessage(errorMessage);
        entity.setFinishedAt(now);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(entity.getUserId());
        requestIdempotencyDao.save(entity);
    }

    private String normalizeRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new ServiceException(BizErrorCode.REQUEST_ID_REQUIRED, "请求ID不能为空");
        }
        return requestId.trim();
    }
}
