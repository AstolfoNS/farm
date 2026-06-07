# FarmRealtimePushService — 实时推送服务

**文件**: `service/FarmRealtimePushService.java` | **实现**: `FarmRealtimePushServiceImp`

## 方法

| 方法 | 说明 |
|------|------|
| `registerSession(Long userId, WebSocketSession)` | WebSocket 连接建立时注册会话 |
| `unregisterSession(Long userId, String sessionId)` | 连接关闭/出错时注销会话 |
| `pushOverviewToUsers(Set<Long> changedUserIds)` | 向指定用户推送农场概览更新（`FARM_OVERVIEW` 事件） |

## 架构

- 使用 `ConcurrentMap<Long, ConcurrentMap<String, WebSocketSession>>` 管理用户→会话映射（支持一用户多设备）
- 推送消息体 `FarmRealtimeMessageVO {event, userId, serverTime, cropStatusChanged, overview}`
- 连接建立时注入当前农场快照，后续推送增量更新
- 定时调度器 `CropStatusSchedulerService` 周期性检查作物状态变化并触发推送
