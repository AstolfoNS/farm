package cn.jxufe.farm.service;

import java.util.Set;
import org.springframework.web.socket.WebSocketSession;

public interface FarmRealtimePushService {

  void registerSession(Long userId, WebSocketSession session);

  void unregisterSession(Long userId, String sessionId);

  void pushOverviewToOnlineUsers(Set<Long> changedUserIds);
}
