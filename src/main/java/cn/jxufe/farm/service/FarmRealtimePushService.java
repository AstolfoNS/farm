package cn.jxufe.farm.service;

import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

public interface FarmRealtimePushService {

    void registerSession(Long userId, WebSocketSession session);

    void unregisterSession(Long userId, String sessionId);

    void pushOverviewToOnlineUsers(Set<Long> changedUserIds);

}
