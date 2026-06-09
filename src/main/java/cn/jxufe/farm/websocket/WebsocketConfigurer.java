package cn.jxufe.farm.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@EnableWebSocket
@Configuration
public class WebsocketConfigurer implements WebSocketConfigurer {

    private final WebsocketHandler websocketHandler;

    private final WebsocketInterceptor websocketInterceptor;

    public  WebsocketConfigurer(
            WebsocketHandler websocketHandler,
            WebsocketInterceptor websocketInterceptor
    ) {
        this.websocketHandler = websocketHandler;
        this.websocketInterceptor = websocketInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketHandler, "/ws/server")
                .setAllowedOrigins("*")
                .addInterceptors(websocketInterceptor);
    }


}
