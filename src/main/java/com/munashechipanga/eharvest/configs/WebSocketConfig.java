package com.munashechipanga.eharvest.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // For Flutter apps on production, allow all origins
        // Security is enforced through JWT token validation, not origin checking
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws/tracking")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Configure WebSocket transport for production stability
        registration
                .setMessageSizeLimit(128 * 1024) // 128KB message size limit
                .setSendTimeLimit(20000)          // 20 seconds send timeout
                .setSendBufferSizeLimit(512 * 1024) // 512KB send buffer
                .setTimeToFirstMessage(30000);    // 30 seconds to first message
    }
}

