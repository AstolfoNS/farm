package cn.jxufe.farm.websocket;

import cn.jxufe.farm.service.FarmRealtimePushService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebsocketHandler implements WebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(WebsocketHandler.class);

  private final FarmRealtimePushService farmRealtimePushService;

  public WebsocketHandler(FarmRealtimePushService farmRealtimePushService) {
    this.farmRealtimePushService = farmRealtimePushService;
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    Long userId = resolveUserId(session);
    if (userId == null || userId <= 0) {
      closeSilently(session, CloseStatus.BAD_DATA.withReason("缺少有效的 userId"));
      return;
    }
    farmRealtimePushService.registerSession(userId, session);
    log.debug("WebSocket 建立连接，userId={}, sessionId={}", userId, session.getId());
  }

  @Override
  public void handleMessage(
      @NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) {
    // 当前为服务端主动推送场景，不处理客户端业务消息
  }

  @Override
  public void handleTransportError(
      @NonNull WebSocketSession session, @NonNull Throwable exception) {
    Long userId = resolveUserId(session);
    if (userId != null && userId > 0) {
      farmRealtimePushService.unregisterSession(userId, session.getId());
    }
    closeSilently(session, CloseStatus.SERVER_ERROR);
  }

  @Override
  public void afterConnectionClosed(
      @NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) {
    Long userId = resolveUserId(session);
    if (userId != null && userId > 0) {
      farmRealtimePushService.unregisterSession(userId, session.getId());
    }
    log.debug(
        "WebSocket 连接关闭，userId={}, sessionId={}, reason={}", userId, session.getId(), closeStatus);
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }

  private Long resolveUserId(WebSocketSession session) {
    Object userId = session.getAttributes().get("userId");
    if (userId instanceof Long id) {
      return id;
    }
    if (userId instanceof Integer id) {
      return id.longValue();
    }
    if (userId instanceof String idStr) {
      try {
        return Long.parseLong(idStr.trim());
      } catch (NumberFormatException ex) {
        return null;
      }
    }
    return null;
  }

  private void closeSilently(WebSocketSession session, CloseStatus closeStatus) {
    if (session == null || !session.isOpen()) {
      return;
    }
    try {
      session.close(closeStatus);
    } catch (Exception ex) {
      log.debug("关闭 WebSocket 连接失败，sessionId={}", session.getId(), ex);
    }
  }
}
