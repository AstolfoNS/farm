package cn.jxufe.farm.websocket;

import cn.jxufe.farm.common.constants.SessionKeys;
import cn.jxufe.farm.entity.User;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WebsocketInterceptor extends HttpSessionHandshakeInterceptor {

  @Override
  public boolean beforeHandshake(
      ServerHttpRequest request,
      @NonNull ServerHttpResponse response,
      @NonNull WebSocketHandler wsHandler,
      @NonNull Map<String, Object> attributes)
      throws Exception {
    /*
     * 在拦截器内强行修改 websocket 协议，将部分浏览器不支持的 x-webkit-deflate-frame 扩展修改成 permessage-deflate
     */
    if (request.getHeaders().containsKey("Sec-WebSocket-Extensions")) {
      request.getHeaders().set("Sec-WebSocket-Extensions", "permessage-deflate");
    }

    Long userId = resolveUserId(request);
    if (userId != null && userId > 0) {
      attributes.put("userId", userId);
    }

    return super.beforeHandshake(request, response, wsHandler, attributes);
  }

  private Long resolveUserId(ServerHttpRequest request) {
    Long userIdFromQuery = resolveUserIdFromQuery(request);
    if (userIdFromQuery != null && userIdFromQuery > 0) {
      return userIdFromQuery;
    }
    if (!(request instanceof ServletServerHttpRequest servletServerHttpRequest)) {
      return null;
    }
    HttpSession session = servletServerHttpRequest.getServletRequest().getSession(false);
    if (session == null) {
      return null;
    }
    Object curUser = session.getAttribute(SessionKeys.CUR_USER);
    if (curUser instanceof User user) {
      return user.getId();
    }
    return null;
  }

  private Long resolveUserIdFromQuery(ServerHttpRequest request) {
    String userIdStr =
        UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("userId");
    if (userIdStr == null || userIdStr.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(userIdStr.trim());
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
