package com.safebirth.config;

import com.safebirth.sms.gateway.websocket.SmsGatewayWebSocketHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for SMS Gateway integration.
 * Enables WebSocket endpoint for Android app to connect and exchange SMS messages.
 *
 * Endpoint: ws://localhost:8080/ws/sms-gateway
 *
 * Note: CORS is wide open for POC - add token authentication in production.
 */
@Configuration
@EnableWebSocket
@ConditionalOnProperty(name = "sms.gateway.websocket.enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketConfig implements WebSocketConfigurer {

    private final SmsGatewayWebSocketHandler smsGatewayHandler;

    public WebSocketConfig(SmsGatewayWebSocketHandler smsGatewayHandler) {
        this.smsGatewayHandler = smsGatewayHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(smsGatewayHandler, "/ws/sms-gateway")
                .setAllowedOrigins("*");  // POC - no security, allow all origins
    }
}
