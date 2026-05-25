package cn.jxufe.farm.service.imp;

import cn.jxufe.farm.bean.dto.MyFarmOverviewDTO;
import cn.jxufe.farm.bean.vo.FarmRealtimeMessageVO;
import cn.jxufe.farm.bean.vo.MyFarmOverviewVO;
import cn.jxufe.farm.service.CropLifecycleService;
import cn.jxufe.farm.service.FarmRealtimePushService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FarmRealtimePushServiceImp implements FarmRealtimePushService {

    private static final Logger log = LoggerFactory.getLogger(FarmRealtimePushServiceImp.class);

    private final ConcurrentMap<Long, ConcurrentMap<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    private final CropLifecycleService cropLifecycleService;

    private final ObjectMapper objectMapper;

    public FarmRealtimePushServiceImp(
            CropLifecycleService cropLifecycleService,
            ObjectMapper objectMapper
    ) {
        this.cropLifecycleService = cropLifecycleService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerSession(Long userId, WebSocketSession session) {
        if (userId == null || userId <= 0 || session == null) {
            return;
        }
        userSessions.computeIfAbsent(userId, key -> new ConcurrentHashMap<>()).put(session.getId(), session);
        pushOverviewToUser(userId, false);
    }

    @Override
    public void unregisterSession(Long userId, String sessionId) {
        if (userId == null || userId <= 0 || sessionId == null || sessionId.isBlank()) {
            return;
        }
        ConcurrentMap<String, WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(sessionId);
        if (sessions.isEmpty()) {
            userSessions.remove(userId, sessions);
        }
    }

    @Override
    public void pushOverviewToOnlineUsers(Set<Long> changedUserIds) {
        if (userSessions.isEmpty() || changedUserIds == null || changedUserIds.isEmpty()) {
            return;
        }
        changedUserIds.forEach(userId -> pushOverviewToUser(userId, true));
    }

    private void pushOverviewToUser(Long userId, boolean changed) {
        ConcurrentMap<String, WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        try {
            MyFarmOverviewVO overview = buildOverview(userId);
            FarmRealtimeMessageVO payload = new FarmRealtimeMessageVO();
            payload.setEvent("FARM_OVERVIEW");
            payload.setUserId(userId);
            payload.setServerTime(OffsetDateTime.now());
            payload.setCropStatusChanged(changed);
            payload.setOverview(overview);
            String json = toJson(payload);
            broadcast(userId, sessions, json);
        } catch (Exception ex) {
            log.warn("实时推送失败，userId={}", userId, ex);
        }
    }

    private void broadcast(Long userId, ConcurrentMap<String, WebSocketSession> sessions, String json) {
        TextMessage message = new TextMessage(json);
        sessions.forEach((sessionId, session) -> {
            try {
                if (!isSessionOpen(session)) {
                    sessions.remove(sessionId);
                    return;
                }
                synchronized (session) {
                    session.sendMessage(message);
                }
            } catch (IOException ex) {
                sessions.remove(sessionId);
                log.debug("发送 WebSocket 消息失败，userId={}, sessionId={}", userId, sessionId, ex);
            }
        });

        sessions.entrySet().removeIf(entry -> !isSessionOpen(entry.getValue()));
        if (sessions.isEmpty()) {
            userSessions.remove(userId, sessions);
        }
    }

    private MyFarmOverviewVO buildOverview(Long userId) {
        MyFarmOverviewDTO dto = new MyFarmOverviewDTO();
        dto.setUserId(userId);
        return cropLifecycleService.myFarmOverview(dto);
    }

    private String toJson(FarmRealtimeMessageVO payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("实时推送消息序列化失败", ex);
        }
    }

    private boolean isSessionOpen(WebSocketSession session) {
        return session != null && session.isOpen();
    }

}
