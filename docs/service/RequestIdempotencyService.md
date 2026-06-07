# RequestIdempotencyService — 请求幂等服务

**文件**: `service/RequestIdempotencyService.java` | **实现**: `RequestIdempotencyServiceImp`

## 方法

### `<T> T getCachedSuccessResult(Long userId, String bizType, String requestId, Class<T> responseType)`
- **说明**: 查询是否有已缓存的成功结果
- **返回**: 缓存的成功响应对象，不存在返回 null
- **幂等维度**: `(userId, bizType, requestId)`

### `RequestIdempotency claimProcessing(Long userId, String bizType, String requestId)`
- **说明**: 声明正在处理该请求
- **行为**: 
  - 已存在 PROCESSING → 抛 `REQUESTS_IN_PROGRESS` 异常
  - 已存在 FAILED → 允许重试，更新为 PROCESSING
  - 不存在 → 新建 PROCESSING 记录
- **事务**: @Transactional

### `void markSuccess(Long idempotencyId, Object responseBody)`
- **说明**: 标记请求处理成功，缓存响应结果
- **事务**: @Transactional

### `void markFailed(Long idempotencyId, String errorMessage)`
- **说明**: 标记请求处理失败
- **事务**: @Transactional

## 幂等业务类型

| bizType | 使用场景 |
|---------|----------|
| `BUY_SEED` | 购买种子 |
| `SELL_FRUIT` | 出售果实 |
| `PLANT` | 种植作物 |
| `HARVEST` | 收获作物 |

## 状态机

```
(null) → claimProcessing() → PROCESSING
                                   ├── markSuccess() → SUCCESS（缓存响应）
                                   └── markFailed() → FAILED（允许重试）
```
