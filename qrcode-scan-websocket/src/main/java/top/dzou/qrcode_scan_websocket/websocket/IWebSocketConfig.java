package top.dzou.qrcode_scan_websocket.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author dingxiang
 * @date 19-11-9 下午8:40
 */
@Configuration
@EnableWebSocketMessageBroker
public class IWebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
//注册一个Stomp 协议的endpoint,并指定 SockJS协议
        registry.addEndpoint("/websocket").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        //registry.setApplicationDestinationPrefixes("/app");
    }
}
